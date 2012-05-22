package org.vanda.studio.modules.profile;

import org.vanda.studio.app.Profile;
import org.vanda.studio.model.immutable.ImmutableWorkflow;

public class FragmentProfile implements Profile {

	@Override
	public String getCategory() {
		return "profiles";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Generates code using simple compositional fragments";
	}

	@Override
	public String getId() {
		return "fragment-profile";
	}

	@Override
	public String getName() {
		return "Fragment Profile";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void generate(ImmutableWorkflow wf) {
		// TODO Auto-generated method stub

	}

}
