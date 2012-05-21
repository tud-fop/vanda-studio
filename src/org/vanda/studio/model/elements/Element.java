package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.util.HasActions;
import org.vanda.studio.util.Observable;

public interface Element extends RepositoryItem, HasActions, Cloneable {
	
	public Element clone() throws CloneNotSupportedException;
	
	public Class<?> getFragmentType();

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
	public Observable<Element> getPortsChangeObservable();

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
}
