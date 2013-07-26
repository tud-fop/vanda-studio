package org.vanda.fragment.bash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.vanda.execution.model.ExecutableJob;
import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.fragment.impl.StaticFragment;
//import org.vanda.fragment.model.DataflowAnalysis;
import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentIO;
import org.vanda.fragment.model.Fragments;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.workflows.data.DataflowAnalysis;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;

// XXX removed: handle ports (see older versions)
// XXX removed: variables (names are computed statically) (see older versions)
public class ShellCompiler implements FragmentCompiler {

	@Override
	public Fragment compile(String name, 
			ExecutableWorkflow ewf, 
//			final DataflowAnalysis dfa,
			ArrayList<Fragment> fragments, final FragmentIO fio) {
		String nname = DataflowAnalysis.normalize(name);
		final StringBuilder sb = new StringBuilder();
		final HashSet<String> dependencies = new HashSet<String>();
		Set<String> im = new HashSet<String>();
		sb.append("function ");
		sb.append(nname);
		sb.append(" {\n");
		int i = 0;
//		for (final Job ji : dfa.getSorted()) {
		for (final ExecutableJob ji : ewf.getSortedJobs()) {
			final Fragment frag = fragments.get(i);
			ji.visit(new ElementVisitor() {

				@Override
				public void visitLiteral(Literal l) {
					// nothing to be done
				}

				@Override
				public void visitTool(Tool t) {
					// im.addAll(ji.job.getElement().getImports());
					// ^^ this comes from the fragment
					// ^^ and it is propagated via the dependency
					assert (frag != null);
					sb.append("  run ");
					sb.append(frag.getInputPorts().size());
					sb.append(' ');
					sb.append(frag.getId());
					sb.append(' ');
//					sb.append(dfa.getRootDir(t));
					sb.append(ji.getToolPrefix());
					for (Port ip : frag.getInputPorts()) {
						sb.append(" \"");
//						sb.append(fio.findFile(dfa.getValue(ji.bindings.get(ip))));
						sb.append(fio.findFile(ji.getValuedBinding(ip).getValue()));
						sb.append('\"');
					}
					for (Port op : frag.getOutputPorts()) {
						sb.append(" \"");
//						sb.append(fio.findFile(dfa.getValue(ji.bindings.get(op))));
						sb.append(fio.findFile(ji.getValuedBinding(op).getValue()));
						sb.append('\"');
					}
					sb.append('\n');
					dependencies.add(frag.getId());
				}
				
			});
			i++;
		}
		sb.append("}\n\n");
		return new StaticFragment(nname, Fragments.EMPTY_LIST,
				Fragments.EMPTY_LIST, sb.toString(), dependencies, im);
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

}
