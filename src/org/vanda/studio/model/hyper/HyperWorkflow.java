package org.vanda.studio.model.hyper;

import java.util.Collection;
import java.util.ListIterator;

import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public interface HyperWorkflow<F> {

	public abstract HyperWorkflow<F> clone()
			throws CloneNotSupportedException;

	public abstract void addChild(Job<F> hj);

	public abstract void addConnection(Connection<F> cc);
	
	public abstract Job<?> dereference(ListIterator<Integer> address);
	
	public abstract ImmutableWorkflow<F> freeze() throws Exception;

	public abstract Observable<Pair<MutableWorkflow<F>, Job<F>>> getAddObservable();
	
	public abstract Integer getAddress(Job<F> child);
	
	public abstract Collection<Job<F>> getChildren();

	public abstract Observable<Pair<MutableWorkflow<F>, Connection<F>>> getConnectObservable();

	public abstract Observable<Pair<MutableWorkflow<F>, Connection<F>>> getDisconnectObservable();

	public abstract Observable<Pair<MutableWorkflow<F>, Job<F>>> getModifyObservable();

	public abstract Observable<Pair<MutableWorkflow<F>, Job<F>>> getRemoveObservable();

	public abstract void removeChild(Job<F> hj);

	public abstract void removeConnection(Connection<F> cc);

}