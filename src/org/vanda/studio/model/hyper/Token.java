package org.vanda.studio.model.hyper;

import java.util.ArrayList;
import java.util.LinkedList;

public class Token implements Cloneable {

	private static ArrayList<Integer> global;
	
	static {
		global = new ArrayList<Integer>();
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
			result = global.get(counter);
			counter++;
		}
		return result;
	}
	
	public void recycleToken(Integer token) {
		local.add(token);
	}

}
