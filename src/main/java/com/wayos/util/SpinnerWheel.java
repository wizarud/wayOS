package com.wayos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpinnerWheel<T> {
	
	private Random randomForce;
	
	private float totalSize;
	
	private class Item {
		
		T value;
		float offset;
		float size;
		
		Item(T value, float offset, float size) {
			this.value = value;
			this.offset = offset;
			this.size = size;
		}
		
		boolean matched(double rand) {
			return rand >= offset && rand < offset + size;
		}
		
		public String toString() {
			return "[ " + offset + " - " + (offset + size) + " ] " + value;
		}
				
	}
	
	private List<Item> itemList;

	public SpinnerWheel() {
		
		this.randomForce = new Random();
		
		this.totalSize = 0;
		
		this.itemList = new ArrayList<>();
	}
	
	public void add(T value, float size) {
		
		itemList.add(new Item(value, totalSize, size));
		
		totalSize += size;		
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		for (Item item:itemList) {
			sb.append(item);
			sb.append(System.lineSeparator());
		}
		
		return sb.toString().trim();
	}
	
	public float totalSize() {
		
		return totalSize;
	}
	
	public T spin(int force) {
		
		double rand = Math.random() * totalSize; 
		
		/**
		 * Random force
		 */
		if (force > 0) {
			
			int round = randomForce.nextInt(force);
			
			for (int i=0; i<round; i++) {
				
				rand = Math.random() * totalSize;			
			}
		}
								
		System.out.print("Pick! [" + rand + "] ");
		
		T item = matched(rand);
		
		System.out.println(item);
		
		return item;
	}
	
	private T matched(double rand) {
		
		for (Item item:itemList) {
			if (item.matched(rand)) return item.value;
		}
		
		throw new IllegalArgumentException("Invalid Random value: " + rand);
	}
	
}

