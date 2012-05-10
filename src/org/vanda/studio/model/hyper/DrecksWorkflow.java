package org.vanda.studio.model.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.util.Pair;

public class DrecksWorkflow<F> {

	protected static class TokenValue<F> {
		public final Job<F> hj;
		public final int port;

		public TokenValue(Job<F> hj, int port) {
			this.hj = hj;
			this.port = port;
		}
	}

	protected class DJobInfo {
		public Integer address;
		public ArrayList<Integer> inputs;
		public int inputsBlocked;
		public ArrayList<Integer> outputs;
		public int outCount;
		public int topSortInputsBlocked;

		public DJobInfo(Job<F> j) {
			inputs = new ArrayList<Integer>(j.getInputPorts().size());
			for (int i = 0; i < j.getInputPorts().size(); i++)
				inputs.add(null);
			inputsBlocked = 0;
			outputs = new ArrayList<Integer>(j.getOutputPorts().size());
			for (int i = 0; i < j.getOutputPorts().size(); i++) {
				Integer t = token.makeToken();
				outputs.add(t);
				connections.put(t,
						new Pair<TokenValue<F>, List<TokenValue<F>>>(
								new TokenValue<F>(j, i),
								new LinkedList<TokenValue<F>>()));
			}
			outCount = 0;
			topSortInputsBlocked = 0;
			address = DrecksWorkflow.this.address.makeToken();
		}

		public DJobInfo(DJobInfo ji) {
			// only apply this when the whole hyperworkflow is copied
			// only copy inputs, because they are mutable
			inputs = new ArrayList<Integer>(ji.inputs);
			inputsBlocked = ji.inputsBlocked;
			outputs = ji.outputs;
			outCount = ji.outCount;
			topSortInputsBlocked = ji.topSortInputsBlocked;
			address = ji.address;
		}

	}

	protected final Token token;
	protected final Token address;
	protected final Map<Job<F>, DJobInfo> children;
	protected final Map<Integer, Pair<TokenValue<F>, List<TokenValue<F>>>> connections;
	protected final Map<Integer, Job<F>> deref;
	private final Class<F> fragmentType;

	public DrecksWorkflow(Class<F> fragmentType) {
		super();
		this.fragmentType = fragmentType;
		children = new HashMap<Job<F>, DJobInfo>();
		connections = new HashMap<Integer, Pair<TokenValue<F>, List<TokenValue<F>>>>();
		deref = new HashMap<Integer, Job<F>>();
		token = new Token();
		address = new Token();
	}

	public DrecksWorkflow(DrecksWorkflow<F> hyperWorkflow)
			throws CloneNotSupportedException {
		fragmentType = hyperWorkflow.fragmentType;
		// clone children because they may contain mutable elements
		children = new HashMap<Job<F>, DJobInfo>();
		for (Entry<Job<F>, DJobInfo> e : hyperWorkflow.children.entrySet()) {
			Job<F> cl = e.getKey().clone();
			children.put(cl, new DJobInfo(e.getValue()));
		}
		connections = new HashMap<Integer, Pair<TokenValue<F>, List<TokenValue<F>>>>(
				hyperWorkflow.connections);
		deref = new HashMap<Integer, Job<F>>(hyperWorkflow.deref);
		token = hyperWorkflow.token.clone();
		address = hyperWorkflow.address.clone();
	}

	/*
	 * public ImmutableWorkflow<F> clone() throws CloneNotSupportedException {
	 * return new ImmutableWorkflow<F>(this); }
	 */

	public Collection<Job<F>> getChildren() {
		return children.keySet();
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (Job<F> c : children.keySet())
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
		for (Job<F> c : children.keySet())
			if (c.isOutputPort())
				list.add(c.getInputPorts().get(0));
		return list;
	}

	public Class<F> getFragmentType() {
		return fragmentType;
	}

	public ImmutableWorkflow<F> freeze() throws Exception {
		ArrayList<Job<F>> topsort = new ArrayList<Job<F>>(children.size());
		LinkedList<Job<F>> workingset = new LinkedList<Job<F>>();
		for (Entry<Job<F>, DJobInfo> e : children.entrySet()) {
			DJobInfo ji = e.getValue();
			ji.topSortInputsBlocked = ji.inputsBlocked;
			if (ji.topSortInputsBlocked == 0)
				workingset.add(e.getKey());
		}
		while (!workingset.isEmpty()) {
			Job<F> j = workingset.pop();
			topsort.add(j);
			for (Integer tok : children.get(j).outputs)
				for (TokenValue<F> tv : connections.get(tok).snd) {
					DJobInfo ji = children.get(tv.hj);
					ji.topSortInputsBlocked--;
					if (ji.topSortInputsBlocked == 0)
						workingset.add(tv.hj);
				}
		}
		if (topsort.size() == children.size()) {
			ArrayList<JobInfo<F>> imch = new ArrayList<JobInfo<F>>(
					topsort.size());
			for (Job<F> j : topsort) {
				DJobInfo ji = children.get(j);
				imch.add(new JobInfo<F>(j.freeze(), ji.address,
						new ArrayList<Integer>(ji.inputs),
						new ArrayList<Integer>(ji.outputs), ji.outCount));
			}
			return new ImmutableWorkflow<F>(imch, token.getMaxToken());
		} else
			throw new Exception("could not do topological sort; cycles probable");
	}
	/*
	 * public List<MutableWorkflow<F>> unfold() throws
	 * CloneNotSupportedException { return new Unfolder<F>(this).unfold(); }
	 */

}
