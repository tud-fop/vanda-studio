package org.vanda.studio.model.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public final class CompositeType extends Type {

	public final String constructor;

	public final List<Type> children;

	public CompositeType(String constructor) {
		this.constructor = constructor;
		children = Collections.emptyList();
	}

	public CompositeType(String constructor, List<Type> children) {
		this.constructor = constructor;
		this.children = children;
	}

	@Override
	public boolean contains(Token v) {
		boolean c = false;
		ListIterator<Type> i = children.listIterator();
		while (!c && i.hasNext()) {
			c = /* c || */ i.next().contains(v);
		}
		return c;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CompositeType) {
			CompositeType o = (CompositeType) other;
			if (!constructor.equals(o.constructor))
				return false;
			else {
				return children.equals(o.children);
			}
		} else
			return false;
	}

	@Override
	public void freshMap(TokenSource t, Map<Token, Token> m) {
		for (Type c : children) {
			c.freshMap(t, m);
		}
	}
	
	@Override
	public int hashCode() {
		return constructor.hashCode();
	}

	@Override
	public Type rename(Map<Token, Token> m) {
		List<Type> nc = new ArrayList<Type>(children.size());
		for (Type c : children) {
			nc.add(c.rename(m));
		}
		return new CompositeType(constructor, nc);
	}

	@Override
	public Type substitute(Token variable, Type nt) {
		List<Type> nc = new ArrayList<Type>(children.size());
		for (Type c : children) {
			nc.add(c.substitute(variable, nt));
		}
		return new CompositeType(constructor, nc);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(constructor);
		ListIterator<Type> it = children.listIterator();
		if (it.hasNext()) {
			sb.append('(');
			while (it.hasNext()) {
				sb.append(it.next().toString());
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append(')');
		}
		return sb.toString();
	}

}
