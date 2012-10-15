package org.vanda.studio.modules.profile;

import java.io.File;
import java.io.IOException;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Generator;
import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.profile.concrete.RootLinker;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;

public class ProfileImpl implements Profile, FragmentIO {

	private static final String basePath = System.getProperty("user.home") + "/"
			+ ".vanda/output/";
	private final Application app;
	private Profiles prof;
	private FragmentLinker rootLinker;
	
	public ProfileImpl(Application app, Profiles prof) {
		this.app = app;
		this.prof = prof;
		rootLinker = new RootLinker();
	}

	@Override
	public File createFile(String name) throws IOException {
		File result = new File(basePath + name);
		result.createNewFile();
		return result;
	}

	@Override
	public Generator createGenerator() {
		return new GeneratorImpl(app, prof, this, rootLinker);
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
	public Type getRootType() {
		return Types.shellType;
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		
	}

}
