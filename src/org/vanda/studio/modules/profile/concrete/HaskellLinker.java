package org.vanda.studio.modules.profile.concrete;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentBase;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;

public class HaskellLinker implements FragmentLinker {

	@Override
	public String getCategory() {
		return "Linkers";
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
		return "haskell-linker";
	}

	@Override
	public String getName() {
		return "Haskell Linker";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
	}

	@Override
	public List<String> convertInputs(List<String> outer) {
		return outer; // TODO change this for dataflow analysis to work
	}

	@Override
	public List<String> convertOutputs(List<String> inner,
			List<String> outerinputs, String name) {
		List<String> result = new ArrayList<String>(inner.size());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		if (outerinputs.size() > 0) {
			sb.append(outerinputs.get(0));
		}
		for (int i = 1; i < outerinputs.size(); i++) {
			sb.append(',');
			sb.append(outerinputs.get(i));
		}
		sb.append(')');
		String s = sb.toString();
		for (int i = 0; i < inner.size(); i++) {
			result.add(name + "#" + Integer.toString(i) + s);
		}
		return result;
	}

	@Override
	public Fragment link(String name, List<Type> outerinput,
			List<Type> innerinput, List<Type> inneroutput,
			List<Type> outeroutput, FragmentBase fb, FragmentIO io)
			throws IOException {
		// convert fragment into a real Haskell program
		assert (outerinput.size() == innerinput.size());
		assert (inneroutput.size() == outeroutput.size());
		ArrayList<String> inputConverters = new ArrayList<String>(
				outerinput.size());
		for (int i = 0; i < outerinput.size(); i++) {
			Tool tool = fb.getConversionTool(outerinput.get(i),
					innerinput.get(i));
			assert (tool != null);
			inputConverters.add(tool.getName());
		}
		ArrayList<String> outputConverters = new ArrayList<String>(
				inneroutput.size());
		for (int i = 0; i < inneroutput.size(); i++) {
			Tool tool = fb.getConversionTool(inneroutput.get(i),
					outeroutput.get(i));
			assert (tool != null);
			outputConverters.add(tool.getName());
		}
		// XXX copied verbatim from RootLinker (improve)
		HashSet<String> dependencies = new HashSet<String>();
		HashSet<String> imports = new HashSet<String>();
		LinkedList<String> queue = new LinkedList<String>();
		queue.add(name);
		while (!queue.isEmpty()) {
			String s = queue.pop();
			if (!dependencies.contains(s)) {
				Fragment fragment = fb.getFragment(s);
				dependencies.add(s);
				queue.addAll(fragment.dependencies);
				imports.addAll(fragment.imports);
			}
		}

		ArrayList<String> sorted = new ArrayList<String>(dependencies);
		Collections.sort(sorted);

		// from here on, not-so-verbatim, but still very similar
		StringBuilder sb = new StringBuilder();
		sb.append("module Main where\n\n");

		imports.add("Vanda.Functions");
		imports.add("System.Environment ( getArgs )");
		for (String imp0rt : imports) {
			sb.append("import ");
			sb.append(imp0rt);
			sb.append("\n");
		}
		sb.append('\n');

		for (String dep : sorted) {
			Fragment fragment = fb.getFragment(dep);
			sb.append(fragment.text);
		}

		sb.append("\nmain :: IO ()\nmain = do\n");
		sb.append("  args <- getArgs\n  case args of\n");
		sb.append("    [");
		int i = 0;
		for (; i < outerinput.size(); i++) {
			if (i > 0)
				sb.append(", ");
			sb.append("input");
			sb.append(Integer.toString(i + 1));
		}
		for (int j = 0; j < outeroutput.size(); j++) {
			if (i > 0 || j > 0)
				sb.append(", ");
			sb.append("output");
			sb.append(Integer.toString(j + 1));
		}
		sb.append("] -> do\n");
		for (i = 0; i < outerinput.size(); i++) {
			sb.append("      x");
			sb.append(Integer.toString(i + 1));
			sb.append(" <- ");
			sb.append(inputConverters.get(i));
			sb.append(' ');
			sb.append("input");
			sb.append(Integer.toString(i + 1));
			sb.append('\n');
		}
		sb.append("      let ");
		if (inneroutput.size() != 1)
			sb.append('(');
		for (i = 0; i < inneroutput.size(); i++) {
			if (i > 0)
				sb.append(", ");
			sb.append("y");
			sb.append(Integer.toString(i + 1));
		}
		if (inneroutput.size() != 1)
			sb.append(')');
		sb.append(" = ");
		sb.append(Fragment.normalize(name));
		for (i = 0; i < innerinput.size(); i++) {
			sb.append(" x");
			sb.append(Integer.toString(i + 1));
		}
		sb.append('\n');
		for (i = 0; i < outeroutput.size(); i++) {
			sb.append("      ");
			sb.append(outputConverters.get(i));
			sb.append(' ');
			sb.append("y");
			sb.append(Integer.toString(i + 1));
			sb.append(" output");
			sb.append(Integer.toString(i + 1));
			sb.append('\n');
		}
		sb.append("    _ -> putStrLn \"Wrong number of parameters.\"");
		String hsname = Fragment.normalize(name + ".hs");
		File f = io.createFile(hsname);
		FileWriter fw = new FileWriter(f, false);
		fw.write(sb.toString());
		fw.close();

		sb = new StringBuilder();
		sb.append("(");
		for (i = 0; i < outerinput.size(); i++) {
			if (i > 0)
				sb.append(',');
			sb.append("$");
			sb.append(Integer.toString(i + 1));
		}
		sb.append(")");
		String args = sb.toString();

		// make shell fragment
		sb = new StringBuilder();
		sb.append("function ");
		sb.append(hsname);
		sb.append(" {\n");
		if (!outeroutput.isEmpty()) {
		sb.append("  local");
		for (i = 0; i < outeroutput.size(); i++) {
			sb.append(' ');
			sb.append(Fragment.normalize(name));
			sb.append(i + 1);
		}
		sb.append('\n');
		}
		for (i = 0; i < outeroutput.size(); i++) {
			sb.append("  ");
			sb.append(Fragment.normalize(name));
			sb.append(i + 1);
			sb.append("=\"");
			sb.append(hsname);
			// sb.append('#');
			sb.append(Integer.toString(i+1));
			sb.append(args);
			sb.append("\"\n");
			sb.append("  eval $");
			sb.append(Integer.toString(outerinput.size() + i + 1));
			sb.append("=\\\"$");
			sb.append(Fragment.normalize(name));
			sb.append(i + 1);
			sb.append("\\\"\n");
		}
		sb.append("  ghc --make ");
		sb.append(hsname);
		sb.append(" &> /dev/null\n  ./");
		sb.append(Fragment.normalize(name));
		for (i = 0; i < outerinput.size(); i++) {
			sb.append(" \"$");
			sb.append(Integer.toString(i + 1));
			sb.append('"');
		}
		for (i = 0; i < outeroutput.size(); i++) {
			sb.append(" \"$");
			sb.append(Fragment.normalize(name));
			sb.append(Integer.toString(i + 1));
			sb.append("\"");
		}
		sb.append("\n}\n\n");

		Set<String> im = Collections.emptySet();
		return new Fragment(hsname, sb.toString(), im, im);
	}

}
