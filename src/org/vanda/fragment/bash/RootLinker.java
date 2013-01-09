package org.vanda.fragment.bash;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.vanda.fragment.model.Fragment;
import org.vanda.fragment.model.FragmentBase;
import org.vanda.fragment.model.FragmentIO;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.types.Type;
import org.vanda.types.Types;

public class RootLinker implements FragmentLinker {

	private static final String BASEPATH = "$OUTPATH";
	private static final String RCPATH = System.getProperty("user.home") + "/.vanda/vandarc";
	
	@Override
	public String getCategory() {
		return "Boxes";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "This box should never be visible to the end user.";
	}

	@Override
	public String getId() {
		return "root-linker";
	}

	@Override
	public String getName() {
		return "Root Box";
	}

	@Override
	public String getVersion() {
		return "n/a";
	}

	@Override
	public List<String> convertInputs(List<String> outer) {
		return outer;
	}

	@Override
	public List<String> convertOutputs(List<String> inner, List<String> l,
			String s) {
		return inner;
	}

	@Override
	public Fragment link(String name, List<Type> outerinput,
			List<Type> innerinput, List<Type> inneroutput,
			List<Type> outeroutput, FragmentBase fb, FragmentIO io)
			throws IOException {
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

		StringBuilder sb = new StringBuilder();
		sb.append("#!/bin/bash\n\nset -e\n\n");
		
		sb.append("source " + RCPATH + "\n");
		// sb.append("if [ -z \"$BASEPATH\" ]\nthen");
		// sb.append("\n    echo \"Path not set\"\n    exit 1\nfi\n\n");

		// source $MYPATH/empty.sh
		// imports.add("empty");
		// source $MYPATH/mapping.sh
		// source $MYPATH/shorten.sh

		for (String imp0rt : imports) {
			sb.append("source ");
			sb.append(imp0rt);
			sb.append("\n");
		}

		sb.append('\n');

		for (String dep : sorted) {
			Fragment fragment = fb.getFragment(dep);
			sb.append(fragment.text);
		}

		/*sb.append("testempty");
		for (String dep : sorted) {
			sb.append(' ');
			sb.append(Fragment.normalize(dep));
		}
		sb.append("\n\n");*/
		sb.append("cd " + BASEPATH + "\n");
		sb.append(Fragment.normalize(name));
		sb.append('\n');

		File f = io.createFile(Fragment.normalize(name));
		FileWriter fw = new FileWriter(f, false);
		fw.write(sb.toString());
		fw.close();
		f.setExecutable(true);
		return new Fragment(name);
	}

	@Override
	public Type getInnerType() {
		return Types.shellType;
	}

}
