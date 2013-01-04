package org.vanda.fragment.impl;

import java.util.Iterator;

import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.fragment.model.Profile;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.CompositeRepository;
import org.vanda.util.MetaRepository;
import org.vanda.workflows.elements.RepositoryItemVisitor;

public final class ProfileImpl implements Profile {

	private final MetaRepository<FragmentCompiler> fragmentCompilers;
	private final MetaRepository<FragmentLinker> fragmentLinkers;

	public ProfileImpl() {
		fragmentCompilers = new CompositeRepository<FragmentCompiler>();
		fragmentLinkers = new CompositeRepository<FragmentLinker>();
	}

	@Override
	public MetaRepository<FragmentCompiler> getFragmentCompilerMetaRepository() {
		return fragmentCompilers;
	}

	@Override
	public MetaRepository<FragmentLinker> getFragmentLinkerMetaRepository() {
		return fragmentLinkers;
	}

	@Override
	public FragmentCompiler getCompiler(Type t) {
		// XXX optimization: do not compute a fresh copy of t in each iteration
		FragmentCompiler result = null;
		Iterator<FragmentCompiler> it = fragmentCompilers.getRepository()
				.getItems().iterator();
		while (it.hasNext() && result == null) {
			FragmentCompiler fc = it.next();
			if (Types.canUnify(t, fc.getFragmentType()))
				result = fc;
		}
		return result;
	}

	@Override
	public FragmentLinker getLinker(String id) {
		return fragmentLinkers.getRepository().getItem(id);
	}

	@Override
	public FragmentLinker getRootLinker(Type t) {
		// XXX optimization: do not compute a fresh copy of t in each iteration
		FragmentLinker result = null;
		Iterator<FragmentLinker> it = fragmentLinkers.getRepository()
				.getItems().iterator();
		while (it.hasNext() && result == null) {
			FragmentLinker fc = it.next();
			if (Types.canUnify(t, fc.getInnerType()))
				result = fc;
		}
		return result;
	}

	@Override
	public Type getRootType() {
		return Types.shellType;
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
	public void visit(RepositoryItemVisitor v) {

	}

}
