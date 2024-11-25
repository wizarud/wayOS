package com.wayos.drawer.ecommerce;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * @author eoss-th
 *
 */
public class CatalogImporter extends PaginationCatalogImporter {
	
    NumberFormat priceFormatter = NumberFormat.getInstance();
    
	protected final String brandName;
	protected final String delimeter;
	protected final InputStream csvInputStream;
		
	public CatalogImporter(String brandName, InputStream csvInputStream, String delimeter) {
		this.brandName = brandName;
		this.delimeter = delimeter;		
		this.csvInputStream = csvInputStream;
		
		/**
		 * Use Catalog, Image URL, Price, Description as CSV Headers
		 */

		DEFAULT_META_MAP.put("cat", "Catalog");
		DEFAULT_META_MAP.put("image", "Image URL");
		DEFAULT_META_MAP.put("price", "Price");
		DEFAULT_META_MAP.put("desc", "Description");
		DEFAULT_META_MAP.put("sku", "SKU");
		DEFAULT_META_MAP.put("discount", "Discount (%)");
	}
	
	@Override
	public Map<String, Catalog> createCatalogMap() {
		
		Scanner sc = new Scanner(csvInputStream);
		
		String line;
		String [] cols;
		
		List<List<String>> rowList = new ArrayList<>();
		
		List<String> colList;
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			colList = new ArrayList<>();
			cols = line.split(delimeter);
			for (int i=0;i<cols.length;i++) {
				colList.add(cols[i].trim());
			}			
			rowList.add(colList);
		}
		
		sc.close();		
		
		Map<String, Catalog> catalogMap = new HashMap<>();
		
		Catalog catalog;
		String cat;
		String desc;
		
		List<String> headers = rowList.get(0);
		
		if (
			!headers.contains("Catalog") ||
			!headers.contains("Image URL") ||
			!headers.contains("Price") ||
			!headers.contains("Description")
			) throw new IllegalArgumentException("Missing Required Columns for TSV Catalog [Catalog, Image URL, Price, Description]!");
		
		Map<String, String> map;
		
		int i;
		
		String headerKey;
		String colValue;
		String sku;
		String image;
		String price;
		
		int minColSize;
		double priceValue;
        for (int row=1; row<rowList.size(); row++) {
        	
        	map = new HashMap<>();
			colList = rowList.get(row);
			
			//Limit to minimum columns size
			minColSize = headers.size();
			if (minColSize > colList.size()) {
				minColSize = colList.size();
			}
			
			for (int j=0; j<minColSize; j++) {
				
				headerKey = headers.get(j).toString();
				colValue = colList.get(j).toString().trim();
				
				if (!colValue.isEmpty()) {
					map.put(headerKey, colValue);
				}
				
			}
			
			image = map.get(meta("image"));
			
			if (image==null) continue;//Skip empty image
			
			cat = map.get(meta("cat"));
			
			if (cat==null) {
				cat = "-";
				map.put("Catalog", cat);//Hot Fix
			}
			
			desc = map.get(meta("desc"));
			
			if (desc==null) {
				desc = "-";
				map.put("Description", desc);//Hot Fix
			}
			
			catalog = catalogMap.get(cat);
			
			if (catalog==null) {
				catalog = new Catalog(cat, desc, image);
			}
			
			catalogMap.put(cat, catalog);
			
			/**
			 * Price > 0 to check that this record of TSV is product
			 */
			try {
				
				price = map.get(meta("price"));
				
				priceValue = priceFormatter.parse(price).doubleValue();
				
				//Prevent Default Numeric Value from jSpreadSheet
				if (priceValue > 0) {
										
					map.put(meta("price"), priceFormatter.format(priceValue));//Clean Price
					
					sku = map.get(meta("sku"));
					if (sku==null) {
						i = catalog.productList.size() + 1;
						if (cat.equals("-")) 
							sku = "P" + i;
						else
							sku = cat + "_" + i;
					}
					
					sku = sku.replace(' ', '_');
					
					Product product = new Product(this, sku, map);
					
					catalog.productList.add(product);
					
				}
				
			} catch(Exception e) {
				
				continue;
				//throw new RuntimeException(e);
			}
			
        }
        
		return catalogMap;
	}

}
