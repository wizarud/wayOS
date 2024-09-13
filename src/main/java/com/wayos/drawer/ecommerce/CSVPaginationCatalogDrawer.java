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

import com.wayos.Application;
import com.wayos.Configuration;
import com.wayos.PathStorage;
import com.wayos.drawer.Canvas2D;
import com.wayos.drawer.Drawer;

import x.org.json.JSONObject;

public class CSVPaginationCatalogDrawer extends Drawer {
	    
	protected final String contextName;
		
	protected String businessInfo;
	
	protected String emptyCartImageURL;
	
	protected String filledCartImageURL;
	
	protected String morePicURL;
	
	protected final PaginationCatalogImporter paginationCatalogImporter;

	public CSVPaginationCatalogDrawer(String contextName, PaginationCatalogImporter paginationCatalogImporter) {
		
		this.contextName = contextName;
		
		this.paginationCatalogImporter = paginationCatalogImporter;
	}

	public void setBusinessInfo(String businessInfo) {
		this.businessInfo = businessInfo;
	}
	
	public void setEmptyCartImageURL(String emptyCartImageURL) {
		this.emptyCartImageURL = emptyCartImageURL;
	}	

	public void setFilledCartImageURL(String filledCartImageURL) {
		this.filledCartImageURL = filledCartImageURL;
	}

	public void setMorePicURL(String morePicURL) {
		this.morePicURL = morePicURL;
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
		canvas2D.context.prop("richMenus", bundle.getString("cart.label") + ", " + /*bundle.getString("cart.checkout") + ", " + */ bundle.getString("cart.clear") + ", " + bundle.getString("cart.order.view"));
		
		PageIterator<Catalog> catalogPageIterator = paginationCatalogImporter.getCatalogPageIterator(9);
		
		List<Catalog> catalogList;
		
		Canvas2D.Entity [] parentEntities = new Canvas2D.Entity[] {canvas2D.GREETING /*, canvas2D.UNKNOWN*/};

		canvas2D.nextRow(50);
		canvas2D.nextColumn(50);
		
		Boolean isForwarder = null;
		
		if (catalogPageIterator.hasMorePage()) {
			
			isForwarder = false;
		}
			
		Canvas2D.Entity homeEntity = canvas2D.newEntity(parentEntities, "", bundle.getString("cart.home"), isForwarder);

		parentEntities = new Canvas2D.Entity[] { homeEntity };
		
		canvas2D.nextRow(100);
		canvas2D.nextColumn(100);
		
		Canvas2D.Entity [] productParentEntities;
		Canvas2D.Entity entity;
		Canvas2D.Entity AddOnlyMenuEntity, AddRemoveMenuEntity, addChoiceEntity, removeChoiceEntity, removeChoiceZeroCaseEntity, removeChoiceMoreThanZeroCaseEntity, emptyItemEntity, hasItemEntity;
		
		List<Canvas2D.Entity> productViewEntityList = new ArrayList<>();
		//List<Canvas2D.Entity> productAfterAddOrRemoveEntityList = new ArrayList<>();
		Map<Canvas2D.Entity, Canvas2D.Entity> productParentMap = new HashMap<>();
		Map<Canvas2D.Entity, Product> productEntityMap = new HashMap<>();
		
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
				
				entity = canvas2D.newEntity(parentEntities, c.name, c.imgURL + " " + c.description, true);
				canvas2D.nextRow(100);
				
				if (c.productList.isEmpty()) {
					
					entity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, bundle.getString("cart.view"), "", null);	
					canvas2D.nextRow(100);
					
					continue;//Skip Empty product catalog
				}
								
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
						
						AddOnlyMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, "0", p.val("image") + " " + bundle.getString("cart.item") + " #i_" + p.sku + " " + bundle.getString("cart.unit"), true);
						canvas2D.nextRow(100);
						
						productViewEntityList.add(AddOnlyMenuEntity);
						
						AddRemoveMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { entity }, "", p.val("image") + " " + bundle.getString("cart.item") + " #i_" + p.sku + " " + bundle.getString("cart.unit"), true);
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
						
						productEntityMap.put(emptyItemEntity, p);
						productEntityMap.put(hasItemEntity, p);
						
						cartOrderLines.append(p.sku.replace('-', ' ') + " " + realPrice + " " + bundle.getString("cart.currency") + " x " + " #i_" + p.sku + " " + bundle.getString("cart.unit") + "[br]");//&#10; is html unicode newline
						cartZeroLines.add(p.sku.replace('-', ' ') + " " + realPrice + " " + bundle.getString("cart.currency") + " x " + " 0 " + bundle.getString("cart.unit") + "[br]");
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
				/**
				 * Set Home as leaf Node if there is no product children
				 */
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
		Canvas2D.Entity mainMenuEntity, backMenuEntity, productParentEntity;
		Product p;
		for (Canvas2D.Entity productEntity:productAfterAddOrRemoveEntities) {
			
			lastX = canvas2D.getX();
			
			productParentEntity = productParentMap.get(productEntity);
			p = productEntityMap.get(productEntity);
			
			mainMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { productEntity }, "", p.val("image") + " " + bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency"), true);	
			canvas2D.nextRow(100);
			canvas2D.nextColumn(200);
			
			backMenuEntity = canvas2D.newEntity(new Canvas2D.Entity[] { mainMenuEntity }, bundle.getString("cart.back"), "", false);
			canvas2D.nextRow(100);
			canvas2D.bind(new Canvas2D.Entity[] { backMenuEntity }, productParentEntity);
			
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
		
		Canvas2D.Entity emptyCartItemsEntity = canvas2D.newEntity(new Canvas2D.Entity[] { cartMenuEntity }, "0", emptyCartImageURL + " " + bundle.getString("cart.empty"), "", true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity backHomeEntity = canvas2D.newEntity(new Canvas2D.Entity[] { emptyCartItemsEntity }, bundle.getString("cart.back"), "", false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		canvas2D.bind(new Canvas2D.Entity[] { backHomeEntity }, homeEntity);
				
		Canvas2D.Entity cartItemsEntity = canvas2D.newEntity(new Canvas2D.Entity[] { cartMenuEntity }, "", filledCartImageURL + " " + bundle.getString("cart.summary") + "\n#s_orders\n" +  bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency"), "", true);
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
		Canvas2D.Entity askContactEntity = canvas2D.newEntity(new Canvas2D.Entity[] { checkoutEntity }, "", bundle.getString("cart.ask.contact"), true);
		canvas2D.nextRow(100);
				
		/**
		 * Return Customer, Confirm Contact & Location
		 */
		Canvas2D.Entity confirmContactEntity = canvas2D.newEntity(new Canvas2D.Entity[] { checkoutEntity }, "*", bundle.getString("cart.confirm.contact") + "\n\n#contact\n\n" + bundle.getString("cart.confirm.no"), true);
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
		 * Request Payment such as Slip from customer
		 * TODO: Need to be validate businessInfo to skip this step???
		 */		
		String businessInfo;
		
		if (this.businessInfo!=null && !this.businessInfo.trim().isEmpty()) {
			
			businessInfo = this.businessInfo;
			
		} else {
			
			String [] tokens = contextName.split("/");
			String accountId = tokens[0];
			
			String accountIdJsonPath = Configuration.USER_PATH + accountId + ".json";
			
			PathStorage storage = Application.instance().get(PathStorage.class);
			JSONObject accountJSONObject = storage.readAsJSONObject(accountIdJsonPath);
			businessInfo = accountJSONObject.optString("businessInfo");
			
		}
		
		Canvas2D.Entity requestPaymentEntity = canvas2D.newEntity(new Canvas2D.Entity[] { yesContactEntity, answerContactEntity }, "", bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency") + "\n" + bundle.getString("cart.request.payment") + "\n\n" + businessInfo, true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		/**
		 * Generate Tracking Order Id & Clear Orders
		 */
		String trackingOrderCmd = "`?THE_ORDER=%timehex` `?l_PAYMENT_#THE_ORDER=##` `?l_ORDER_#THE_ORDER=Order ID:#THE_ORDER[br]#contact[br][br]#s_orders[br]" + bundle.getString("cart.total") + " #i_totalPrice " + bundle.getString("cart.currency") + "[br][br]%year-%monthNumber-%date %hour:%minute:%second NEW`" + " " + "`?e_.=" + bundle.getString("cart.order.notify") + " #contact,#channel/#sessionId,#THE_ORDER`";

		Canvas2D.Entity clearOrdersEntity = canvas2D.newEntity(new Canvas2D.Entity[] { requestPaymentEntity }, "", "", trackingOrderCmd + " " + cartClearCmds.toString().trim(), false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity trackingEntity = canvas2D.newEntity(new Canvas2D.Entity[] { clearOrdersEntity }, "", bundle.getString("cart.order.id") + " #THE_ORDER" + "\n\n" + bundle.getString("cart.thanks"), null);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);		
		
		//Canvas2D.Entity thankEntity = canvas2D.newEntity(new Canvas2D.Entity[] { trackingEntity }, "", bundle.getString("cart.thanks"), null);
		
		canvas2D.setPosition(400, 100);
		
		/**
		 * Callback command for Admin Menu
		 */
		String updateOrderParams = "`?VIEW_ORDER=#1` `?STATUS=#2`";//The last #l_ORDER_ is for protect from removing session vars policy.
		
		Canvas2D.Entity updateOrderParamsEntity = canvas2D.newEntity(null, bundle.getString("cart.order.remark"), "", updateOrderParams, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		String updateOrderStatus = "`?l_ORDER_#VIEW_ORDER=+[br]%year-%monthNumber-%date %hour:%minute:%second #STATUS`";
		
		Canvas2D.Entity updateOrderStatusEntity = canvas2D.newEntity(new Canvas2D.Entity[] { updateOrderParamsEntity }, "", updateOrderStatus, false);
		canvas2D.nextRow(200);
		
		/**
		 * General Commands For Customer to track their status
		 */		
		Canvas2D.Entity viewOrderStatusEntity = canvas2D.newEntity(null, bundle.getString("cart.order.view"), bundle.getString("cart.order.view.request.order.id"), true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		String viewOrderCmd = "`?VIEW_ORDER=##`";
		Canvas2D.Entity viewOrderAnswerEntity1 = canvas2D.newEntity(new Canvas2D.Entity[] { viewOrderStatusEntity }, "", "", viewOrderCmd, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		viewOrderCmd = "`?result=#l_ORDER_#VIEW_ORDER`";
		Canvas2D.Entity viewOrderAnswerEntity2 = canvas2D.newEntity(new Canvas2D.Entity[] { viewOrderAnswerEntity1, updateOrderStatusEntity }, "", "", viewOrderCmd, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity viewOrderDisplayEntity = canvas2D.newEntity(new Canvas2D.Entity[] { viewOrderAnswerEntity2 }, "", "#result", null);
		
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		/**
		 * Simple update Commands For Administrator to update order status
		 * Redirect to target session to parse e's value command
		 */
		String updateOrderParamsForAdmin = "`?$=,` `?contact=#1` `?target=#2` `?THE_ORDER=#3`";
		Canvas2D.Entity updateOrderParamsForAdminEntity = canvas2D.newEntity(null, bundle.getString("cart.order.notify"), "", updateOrderParamsForAdmin, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity adminMenu1 = canvas2D.newEntity(new Canvas2D.Entity[] { updateOrderParamsForAdminEntity }, "", bundle.getString("cart.order.remark") + "\n\n#contact\n\n#THE_ORDER", true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		Canvas2D.Entity adminMenu2 = canvas2D.newEntity(new Canvas2D.Entity[] { adminMenu1 }, "Push", "Enter new status for #THE_ORDER", true);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		String eventCommand = "`?e_./#target=" + bundle.getString("cart.order.remark") + " #THE_ORDER ##`";
		Canvas2D.Entity adminFireEvent = canvas2D.newEntity(new Canvas2D.Entity[] { adminMenu2 }, "", "", eventCommand, false);
		canvas2D.nextRow(100);
		canvas2D.nextColumn(200);
		
		canvas2D.newEntity(new Canvas2D.Entity[] { adminFireEvent }, "", "..(^o^)ๆ", null);
	}

}
