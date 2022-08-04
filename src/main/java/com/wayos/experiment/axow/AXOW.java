package com.wayos.experiment.axow;

public class AXOW {
	
	private int x;
	private int y;
	
	public AXOW(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Single Step of release
	 *
	 * Ex.
	 * 3W2A
	 * -3, 2
	 * -1, 1; 3, 2*
	 *  1, 0; 3, 2*
	 *  2, 0; 1, 0; apply init polars
	 *  2, 0; -1, 0
	 *  1, 0
	 * 
	 */
	public void release() {
		
		if (isStable()) return;
		
		int [] polars = {x/Math.abs(x), y/Math.abs(y)};
		
		int [] absolutes = {Math.abs(x), Math.abs(y)};
		
		int minIndex = -1;
		int minValue = Integer.MAX_VALUE;
		int maxIndex = -1;
		int maxValue = Integer.MIN_VALUE;
		
		for (int i=0; i<absolutes.length; i++) {
			
			if (absolutes[i]>maxValue) {
				maxIndex = i;
				maxValue = absolutes[i];
			}
			
			if (absolutes[i]<minValue) {
				minIndex = i;
				minValue = absolutes[i];
			}
		}
		
		absolutes[maxIndex] -= minValue;
		absolutes[maxIndex] *= polars[maxIndex];
		absolutes[minIndex] = 0;
		
		/**
		 * Polar transformation
		 * -1, 1 -> 1, 0
		 * 1, 1 -> 0, -1
		 * 1, -1 -> -1, 0
		 * -1, -1 -> 0, 1
		 */
		if (polars[0]==-1 && polars[1]==1) {
			polars[0] = 1;
			polars[1] = 0;
		} else if (polars[0]==1 && polars[1]==1) {
			polars[0] = 0;
			polars[1] = -1;
		} else if (polars[0]==1 && polars[1]==-1) {
			polars[0] = -1;
			polars[1] = 0;
		} else if (polars[0]==-1 && polars[1]==-1) {
			polars[0] = 0;
			polars[1] = 1;
		}
		
		if (minValue!=maxValue) {
			x = polars[0] * minValue + absolutes[0];
			y = polars[1] * minValue + absolutes[1];			
		} else {
			x = polars[0] * minValue;
			y = polars[1] * minValue;
		}
						
	}
	
	public Element eval() {
		
		String code = this.toString();
		
		String text = "";
		char c;
		Character workingChar = null;
		int duplicateAmount = 0;
		
		for (int i=code.length()-1; i>=0; i--) {
			
			if (workingChar==null) {
				workingChar = code.charAt(code.length()-1);
			}
			
			c = code.charAt(i);
			
			if (Character.isAlphabetic(c)) {
				workingChar = c;
				text = workingChar + text;
				continue;
			}
			
			if (Character.isDigit(c)) {
				duplicateAmount = Integer.parseInt("" + c) - 1;
				for (int j=0; j<duplicateAmount;j++) {
    				text = workingChar + text;
				}
			}
		}
		
		/**
		 * Pack
		 */
		String result, replacer;
		String pair;
		
		while (text.contains("WA") || text.contains("AW") ||
				text.contains("AX") || text.contains("XA") || 
				text.contains("XO") || text.contains("OX") || 
				text.contains("OW") || text.contains("WO") ||
				text.contains("AO") || text.contains("OA") || 
				text.contains("WX") || text.contains("XW")) {
			
			result = "";
			replacer = "";
    		for (int i=0; i<text.length(); i++) {
    			
    			if (i<text.length()-1) {
        			pair = text.substring(i, i+2);
        			        			
        			if (pair.equals("WA") || pair.equals("AW")) {
        				replacer += "X";
        				i += 1;
        			} else if (pair.equals("AX") || pair.equals("XA")) {
        				replacer += "O";
        				i += 1;
        			} else if (pair.equals("XO") || pair.equals("OX")) {
        				replacer += "W";    				
        				i += 1;
        			} else if (pair.equals("OW") || pair.equals("WO")) {
        				replacer += "A";
        				i += 1;
        			} else if (pair.equals("AO") || pair.equals("OA") || pair.equals("WX") || pair.equals("XW")) {
        				replacer += "";
        				i += 1;
        			} else {
        				result += text.charAt(i);
        			}
    			} else {
    				result += text.charAt(i);
    			}
    			
    		}
    		
    		text = result + replacer;
    		
		}
		
		/**
		 * Validate Result
		 */
		Character lastChar = null;
		for (int i=0; i<text.length(); i++) {
			if (lastChar==null) {
				lastChar = text.charAt(i);
				continue;
			}
			if (text.charAt(i)!=lastChar) throw new RuntimeException("Invalid result: " + text);
		}		
		
		int amplitude = text.length();
		
		if (amplitude==0) {
			return new Element.N(0);
		}
		
		if (text.startsWith("A")) {
			return new Element.A(amplitude);
		}
		
		if (text.startsWith("X")) {
			return new Element.X(amplitude);
		}
		
		if (text.startsWith("O")) {
			return new Element.O(amplitude);
		}
		
		if (text.startsWith("W")) {
			return new Element.W(amplitude);
		}
		
		throw new RuntimeException("Invalid result: " + text);
	}
	
	public static AXOW interpret(String code) {
		
		int x, y;
		x = y = 0;
		
		char c;
		for (int i=0; i<code.length(); i++) {
			
			c = code.charAt(i);
			
			if (c == 'W') {
				x -= 1;
			} else if (c == 'X') {
				x += 1;
			} else if (c == 'A') {
				y += 1;
			} else if (c == 'O') {
				y -= 1;
			}
		}
		
		return new AXOW(x, y);
	}
	
	public Element max() {
		
		if (Math.abs(x) > Math.abs(y)) {
			
			if (x>0)
				return new Element.X(x);
			if (x<0)
				return new Element.W(x);
			
		} else if (Math.abs(x) < Math.abs(y)) {
			
			if (y>0)
				return new Element.A(y);
			if (y<0)
				return new Element.O(y);			
			
		} 
		
		return new Element.N(Math.abs(x));
	}
	
	public boolean isStable() {
		return x == 0 || y == 0;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public String toString() {
				
		String val = "";
		if (x > 0)
			val += x + "X";
		else if (x < 0)
			val += x + "W";			
		
		if (y > 0)
			val += y + "A";
		else if (y < 0)
			val += y + "O";
								
		val = val.replace("-", "");//erase minus
		val = val.replace("1", "");//erase 1
		
		if (val.isEmpty()) {
			val = "-";
		}
				
		return val;
	}
	
	public static void debug(AXOW axow) {
		
    	System.out.println(axow);
    	
    	Element e = axow.eval();
    	System.out.println(e);
    	System.out.println(e.shade());
    	
    	System.out.println();
    	System.out.println("vs");
    	System.out.println();
    	
    	while (!axow.isStable()) {
        	System.out.println(axow);
        	axow.release();
    	}
    	
    	System.out.println(axow);
    	System.out.println("-----------");
    	System.out.println();
		
	}
    
    public static void main(String[]args) {
    	
    	debug(new AXOW(-3, 2));
    	    	
    	debug(new AXOW(8, 1));
    	
    	debug(new AXOW(3, 1));
    	
    	debug(new AXOW(-4, -2));
    	
    	debug(new AXOW(-50, 50));
    	
    	debug(new AXOW(50, 0));
    }
        
}
