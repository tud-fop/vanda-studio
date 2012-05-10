package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.AtomicInvokation;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.generation.WorkflowElement;

public abstract class Job<V> implements WorkflowElement {

	private final WorkflowElement identity;

	public Job(WorkflowElement identity) {
		this.identity = identity;
	}

	/**
	 * (Potentially) memoized version of doConvert. If you want memoization,
	 * supply a non-null reference for the InvokationMemo argument. In that
	 * case, please note the documentation of said class.
	 * 
	 * @param m
	 * @param af
	 * @param profile
	 * @return
	 */
	public final <T extends ArtifactConn, A extends Artifact<T>, F> AtomicInvokation<T, A> convert(
			MemoTable m, ArtifactFactory<T, A, F, V> af, Profile profile) {
		AtomicInvokation<T, A> result = null;
		if (m != null)
			result = m.retrieve(this, af, profile);
		if (result == null) {
			result = doConvert(m, af, profile);
			if (m != null)
				m.register(this, af, profile, result);
		}
		return result;
	}

	/**
	 * Converts the (abstract) WorkflowElement into a (concrete) Invokation,
	 * that is, applies the given ArtifactFactory and Profile. This method
	 * should not apply the given InvokationMemo instance itself, however, it
	 * should use it for recursive calls to convert.
	 * 
	 * This method should not call itself recursively, and it should only be
	 * called via convert. If you don't want memoization, call convert with null
	 * for the InvokationMemo.
	 * 
	 * @param m
	 * @param af
	 * @param profile
	 * @return
	 */
	public abstract <T extends ArtifactConn, A extends Artifact<T>, F> AtomicInvokation<T, A> doConvert(
			MemoTable m, ArtifactFactory<T, A, F, V> af, Profile profile);

	public abstract boolean isInputPort();

	public abstract boolean isOutputPort();

	public final WorkflowElement getIdentity() {
		return identity;
	}

	@Override
	public abstract List<Port> getInputPorts();

	@Override
	public abstract List<Port> getOutputPorts();
}
