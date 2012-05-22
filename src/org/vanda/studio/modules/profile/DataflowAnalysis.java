package org.vanda.studio.modules.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.util.TokenSource.Token;

// XXX I've had it with all of this instanceof/typecast bullshit
// find a principled solution (e.g., visitor)
public class DataflowAnalysis {
	private final Map<Token, DataflowAnalysis> deref;
	private final String[] values;
	private final ImmutableWorkflow workflow;

	public DataflowAnalysis(ImmutableWorkflow iwf) {
		workflow = iwf;
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

	public List<String> doIt(List<String> inputs, Profiles p) {
		List<String> result = new ArrayList<String>();
		for (JobInfo ji : workflow.getChildren()) {
			if (ji.job instanceof AtomicImmutableJob) {
				AtomicImmutableJob aj = (AtomicImmutableJob) ji.job;
				if (aj.isInputPort()) {
					values[ji.outputs.get(0).intValue()] = inputs
							.get(((InputPort) aj.getElement()).getNumber());
				} else if (aj.isOutputPort()) {
					int n = ((OutputPort) aj.getElement()).getNumber();
					while (n >= result.size())
						result.add(null);
					result.set(n, values[ji.inputs.get(0).intValue()]);
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
						sb.append(values[ji.inputs.get(0).intValue()]);
					}
					for (int i = 1; i < ji.inputs.size(); i++) {
						sb.append(',');
						sb.append(values[ji.inputs.get(i).intValue()]);
					}
					sb.append(')');
					String s = sb.toString();
					for (int i = 0; i < ji.outputs.size(); i++) {
						values[ji.outputs.get(i).intValue()] = aj.getElement()
								.getName() + "#" + Integer.toString(i) + s;
					}
				}
			} else if (ji.job instanceof CompositeImmutableJob) {
				CompositeImmutableJob cj = (CompositeImmutableJob) ji.job;
				FragmentLinker fl = p.getFragmentLinkerMetaRepository()
						.getRepository().getItem(cj.getLinker().getId());
				assert (fl != null);
				List<String> inp2 = new ArrayList<String>(ji.inputs.size());
				for (int i = 0; i < ji.inputs.size(); i++)
					inp2.add(values[ji.inputs.get(i).intValue()]);
				inp2 = fl.convertInputs(inp2);
				List<String> out2 = deref.get(ji.job.getAddress())
						.doIt(inp2, p);
				out2 = fl.convertOutputs(out2);
				assert (out2.size() == ji.outputs.size());
				for (int i = 0; i < ji.outputs.size(); i++)
					values[ji.outputs.get(i).intValue()] = out2.get(i);
			}
		}

		return result;
	}
}
