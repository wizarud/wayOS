package com.wayos.drawer.ecommerce;

import java.util.ArrayList;
import java.util.List;

public class Catalog {
	public final String name;
	public final String description;
	public final String imgURL;
	public List<Product> productList;
	
	public Catalog(String name, String description, String imgURL) {
		this.name = name;
		this.description = description;
		this.imgURL = imgURL;
		this.productList = new ArrayList<>();
	}
	
	public Catalog(String name, String imgURL) {
		this(name, name, imgURL); //Use name as Description
	}
	
	public PageIterator<Product> getProductPageIterator(int pageSize) {
		return new PageIterator<Product>(productList, pageSize);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Catalog) {
			return this.name.equals(((Catalog)o).name);
		}
		return false;
	}
	@Override
	public int hashCode() {
	    return name.hashCode();
	}	
	@Override
	public String toString() {
		return this.name + "\t" + this.imgURL + "\t" + this.productList;
	}
}