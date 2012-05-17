package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.util.HasActions;

public interface Element extends RepositoryItem, HasActions, Cloneable {
	
	public Element clone() throws CloneNotSupportedException;

	public Class<?> getFragmentType();

	public List<Port> getInputPorts();
	
	public List<Port> getOutputPorts();

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
}
