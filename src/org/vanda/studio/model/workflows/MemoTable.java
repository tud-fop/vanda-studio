package org.vanda.studio.model.workflows;

import java.util.HashMap;
import java.util.Map;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.AtomicInvokation;
import org.vanda.studio.model.generation.InvokationWorkflow;
import org.vanda.studio.model.generation.Profile;

/**
 * Used to memoize calls to WorkflowElement.convert and Workflow.convert. If you
 * want to convert several instances of the same HyperWorkflow, those may share
 * some WorkflowElements and Workflows. You don't want to convert them twice. So
 * that's why we have this class.
 * 
 * Only the WorkflowElement/Workflow instance is used as a key to the map; the
 * ArtifactFactory and the Profile are just checked for equality. Use a separate
 * InvokationMemo instance for each profile, and make sure that you call
 * WorkflowElement.convert with consistent ArtifactFactories.
 * 
 * Note that this is not merely a cache (for which SoftReferences and the likes
 * would be in order). The memo table must be retained for the whole conversion
 * process.
 * 
 * @author mbue
 * 
 */
public final class MemoTable {

	private class InvokationEntry<T extends ArtifactConn, A extends Artifact<T>, V> {
		public ArtifactFactory<T, A, ?, V> af;
		public AtomicInvokation<T, A> i;

		public InvokationEntry(ArtifactFactory<T, A, ?, V> af,
				AtomicInvokation<T, A> i) {
			this.af = af;
			this.i = i;
		}
	}

	private class InvokationWorkflowEntry<T extends ArtifactConn, A extends Artifact<T>, F> {
		public InvokationWorkflow<T, A, F> iw;

		public InvokationWorkflowEntry(InvokationWorkflow<T, A, F> iw) {
			this.iw = iw;
		}
	}

	private Profile profile;
	private Map<Object, Object> storage;

	public MemoTable(Profile profile) {
		this.profile = profile;
		storage = new HashMap<Object, Object>();
	}

	public Profile getProfile() {
		return profile;
	}

	<T extends ArtifactConn, A extends Artifact<T>, V> void register(
			Object key, ArtifactFactory<T, A, ?, V> af, Profile profile,
			AtomicInvokation<T, A> i) {
		assert (profile == this.profile);
		storage.put(key, new InvokationEntry<T, A, V>(af, i));
	}

	<T extends ArtifactConn, A extends Artifact<T>, F> void register(
			Object key, Profile profile, InvokationWorkflow<T, A, F> iw) {
		assert (profile == this.profile);
		storage.put(key, new InvokationWorkflowEntry<T, A, F>(iw));
	}

	@SuppressWarnings("unchecked")
	<T extends ArtifactConn, A extends Artifact<T>, V> AtomicInvokation<T, A> retrieve(
			Object key, ArtifactFactory<T, A, ?, V> af, Profile profile) {
		/*
		 * The following code uses a lot of type casts for two reasons: (1) the
		 * compiler is not smart enough: there is no way of convincing it that
		 * Object equality means type parameter equality, and (2) it is
		 * convenient: we can use the same map for disjoint value types
		 * (InvokationEntry vs. InvokationWorkflowEntry), which is possible
		 * because the keys must be disjoint as well (WorkflowElement vs.
		 * Workflow)
		 */
		assert (profile == this.profile);
		AtomicInvokation<T, A> result = null;
		InvokationEntry<?, ?, ?> e = (InvokationEntry<?, ?, ?>) storage
				.get(key);
		if (e != null) {
			assert (e.af == af);
			result = (AtomicInvokation<T, A>) e.i;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	<T extends ArtifactConn, A extends Artifact<T>, F> InvokationWorkflow<T, A, F> retrieve(
			Object key, Profile profile) {
		/*
		 * The following code uses a lot of type casts for the same two reasons
		 * stated in the comment for the other version of retrieve.
		 */
		assert (profile == this.profile);
		InvokationWorkflow<T, A, F> result = null;
		InvokationWorkflowEntry<?, ?, ?> e = (InvokationWorkflowEntry<?, ?, ?>) storage
				.get(key);
		if (e != null) {
			result = (InvokationWorkflow<T, A, F>) e.iw;
		}
		return result;
	}

}
