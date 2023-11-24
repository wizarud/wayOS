package com.wayos.drawer.basic;

import com.wayos.Context;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

public class CopyDrawer extends Drawer {
	
	protected final Context fromContext;
	
	public CopyDrawer(Context fromContext) {
		this.fromContext = fromContext;
		
		try {
			this.fromContext.load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		
		canvas2D.context.loadJSON(this.fromContext.toJSONString());
		
	}

}
