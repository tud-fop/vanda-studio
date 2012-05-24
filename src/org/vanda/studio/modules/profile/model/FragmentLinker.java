package org.vanda.studio.modules.profile.model;

import java.io.IOException;
import java.util.List;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.types.Type;

public interface FragmentLinker extends RepositoryItem {

	List<String> convertInputs(List<String> outer);

	List<String> convertOutputs(List<String> inner, List<String> outerinputs,
			String name);

	Fragment link(String name, List<Type> outerinput, List<Type> innerinput,
			List<Type> inneroutput, List<Type> outeroutput, FragmentBase fb,
			FragmentIO io) throws IOException;

}
