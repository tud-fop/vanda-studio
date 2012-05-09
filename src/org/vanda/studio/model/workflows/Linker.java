package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.InvokationWorkflow;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.util.HasActions;

/*
 * DONE (almost) think about types and type conversion in general; what about
 * different file formats for what is essentially the same grammar type?
 * Solution: specify types as precisely as possible to avoid requiring the
 * Vanda Studio modules to have implicit knowledge. Augment the linker so that
 * it can handle type conversions at the ports, also enable polymorphic types
 * and type inference.
 */
/**
 * This interface could be extended to allow for port conversions.
 * <p>
 * For instance, an inner Haskell fragment f :: a1 -> ... -> an -> a -> a could
 * be converted to f' :: a1 -> ... -> an -> a -> [a] with f' a1 ... an a =
 * iterate (f a1 ... an) a
 * <p>
 * Moreover, since we allow for ToolInstances holding constant parameter values
 * that "shadow ports", we might introduce something similar here for composite
 * jobs.
 * <p>
 * 
 * @author mbue
 * 
 * @param <IF>
 * @param <F>
 */
public interface Linker<IF, F> extends HasActions {
	<T extends ArtifactConn, A extends Artifact<T>> A link(
			ArtifactFactory<T, A, ?, F> af, InvokationWorkflow<?, ?, IF> pre);

	/**
	 * Check whether the (inferred) outer types and the (inferred) inner types
	 * are compatible for the input ports of a composite Job.
	 *  
	 * @param outer
	 * @param inner
	 * @return
	 */
	boolean checkInputTypes(List<String> outer, List<String> inner);

	/**
	 * Check whether the (inferred) outer types and the (inferred) inner types
	 * are compatible for the output ports of a composite Job.
	 *  
	 * @param outer
	 * @param inner
	 * @return
	 */
	boolean checkOutputTypes(List<String> outer, List<String> inner);

	/**
	 * Convert the (potentially polymorphic) inner input ports of a composite
	 * Job to (potentially polymorphic) outer input ports. This is the place to
	 * apply additional type constructors. 
	 * 
	 * @param ips
	 * @return
	 */
	List<Port> convertInputPorts(List<Port> ips);

	/**
	 * Convert the (potentially polymorphic) inner output ports of a composite
	 * Job to (potentially polymorphic) outer output ports. This is the place to
	 * apply additional type constructors. 
	 * 
	 * @param ops
	 * @return
	 */
	List<Port> convertOutputPorts(List<Port> ops);
	
	Class<IF> getFragmentType();

	Class<F> getViewType();
}
