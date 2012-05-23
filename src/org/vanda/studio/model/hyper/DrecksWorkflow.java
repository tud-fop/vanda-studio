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

public class DrecksWorkflow {

	protected static final class DConnInfo {
		public final Token variable;
		public final Connection cc; // somewhat redundant

		public DConnInfo(Token variable, Connection cc) {
			this.variable = variable;
			this.cc = cc;
		}
	}

	protected static final class DJobInfo {
		public final Job job;
		public final ArrayList<Token> inputs;
		public int inputsBlocked;
		public final ArrayList<Token> outputs;
		public int outCount;
		public int topSortInputsBlocked;

		public DJobInfo(DrecksWorkflow parent, Job j) {
			job = j;
			inputs = new ArrayList<Token>(j.getInputPorts().size());
			for (Port p : j.getInputPorts())
				inputs.add(null);
			inputsBlocked = 0;
			outputs = new ArrayList<Token>(j.getOutputPorts().size());
			for (Port p : j.getOutputPorts()) {
				Token t = parent.variableSource.makeToken();
				outputs.add(t);
			}
			outCount = 0;
			topSortInputsBlocked = 0;
		}

		public DJobInfo(DJobInfo ji) throws CloneNotSupportedException {
			// only apply this when the whole hyperworkflow is copied
			// only copy inputs, because they are mutable
			job = ji.job.clone();
			inputs = new ArrayList<Token>(ji.inputs);
			inputsBlocked = ji.inputsBlocked;
			outputs = ji.outputs;
			outCount = ji.outCount;
			topSortInputsBlocked = ji.topSortInputsBlocked;
		}

	}

	protected final TokenSource variableSource;
	protected final TokenSource childAddressSource;
	protected final TokenSource connectionAddressSource;
	protected final ArrayList<DJobInfo> children;
	// protected final Map<Token, Pair<TokenValue<F>, List<TokenValue<F>>>>
	// connections;
	protected final ArrayList<DConnInfo> connections;
	protected String name;

	public DrecksWorkflow(String name) {
		super();
		this.name = name;
		children = new ArrayList<DJobInfo>();
		connections = new ArrayList<DConnInfo>();
		variableSource = new TokenSource();
		childAddressSource = new TokenSource();
		connectionAddressSource = new TokenSource();
	}

	public DrecksWorkflow(DrecksWorkflow hyperWorkflow)
			throws CloneNotSupportedException {
		name = hyperWorkflow.name;
		// clone children because they may contain mutable elements
		children = new ArrayList<DJobInfo>();
		ListIterator<DJobInfo> it = hyperWorkflow.children.listIterator();
		while (it.hasNext()) {
			DJobInfo ji = it.next();
			if (ji == null)
				children.add(null);
			else
				children.add(new DJobInfo(ji));
		}
		connections = new ArrayList<DConnInfo>(hyperWorkflow.connections);
		variableSource = hyperWorkflow.variableSource.clone();
		childAddressSource = hyperWorkflow.childAddressSource.clone();
		connectionAddressSource = hyperWorkflow.connectionAddressSource.clone();
	}

	public Collection<Job> getChildren() {
		ArrayList<Job> result = new ArrayList<Job>();
		for (DJobInfo ji : children)
			if (ji != null)
				result.add(ji.job);
		return result;
	}

	/**
	 * Looks for children that are InputPorts.
	 * 
	 * @return
	 */
	public List<Port> getInputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (DJobInfo ji : children)
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
		for (DJobInfo ji : children)
			if (ji != null && ji.job.isOutputPort())
				list.add(ji.job.getInputPorts().get(0));
		return list;
	}

	/*
	 * public Class<F> getFragmentType() { return fragmentType; }
	 */

	public ImmutableWorkflow freeze() throws Exception {
		// Two steps. Step 1: topological sort
		// XXX potential optimization: compute forward star
		int count = 0;
		LinkedList<DJobInfo> workingset = new LinkedList<DJobInfo>();
		for (DJobInfo ji : children) {
			if (ji != null) {
				ji.topSortInputsBlocked = ji.inputsBlocked;
				if (ji.topSortInputsBlocked == 0)
					workingset.add(ji);
				count++;
			}
		}
		ArrayList<DJobInfo> topsort = new ArrayList<DJobInfo>(count);
		while (!workingset.isEmpty()) {
			DJobInfo ji = workingset.pop();
			topsort.add(ji);
			for (Token tok : ji.outputs)
				for (DConnInfo ci : connections) {
					if (ci != null && ci.variable == tok) {
						DJobInfo ji2 = children.get(ci.cc.target.intValue());
						ji2.topSortInputsBlocked--;
						if (ji2.topSortInputsBlocked == 0)
							workingset.add(ji2);
					}
				}
		}
		// Step 2: actual freeze
		if (topsort.size() == count) {
			ArrayList<JobInfo> imch = new ArrayList<JobInfo>(topsort.size());
			for (DJobInfo ji : topsort) {
				List<Port> ports = null;
				ports = ji.job.getInputPorts();
				ArrayList<Token> intoken = new ArrayList<Token>(ports.size());
				for (int i = 0; i < ports.size(); i++) {
					if (ports.get(i) != null)
						intoken.add(ji.inputs.get(i));
				}
				ports = ji.job.getOutputPorts();
				ArrayList<Token> outtoken = new ArrayList<Token>(ports.size());
				for (int i = 0; i < ports.size(); i++) {
					if (ports.get(i) != null)
						outtoken.add(ji.outputs.get(i));
				}
				imch.add(new JobInfo(ji.job.freeze(), ji.job.address, intoken,
						outtoken, ji.outCount));
			}
			return new ImmutableWorkflow(name, getInputPorts(),
					getOutputPorts(), null, imch, variableSource,
					variableSource.getMaxToken());
		} else
			throw new Exception(
					"could not do topological sort; cycles probable");
	}

}
