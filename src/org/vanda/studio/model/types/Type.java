package org.vanda.studio.model.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public abstract class Type {
	
	public abstract boolean canDecompose();
	
	public abstract boolean contains(Token v);
	
	public abstract Pair<String, List<Type>> decompose();
	
	public abstract boolean failsOccursCheck(Type rhs);
	
	public abstract void freshMap(TokenSource t, Map<Token, Token> m);
	
	public abstract Type rename(Map<Token, Token> m);
	
	public abstract Type subst(Token variable, Type nt);
	
	public abstract Set<Type> getSubTypes(Set<Type> types);

}
