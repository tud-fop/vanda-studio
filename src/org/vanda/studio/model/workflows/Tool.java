package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.util.HasActions;

public interface Tool<V, I extends ToolInstance> extends HasActions {
	<T extends ArtifactConn, A extends Artifact<T>, F> A createArtifact(
			ArtifactFactory<T, A, F, V> af, I instance);

	/**
	 * Creates an instance with default parameters.
	 * 
	 * @return
	 */
	I createInstance();

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
	
	Class<V> getViewType();

	<R> R selectRenderer(RendererAssortment<R> ra);
}
