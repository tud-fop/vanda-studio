package org.vanda.studio.model.types;

import java.util.Map;

import org.vanda.studio.util.TokenSource;

public final class TypeVariable extends Type {
	
	public final Object variable;
	
	public TypeVariable(Object variable) {
		this.variable = variable;
	}
	
	@Override
	public boolean contains(Object v) {
		// use object identity because variables are assumed to be interned
		return variable == v;
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
	public void freshMap(TokenSource t, Map<Object, Object> m) {
		if (!m.containsKey(variable))
			m.put(variable, t.makeToken());
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
	public Type substitute(Object variable, Type nt) {
		if (variable == this.variable)
			return nt;
		else
			return this;
	}
	
	@Override
	public String toString() {
		return "t" + variable.toString();
	}

}
