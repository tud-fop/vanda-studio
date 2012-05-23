package org.vanda.studio.modules.profile.concrete;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentBase;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;

public class RootLinker implements FragmentLinker {

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
	public List<String> convertOutputs(List<String> inner) {
		return inner;
	}

	@Override
	public Fragment link(String name, FragmentBase fb, FragmentIO io)
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
		sb.append("#!/bin/bash\n\nset -e\n\nif [ -z \"$MYPATH\" ]\nthen\n    echo \"Path not set\"\n    exit 1\nfi\n\n");
		
		// source $MYPATH/empty.sh
		imports.add("empty");
		// source $MYPATH/mapping.sh
		// source $MYPATH/shorten.sh
		
		for (String imp0rt : imports) {
			sb.append("source $MYPATH/");
			sb.append(imp0rt);
			sb.append(".sh\n");
		}
		
		sb.append('\n');

		for (String dep : sorted) {
			Fragment fragment = fb.getFragment(dep);
			sb.append(fragment.text);
		}
		
		sb.append("testempty");
		for (String dep : sorted) {
			sb.append(' ');
			sb.append(Fragment.normalize(dep));
		}
		sb.append("\n\n");
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
	public void visit(RepositoryItemVisitor v) {
	}

}
