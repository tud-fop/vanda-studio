package org.vanda.studio.model.hyper;

import java.util.LinkedList;

public class Token implements Cloneable {

	private static LinkedList<Integer> global;
	
	static {
		global = new LinkedList<Integer>();
	}
	
	LinkedList<Integer> local;
	int counter;
	
	public Token() {
		local = new LinkedList<Integer>();
		counter = 0;
	}
	
	@Override
	public Token clone() throws CloneNotSupportedException {
		Token result = (Token) super.clone();
		result.local = new LinkedList<Integer>(local);
		return result;
	}
	
	public int getMaxToken() {
		return counter;
	}
	
	public Integer makeToken() {
		Integer result = local.poll();
		if (result == null)	{
			while (counter >= global.size())
				global.add(new Integer(counter));
			counter++;
			result = global.getLast();
		}
		return result;
	}
	
	public void recycleToken(Integer token) {
		local.add(token);
	}

}
