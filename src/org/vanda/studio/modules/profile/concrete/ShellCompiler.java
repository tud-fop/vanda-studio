package org.vanda.studio.modules.profile.concrete;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.profile.fragments.DataflowAnalysis;
import org.vanda.studio.modules.profile.fragments.Fragment;
import org.vanda.studio.modules.profile.fragments.FragmentCompiler;
import org.vanda.studio.modules.profile.fragments.FragmentIO;

// XXX removed: handle ports (see older versions)
// XXX removed: variables (names are computed statically) (see older versions)
public class ShellCompiler implements FragmentCompiler {

	@Override
	public Fragment compile(String name, DataflowAnalysis dfa,
			ArrayList<String> fragments, FragmentIO fio) {
		StringBuilder sb = new StringBuilder();
		HashSet<String> dependencies = new HashSet<String>();
		Set<String> im = new HashSet<String>();
		ImmutableWorkflow iwf = dfa.getWorkflow();
		sb.append("function ");
		sb.append(Fragment.normalize(name));
		sb.append(" {\n");
		int i = 0;
		for (JobInfo ji : iwf.getChildren()) {
			if (ji.job instanceof AtomicImmutableJob) {
				if (((AtomicImmutableJob) ji.job).getElement() instanceof Literal) {
					// nothing to be done
				} else {
					Element e = ((AtomicImmutableJob) ji.job).getElement();
					im.addAll(e.getImports());
					String frag = fragments.get(i);
					assert (frag != null);
					sb.append("  ");
					sb.append(Fragment.normalize(frag));
					for (int j = 0; j < ji.inputs.size(); j++) {
						sb.append(" \"");
						sb.append(fio.findFile(dfa.getValue(ji.inputs.get(j))));
						sb.append("\"");
					}
					for (int j = 0; j < ji.outputs.size(); j++) {
						sb.append(" \"");
						sb.append(fio.findFile(dfa.getValue(ji.outputs.get(j))));
						sb.append("\"");
					}
					sb.append('\n');
					dependencies.add(frag);
				}
			}
			i++;
		}
		sb.append("}\n\n");
		return new Fragment(name, sb.toString(), dependencies, im);
	}

	@Override
	public Type getFragmentType() {
		return Types.shellType;
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
