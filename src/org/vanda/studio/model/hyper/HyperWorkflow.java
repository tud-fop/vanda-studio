package org.vanda.studio.model.hyper;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public interface HyperWorkflow<F> {

	public abstract HyperWorkflow<F> clone()
			throws CloneNotSupportedException;

	public abstract Token addChild(Job<F> hj);

	public abstract Token addConnection(Connection cc);
	
	public abstract HyperWorkflow<?> dereference(ListIterator<Token> address);
	
	public abstract ImmutableWorkflow<F> freeze() throws Exception;
	
	public abstract Job<F> getChild(Token address);
	
	public abstract Connection getConnection(Token address);

	public abstract Observable<Pair<MutableWorkflow<F>, Token>> getAddObservable();
	
	public abstract Collection<Token> getChildren();

	public abstract Observable<Pair<MutableWorkflow<F>, Token>> getConnectObservable();

	public abstract Observable<Pair<MutableWorkflow<F>, Token>> getDisconnectObservable();

	public abstract Observable<Pair<MutableWorkflow<F>, Token>> getModifyObservable();

	public abstract Observable<Pair<MutableWorkflow<F>, Token>> getRemoveObservable();

	public abstract void removeChild(Token address);

	public abstract void removeConnection(Token address);

	public abstract List<Token> getConnections();

	public abstract Token getVariable(Token source, int sourcePort);

	public abstract Token getVariable(Token address);

}