package org.vanda.studio.modules.profile;

import java.io.File;
import java.util.List;

import org.vanda.studio.app.Generator;
import org.vanda.studio.app.Profile;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;

public class ProfileImpl implements Profile {
	
	public class FragmentIOImpl implements FragmentIO {

		@Override
		public String makeUnique(String prefix) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public File createFile(String name) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public class RootLinker implements FragmentLinker {

		@Override
		public String getCategory() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContact() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getVersion() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> convertInputs(List<String> outer) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> convertOutputs(List<String> inner) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Fragment link(Fragment inner, FragmentIO io) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private Profiles prof;
	private FragmentIO io;
	private FragmentLinker rootLinker;
	
	public ProfileImpl(Profiles prof) {
		this.prof = prof;
		io = new FragmentIOImpl();
		rootLinker = new RootLinker();
	}

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
	public Generator createGenerator() {
		return new GeneratorImpl(prof, io, rootLinker);
	}

}
