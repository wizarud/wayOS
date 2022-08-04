package com.wayos.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

public class ImageGenerator {
	
	public static enum Layout {
		
		OVER, VERTICAL, HORIZONTAL;
		
	}
	
	public static class Parameters {
		
        public int width;
        
        public int height;
        
        public Color color;
        
        public String [] layers;
        
		public Parameters(HttpServletRequest request) {
			
	        try {
	        	width = Integer.parseInt(request.getParameter("w"));
	        } catch (Exception e) {
	        	width = 400;
	        }
	        
	        try {
	        	height = Integer.parseInt(request.getParameter("h"));
	        } catch (Exception e) {
	        	height = width;
	        }
	        
	        try {
	        	color = Color.decode(request.getParameter("c"));
	        } catch (Exception e) {
	        	color = null;
	        }
	        
	        String operator = request.getParameter("opt");
	        if (operator!=null) {
	        	char opt;
	        	for (int i=0;i<operator.length();i++) {
	        		opt = operator.charAt(i);
	        		if (opt=='+') {
	        			color = color.brighter();
	        		} else if (opt=='-') {
	        			color = color.darker();
	        		}
	        	}
	        }
	        
	        layers = request.getParameterValues("layers");			
		}
		
	}
	
	private Map<String, BufferedImage> imageCache = new HashMap<>();
	
	private Layout layout;
	
	public ImageGenerator(Layout layout) {
		
		this.layout = layout;
		
		if (this.layout==null) {
			
			this.layout = Layout.OVER;
		}
				
	}
	
    // create the image
    private BufferedImage createClockImage() {
    	
        GregorianCalendar cal = new GregorianCalendar();

        BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();

        // white background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 400, 400);

        // draw black circle around clock
        g.setColor(Color.BLACK);
        
        g.setStroke(new BasicStroke(5));
        g.drawOval(20, 20, 360, 360);

        // draw hour hand
        double hourRad = cal.get(Calendar.HOUR) * 2 * Math.PI / 12 - 0.5 * Math.PI;
        g.drawLine(200, 200, 200 + (int) (100 * Math.cos(hourRad)), 
                   200 + (int) (100 * Math.sin(hourRad)));

        // draw minute hand
        double minuteRad = cal.get(Calendar.MINUTE) * 2 * Math.PI / 60 - 0.5 * Math.PI;
        g.drawLine(200, 200, 200 + (int) (170 * Math.cos(minuteRad)), 
                   200 + (int) (170 * Math.sin(minuteRad)));
        return img;
    }
    
    public BufferedImage flatten(Parameters parameter) {
    	
    	return flatten(parameter.width, parameter.height, parameter.color, parameter.layers);
    }
    
    
    public BufferedImage flatten(int width, int height, Color color, String [] layers) {
    
    	BufferedImage img;
        Graphics2D g;
        
    	if (color!=null) {
    		
            img = imageCache.get(color.toString());
    		
            if (img==null) {
            	
            	img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        		g = img.createGraphics();        		
        		imageCache.put(color.toString(), img);
        		
            } else {
            	
                g = (Graphics2D) img.getGraphics();
                
            }
            
    		g.setColor(color);
    		g.fillRect(0, 0, width, height);        	
    		
    	} else {
    		
    		img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    		g = img.createGraphics();        		
    		
    	}
        
        //Merge All layers
        if (layers!=null) {
        	        	
        	String fontConfig = System.getProperty("java.home")
                    + File.separator + "lib"
                    + File.separator + "fontconfig.Prodimage.properties";
            if (new File(fontConfig).exists())
                System.setProperty("sun.awt.fontconfig", fontConfig);
            
            List<BufferedImage> layerList = new ArrayList<>();
            
            BufferedImage layer;
            BufferedImage defaultImage;
            Graphics2D defaultGraphic;
            int defaultY = 50;
            for (String imageURL:layers) {
            	
            	try {
            		
            		if (!imageURL.startsWith("https://")) {
            			imageURL = "https://wayobot.com/bin/" + imageURL;
            		}
            		
            		if (imageURL.startsWith("https://wayobot.com/bin/")) {
            			imageURL = imageURL.replace(" ", "%20");            			
            		}
            		
            		layer = imageCache.get(imageURL);
            		
            		if (layer==null) {
            			layer = ImageIO.read(new URL(imageURL));
            			imageCache.put(imageURL, layer);
            		}
            		
    				layerList.add(layer);
    				
    			} catch (Exception e) {
    				    				
    				throw new RuntimeException(e + ":" + imageURL);
    				
    				/*
    				 * Paint Exceptio as image
    				defaultImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    				
    				defaultGraphic = defaultImage.createGraphics();
    				
    				defaultGraphic.setColor(Color.WHITE);
    				defaultGraphic.drawString(e.getMessage() + ":" + imageURL, 20, defaultY);
    				defaultY += 50;
    				
    				layerList.add(defaultImage);*/
    			}
            }
            
            int i=0;
            for (BufferedImage image:layerList) {
            	
            	if (i>0) {
            		
            		/**
            		 * Center it!
            		 */
                	g.drawImage(image, width/2 - width/4, height/2 - height/4, width/2, height/2, null);
            		
            	} else {
            		
            		/**
            		 * Fill as background
            		 */
                	g.drawImage(image, 0, 0, width, height, null);
            		
            	}
            	i++;
            }
        	
        }
            	
        return img;
    }	

}
