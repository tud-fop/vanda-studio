package org.vanda.studio.modules.profile.concrete;

import java.util.List;

import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;

public class IdentityLinker implements FragmentLinker {

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
		return "This box can be used to contain any kind of subworkflow.";
	}

	@Override
	public String getId() {
		return "identity-linker";
	}

	@Override
	public String getName() {
		return "Identity Box";
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
		return inner;
	}

}
