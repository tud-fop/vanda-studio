package org.vanda.studio.util;

import java.util.Stack;

public class Lexer {
	private String copiedDividers = "";
	private String droppedDividers = "";
	
	public Lexer() {
		
	}
	
	public Lexer(String copiedDividers, String droppedDividers) {
		this.copiedDividers = copiedDividers;
		this.droppedDividers = droppedDividers;
	}
	
	public String getCopiedDividers() {
		return copiedDividers;
	}
	
	public void addCopiedDividers(String d) {
		copiedDividers += d;
	}
	
	public void clearCopiedDividers() {
		copiedDividers = "";
	}
	
	public String getDroppedDividers() {
		return droppedDividers;
	}
	
	public void addDroppedDividers(String d) {
		droppedDividers += d;
	}

	public void clearDroppedDividers() {
		droppedDividers = "";
	}

	public void clearDividers() {
		clearCopiedDividers();
		clearDroppedDividers();
	}
	
	public Stack<String> lex(String s1) {
		Stack<String> st = new Stack<String>();
		int idx = 0;
		String t = "";
		while (idx < s1.length()) {
			if (copiedDividers.contains(s1.substring(idx, idx + 1))) {
				if (!t.equals(""))
					st.add(0, String.copyValueOf(t.trim().toCharArray()));
				st.add(0, s1.substring(idx, idx + 1));
				t = "";
			} else if (droppedDividers.contains(s1.substring(idx, idx + 1))) {
				if (!t.equals(""))
					st.add(0, String.copyValueOf(t.trim().toCharArray()));
				t = "";
			} else {
				t += s1.substring(idx, idx + 1);
			}
			idx++;
		}
		if (!t.equals(""))
			st.add(0, t.trim());
		return st;
	}
}
