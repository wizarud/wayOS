package com.wayos.drawer.ecommerce;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

public class PaginationDrawer extends Drawer {
	    
	private static final String morePicURL = "https://wayos.yiem.cc/images/More.png";	
	
	protected final PaginationCatalogImporter paginationCatalogImporter;

	public PaginationDrawer(PaginationCatalogImporter paginationCatalogImporter) {
		this.paginationCatalogImporter = paginationCatalogImporter;
	}

	@Override
	public void draw(Canvas2D canvas2D) {
				
		String language = canvas2D.context.prop("language");
		if (language==null) {
			language = "en";
		}
		
		Locale locale = new Locale(language);
		
		ResourceBundle bundle = ResourceBundle.getBundle("com.wayos.i18n.text", locale);
		
		/**
		 * Register Rich Menu Items for shortcut!
		 */
		canvas2D.context.prop("richMenus", bundle.getString("cart.label") + ", " + bundle.getString("cart.checkout") + ", " + bundle.getString("cart.clear") + ", " + bundle.getString("cart.order.view"));
		
		PageIterator<Catalog> catalogPageIterator = paginationCatalogImporter.getCatalogPageIterator(9);
		
		List<Catalog> catalogList;
		
		Canvas2D.Entity [] parentEntities = new Canvas2D.Entity[] {canvas2D.GREETING, canvas2D.UNKNOWN};

		canvas2D.nextRow(50);
		canvas2D.nextColumn(50);
		
		Canvas2D.Entity homeEntity = canvas2D.newEntity(parentEntities, "", bundle.getString("cart.home"), false);

		parentEntities = new Canvas2D.Entity[] { homeEntity };
		
		canvas2D.nextRow(100);
		canvas2D.nextColumn(100);
		
		Canvas2D.Entity [] productParentEntities;
		Canvas2D.Entity entity;
		Canvas2D.Entity AddOnlyMenuEntity, AddRemoveMenuEntity, addChoiceEntity, removeChoiceEntity, removeChoiceZeroCaseEntity, removeChoiceMoreThanZeroCaseEntity, emptyItemEntity, hasItemEntity;
		
		List<Canvas2D.Entity> productViewEntityList = new ArrayList<>();
		//List<Canvas2D.Entity> productAfterAddOrRemoveEntityList = new ArrayList<>();
		Map<Canvas2D.Entity, Canvas2D.Entity> productParentMap = new HashMap<>();
		
		StringBuilder cartOrderLines = new StringBuilder();
		List<String> cartZeroLines = new ArrayList<>();
		StringBuilder cartClearCmds = new StringBuilder("`?s_orders=` `?i_totalPrice=0`");
		
		Canvas2D.Entity parentEntity;
		
	    NumberFormat priceFormatter = NumberFormat.getInstance();
	    
		String realPrice, calPrice;
		
		int lastX, lastY;
		while (catalogPageIterator.hasMorePage()) {
			catalogList = catalogPageIterator.nextPage();
			for (Catalog c:catalogList) {
				
				if (c.productList.isEmpty()) continue;//Skip Empty product catalog
				
				entity = canvas2D.newEntity(parentEntities, c.name, c.imgURL + " " + c.description, true);
				canvas2D.nextRow(100);
				
				entity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, bundle.getString("cart.view"), "", false);	
				canvas2D.nextRow(100);
				
				parentEntity = entity;				
				productParentEntities = new Canvas2D.Entity[] { entity };

				List<Product> productList;
				PageIterator<Product> productPageIterator = c.getProductPageIterator(9);
				int count = 0;
				if (productPageIterator.hasMorePage()) {
					canvas2D.nextColumn(400);
				}
				
				String sku;
				while (productPageIterator.hasMorePage()) {
					
					canvas2D.nextRow(100);
					productList = productPageIterator.nextPage();
					for (Product p:productList) {
						
						lastX = canvas2D.getX();
						sku = p.sku.replace('_', ' ');
						
						if (p.hasDiscount()) {
							
							entity = canvas2D.newEntity(productParentEntities, sku, 
									p.val("image") + " " + sku + " " + bundle.getString("cart.price") + " " + p.val("price") + " " + bundle.getString("cart.currency") + " " + bundle.getString("cart.discount") + " " + p.val("discount") + "% " + bundle.getString("cart.discount.off"), true);
							canvas2D.nextRow(100);
							canvas2D.nextColumn(400);
							
							realPrice = p.val("discountedPrice");
							
						} else {
							
							entity = canvas2D.newEntity(productParentEntities, sku, 
									p.val("image") + " " + sku + " " + bundle.getString("cart.price") + " " + p.val("price") + " " + bundle.getString("cart.currency"), true);
							canvas2D.nextRow(100);
							canvas2D.nextColumn(400);
							
							realPrice = p.val("price");
						}
						
						try {
							calPrice = priceFormatter.parse(realPrice).toString();
						} catch (ParseException e) {
							throw new IllegalArgumentException("Invalid price for " + realPrice);
						}
						
						entity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, bundle.getString("cart.detail"), p.val("desc"), "#i_" + p.sku, false);
						canvas2D.nextRow(100);
						canvas2D.nextColumn(200);
						
						AddOnlyMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, "0", bundle.getString("cart.item") + " #i_" + p.sku + " " + bundle.getString("cart.unit"), true);
						canvas2D.nextRow(100);
						
						productViewEntityList.add(AddOnlyMenuEntity);
						
						AddRemoveMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, "", bundle.getString("cart.item") + " #i_" + p.sku + " " + bundle.getString("cart.unit"), true);
						canvas2D.nextRow(100);
						canvas2D.nextColumn(200);
						
						productViewEntityList.add(AddRemoveMenuEntity);

						addChoiceEntity = canvas2D.newEntity(new Canvas2D.Entity[] { AddOnlyMenuEntity, AddRemoveMenuEntity }, bundle.getString("cart.add"), 
								"`?i_" + p.sku + "=+1` `?i_totalPrice=+" + calPrice + "`", false);
						canvas2D.nextRow(100);
						
						removeChoiceEntity = canvas2D.newEntity(new Canvas2D.Entity[] { AddRemoveMenuEntity }, bundle.getString("cart.remove"), "", 
								"#i_" + p.sku, false);//Attach Conditional Parameter
						canvas2D.nextRow(100);
						canvas2D.nextColumn(200);

						removeChoiceMoreThanZeroCaseEntity = canvas2D.newEntity(new Canvas2D.Entity[] { removeChoiceEntity }, "", "", "`?i_" + p.sku + "=-1` `?i_totalPrice=-" + calPrice + "`" + " #i_" + p.sku, false);
						canvas2D.nextRow(100);
						
						removeChoiceZeroCaseEntity = canvas2D.newEntity(new Canvas2D.Entity[] { removeChoiceEntity, removeChoiceMoreThanZeroCaseEntity }, "0", 
								"", false);
						canvas2D.nextRow(100);
						canvas2D.nextColumn(200);
						
						emptyItemEntity = canvas2D.newEntity(new Canvas2D.Entity[] { removeChoiceZeroCaseEntity }, "", 
								bundle.getString("cart.item.empty") + " " + p.val("desc") + " " + bundle.getString("cart.item.empty.tail"), false);
						canvas2D.nextRow(100);
						
						//productAfterAddOrRemoveEntityList.add(emptyItemEntity);						
						
						hasItemEntity = canvas2D.newEntity(new Canvas2D.Entity[] { addChoiceEntity, removeChoiceMoreThanZeroCaseEntity }, "", 
								bundle.getString("cart.item") + " " + p.val("desc") + " " + realPrice  + " " + bundle.getString("cart.currency") + " x " + " #i_" + p.sku + " " + bundle.getString("cart.unit"), false);
						canvas2D.nextRow(100);
						canvas2D.nextColumn(200);
						
						//productAfterAddOrRemoveEntityList.add(hasItemEntity);
						
						productParentMap.put(emptyItemEntity, parentEntity);
						productParentMap.put(hasItemEntity, parentEntity);
						
						//cartOrderLines.append(p.sku.replace('-', ' ') + " " + realPrice + " " + bundle.getString("cart.currency") + " x " + " #i_" + p.sku + " " + bundle.getString("cart.unit") + "[br]");//&#10; is html unicode newline
						cartOrderLines.append(p.sku.replace('-', ' ') + " " + realPrice + " " + bundle.getString("cart.currency") + " x " + " #i_" + p.sku + " " + bundle.getString("cart.unit") + "\n");//&#10; is html unicode newline
						//cartZeroLines.add(p.sku.replace('-', ' ') + " " + realPrice + " " + bundle.getString("cart.currency") + " x " + " 0 " + bundle.getString("cart.unit") + "[br]");
						cartZeroLines.add(p.sku.replace('-', ' ') + " " + realPrice + " " + bundle.getString("cart.currency") + " x " + " 0 " + bundle.getString("cart.unit") + "\n");
						cartClearCmds.append(" `?i_" + p.sku + "=0`");
						
						lastY = canvas2D.getY();
						
						canvas2D.setPosition(lastX, lastY);						
					}
					if (count>0) break;
					if (productPageIterator.hasMorePage()) {
						productParentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(productParentEntities, bundle.getString("cart.more"), morePicURL + " ...", true) };
						canvas2D.nextRow(100);
						productParentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(productParentEntities, bundle.getString("cart.more"), "", false) };
						canvas2D.nextColumn(400);
					}
					count++;
				}
			}
			
			if (catalogPageIterator.hasMorePage()) {
				parentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(parentEntities, bundle.getString("cart.more"), morePicURL + " ...", true) };
				canvas2D.nextRow(100);
				parentEntities = new Canvas2D.Entity[] { canvas2D.newEntity(parentEntities, bundle.getString("cart.more"), "", false) };
				canvas2D.nextColumn(400);
			}
		}
		
		canvas2D.nextColumn(100);
		
		/**
		 * Create Shopping Cart & Home Menu
		 */		
		Set<Canvas2D.Entity> productAfterAddOrRemoveEntitySet = productParentMap.keySet();
		
		Canvas2D.Entity[] productAfterAddOrRemoveEntities = new Canvas2D.Entity[productAfterAddOrRemoveEntitySet.size()];
		productAfterAddOrRemoveEntities = productAfterAddOrRemoveEntitySet.toArray(productAfterAddOrRemoveEntities);
				
		/**
		 * Back to Parent Category
		 */
		Canvas2D.Entity mainMenuEntity, backMenuEntity;
		
		for (Canvas2D.Entity productEntity:productAfterAddOrRemoveEntities) {
			
			lastX = canvas2D.getX();
						
			mainMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { productEntity }, "", bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency"), true);	
			canvas2D.nextRow(100);
			canvas2D.nextColumn(200);
			
			backMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { mainMenuEntity }, bundle.getString("cart.back"), "", false);
			canvas2D.nextRow(100);
			canvas2D.bind(new Canvas2D.Entity[] { backMenuEntity }, productParentMap.get(productEntity));
			
			productViewEntityList.add(mainMenuEntity);
			
			lastY = canvas2D.getY();
			
			canvas2D.setPosition(lastX, lastY);
		}
		
		Canvas2D.Entity[] productViewEntities = new Canvas2D.Entity[productViewEntityList.size()];
		productViewEntities = productViewEntityList.toArray(productViewEntities);
		
		String orders = cartOrderLines.toString().trim();
		StringBuilder subZeroCommands = new StringBuilder("`?s_orders=" + orders + "`");
		for (String subZeroItem:cartZeroLines) {
			subZeroCommands.append(" `?s_orders=-" + subZeroItem + "`");
		}
				
		Canvas2D.Entity cartMenuEntity = canvas2D.newEntity(productViewEntities, bundle.getString("cart.label"), "",
				 subZeroCommands.toString() + " #i_totalPrice", false);
		canvas2D.nextRow(500);
		canvas2D.nextColumn(700);
		
		Canvas2D.Entity emptyCartItemsEntity = canvas2D.newEntity(new Canvas2D.Entity[] { cartMenuEntity }, "0", bundle.getString("cart.empty"), "", true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity backHomeEntity = canvas2D.newEntity(new Canvas2D.Entity[] { emptyCartItemsEntity }, bundle.getString("cart.back"), "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		canvas2D.bind(new Canvas2D.Entity[] { backHomeEntity }, homeEntity);
				
		Canvas2D.Entity cartItemsEntity = canvas2D.newEntity(new Canvas2D.Entity[] { cartMenuEntity }, "", bundle.getString("cart.summary") + "\n#s_orders\n" +  bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency"), "", true);
		canvas2D.nextColumn(200);
		
		/**
		 * Simple Checkout
		 */
		Canvas2D.Entity clearEntity = canvas2D.newEntity(new Canvas2D.Entity[] { cartItemsEntity }, bundle.getString("cart.clear"), "🚮", cartClearCmds.toString().trim(), false);
		canvas2D.nextRow(400);
		canvas2D.bind(new Canvas2D.Entity[] { clearEntity }, homeEntity);
		
		Canvas2D.Entity checkoutEntity = canvas2D.newEntity(new Canvas2D.Entity[] { cartItemsEntity }, bundle.getString("cart.checkout"), "", "#contact", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
				
		/**
		 * New Customer, Ask Contact & Location
		 */
		Canvas2D.Entity askContactEntity = canvas2D.newEntity(new Canvas2D.Entity[] { checkoutEntity }, "#contact", bundle.getString("cart.ask.contact"), true);
		canvas2D.nextRow(100);
				
		/**
		 * Return Customer, Confirm Contact & Location
		 */
		Canvas2D.Entity confirmContactEntity = canvas2D.newEntity(new Canvas2D.Entity[] { checkoutEntity }, "", bundle.getString("cart.confirm.contact") + "\n\n#contact\n\n" + bundle.getString("cart.confirm.no"), true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity yesContactEntity = canvas2D.newEntity(new Canvas2D.Entity[] { confirmContactEntity }, bundle.getString("cart.confirm.yes"), "", false);
		canvas2D.nextRow(100);
		
		/**
		 * Enter & Save contact
		 */
		Canvas2D.Entity answerContactEntity = canvas2D.newEntity(new Canvas2D.Entity[] { askContactEntity, confirmContactEntity }, "", bundle.getString("cart.confirm.contact") + "\n\n##", "`?contact=##`", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(400);
		
		/**
		 * Generate Tracking Order Id & Clear Orders
		 */
		//String trackingOrderCmd = "`?THE_ORDER=%timehex` `?ORDER_#THE_ORDER=%year-%monthNumber-%date %hour:%minute:%second NEW[br][br]#s_orders " + bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency") + "`";
		String trackingOrderCmd = "`?THE_ORDER=%timehex` `?ORDER_#THE_ORDER=%year-%monthNumber-%date %hour:%minute:%second NEW\n\n#s_orders " + bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency") + "`";

		Canvas2D.Entity clearOrdersEntity = canvas2D.newEntity(new Canvas2D.Entity[] { yesContactEntity, answerContactEntity }, "", "", trackingOrderCmd + " " + cartClearCmds.toString().trim(), false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity trackingEntity = canvas2D.newEntity(new Canvas2D.Entity[] { clearOrdersEntity }, "", bundle.getString("cart.order.id") + " #THE_ORDER" + "\n\n" + bundle.getString("cart.thanks"), null);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);		
		
		//Canvas2D.Entity thankEntity = canvas2D.newEntity(new Canvas2D.Entity[] { trackingEntity }, "", bundle.getString("cart.thanks"), null);
		
		canvas2D.setPosition(400, 100);
		/**
		 * General Commands to track & update orders
		 */
		String viewOrderCmd = "`?VIEW_ORDER=##`";
		
		Canvas2D.Entity viewOrderStatusEntity = canvas2D.newEntity(null, bundle.getString("cart.order.view"), "", viewOrderCmd, false);
		canvas2D.nextRow(100);
				
		String updateOrderParams = "`?VIEW_ORDER=#1` `?STATUS=#2`";//The last #ORDER_ is for protect from removing session vars policy.
		
		Canvas2D.Entity updateOrderParamsEntity = canvas2D.newEntity(null, bundle.getString("cart.order.remark"), "", updateOrderParams, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		//String updateOrderStatus = "`?ORDER_#VIEW_ORDER=+[br]%year-%monthNumber-%date %hour:%minute:%second #STATUS`";
		String updateOrderStatus = "`?ORDER_#VIEW_ORDER=+\n%year-%monthNumber-%date %hour:%minute:%second #STATUS`";
		
		Canvas2D.Entity updateOrderStatusEntity = canvas2D.newEntity(new Canvas2D.Entity[] { updateOrderParamsEntity }, "", updateOrderStatus, false);
		canvas2D.nextRow(200);
		
		canvas2D.newEntity(new Canvas2D.Entity[] { viewOrderStatusEntity, updateOrderStatusEntity }, "", "#ORDER_#VIEW_ORDER", null);
		
	}

}
