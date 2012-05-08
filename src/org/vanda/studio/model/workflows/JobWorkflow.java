package org.vanda.studio.model.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.AtomicInvokation;
import org.vanda.studio.model.generation.Connection;
import org.vanda.studio.model.generation.InvokationConnection;
import org.vanda.studio.model.generation.InvokationWorkflow;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.generation.Workflow;

public final class JobWorkflow<F, V> extends Workflow<Job<V>, JobConnection<V>> {

	private final Workflow<?, ?> identity;

	private Compiler<F, V> compiler;

	public JobWorkflow(Compiler<F, V> compiler, Workflow<?, ?> identity) {
		this.identity = identity;
		this.compiler = compiler;
	}

	public final Workflow<?, ?> getIdentity() {
		return identity;
	}

	/*
	 * Helper class for compile (see below); actually just a tuple
	 */
	private static class Entry<T extends ArtifactConn, A extends Artifact<T>> {
		public final AtomicInvokation<T, A> source;
		public final int sourcePort;
		public final int targetPort;
		public final T ac;
		public final Connection<?> identity;

		public Entry(AtomicInvokation<T, A> source, int sourcePort,
				int targetPort, T ac, Connection<?> identity) {
			this.source = source;
			this.sourcePort = sourcePort;
			this.targetPort = targetPort;
			this.ac = ac;
			this.identity = identity;
		}
	}

	/*
	 * This method does most of the actual work for doConvert. This separation
	 * is necessary to get the type parameters right. <p> We do a topological
	 * sort of the Workflow and construct the InvokationWorkflow on the fly. The
	 * class Entry<T, A> is used to store connections of which the source has
	 * already been converted, but the target has not.
	 */
	private <T extends ArtifactConn, A extends Artifact<T>> InvokationWorkflow<T, A, F> compile(
			MemoTable m, ArtifactFactory<T, A, F, V> af, Profile profile) {

		InvokationWorkflow<T, A, F> result = new InvokationWorkflow<T, A, F>(
				this);
		Map<Job<V>, Integer> ingoing = new HashMap<Job<V>, Integer>();
		Map<Job<V>, LinkedList<JobConnection<V>>> forward = new HashMap<Job<V>, LinkedList<JobConnection<V>>>();
		Map<Job<V>, LinkedList<Entry<T, A>>> backward = new HashMap<Job<V>, LinkedList<Entry<T, A>>>();
		LinkedList<Job<V>> workingset = new LinkedList<Job<V>>();

		// determine number of ingoing edges for each job
		// and add jobs with zero ingoing edges to the workingset
		for (Job<V> c : children) {
			int j = c.getInputPorts().size();
			ingoing.put(c, j);
			if (j == 0)
				workingset.add(c);
		}

		// determine forward star for each job (outgoing connections)
		for (JobConnection<V> c : connections) {
			Job<V> s = c.getSource();
			LinkedList<JobConnection<V>> f = forward.get(s);
			if (f == null) {
				f = new LinkedList<JobConnection<V>>();
				forward.put(s, f);
			}
			f.add(c);
		}

		/*
		 * take out jobs from the working set, update the number of ingoing
		 * edges for their successors, and add them to the working set once that
		 * number reaches zero
		 */
		while (!workingset.isEmpty()) {
			Job<V> c = workingset.pop();

			AtomicInvokation<T, A> inv = c.doConvert(m, af, profile);
			assert (c.getInputPorts().size() == inv.getArtifact()
					.getInputPorts().size());
			assert (c.getOutputPorts().size() == inv.getArtifact()
					.getOutputPorts().size());
			result.addChild(inv);

			// collect all ingoing edges for c and convert them
			// meanwhile, also construct the array of inputs to the artifact
			LinkedList<Entry<T, A>> b = backward.get(c);
			ArrayList<T> inputs = new ArrayList<T>(b == null ? 0 : b.size());
			if (b != null) {
				for (Entry<T, A> e : b) {
					result.addConnection(new InvokationConnection<T, A>(
							e.source, e.sourcePort, inv, e.targetPort, e.ac,
							e.identity));
					assert (inputs.get(e.targetPort) == null);
					inputs.set(e.targetPort, e.ac);
				}
			} else
				assert (c.getInputPorts().size() == 0);

			// ask the artifact to compute the outputs from the inputs
			// also check pre- and post-conditions
			for (T ac : inputs)
				assert (ac != null);
			assert (inputs.size() == inv.getArtifact().getInputPorts().size());
			List<T> outputs = inv.getArtifact().getOutputs(inputs);
			assert (outputs.size() == inv.getArtifact().getOutputPorts().size());
			for (T ac : outputs)
				assert (ac != null);

			/*
			 * for each job that is reachable from c, we have to update the
			 * ingoing edges and the number of ingoing edges
			 */
			LinkedList<JobConnection<V>> f = forward.get(c);
			if (f != null) { // f can be null if c has no outgoing connections
				for (JobConnection<V> cc : f) {
					Job<V> t = cc.getTarget();
					b = backward.get(t);
					if (b == null) {
						b = new LinkedList<Entry<T, A>>();
						backward.put(t, b);
					}
					b.add(new Entry<T, A>(inv, cc.getSourcePort(), cc
							.getTargetPort(), outputs.get(cc.getSourcePort()),
							cc));
					Integer j = ingoing.get(t);
					assert (j != null); // each child should be in the map!
					if (j != null) {
						assert (j.intValue() > 0);
						ingoing.put(t, j.intValue() - 1);
						// the job t is ready for conversion:
						if (j.intValue() == 1)
							workingset.push(t);
					}
				}
			}
		}

		result.setFragment(af.compose(result));
		return result;
	}

	/**
	 * Converts the (abstract) Workflow into a (concrete) InvokationWorkflow.
	 * The documentation of WorkflowElement.convert applies accordingly.
	 * 
	 * @param m
	 * @param profile
	 * @return
	 */
	public InvokationWorkflow<?, ?, F> convert(MemoTable m, Profile profile) {
		InvokationWorkflow<?, ?, F> result = null;
		if (m != null)
			result = m.retrieve(this, profile);
		if (result == null) {
			result = doConvert(m, profile);
			if (m != null)
				m.register(this, profile, result);
		}
		return result;
	}

	/**
	 * The documentation of WorkflowElement.doConvert applies accordingly.
	 * 
	 * @param m
	 * @param profile
	 * @return
	 */
	public InvokationWorkflow<?, ?, F> doConvert(MemoTable m, Profile profile) {
		ArtifactFactory<?, ?, F, V> af = compiler
				.createArtifactFactory(profile);
		return compile(m, af, profile);
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (Job<V> c : children)
			if (c.isInputPort())
				list.add(c.getOutputPorts().get(0));
		return list;
	}

	/**
	 * Looks for children that are OutputPorts.
	 * 
	 * @return
	 */
	public List<Port> getOutputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (Job<V> c : children)
			if (c.isOutputPort())
				list.add(c.getInputPorts().get(0));
		return list;
	}

}
