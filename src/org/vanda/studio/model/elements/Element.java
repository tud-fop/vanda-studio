package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.HasActions;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public interface Element extends RepositoryItem, HasActions, Cloneable {
	
	public Element clone() throws CloneNotSupportedException;
	
	public Type getFragmentType();

	public List<Port> getInputPorts();
	
	/**
	 * may return null if name is immutable
	 * @return
	 */
	public Observable<Element> getNameChangeObservable();
	
	public List<Port> getOutputPorts();
	
	/**
	 * may return null if ports are immutable
	 * @return
	 */
	public Observable<Pair<Element, Integer>> getAddInputPortObservable();

	public Observable<Pair<Element, Integer>> getAddOutputPortObservable();
	
	public Observable<Pair<Element, Integer>> getRemoveInputPortObservable();
	
	public Observable<Pair<Element, Integer>> getRemoveOutputPortObservable();

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
}
