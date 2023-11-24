package com.wayos.drawer.ecommerce.lazada;

import java.util.List;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;
import com.wayos.drawer.ecommerce.Catalog;
import com.wayos.drawer.ecommerce.PageIterator;
import com.wayos.drawer.ecommerce.Product;

public class LazadaPaginationDrawer extends Drawer {
	
	private static final String morePicURL = "https://wayos.yiem.ai/images/More.png";	
	
	protected final LazadaCSVPaginationCatalogImporter paginationCatalogImporter;

	public LazadaPaginationDrawer(LazadaCSVPaginationCatalogImporter paginationCatalogImporter) {
		this.paginationCatalogImporter = paginationCatalogImporter;
	}

	@Override
	public void draw(Canvas2D canvas2D) {
		
		PageIterator<Catalog> catalogPageIterator = paginationCatalogImporter.getCatalogPageIterator(9);
		
		List<Catalog> catalogList;
		
		Canvas2D.Entity [] parentEntities = new Canvas2D.Entity[] {canvas2D.GREETING, canvas2D.UNKNOWN};

		canvas2D.nextRow(50);
		canvas2D.nextColumn(50);

		parentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(parentEntities, "", "Home", false) };
		
		canvas2D.nextRow(100);
		canvas2D.nextColumn(100);
		
		Canvas2D.Entity [] productParentEntities;
		Canvas2D.Entity entity;
		
		while (catalogPageIterator.hasMorePage()) {
			catalogList = catalogPageIterator.nextPage();
			for (Catalog c:catalogList) {
				
				entity = canvas2D.newEntity(parentEntities, "", c.imgURL + " ...", true);
				canvas2D.nextRow(100);
				
				entity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, "ดู", "", false);	
				canvas2D.nextRow(100);
				
				productParentEntities = new Canvas2D.Entity[] { entity };				

				List<Product> productList;
				PageIterator<Product> productPageIterator = c.getProductPageIterator(9);
				int count = 0;
				if (productPageIterator.hasMorePage()) {
					canvas2D.nextColumn(200);					
				}
				while (productPageIterator.hasMorePage()) {
					
					canvas2D.nextRow(100);
					productList = productPageIterator.nextPage();
					for (Product p:productList) {
						entity = canvas2D.newEntity(productParentEntities, "", p.val("product_big_img") + " " + p.val("price"), true);
						canvas2D.nextRow(100);
						entity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, "ดู", p.val("cps_tracking_link"), false);
						canvas2D.nextRow(100);
					}
					if (count>0) break;
					if (productPageIterator.hasMorePage()) {
						productParentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(productParentEntities, "More", morePicURL + " ...", true) };
						canvas2D.nextRow(100);
						productParentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(productParentEntities, "เพิ่มเติม", "", false) };
						canvas2D.nextColumn(200);
					}
					count++;
				}
			}
			
			if (catalogPageIterator.hasMorePage()) {
				parentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(parentEntities, "More", morePicURL + " ...", true) };
				canvas2D.nextRow(100);
				parentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(parentEntities, "เพิ่มเติม", "", false) };
				canvas2D.nextColumn(200);
			}
		}		
	}

}
