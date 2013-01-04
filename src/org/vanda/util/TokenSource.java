package org.vanda.util;

import java.util.ArrayList;
import java.util.LinkedList;

public class TokenSource implements Cloneable {
	
	public static class Token {
		private final int value;
		
		private Token(int value) {
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

	private static ArrayList<Token> global;
	
	static {
		global = new ArrayList<Token>();
	}
	
	LinkedList<Token> local;
	int counter;
	
	public TokenSource() {
		local = new LinkedList<Token>();
		counter = 0;
	}
	
	@Override
	public TokenSource clone() throws CloneNotSupportedException {
		TokenSource result = (TokenSource) super.clone();
		result.local = new LinkedList<Token>(local);
		return result;
	}
	
	public static Token getToken(int i) {
		while (i >= global.size())
			global.add(new Token(global.size()));
		return global.get(i);
	}
	
	public int getMaxToken() {
		return counter;
	}
	
	public Token makeToken() {
		Token result = local.poll();
		if (result == null)	{
			result = getToken(counter);
			counter++;
		}
		return result;
	}
	
	public void recycleToken(Token token) {
		local.add(token);
	}

}
