package com.wayos.servlet.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wayos.Context;
import com.wayos.context.MemoryContext;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.basic.QuizDrawer;
import com.wayos.drawer.basic.VerticallyDrawer;
import com.wayos.drawer.ecommerce.CSVPaginationCatalogDrawer;
import com.wayos.drawer.ecommerce.Catalog;
import com.wayos.drawer.ecommerce.PaginationCatalogImporter;
import com.wayos.drawer.ecommerce.PaginationDrawer;
import com.wayos.drawer.ecommerce.Product;

@SuppressWarnings("serial")
//@WebServlet("/factory")
@Deprecated
public class BotParamsFactoryServlet extends ConsoleServlet {

	private static final String domain = System.getenv("domain");

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

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String contact = request.getParameter("contact");

		if (contact==null) {
			response.getWriter().print("Missing contact parameter");
			return;
		}

		String brand;
		String cover;
		String desc;

		Drawer drawer;

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

		String qa = request.getParameter("qa");

		if (qa!=null) {

			desc = request.getParameter("desc");

			if (desc==null) {
				desc = brand;
			}

			drawer = new QuizDrawer(qa);

		} else {

			/**
			 * Convert ParameterValues to List of Map.
			 */

			/*
			if (mapList.size()==1) {

				Map<String, String> map = mapList.get(0);
				List<String> textList = new ArrayList<>();
				String val;
				for (String col:columns) {
					val = map.get(col);
					if (val==null||val.trim().isEmpty()) continue;
					textList.add(val);
				}

				//System.out.println(textList);

				drawer = new VerticallyDrawer(textList);

			} else
			*/
			 {

				
				//System.out.println(paginationCatalogImporter.createCatalogMap());

				//drawer = new PaginationDrawer(paginationCatalogImporter);

			}			

		}			

		try {
			
			String [] columns = {"cats", "images", "descs", "prices", "contact"};

			List<Map<String, String>> mapList = createMapList(request, columns);

			if (mapList.isEmpty()) {
				response.getWriter().print("No such products");
				return;
			}

			desc = mapList.get(0).get("descs");
			
			System.out.println(mapList);
			
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
							cat = "-";
						}

						catalog = catalogMap.get(cat);

						if (catalog==null) {
							catalog = new Catalog(cat, map.get("images").split("\n\n\n", 2)[0]);
						}

						i = catalog.productList.size() + 1;

						Product product = new Product(this, cat + "_" + i, map);
						catalog.productList.add(product);		
						catalogMap.put(cat, catalog);

					}

					return catalogMap;
				}

			};			

			/**
			 * Check from current session first. if not signed, use preview as accountId.
			 */
			HttpSession session = request.getSession();

			String accountId = (String) session.getAttribute("accountId");
			if (accountId==null) accountId = "preview";

			String botId = request.getParameter("botId");

			if (botId==null) botId = Long.toHexString(System.currentTimeMillis());

			Context context = new MemoryContext(accountId + "/" + botId);

			context.prop("title", brand);
			
			super.addNewBot(context.toJSONString(), context.name(), context.prop());
			
			context = sessionPool().getContext(context.name());
			
			context.load();

			Canvas2D canvas2D = new Canvas2D(context, brand, 100, true);
			
			drawer = new CSVPaginationCatalogDrawer(context.name(), paginationCatalogImporter);			

			drawer.draw(canvas2D);
			
			context.save();

			context.load();

			String contextRoot = request.getContextPath();			
			
			response.getWriter().print(domain + contextRoot + "/x/" + accountId + "/" + botId);

		} catch (Exception e) {

			e.printStackTrace();
			
			response.getWriter().print(e.getMessage());
		}
	}



}
