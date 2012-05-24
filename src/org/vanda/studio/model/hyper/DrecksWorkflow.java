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
		public Token portNumber = null;

		public DJobInfo(DrecksWorkflow parent, Job j) {
			job = j;
			inputs = new ArrayList<Token>(j.getInputPorts().size());
			for (int i = 0; i < j.getInputPorts().size(); i++)
				inputs.add(null);
			inputsBlocked = 0;
			outputs = new ArrayList<Token>(j.getOutputPorts().size());
			for (int i = 0; i < j.getOutputPorts().size(); i++) {
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
	protected final TokenSource inputPortSource;
	protected final TokenSource outputPortSource;
	protected final ArrayList<DJobInfo> children;
	// protected final Map<Token, Pair<TokenValue<F>, List<TokenValue<F>>>>
	// connections;
	protected final ArrayList<DConnInfo> connections;
	protected final ArrayList<Token> inputPorts;
	protected final ArrayList<Token> outputPorts;
	protected String name;

	public DrecksWorkflow(String name) {
		super();
		this.name = name;
		children = new ArrayList<DJobInfo>();
		connections = new ArrayList<DConnInfo>();
		variableSource = new TokenSource();
		childAddressSource = new TokenSource();
		connectionAddressSource = new TokenSource();
		inputPortSource = new TokenSource();
		outputPortSource = new TokenSource();
		inputPorts = new ArrayList<Token>();
		outputPorts = new ArrayList<Token>();
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
		inputPortSource = hyperWorkflow.inputPortSource.clone();
		outputPortSource = hyperWorkflow.outputPortSource.clone();
		inputPorts = new ArrayList<Token>(hyperWorkflow.inputPorts);
		outputPorts = new ArrayList<Token>(hyperWorkflow.outputPorts);
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
		for (Token t : inputPorts)
			if (t == null)
				list.add(null);
			else
				list.add(children.get(t.intValue()).job.getOutputPorts().get(0));
		/*
		 * for (DJobInfo ji : children) if (ji != null && ji.job.isInputPort())
		 * list.add(ji.job.getOutputPorts().get(0));
		 */
		return list;
	}

	/**
	 * Looks for children that are OutputPorts.
	 * 
	 * @return
	 */
	public List<Port> getOutputPorts() {
		ArrayList<Port> list = new ArrayList<Port>();
		for (Token t : outputPorts)
			if (t == null)
				list.add(null);
			else
				list.add(children.get(t.intValue()).job.getInputPorts().get(0));

		/*
		 * for (DJobInfo ji : children) if (ji != null && ji.job.isOutputPort())
		 * list.add(ji.job.getInputPorts().get(0));
		 */
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
			List<Port> ports = null;
			for (DJobInfo ji : topsort) {
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
			ports = getInputPorts();
			ArrayList<Port> inputPorts = new ArrayList<Port>();
			for (int i = 0; i < ports.size(); i++) {
				if (ports.get(i) != null)
					inputPorts.add(ports.get(i));
			}
			ports = getOutputPorts();
			ArrayList<Port> outputPorts = new ArrayList<Port>();
			for (int i = 0; i < ports.size(); i++) {
				if (ports.get(i) != null)
					outputPorts.add(ports.get(i));
			}
			return new ImmutableWorkflow(name, inputPorts, outputPorts, null,
					imch, variableSource, variableSource.getMaxToken());
		} else
			throw new Exception(
					"could not do topological sort; cycles probable");
	}

}
