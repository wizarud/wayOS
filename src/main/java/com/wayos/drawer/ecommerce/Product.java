package com.wayos.drawer.ecommerce;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class Product {
	
	public final PaginationCatalogImporter paginationCatalogImporter;
	public final String sku;
	private final Map<String, String> attr;
	
	private static NumberFormat numberFormat = new DecimalFormat("0.00");
	
	/*
	public Product(String sku, Map<String, String> attr) {
		this(null, sku, attr);
	}
	*/
	
	public Product(PaginationCatalogImporter paginationCatalogImporter, String sku, Map<String, String> attr) {
		this.paginationCatalogImporter = paginationCatalogImporter;
		this.sku = sku;
		this.attr = attr;
		
		/**
		 * Add Discount Calculation
		 * 
		 */
		if (this.attr.containsKey("Price") && this.attr.containsKey("Discount (%)")) {
			
			try {
				
				double price = Double.parseDouble(this.attr.get("Price"));
				double discount = Double.parseDouble(this.attr.get("Discount (%)"));
				double discountedPrice = price - (price * (discount / 100) );
				
				this.attr.put("discountedPrice", numberFormat.format(discountedPrice));
				
			} catch (Exception e) {
				
				throw new RuntimeException(e);
				
			}
		} 
		
	}
	
	public boolean hasDiscount() {
		
		return this.attr.containsKey("discountedPrice");
	}
		
	@Override
	public boolean equals(Object o) {
		if (o instanceof Product) {
			return this.sku.equals(((Product)o).sku);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
	    return sku.hashCode();
	}	
	
	public String val(String name) {
		
		String val;
		if (paginationCatalogImporter!=null) {
			val = attr.get(paginationCatalogImporter.meta(name));			
		} else {
			val = attr.get(name);
		}
		
		if (val==null) return paginationCatalogImporter + ":" + name + ":" + paginationCatalogImporter.meta(name);
		return val;
	}
	
	@Override
	public String toString() {
		//return attr.get("bonus_link");
		return sku + ":" + attr.toString();
	}
}