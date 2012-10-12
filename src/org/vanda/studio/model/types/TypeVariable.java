package org.vanda.studio.model.types;

import java.util.Map;
import java.util.Set;

import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public final class TypeVariable extends Type {
	
	public final Token variable;
	
	public TypeVariable(Token variable) {
		this.variable = variable;
	}
	
	@Override
	public boolean contains(Token v) {
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
	public int hashCode() {
		return variable.hashCode();
	}

	@Override
	public void freshMap(TokenSource t, Map<Token, Token> m) {
		if (!m.containsKey(variable))
			m.put(variable, t.makeToken());
	}

	@Override
	public Type rename(Map<Token, Token> m) {
		Token nv = m.get(variable);
		if (nv != null)
			return new TypeVariable(nv);
		else
			return this;
	}

	@Override
	public Type substitute(Token variable, Type nt) {
		if (variable == this.variable)
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
