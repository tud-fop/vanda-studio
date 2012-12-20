package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public final class ImmutableWorkflow {

	final ArrayList<JobInfo> children;
	final TokenSource variableSource;
	final int[] variableOrigins;
	final String name;
	private final Map<Token, ImmutableJob> deref;
	Type[] types;
	private Type fragmentType;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private final List<Token> inputPortVariables;
	private final List<Token> outputPortVariables;

	/**
	 * 
	 * @param children
	 *            topological sort of children
	 * @param maxtoken
	 *            maximum token
	 */
	public ImmutableWorkflow(String name, List<Port> inputPorts,
			List<Port> outputPorts, List<Token> inputPortVariables,
			List<Token> outputPortVariables, Type[] types, Type fragmentType,
			ArrayList<JobInfo> children, TokenSource token, int maxtoken) {
		this.name = name;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.inputPortVariables = inputPortVariables;
		this.outputPortVariables = outputPortVariables;
		this.children = children;
		deref = new HashMap<Token, ImmutableJob>();
		this.variableSource = token;
		variableOrigins = new int[maxtoken];
		for (int i = 0; i < children.size(); i++) {
			JobInfo ji = children.get(i);
			if (maxtoken > 0) {
				for (Object tok : ji.outputs)
					variableOrigins[((TokenSource.Token) tok).intValue()] = i;
			}
			deref.put(ji.address, ji.job);
		}
		this.types = types;
		this.fragmentType = fragmentType;
	}

	public ImmutableWorkflow dereference(ListIterator<Token> path) {
		assert (path != null);
		if (path.hasNext()) {
			ImmutableJob job = deref.get(path.next());
			if (job != null)
				return job.dereference(path);
			else
				return null;
		} else
			return this;
	}

	public ArrayList<JobInfo> getChildren() {
		return children;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public List<Port> getInputPorts() {
		return inputPorts;
	}

	public List<Token> getInputPortVariables() {
		return inputPortVariables;
	}

	public List<Port> getOutputPorts() {
		return outputPorts;
	}

	public List<Token> getOutputPortVariables() {
		return outputPortVariables;
	}

	public String getName() {
		return name;
	}

	public Type getType(Token variable) {
		if (types == null || variable.intValue() >= types.length)
			return null;
		else
			return types[variable.intValue()];
	}

	public boolean isSane() {
		boolean result = true;
		for (JobInfo ji : children) {
			for (Object o : ji.inputs)
				result = result && (o != null);
		}
		return result;
	}

	public void typeCheck() throws Exception {
		// only type check once, as this is an immutable workflow...
		if (fragmentType != null)
			return;
		TypeChecker tc = new TypeChecker(variableSource);
		for (JobInfo ji : children) {
			ji.job.typeCheck();
			ji.job.addFragmentTypeEquation(tc);
			tc.addDataFlowEquations(ji);
		}
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();
		List<Port> oldInputPorts = inputPorts;
		List<Port> oldOutputPorts = outputPorts;
		inputPorts = new ArrayList<Port>(inputPorts.size());
		outputPorts = new ArrayList<Port>(outputPorts.size());
		for (int i = 0; i < oldInputPorts.size(); i++)
			inputPorts.add(new Port(oldInputPorts.get(i).getIdentifier(),
					types[inputPortVariables.get(i).intValue()]));
		for (int i = 0; i < oldOutputPorts.size(); i++)
			if (outputPortVariables.get(i) != null)
				outputPorts.add(new Port(oldOutputPorts.get(i).getIdentifier(),
						types[outputPortVariables.get(i).intValue()]));
			else
				outputPorts.add(new Port(oldOutputPorts.get(i).getIdentifier(),
						Types.undefined));
		// System.out.println(fragmentType);
	}

	public void appendText(StringBuilder sections) {
		StringBuilder lines = new StringBuilder();

		lines.append(getName());
		ImmutableJob.appendInput(inputPortVariables, lines);
		lines.append(" = ");
		ImmutableJob.appendOutput(outputPortVariables, lines);
		if (children.size() > 0)
			lines.append(" where");
		lines.append('\n');
		for (JobInfo ji : children) {
			ji.job.appendText(ji.inputs, ji.outputs, lines, sections);
		}
		sections.append(lines);
		sections.append('\n');

	}

}
