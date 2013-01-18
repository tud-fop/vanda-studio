package org.vanda.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.util.Pair;

public abstract class Type {
	
	public abstract boolean canDecompose();
	
	public abstract boolean contains(Object v);
	
	public abstract Pair<String, List<Type>> decompose();
	
	public abstract boolean failsOccursCheck(Type rhs);
	
	public abstract void freshMap(Map<Object, Object> m);
	
	public abstract Type rename(Map<Object, Object> m);
	
	public abstract Type subst(Object variable, Type nt);
	
	public abstract Set<Type> getSubTypes(Set<Type> types);

}
