package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.util.HasActions;

public interface Tool<F> extends HasActions {
	String getAuthor();

	/**
	 * The category is used like a path in a file system. The separator is a
	 * period.
	 */
	String getCategory();

	String getDate();

	String getDescription();

	String getId();

	List<Port> getInputPorts();

	String getName();

	List<Port> getOutputPorts();
	
	Class<F> getFragmentType();

	<R> R selectRenderer(RendererAssortment<R> ra);
}
