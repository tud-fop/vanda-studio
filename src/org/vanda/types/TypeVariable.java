package org.vanda.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.util.Pair;

public final class TypeVariable extends Type {
	
	public final Object variable;
	
	public TypeVariable(Object variable) {
		this.variable = variable;
	}

	@Override
	public boolean canDecompose() {
		return false;
	}
	
	@Override
	public boolean contains(Object v) {
		return variable.equals(v);
	}

	@Override
	public Pair<String, List<Type>> decompose() {
		return null;
	}
	
	@Override
	public boolean equals(Object other) {
		// use object identity because variables are assumed to be interned
		if (other instanceof TypeVariable)
			return ((TypeVariable) other).variable == variable;
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return variable.hashCode();
	}

	@Override
	public boolean failsOccursCheck(Type rhs) {
		return rhs.contains(variable);
	}

	@Override
	public void freshMap(Map<Object, Object> m) {
		if (!m.containsKey(variable))
			m.put(variable, new Object());
	}

	@Override
	public Type rename(Map<Object, Object> m) {
		Object nv = m.get(variable);
		if (nv != null)
			return new TypeVariable(nv);
		else
			return this;
	}

	@Override
	public Type subst(Object v, Type nt) {
		if (v == variable)
			return nt;
		else
			return this;
	}
	
	@Override
	public String toString() {
		return "t" + variable.toString();
	}

	@Override
	public Set<Type> getSubTypes(Set<Type> types) {
		return types;
	}

}
