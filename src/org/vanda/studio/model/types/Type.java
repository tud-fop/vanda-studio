package org.vanda.studio.model.types;

import java.util.Map;

import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public abstract class Type {
	
	public abstract boolean contains(Token v);
	
	public abstract void freshMap(TokenSource t, Map<Token, Token> m);
	
	public abstract Type rename(Map<Token, Token> m);
	
	public abstract Type substitute(Token variable, Type nt);

}
