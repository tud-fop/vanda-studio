package org.vanda.studio.modules.profile;

import java.io.File;

import org.vanda.studio.app.Generator;
import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.concrete.RootLinker;
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

	@Override
	public Type getRootType() {
		return Profile.shellType;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		
	}

}
