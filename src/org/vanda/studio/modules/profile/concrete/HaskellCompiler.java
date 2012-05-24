package org.vanda.studio.modules.profile.concrete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.util.TokenSource.Token;

public class HaskellCompiler implements FragmentCompiler {

	@Override
	public Fragment compile(String name, ImmutableWorkflow iwf,
			ArrayList<String> fragments) {

		StringBuilder sb = new StringBuilder();
		HashSet<String> dependencies = new HashSet<String>();
		sb.append(Fragment.normalize(name));
		for (Token var : iwf.getInputPortVariables()) {
			sb.append(' ');
			ImmutableJob.appendVariable(var, sb);
		}
		sb.append(" = ");
		ImmutableJob.appendOutput(iwf.getOutputPortVariables(), sb);
		sb.append(" where \n");
		int i = 0;
		for (JobInfo ji : iwf.getChildren()) {
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
			i++;
		}
		sb.append('\n');
		Set<String> im = Collections.emptySet();
		return new Fragment(name, sb.toString(), dependencies, im);
	}

	@Override
	public Type getFragmentType() {
		return Types.haskellType;
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
