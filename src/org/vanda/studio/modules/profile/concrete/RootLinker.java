package org.vanda.studio.modules.profile.concrete;

import java.io.File;
import java.util.List;

import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.modules.profile.model.Fragment;
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
		return "root-box";
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
	public Fragment link(Fragment inner, FragmentIO io) {
		Fragment result = inner.compose();
		String filename = io.makeUnique(inner.name);
		File f = io.createFile(filename);
		f.notify();
		return result;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
	}

}
