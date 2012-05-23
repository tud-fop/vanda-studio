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

public class ShellCompiler implements FragmentCompiler {

	private static void appendVariable(String name, Token t, StringBuilder sb) {
		sb.append(Fragment.normalize(name));
		ImmutableJob.appendVariable(t, sb);
	}

	@Override
	public Fragment compile(String name, ArrayList<JobInfo> jobs,
			ArrayList<String> fragments) {
		StringBuilder sb = new StringBuilder();
		HashSet<String> dependencies = new HashSet<String>();
		sb.append("function ");
		sb.append(Fragment.normalize(name));
		sb.append(" {\n");
		StringBuilder sb2 = new StringBuilder();
		sb2.append("  local");
		boolean flag = false;
		for (int i = 0; i < jobs.size(); i++) {
			for (Token t : jobs.get(i).outputs) {
				sb2.append(' ');
				appendVariable(name, t, sb2);
				flag = true;
			}
		}
		sb2.append('\n');
		if (flag)
			sb.append(sb2.toString());
		for (int i = 0; i < jobs.size(); i++) {
			JobInfo ji = jobs.get(i);
			sb.append("  ");
			if (ji.job.isInputPort()) {
				InputPort ip = (InputPort) ((AtomicImmutableJob) ji.job)
						.getElement();
				appendVariable(name, ji.outputs.get(0), sb);
				sb.append("=\"$");
				sb.append(Integer.toString(ip.getNumber() + 1));
				sb.append('"');
			} else if (ji.job.isOutputPort()) {
				OutputPort op = (OutputPort) ((AtomicImmutableJob) ji.job)
						.getElement();
				sb.append("eval $");
				sb.append(Integer.toString(op.getNumber() + 1));
				sb.append("=\\\"$");
				appendVariable(name, ji.inputs.get(0), sb);
				sb.append("\\\"");
			} else if (ji.job.isChoice()) {
				for (int j = 0; j < ji.inputs.size(); j++) {
					Token var = ji.inputs.get(j);
					if (var != null) {
						appendVariable(name, ji.outputs.get(0), sb);
						sb.append("=\"$");
						appendVariable(name, var, sb);
						sb.append('"');
						break; // <------------------------------#############
					}
				}
			} else if (ji.job instanceof AtomicImmutableJob
					&& ((AtomicImmutableJob) ji.job).getElement() instanceof Literal) {
				Literal lit = (Literal) ((AtomicImmutableJob) ji.job)
						.getElement();
				appendVariable(name, ji.outputs.get(0), sb);
				sb.append("=\"");
				sb.append(lit.getValue());
				sb.append('"');
			} else {
				String frag = fragments.get(i);
				assert (frag != null);
				sb.append(Fragment.normalize(frag));
				for (int j = 0; j < ji.inputs.size(); j++) {
					sb.append(" \"$");
					appendVariable(name, ji.inputs.get(j), sb);
					sb.append('"');
				}
				for (int j = 0; j < ji.outputs.size(); j++) {
					sb.append(' ');
					appendVariable(name, ji.outputs.get(j), sb);
				}
				dependencies.add(frag);
			}
			sb.append('\n');
		}
		sb.append("}\n\n");
		Set<String> im = Collections.emptySet();
		return new Fragment(name, sb.toString(), dependencies, im);
	}

	@Override
	public Type getFragmentType() {
		return Profile.shellType;
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
		return "shell-compiler";
	}

	@Override
	public String getName() {
		return "Shell Fragment Compiler";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		
	}

}
