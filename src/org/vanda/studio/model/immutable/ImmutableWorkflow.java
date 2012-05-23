package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.vanda.studio.model.elements.IdentityLinker;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public final class ImmutableWorkflow {

	final ArrayList<JobInfo> children;
	final TokenSource variableSource;
	final int[] variableOrigins;
	final String name;
	private final Map<Token, ImmutableJob> deref;
	private Type[] types;
	private Type fragmentType;

	/**
	 * 
	 * @param children
	 *            topological sort of children
	 * @param maxtoken
	 *            maximum token
	 */
	public ImmutableWorkflow(String name, Type fragmentType,
			ArrayList<JobInfo> children, TokenSource token, int maxtoken) {
		this.name = name;
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
		types = null;
		this.fragmentType = fragmentType;
	}

	public ImmutableWorkflow(String name, List<ImmutableWorkflow> unfolded) {
		ArrayList<Token> empty = new ArrayList<Token>();

		this.name = name;
		variableSource = new TokenSource();
		variableOrigins = new int[0];
		children = new ArrayList<JobInfo>(unfolded.size());
		deref = Collections.emptyMap();
		types = null;
		fragmentType = null;
		for (int i = 0; i < unfolded.size(); i++) {
			ImmutableWorkflow iwf = unfolded.get(i);
			Token address = TokenSource.getToken(i);
			if (fragmentType == null)
				fragmentType = iwf.fragmentType;
			children.add(new JobInfo(new CompositeImmutableJob(address,
					IdentityLinker.getInstance(), iwf), address, empty, empty,
					0));
		}
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
			if (ji.job.isChoice()) {
				// for CHOOSE nodes, at least one input has to be connected
				boolean r = false;
				for (Object o : ji.inputs)
					r = r || (o != null);
				result = result && r;
			} else {
				for (Object o : ji.inputs)
					result = result && (o != null);
			}
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
			if (!ji.job.isInputPort() && !ji.job.isOutputPort())
				tc.addDataFlowEquations(ji);
		}
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();
		// System.out.println(fragmentType);
	}

	public List<ImmutableWorkflow> unfold() {
		// XXX this hack did not work, find another one
		// if (variableOrigins.length == 0)
		// return Collections.singletonList(this);
		// else
		return new Unfolder(this).unfold();
	}

	public void appendText(StringBuilder sections) {
		ArrayList<Token> outputs = new ArrayList<Token>();
		ArrayList<Token> inputs = new ArrayList<Token>();
		ArrayList<JobInfo> inputJI = new ArrayList<JobInfo>();
		ArrayList<JobInfo> outputJI = new ArrayList<JobInfo>();
		for (JobInfo ji : children) {
			if (ji.job.isInputPort()) {
				inputJI.add(ji);
				inputs.add(null);
			}
			if (ji.job.isOutputPort()) {
				outputJI.add(ji);
				outputs.add(null);
			}
		}
		for (JobInfo ji : inputJI)
			inputs.set(((InputPort) ((AtomicImmutableJob) ji.job).getElement())
					.getNumber(), ji.outputs.get(0));
		for (JobInfo ji : outputJI)
			inputs.set(
					((OutputPort) ((AtomicImmutableJob) ji.job).getElement())
							.getNumber(), ji.inputs.get(0));
		StringBuilder lines = new StringBuilder();
		ImmutableJob.appendOutput(outputs, lines);
		lines.append(" = ");
		lines.append(getName());
		ImmutableJob.appendInput(inputs, lines);
		lines.append('\n');
		for (JobInfo ji : children) {
			lines.append("  ");
			ji.job.appendText(ji.inputs, ji.outputs, lines, sections);
		}
		sections.append(lines);
		sections.append('\n');
	}

}
