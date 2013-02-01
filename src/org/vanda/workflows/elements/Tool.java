package org.vanda.workflows.elements;

import java.util.List;

import org.vanda.types.Type;
import org.vanda.util.RepositoryItem;

public interface Tool extends RepositoryItem {
	
	public abstract String getStatus();
	
	public abstract ToolInterface getInterface();
	
	public Type getFragmentType();

	public List<Port> getInputPorts();
	
	public List<Port> getOutputPorts();

	public <R> R selectRenderer(RendererAssortment<R> ra);

}
