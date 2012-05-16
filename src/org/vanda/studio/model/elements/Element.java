package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.util.HasActions;

public interface Element extends RepositoryItem, HasActions, Cloneable {
	
	public Element clone() throws CloneNotSupportedException;
	
	public String getDescription();

	public Class<?> getFragmentType();

	/**
	 * The category is used like a path in a file system. The separator is a
	 * period.
	 */
	String getCategory();

	public List<Port> getInputPorts();
	
	public List<Port> getOutputPorts();

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
}
