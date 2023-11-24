package com.wayos.drawer.ecommerce;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public abstract class PaginationCatalogImporter {
	
	public static final Map<String, String> DEFAULT_META_MAP = new HashMap<>();
	
	static {
		DEFAULT_META_MAP.put("cat", "cats");
		DEFAULT_META_MAP.put("image", "images");
		DEFAULT_META_MAP.put("price", "prices");
		DEFAULT_META_MAP.put("desc", "descs");
	}
	
	private Map<String, String> metaMap;
	
	public PaginationCatalogImporter() {
		this.metaMap = DEFAULT_META_MAP;
	}
	
	public PaginationCatalogImporter(Map<String, String> metaMap) {
		this.metaMap = metaMap;
	}
	
	private Map<String, Catalog> catalogMap;
	
	private final Map<String, Catalog> catalogMap() {
		if (catalogMap==null) {
			catalogMap = createCatalogMap();
		}
		return catalogMap;
	}
	
	public String meta(String name) {
		String val = this.metaMap.get(name);
		if (val==null) {
			return name;
		}
		return val;
	}
	
	public abstract Map<String, Catalog> createCatalogMap();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		List<Product> productList;
		for (String catalogName:catalogMap().keySet()) {
			sb.append(catalogName);
			sb.append(System.lineSeparator());
			productList = catalogMap().get(catalogName).productList;
			for (Product p:productList) {
				sb.append("\t" + p.sku + System.lineSeparator());
			}
		}
		
		return sb.toString();
	}	
	
	public boolean isFitForWayobot() {
		if (catalogMap().keySet().size() > 10) {
			
			System.out.println(catalogMap().keySet().size() + "\t" + Math.sqrt(catalogMap().keySet().size()));
			/*
			for (Entry<Catalog, List<Product>> entry:catalogProductMap.entrySet()) {
				if (entry.getValue().size()>10) return false;
			}
			*/
			return true;
		}
		return false;
	}	
	
	public static Set<String> brandSet(InputStream is, String delimeter) {
		Scanner sc = new Scanner(is);
		Set<String> brandSet = new HashSet<>();
		
		String line;
		String [] headers;
		String [] cols;
		Map<String, String> valMap;
		line = sc.nextLine();
		headers = line.split(delimeter);
		
		while(sc.hasNextLine()) {
			valMap = new HashMap<>();
			line = sc.nextLine();
			cols = line.split(delimeter);
			for (int i=0;i<headers.length;i++) {
				valMap.put(headers[i], cols[i].trim());
			}
			brandSet.add(valMap.get("brand_name"));
		}
		
		return brandSet;
	}
	
	public PageIterator<Catalog> getCatalogPageIterator(int pageSize) {	
		return new PageIterator<Catalog>(new ArrayList<>(catalogMap().values()), pageSize);
	}

}
