package com.wayos.experiment.axow;

/**
 * 
 * State Machine of Element A => X => O => W => A..
 * @author eoss-th
 *
 */
public abstract class Element {

	public static class A extends Element {
		public A(int amplitude) {
			super(amplitude);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof A)) return false;			
			return amplitude == ((A)o).amplitude;
		}

		@Override
		public Element step(int amount) {
			if (amount==-1)
				return new W(1);
			if (amount==0)
				return new A(1);
			if (amount==1)
				return new X(1);
			if (amount==2)
				return new O(1);
			throw new IllegalArgumentException("Invalid step number: " + amount);
		}
	}
	
	public static class X extends Element {
		public X(int amplitude) {
			super(amplitude);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof X)) return false;			
			return amplitude == ((X)o).amplitude;
		}
		
		@Override
		public Element step(int amount) {
			if (amount==-1)
				return new A(1);
			if (amount==0)
				return new X(1);
			if (amount==1)
				return new O(1);
			if (amount==2)
				return new W(1);
			throw new IllegalArgumentException("Invalid step number: " + amount);
		}
	}
	
	public static class O extends Element {
		public O(int amplitude) {
			super(amplitude);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof O)) return false;			
			return amplitude == ((O)o).amplitude;
		}
		
		@Override
		public Element step(int amount) {
			if (amount==-1)
				return new X(1);
			if (amount==0)
				return new O(1);
			if (amount==1)
				return new W(1);
			if (amount==2)
				return new A(1);
			throw new IllegalArgumentException("Invalid step number: " + amount);
		}		
	}
	
	public static class W extends Element {
		public W(int amplitude) {
			super(amplitude);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof W)) return false;			
			return amplitude == ((W)o).amplitude;
		}
		
		@Override
		public Element step(int amount) {
			if (amount==-1)
				return new O(1);
			if (amount==0)
				return new W(1);
			if (amount==1)
				return new A(1);
			if (amount==2)
				return new X(1);
			throw new IllegalArgumentException("Invalid step number: " + amount);
		}		
	}
	
	public static class N extends Element {
		public N(int amplitude) {
			super(amplitude);
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof N)) return false;			
			return amplitude == ((N)o).amplitude;
		}
		
		@Override
		public Element step(int amount) {
			return new N(amplitude);
		}
	}
	
	protected int amplitude;
	
	public Element(int amplitude) {
		this.amplitude = amplitude;
	}
	
	public int amplitude() {
		return amplitude;
	}
	
	public Element shade() {
		
		int amount = amplitude % 6;
		
		if (amount==0 || amount==1)
			return step(0);
		
		if (amount==2)
			return step(-1);
		
		if (amount==3)
			return step(1);
		
		if (amount==4)
			return step(2);
		
		if (amount==5)
			return new N(0);
		
		throw new IllegalArgumentException("Ivanlid amount: " + amount);
	}
	
	public boolean isSame(Element anotherElement) {		
		return this.getClass().isInstance(anotherElement);		
	}
	
	public abstract Element step(int amount);
		
	public String _toString() {
		return this.getClass().getName() + " (" + amplitude + ")";
	}
		
	public String __toString() {
		return this.getClass().getName().replace("com.brainy.axow.Element$", "") + "=" + amplitude;
	}

	@Override
	public String toString() {
		
		String name = this.getClass().getName().replace("com.brainy.axow.Element$", "");
		String result = "";
		
		for (int i=0; i<amplitude; i++) {
			result += name;
		}
		
		if (result.isEmpty()) {
			return "-";
		}
		
		return result;
	}
}
