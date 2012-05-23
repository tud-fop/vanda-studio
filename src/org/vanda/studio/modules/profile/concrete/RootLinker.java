package org.vanda.studio.modules.profile.concrete;

import java.io.IOException;
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
	public Fragment link(String name, FragmentBase fb, FragmentIO io) throws IOException {
		// TODO create a shell script with all imports and fragments
		// along with the main parts
		return null;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
	}

}
