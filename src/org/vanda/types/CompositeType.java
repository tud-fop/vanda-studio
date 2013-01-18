package org.vanda.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.vanda.util.Pair;

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
	public boolean canDecompose() {
		return true;
	}

	@Override
	public boolean contains(Object v) {
		boolean c = false;
		ListIterator<Type> i = children.listIterator();
		while (!c && i.hasNext()) {
			c = /* c || */ i.next().contains(v);
		}
		return c;
	}

	@Override
	public Pair<String, List<Type>> decompose() {
		return new Pair<String, List<Type>>(constructor, children);
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
	public boolean failsOccursCheck(Type rhs) {
		return false;
	}

	@Override
	public void freshMap(Map<Object, Object> m) {
		for (Type c : children) {
			c.freshMap(m);
		}
	}
	
	@Override
	public int hashCode() {
		return constructor.hashCode();
	}

	@Override
	public Type rename(Map<Object, Object> m) {
		List<Type> nc = new ArrayList<Type>(children.size());
		for (Type c : children) {
			nc.add(c.rename(m));
		}
		return new CompositeType(constructor, nc);
	}

	@Override
	public Type subst(Object variable, Type nt) {
		List<Type> ncs = new ArrayList<Type>(children.size());
		boolean didsubst = false;
		for (Type c : children) {
			Type nc = c.subst(variable, nt);
			ncs.add(nc);
			didsubst = nc != c || didsubst;
		}
		Type result;
		if (didsubst)
			result = new CompositeType(constructor, ncs);
		else
			result = this;
		return result;
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

	@Override
	public Set<Type> getSubTypes(Set<Type> types) {
		types.add(this);
		for (Type t : children)
			t.getSubTypes(types);
		return types;
	}

}
