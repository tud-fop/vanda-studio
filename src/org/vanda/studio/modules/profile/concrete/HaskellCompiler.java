package org.vanda.studio.modules.profile.concrete;

import java.util.ArrayList;
import java.util.Set;

import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.util.TokenSource.Token;

public class HaskellCompiler implements FragmentCompiler {

	@Override
	public Fragment compile(String name, ArrayList<JobInfo> jobs,
			ArrayList<Fragment> fragments) {

		ArrayList<Token> outputs = new ArrayList<Token>();
		ArrayList<Token> inputs = new ArrayList<Token>();
		ArrayList<JobInfo> inputJI = new ArrayList<JobInfo>();
		ArrayList<JobInfo> outputJI = new ArrayList<JobInfo>();
		for (JobInfo ji : jobs) {
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

		StringBuilder sb = new StringBuilder();
		Set<String> im = null;
		sb.append(name);
		for (int i = 0; i < inputs.size(); i++) {
			sb.append(' ');
			ImmutableJob.appendVariable(inputs.get(i), sb);
		}
		sb.append(" = ");
		ImmutableJob.appendOutput(outputs, sb);
		sb.append(" where \n");
		for (int i = 0; i < jobs.size(); i++) {
			JobInfo ji = jobs.get(i);
			sb.append("  ");
			if (ji.job.isInputPort() || ji.job.isOutputPort()) {
				// do nothing
			} else if (ji.job.isChoice()) {
				for (int j = 0; j < ji.inputs.size(); j++) {
					Token var = ji.inputs.get(j);
					if (var != null) {
						ImmutableJob.appendVariable(ji.outputs.get(0), sb);
						sb.append(" = ");
						ImmutableJob.appendVariable(var, sb);
						break; // <------------------------------#############
					}
				}
			} else if (ji.job instanceof AtomicImmutableJob
					&& ((AtomicImmutableJob) ji.job).getElement() instanceof Literal) {
				Literal lit = (Literal) ((AtomicImmutableJob) ji.job)
						.getElement();
				ImmutableJob.appendVariable(ji.outputs.get(0), sb);
				sb.append(" = ");
				if ("String".equals(lit.getType())) {
					sb.append('"');
					sb.append(lit.getValue());
					sb.append('"');
				} else
					sb.append(lit.getValue());
			} else {
				Fragment frag = fragments.get(i);
				assert (frag != null);
				ImmutableJob.appendOutput(ji.outputs, sb);
				sb.append(" = ");
				sb.append(frag.name);
				for (int j = 0; j < ji.inputs.size(); j++) {
					sb.append(" ");
					ImmutableJob.appendVariable(ji.inputs.get(j), sb);
				}
			}
			sb.append('\n');
		}
		sb.append('\n');
		return new Fragment(name, sb.toString(), fragments, im);
	}

	@Override
	public Type getFragmentType() {
		return Profile.haskellType;
	}

}
