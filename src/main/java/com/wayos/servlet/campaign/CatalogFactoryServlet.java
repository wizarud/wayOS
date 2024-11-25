package com.wayos.servlet.campaign;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wayos.Configuration;
import com.wayos.Context;
import com.wayos.context.MemoryContext;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.ecommerce.CatalogDrawer;
import com.wayos.drawer.ecommerce.Catalog;
import com.wayos.drawer.ecommerce.PaginationCatalogImporter;
import com.wayos.drawer.ecommerce.PaginationDrawer;
import com.wayos.drawer.ecommerce.Product;
import com.wayos.servlet.console.ConsoleServlet;

@SuppressWarnings("serial")
@WebServlet("/catalogFactory")
public class CatalogFactoryServlet extends ConsoleServlet {

	final List<Map<String, String>> createMapList(HttpServletRequest request, String...keys) {

		List<Map<String, String>> mapList = new ArrayList<>();

		//Find max row
		String [] vals;
		int maxRow = Integer.MIN_VALUE;
		for (String key:keys) {
			vals = request.getParameterValues(key);
			if (vals==null) continue;
			if (vals.length > maxRow) {
				maxRow = vals.length;
			}				
		}

		//Allocate empty table
		for (int i=0; i<maxRow; i++) {
			mapList.add(new HashMap<>());
		}

		Map<String, String> map;		
		int row;		
		for (String key:keys) {
			vals = request.getParameterValues(key);			
			if (vals==null) continue;
			row = 0;
			for (String val:vals) {				
				map = mapList.get(row++);
				map.put(key, val);
			}						
		}

		return mapList;
	}
	
	/**
	 * Returns the complimentary (opposite) color.
	 * @param color int RGB color to return the compliment of
	 * @return int RGB of compliment color
	 */
	public static Color getComplimentColor(Color color) {
		
		// get existing colors
		int red = color.getRed();
		int blue = color.getBlue();
		int green = color.getGreen();
		int alpha = color.getAlpha();

		// find compliments
		red = (~red) & 0xff;
		blue = (~blue) & 0xff;
		green = (~green) & 0xff;
		
		return new Color(red, green, blue, alpha);
	}
	
