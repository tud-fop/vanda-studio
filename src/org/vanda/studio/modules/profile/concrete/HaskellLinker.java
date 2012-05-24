package org.vanda.studio.modules.profile.concrete;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.modules.profile.model.Fragment;
import org.vanda.studio.modules.profile.model.FragmentBase;
import org.vanda.studio.modules.profile.model.FragmentIO;
import org.vanda.studio.modules.profile.model.FragmentLinker;

public class HaskellLinker implements FragmentLinker {

	@Override
	public String getCategory() {
		return "Linkers";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public String getId() {
		return "haskell-linker";
	}

	@Override
	public String getName() {
		return "Haskell Linker";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
	}

	@Override
	public List<String> convertInputs(List<String> outer) {
		return outer;
	}

	@Override
	public List<String> convertOutputs(List<String> inner,
			List<String> outerinputs, String name) {
		List<String> result = new ArrayList<String>(inner.size());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		if (outerinputs.size() > 0) {
			sb.append(outerinputs.get(0));
		}
		for (int i = 1; i < outerinputs.size(); i++) {
			sb.append(',');
			sb.append(outerinputs.get(i));
		}
		sb.append(')');
		String s = sb.toString();
		for (int i = 0; i < inner.size(); i++) {
			result.add(name + "#" + Integer.toString(i) + s);
		}
		return result;
	}

	@Override
	public Fragment link(String name, FragmentBase fb, FragmentIO io)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
