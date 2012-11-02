package org.vanda.studio.modules.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;
import org.vanda.studio.util.TokenSource.Token;

// XXX I've had it with all of this instanceof/typecast bullshit
// find a principled solution (e.g., visitor)
public class DataflowAnalysis {
	private final Map<Token, DataflowAnalysis> deref;
	private final Map<Port, Integer> inportIndex;
	private final Map<Port, Integer> outportIndex;
	private final String[] values;
	private final ImmutableWorkflow workflow;

	public DataflowAnalysis(ImmutableWorkflow iwf) {
		workflow = iwf;
		inportIndex = new HashMap<Port, Integer>();
		makeIndex(iwf.getInputPorts(), inportIndex);
		outportIndex = new HashMap<Port, Integer>();
		makeIndex(iwf.getOutputPorts(), outportIndex);
		deref = new HashMap<Token, DataflowAnalysis>();
		int varcount = 0;
		for (JobInfo ji : iwf.getChildren()) {
			for (Token t : ji.outputs) {
				if (t.intValue() >= varcount)
					varcount = t.intValue() + 1;
			}
			if (ji.job instanceof CompositeImmutableJob) {
				CompositeImmutableJob cj = (CompositeImmutableJob) ji.job;
				deref.put(cj.getAddress(),
						new DataflowAnalysis(cj.getWorkflow()));
			}
		}
		values = new String[varcount];
	}

	public void doIt(List<String> inputs, Profiles p) {
		// List<String> result = new ArrayList<String>(outportIndex.size());
		for (JobInfo ji : workflow.getChildren()) {
			if (ji.job instanceof AtomicImmutableJob) {
				AtomicImmutableJob aj = (AtomicImmutableJob) ji.job;
				if (aj.isInputPort()) {
					values[ji.outputs.get(0).intValue()] = inputs
							.get(inportIndex.get(aj.getOutputPorts().get(0)));
				} else if (aj.isOutputPort()) {
					/*
					 * int n = outportIndex.get(aj.getInputPorts().get(0));
					 * while (n >= result.size()) result.add(null);
					 * result.set(n, values[ji.inputs.get(0).intValue()]);
					 */
				} else if (aj.isChoice()) {
					for (int i = 0; i < ji.inputs.size(); i++) {
						Token var = ji.inputs.get(i);
						if (var != null)
							values[ji.outputs.get(0).intValue()] = values[var
									.intValue()];
					}
				} else if (aj.getElement() instanceof Literal) {
					Literal lit = (Literal) aj.getElement();
					values[ji.outputs.get(0).intValue()] = lit.getValue();
				} else if (aj.getElement() instanceof Tool) {
					StringBuilder sb = new StringBuilder();
					sb.append('(');
					if (ji.inputs.size() > 0) {
						sb.append(values[ji.inputs.get(0).intValue()].replace(
								'/', '#'));
					}
					for (int i = 1; i < ji.inputs.size(); i++) {
						sb.append(',');
						sb.append(values[ji.inputs.get(i).intValue()].replace(
								'/', '#'));
					}
					sb.append(')');
					String s = sb.toString();
					for (int i = 0; i < ji.outputs.size(); i++) {
						values[ji.outputs.get(i).intValue()] = Fragment
								.normalize(aj.getElement().getId())
								+ s
								+ "."
								+ Integer.toString(i);
					}
				}
			} else if (ji.job instanceof CompositeImmutableJob) {
				CompositeImmutableJob cj = (CompositeImmutableJob) ji.job;
				FragmentLinker fl = p.getLinker(cj.getLinker().getId());
				assert (fl != null);
				List<String> inp2 = new ArrayList<String>(ji.inputs.size());
				for (int i = 0; i < ji.inputs.size(); i++)
					inp2.add(values[ji.inputs.get(i).intValue()]);
				inp2 = fl.convertInputs(inp2);
				DataflowAnalysis dfa = deref.get(ji.job.getAddress());
				dfa.doIt(inp2, p);
				List<String> out2 = dfa.getOutputs();
				out2 = fl.convertOutputs(out2, inp2, "SALAD"); // FIXME
				assert (out2.size() == ji.outputs.size());
				for (int i = 0; i < ji.outputs.size(); i++)
					values[ji.outputs.get(i).intValue()] = out2.get(i);
			}
		}
	}

	public List<String> getOutputs() {
		List<Token> outs = workflow.getOutputPortVariables();
		ArrayList<String> result = new ArrayList<String>(outs.size());
		for (int i = 0; i < outs.size(); i++)
			result.add(values[outs.get(i).intValue()]);
		return result;
	}

	public String getValue(Token address) {
		if (address != null && address.intValue() < values.length)
			return values[address.intValue()];
		else
			return null;
	}

	private static final void makeIndex(List<Port> l, Map<Port, Integer> m) {
		for (int i = 0; i < l.size(); i++)
			m.put(l.get(i), i);
	}
}
