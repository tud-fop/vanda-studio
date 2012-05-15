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

public final class ImmutableWorkflow<F> {

	final ArrayList<JobInfo<F>> children;
	final TokenSource token;
	final int[] tokenSource;
	private final Map<Object, ImmutableJob<F>> deref;
	private Type[] types;

	/**
	 * 
	 * @param children
	 *            topological sort of children
	 * @param maxtoken
	 *            maximum token
	 */
	public ImmutableWorkflow(ArrayList<JobInfo<F>> children, TokenSource token,
			int maxtoken) {
		this.children = children;
		deref = new HashMap<Object, ImmutableJob<F>>();
		this.token = token;
		tokenSource = new int[maxtoken];
		for (int i = 0; i < children.size(); i++) {
			JobInfo<F> ji = children.get(i);
			if (maxtoken > 0) {
				for (Object tok : ji.outputs)
					tokenSource[((TokenSource.Token) tok).intValue()] = i;
			}
			deref.put(ji.address, ji.job);
		}
		types = null;
	}

	public ImmutableJob<?> dereference(ListIterator<Token> address) {
		assert (address != null && address.hasNext());
		ImmutableJob<?> hj = deref.get(address.next());
		if (hj != null)
			hj = hj.dereference(address);
		return hj;
	}

	public ArrayList<JobInfo<F>> getChildren() {
		return children;
	}

	public Type getType(Object variable) {
		if (types == null || !(variable instanceof Token)
				|| ((Token) variable).intValue() >= types.length)
			return null;
		else
			return types[((Token) variable).intValue()];
	}

	public boolean isSane() {
		boolean result = true;
		for (JobInfo<F> ji : children) {
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
		Map<Object, Object> rename = null;
		Set<Equation> s = new HashSet<Equation>();
		for (JobInfo<F> ji : children) {
			rename = new HashMap<Object, Object>();
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
			Token i = (Token) ((TypeVariable) e.lhs).variable;
			if (i.intValue() < types.length)
				types[i.intValue()] = e.rhs;
		}
		for (int i = 0; i < types.length; i++)
			if (types[i] == null)
				types[i] = new TypeVariable(TokenSource.getToken(i));
		System.out.println(s);
	}

	public List<ImmutableWorkflow<F>> unfold() {
		if (tokenSource.length == 0)
			return Collections.singletonList(this);
		else
			return new Unfolder<F>(this).unfold();
	}

	public void appendText(StringBuilder sections) {
		ArrayList<Token> outputs = new ArrayList<Token>();
		ArrayList<Token> inputs = new ArrayList<Token>();
		ArrayList<JobInfo<F>> inputJI = new ArrayList<JobInfo<F>>();
		ArrayList<JobInfo<F>> outputJI = new ArrayList<JobInfo<F>>();
		for (JobInfo<F> ji : children) {
			if (ji.job.isInputPort()) {
				inputJI.add(ji);
				inputs.add(null);
			}
			if (ji.job.isOutputPort()) {
				outputJI.add(ji);
				outputs.add(null);
			}
		}
		for (JobInfo<F> ji : inputJI)
			inputs.set(((InputPort) ((AtomicImmutableJob<F>) ji.job)
					.getElement()).getNumber(), ji.outputs.get(0));
		for (JobInfo<F> ji : outputJI)
			inputs.set(((OutputPort) ((AtomicImmutableJob<F>) ji.job)
					.getElement()).getNumber(), ji.inputs.get(0));
		StringBuilder lines = new StringBuilder();
		ImmutableJob.appendOutput(outputs, lines);
		lines.append(" = ");
		lines.append(toString());
		ImmutableJob.appendInput(inputs, lines);
		lines.append('\n');
		for (JobInfo<F> ji : children) {
			lines.append("  ");
			ji.job.appendText(ji.inputs, ji.outputs, lines, sections);
		}
		sections.append(lines);
		sections.append('\n');
	}

}
