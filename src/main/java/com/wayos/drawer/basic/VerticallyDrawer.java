package com.wayos.drawer.basic;

import java.util.ArrayList;
import java.util.List;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

public class VerticallyDrawer extends Drawer {
	
	protected final List<String> textList;
	
	public VerticallyDrawer(List<String> textList) {
		this.textList = textList;
	}
	
	public VerticallyDrawer(List<String> textList, int limitSize) {
		
		//Repack to single message
		if (limitSize < textList.size()) {
			
        	StringBuilder sb = new StringBuilder();
        	
            for (String text:textList) {
            	
            	sb.append(text + System.lineSeparator());
            	
            }
			
			this.textList = new ArrayList<>();
			this.textList.add(sb.toString().trim());
			
			return;
		}
		
		this.textList = textList;
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		
		Canvas2D.Entity parent = canvas2D.GREETING;
		
		for (String text:textList) {
			
			canvas2D.nextRow(100);			
			canvas2D.nextColumn(100);
			
			parent = canvas2D.newEntity(new Canvas2D.Entity[] { parent }, "", text, false);	
			
		}
		
	}

}
