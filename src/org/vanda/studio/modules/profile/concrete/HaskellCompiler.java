package org.vanda.studio.modules.profile.concrete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
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
			ArrayList<String> fragments) {

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
		HashSet<String> dependencies = new HashSet<String>();
		sb.append(Fragment.normalize(name));
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
				String frag = fragments.get(i);
				assert (frag != null);
				ImmutableJob.appendOutput(ji.outputs, sb);
				sb.append(" = ");
				sb.append(Fragment.normalize(name));
				for (int j = 0; j < ji.inputs.size(); j++) {
					sb.append(" ");
					ImmutableJob.appendVariable(ji.inputs.get(j), sb);
				}
				dependencies.add(frag);
			}
			sb.append('\n');
		}
		sb.append('\n');
		Set<String> im = Collections.emptySet();
		return new Fragment(name, sb.toString(), dependencies, im);
	}

	@Override
	public Type getFragmentType() {
		return Profile.haskellType;
	}

	@Override
	public String getCategory() {
		return "Fragment Compilers";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getId() {
		return "haskell-compiler";
	}

	@Override
	public String getName() {
		return "Haskell Fragment Compiler";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		
	}

}
