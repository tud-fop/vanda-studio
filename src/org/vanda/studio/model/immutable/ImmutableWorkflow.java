package org.vanda.studio.model.immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.types.Equation;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.TokenSource;
import org.vanda.studio.util.TokenSource.Token;

public final class ImmutableWorkflow {

	final ArrayList<JobInfo> children;
	final TokenSource token;
	final int[] tokenSource;
	final String name;
	private final Map<Object, ImmutableJob> deref;
	private Type[] types;
	private Type fragmentType;

	/**
	 * 
	 * @param children
	 *            topological sort of children
	 * @param maxtoken
	 *            maximum token
	 */
	public ImmutableWorkflow(String name, ArrayList<JobInfo> children,
			TokenSource token, int maxtoken) {
		this.name = name;
		this.children = children;
		deref = new HashMap<Object, ImmutableJob>();
		this.token = token;
		tokenSource = new int[maxtoken];
		for (int i = 0; i < children.size(); i++) {
			JobInfo ji = children.get(i);
			if (maxtoken > 0) {
				for (Object tok : ji.outputs)
					tokenSource[((TokenSource.Token) tok).intValue()] = i;
			}
			deref.put(ji.address, ji.job);
		}
		types = null;
		fragmentType = null;
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
		TokenSource t = token.clone();
		Map<Token, Token> rename = null;
		Set<Equation> s = new HashSet<Equation>();
		Token fragmentTypeToken = t.makeToken();
		for (JobInfo ji : children) {
			rename = new HashMap<Token, Token>();
			ji.job.getFragmentType().freshMap(t, rename);
			s.add(new Equation(new TypeVariable(fragmentTypeToken), ji.job.getFragmentType()
					.rename(rename)));
			rename = new HashMap<Token, Token>();
			if (!ji.job.isInputPort() && !ji.job.isOutputPort()) {
				List<Port> in = ji.job.getInputPorts();
				List<Port> ou = ji.job.getOutputPorts();
				if (in != null && ou != null) {
					assert (in.size() == ji.inputs.size() && ou.size() == ji.outputs
							.size());
					for (Port p : in)
						p.getType().freshMap(t, rename);
					for (Port p : ou)
						p.getType().freshMap(t, rename);
					for (int i = 0; i < in.size(); i++) {
						if (ji.inputs.get(i) != null)
							s.add(new Equation(new TypeVariable(ji.inputs
									.get(i)), in.get(i).getType()
									.rename(rename)));
					}
					for (int i = 0; i < ou.size(); i++) {
						s.add(new Equation(new TypeVariable(ji.outputs.get(i)),
								ou.get(i).getType().rename(rename)));
					}
				}
			}
		}
		// System.out.println(s);
		Types.unify(s);
		types = new Type[token.getMaxToken()];
		for (Equation e : s) {
			Token i = ((TypeVariable) e.lhs).variable;
			if (i.intValue() < types.length)
				types[i.intValue()] = e.rhs;
			if (i.intValue() == fragmentTypeToken.intValue())
				fragmentType = e.rhs;
		}
		System.out.println(fragmentType);
		for (int i = 0; i < types.length; i++)
			if (types[i] == null)
				types[i] = new TypeVariable(TokenSource.getToken(i));
		// System.out.println(s);
	}

	public List<ImmutableWorkflow> unfold() {
		if (tokenSource.length == 0)
			return Collections.singletonList(this);
		else
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
