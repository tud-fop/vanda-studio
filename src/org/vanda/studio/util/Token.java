package org.vanda.studio.util;

import java.util.ArrayList;
import java.util.LinkedList;

public class Token implements Cloneable {
	
	public static class InternedInteger {
		private final int value;
		
		private InternedInteger(int value) {
			this.value = value;
		}
		
		public int intValue() {
			return value;
		}
		
		@Override
		public String toString() {
			return Integer.toString(value);
		}
	}

	private static ArrayList<InternedInteger> global;
	
	static {
		global = new ArrayList<InternedInteger>();
	}
	
	LinkedList<Object> local;
	int counter;
	
	public Token() {
		local = new LinkedList<Object>();
		counter = 0;
	}
	
	@Override
	public Token clone() throws CloneNotSupportedException {
		Token result = (Token) super.clone();
		result.local = new LinkedList<Object>(local);
		return result;
	}
	
	public static Object getToken(int i) {
		while (i >= global.size())
			global.add(new InternedInteger(global.size()));
		return global.get(i);
	}
	
	public int getMaxToken() {
		return counter;
	}
	
	public Object makeToken() {
		Object result = local.poll();
		if (result == null)	{
			result = getToken(counter);
			counter++;
		}
		return result;
	}
	
	public void recycleToken(Object token) {
		assert (token instanceof InternedInteger);
		local.add(token);
	}

}
