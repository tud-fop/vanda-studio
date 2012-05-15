package org.vanda.studio.model.types;

import java.util.Map;

import org.vanda.studio.util.TokenSource;

public abstract class Type {
	
	public abstract boolean contains(Object v);
	
	public abstract void freshMap(TokenSource t, Map<Object, Object> m);
	
	public abstract Type rename(Map<Object, Object> m);
	
	public abstract Type substitute(Object variable, Type nt);

}
