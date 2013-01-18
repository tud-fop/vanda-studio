package org.vanda.fragment.bash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.vanda.fragment.impl.StaticFragment;
import org.vanda.fragment.model.DataflowAnalysis;
import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentIO;
import org.vanda.fragment.model.Fragments;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

// XXX removed: handle ports (see older versions)
// XXX removed: variables (names are computed statically) (see older versions)
public class ShellCompiler implements FragmentCompiler {

	@Override
	public Fragment compile(String name, DataflowAnalysis dfa,
			ArrayList<Fragment> fragments, FragmentIO fio) {
		StringBuilder sb = new StringBuilder();
		HashSet<String> dependencies = new HashSet<String>();
		Set<String> im = new HashSet<String>();
		MutableWorkflow iwf = dfa.getWorkflow();
		sb.append("function ");
		sb.append(Fragments.normalize(name));
		sb.append(" {\n");
		int i = 0;
		for (Job ji : iwf.getSorted()) {
			if (ji.getElement() instanceof Literal) {
				// nothing to be done
			} else {
				// im.addAll(ji.job.getElement().getImports());
				// ^^ this comes from the fragment
				// ^^ and it is propagated via the dependency
				Fragment frag = fragments.get(i);
				assert (frag != null);
				sb.append("  ");
				sb.append(Fragments.normalize(frag.getId()));
				for (Port ip : frag.getInputPorts()) {
					sb.append(" \"");
					sb.append(fio.findFile(dfa.getValue(ji.bindings.get(ip))));
					sb.append("\"");
				}
				for (Port op : frag.getOutputPorts()) {
					sb.append(" \"");
					sb.append(fio.findFile(dfa.getValue(ji.bindings.get(op))));
					sb.append("\"");
				}
				sb.append('\n');
				dependencies.add(frag.getId());
			}
			i++;
		}
		sb.append("}\n\n");
		return new StaticFragment(name, Fragments.EMPTY_LIST,
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
