package org.vanda.studio.model.hyper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public class DrecksWorkflow<F> {

	/*
	protected static class TokenValue<F> {
		public final Token hj;
		public final int port;

		public TokenValue(Token hj, int port) {
			this.hj = hj;
			this.port = port;
		}
	}*/

	protected static class DConnInfo {
		public final Token address;  // XXX somewhat redundant
		public final Token variable;
		public final Connection cc; // super-redundant

		public DConnInfo(Token address, Token variable, Connection cc) {
			this.address = address;
			this.variable = variable;
			this.cc = cc;
		}
	}

	protected static class DJobInfo<F> {
		public final Job<F> job;
		public final Token address; // XXX somewhat redundant
		public final ArrayList<Token> inputs;
		public int inputsBlocked;
		public final ArrayList<Token> outputs;
		public int outCount;
		public int topSortInputsBlocked;

		public DJobInfo(DrecksWorkflow<F> parent, Job<F> j) {
			job = j;
			address = parent.addressSource.makeToken();
			inputs = new ArrayList<Token>(j.getInputPorts().size());
			for (int i = 0; i < j.getInputPorts().size(); i++)
				inputs.add(null);
			inputsBlocked = 0;
			outputs = new ArrayList<Token>(j.getOutputPorts().size());
			for (int i = 0; i < j.getOutputPorts().size(); i++) {
				Token t = parent.variableSource.makeToken();
				outputs.add(t);
				/*
				parent.connections.put(t,
						new Pair<TokenValue<F>, List<TokenValue<F>>>(
								new TokenValue<F>(address, i),
								new LinkedList<TokenValue<F>>()));
								*/
			}
			outCount = 0;
			topSortInputsBlocked = 0;
			// parent.deref.put(address, j);
		}

		public DJobInfo(DJobInfo<F> ji) throws CloneNotSupportedException {
			// only apply this when the whole hyperworkflow is copied
			// only copy inputs, because they are mutable
			job = ji.job.clone();
			inputs = new ArrayList<Token>(ji.inputs);
			inputsBlocked = ji.inputsBlocked;
			outputs = ji.outputs;
			outCount = ji.outCount;
			topSortInputsBlocked = ji.topSortInputsBlocked;
			address = ji.address;
		}

	}

	protected final TokenSource variableSource;
	protected final TokenSource addressSource;
	protected final ArrayList<DJobInfo<F>> children;
	// protected final Map<Token, Pair<TokenValue<F>, List<TokenValue<F>>>>
	// connections;
	protected final ArrayList<DConnInfo> connections;
	private final Class<F> fragmentType;

	public DrecksWorkflow(Class<F> fragmentType) {
		super();
		this.fragmentType = fragmentType;
		children = new ArrayList<DJobInfo<F>>();
		connections = new ArrayList<DConnInfo>();
		variableSource = new TokenSource();
		addressSource = new TokenSource();
	}

	public DrecksWorkflow(DrecksWorkflow<F> hyperWorkflow)
			throws CloneNotSupportedException {
		fragmentType = hyperWorkflow.fragmentType;
		// clone children because they may contain mutable elements
		children = new ArrayList<DJobInfo<F>>();
		ListIterator<DJobInfo<F>> it = hyperWorkflow.children.listIterator();
		while (it.hasNext())
			children.add(new DJobInfo<F>(it.next()));
		connections = new ArrayList<DConnInfo>(hyperWorkflow.connections);
		variableSource = hyperWorkflow.variableSource.clone();
		addressSource = hyperWorkflow.addressSource.clone();
	}

	/*
	 * public ImmutableWorkflow<F> clone() throws CloneNotSupportedException {
	 * return new ImmutableWorkflow<F>(this); }
	 */

	public Collection<Token> getChildren() {
		ArrayList<Token> result = new ArrayList<Token>();
		for (DJobInfo<F> ji : children)
			if (ji != null)
				result.add(ji.address);
		return result;
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (DJobInfo<F> ji : children)
			if (ji != null && ji.job.isInputPort())
				list.add(ji.job.getOutputPorts().get(0));
		return list;
	}

	/**
	 * Looks for children that are OutputPorts.
	 * 
	 * @return
	 */
	public List<Port> getOutputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (DJobInfo<F> ji : children)
			if (ji != null && ji.job.isOutputPort())
				list.add(ji.job.getInputPorts().get(0));
		return list;
	}

	public Class<F> getFragmentType() {
		return fragmentType;
	}

	public ImmutableWorkflow<F> freeze() throws Exception {
		ArrayList<DJobInfo<F>> topsort = new ArrayList<DJobInfo<F>>(
				children.size());
		LinkedList<DJobInfo<F>> workingset = new LinkedList<DJobInfo<F>>();
		for (DJobInfo<F> ji : children) {
			if (ji != null) {
				ji.topSortInputsBlocked = ji.inputsBlocked;
				if (ji.topSortInputsBlocked == 0)
					workingset.add(ji);
			}
		}
		while (!workingset.isEmpty()) {
			DJobInfo<F> ji = workingset.pop();
			topsort.add(ji);
			for (Token tok : ji.outputs)
				// for (TokenValue<F> tv : connections.get(tok).snd) {
				for (DConnInfo ci : connections) {
					if (ci != null && ci.variable == tok) {
						DJobInfo<F> ji2 = children.get(ci.cc.target.intValue());
						ji2.topSortInputsBlocked--;
						if (ji2.topSortInputsBlocked == 0)
							workingset.add(ji2);
					}
				}
		}
		if (topsort.size() == children.size()) {
			ArrayList<JobInfo<F>> imch = new ArrayList<JobInfo<F>>(
					topsort.size());
			for (DJobInfo<F> ji : topsort) {
				imch.add(new JobInfo<F>(ji.job.freeze(), ji.address,
						new ArrayList<Token>(ji.inputs), new ArrayList<Token>(
								ji.outputs), ji.outCount));
			}
			return new ImmutableWorkflow<F>(imch, variableSource, variableSource.getMaxToken());
		} else
			throw new Exception(
					"could not do topological sort; cycles probable");
	}
	/*
	 * public List<MutableWorkflow<F>> unfold() throws
	 * CloneNotSupportedException { return new Unfolder<F>(this).unfold(); }
	 */

}
