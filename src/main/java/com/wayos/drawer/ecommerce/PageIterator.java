package com.wayos.drawer.ecommerce;

import java.util.List;

public class PageIterator<T> {
	
	private final List<T> elementList;
	
	private final int pageSize;
	
	private int page;		
	
	public PageIterator(List<T> elementList, int pageSize) {
		this.elementList = elementList;
		this.pageSize = pageSize;
		this.page = 0;
	}
	
	public boolean hasMorePage() {
		
		int offset = page * pageSize;
		
		return offset <= elementList.size();
		
	}
	
	public List<T> nextPage() {
		
		if (hasMorePage()) {
			
			int offset = page * pageSize;
			
			int toIndex = offset + pageSize;
			
			//Trim
			if (toIndex > elementList.size()) {
				toIndex = elementList.size();
			}
			
			page ++;
			
			return elementList.subList(offset, toIndex);				
		}
		return null;
	}
	
}