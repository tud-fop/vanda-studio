package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.app.Profile;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;

public final class IdentityLinker implements Linker {
	
	private static Linker instance = null;
	
	private IdentityLinker() {
		
	}

	@Override
	public void appendActions(List<Action> as) {

	}

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
		return Profile.identityLinker;
	}
	
	public static Linker getInstance() {
		if (instance == null)
			instance = new IdentityLinker();
		return instance;
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
	public void visit(RepositoryItemVisitor v) {
		v.visitLinker(this);
	}

	@Override
	public boolean checkInputTypes(List<Type> outer, List<Type> inner) {
		return true;
	}

	@Override
	public boolean checkOutputTypes(List<Type> outer, List<Type> inner) {
		return true;
	}

	@Override
	public List<Port> convertInputPorts(List<Port> ips) {
		return ips;
	}

	@Override
	public List<Port> convertOutputPorts(List<Port> ops) {
		return ops;
	}

	@Override
	public Type getInnerFragmentType() {
		return Types.genericType;
	}

	@Override
	public Type getFragmentType() {
		return Types.genericType;
	}

}
