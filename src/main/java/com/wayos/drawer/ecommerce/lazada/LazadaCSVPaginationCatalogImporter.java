package com.wayos.drawer.ecommerce.lazada;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.wayos.drawer.ecommerce.CSVPaginationCatalogImporter;
import com.wayos.drawer.ecommerce.Catalog;
import com.wayos.drawer.ecommerce.Product;

public class LazadaCSVPaginationCatalogImporter extends CSVPaginationCatalogImporter {
	
	public LazadaCSVPaginationCatalogImporter(String brandName, InputStream csvInputStream, String delimeter) {		
		super(brandName, csvInputStream, delimeter);		
	}
	
	@Override
	public Map<String, Catalog> createCatalogMap() {
		
		Map<String, Catalog> catalogMap = new HashMap<>();
		
		Scanner sc = new Scanner(csvInputStream);
		
		/**
		 * product_small_img	
		 * is_flash_sale	
		 * product_id	
		 * seller_name	
		 * has_bonus_commission	
		 * is_hot_deals	
		 * product_medium_img	
		 * seller_rating	
		 * image_url_2	
		 * current_price	
		 * bonus_commission_rate	
		 * image_url_5	
		 * price	
		 * currency	
		 * product_big_img	
		 * sku_id	
		 * lazada_sku	
		 * image_url_4	
		 * discount_percentage	
		 * product_name	
		 * is_purchasable	
		 * product_url	
		 * brand_name	
		 * image_url_3	
		 * description	
		 * cps_tracking_link	
		 * bonus_link
		 */
		String line;
		String [] headers;
		String [] cols;
		Map<String, String> valMap;
		line = sc.nextLine();
		headers = line.split(delimeter);
		
		Catalog catalog;
		String catalogName;
		String productURL;
		String sku;
		
		while (sc.hasNextLine()) {
			valMap = new HashMap<>();
			line = sc.nextLine();
			cols = line.split(delimeter);
			for (int i=0;i<headers.length;i++) {
				valMap.put(headers[i], cols[i].trim());
			}
			
			if (!valMap.get("brand_name").equals(brandName)) {
				continue;
			}
			
			sku = valMap.get("lazada_sku");
			productURL = valMap.get("product_url");
			catalogName = productURL.replace("https://www.lazada.co.th/products/", "").replace(sku.replace("_TH-", "-s"), "").replace(".html", "").replace(brandName.replace(" ", "-").toLowerCase(), "");

			catalog = catalogMap.get(catalogName);
			
			if (catalog==null) {
				catalog = new Catalog(catalogName, valMap.get("product_big_img"));				
				catalogMap.put(catalogName, catalog);
			}
			
			catalog.productList.add(new Product(this, sku, valMap));
		}		
		
		sc.close();
		
		return catalogMap;
	}
	
}