	public static Color getAverageColor(BufferedImage image) {
		
		int width = image.getWidth();
		int height = image.getHeight();
		
	    long sumr = 0, sumg = 0, sumb = 0, suma = 0;
	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            Color pixel = new Color(image.getRGB(x, y));
	            sumr += pixel.getRed();
	            sumg += pixel.getGreen();
	            sumb += pixel.getBlue();
	            suma += pixel.getAlpha();
	        }
	    }
	    int num = width * height;
	    
	    int r = (int) (sumr / num);
	    int g = (int) (sumg / num);
	    int b = (int) (sumb / num);
	    int a = (int) (suma / num);
	    
	    return new Color(r, g, b, a);
		
	}
	
	public static float whiteRate(Color color) {
		
		int red = color.getRed();
		
		int green = color.getGreen();
		
		int blue = color.getBlue();
		
		float redRate = red / 255.0f;
		
		float greenRate = green / 255.0f;
		
		float blueRate = blue / 255.0f;
		
		float avgWhiteRate = (redRate + greenRate + blueRate) / 3;
		
		return avgWhiteRate;
	}
	
	public static Color darkerForWhiteContrast(Color color, float contrastRate) {
		
		float whiteRate;
		
		while (true) {
			
			whiteRate = whiteRate(color);
			
			if (whiteRate < 1 - contrastRate) break;
			
			color = color.darker();
		}
		
		return color;
	}
	
	public static String toHex(Color color) {
		
		return String.format("#%02X%02X%02X", 
    			color.getRed(),
    			color.getGreen(),
    			color.getBlue());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");

		String contact = request.getParameter("contact");

		if (contact==null) {
			response.getWriter().print("Missing contact parameter");
			return;
		}

		String brand;
		String cover;
		String desc;
		String price;

		brand = request.getParameter("brand");
		if (brand==null) {
			response.getWriter().print("Missing brand parameter");
			return;				
		}

		cover = request.getParameter("cover");
		if (cover==null) {
			response.getWriter().print("Missing cover parameter");
			return;				
		}
		
		price = request.getParameter("prices");
		if (price==null) {
			response.getWriter().print("Missing prices parameter");
			return;				
		}

		try {
			
			String [] columns = {"cats", "images", "descs", "prices", "contact"};

			List<Map<String, String>> mapList = createMapList(request, columns);

			if (mapList.isEmpty()) {
				response.getWriter().print("No such products");
				return;
			}

			desc = mapList.get(0).get("descs");
			
			PaginationCatalogImporter paginationCatalogImporter = new PaginationCatalogImporter() {

				@Override
				public Map<String, Catalog> createCatalogMap() {

					Map<String, Catalog> catalogMap = new HashMap<>();

					Catalog catalog;
					String cat;
					int i;
					for (Map<String, String> map:mapList) {

						cat = map.get("cats");
						if (cat==null) {
							cat = brand;
						}

						catalog = catalogMap.get(cat);

						if (catalog==null) {
							
							//catalog = new Catalog(cat, map.get("images").split("\n\n\n", 2)[0]);
							
							catalog = new Catalog(cat, cover);
						}

						i = catalog.productList.size() + 1;

						Product product = new Product(this, "P" + i, map);
						catalog.productList.add(product);		
						catalogMap.put(cat, catalog);

					}

					return catalogMap;
				}

			};			

			String accountId = "shop";

			String botId = request.getParameter("botId");

			if (botId==null) botId = Long.toHexString(System.currentTimeMillis());

			Context context = new MemoryContext(accountId + "/" + botId);

			context.prop("title", brand);
			
			context.prop("language", "th");
			
			/**
			 * Calculate ComplimentColor from cover
			 */
			String internalCoverImageURL = cover;
			
			/**
			 * For Yiem, Cannot use https for internal REST invoke
			 */
			if (internalCoverImageURL.startsWith("https://wayos.yiem.cc")) {
				
				internalCoverImageURL = internalCoverImageURL.replace("https://wayos.yiem.cc", "http://wayos:8080");
			}
			
			BufferedImage coverImage = ImageIO.read(new URL(internalCoverImageURL));
			
			Color averageColor = getAverageColor(coverImage);
			
			Color complimentColor = getComplimentColor(averageColor);
			
			Color darkerColor = darkerForWhiteContrast(complimentColor, 0.5f);
			
			String hexColor = toHex(darkerColor);
			
			context.prop("borderColor", hexColor);
			
			super.addNewBot(context.toJSONString(), context.name(), context.prop());
			
			context = sessionPool().getContext(context.name());
			
			context.load();

			Canvas2D canvas2D = new Canvas2D(context, brand, 100, true);
			
			CatalogDrawer drawer = new CatalogDrawer(context.name(), paginationCatalogImporter);			
			
			drawer.setBusinessInfo(contact);
			
			drawer.setMorePicURL(Configuration.domain + request.getContextPath() + "/images/More.png");
			
			drawer.setEmptyCartImageURL(Configuration.domain + request.getContextPath() + "/images/EmptyCart.png");
			
			drawer.setFilledCartImageURL(Configuration.domain + request.getContextPath() + "/images/FilledCart.png");
			
			drawer.draw(canvas2D);
			
			context.save();

			context.load();

			response.getWriter().print(Configuration.domain + request.getContextPath() + "/x/" + accountId + "/" + botId);

		} catch (Exception e) {

			e.printStackTrace();
			
			response.getWriter().print(e.getMessage());
		}
	}
	
	public static void main(String[]args) throws Exception {
		
		BufferedImage image = ImageIO.read(new File("/Users/apple/Desktop/wayOS/823478.jpg"));
		
		//BufferedImage image = ImageIO.read(new File("/Users/apple/Desktop/IMG_0788.jpg"));
		
		Color averageColor = getAverageColor(image);
		
		Color complimentColor = getComplimentColor(averageColor);
				
		System.out.println(toHex(complimentColor));
		
		System.out.println(whiteRate(complimentColor));
		
		Color targetColor = darkerForWhiteContrast(complimentColor, 0.5f);
		
		System.out.println(toHex(targetColor));
				
		/*
		System.out.println(whiteRate(Color.WHITE));
		
		System.out.println(whiteRate(Color.RED));
		
		System.out.println(whiteRate(Color.GREEN));
		
		System.out.println(whiteRate(Color.BLUE));
		
		System.out.println(whiteRate(Color.BLACK));
		*/
	}

}
