package org.vanda.fragment.model;

import java.io.IOException;
import java.util.List;

import org.vanda.types.Type;
import org.vanda.util.RepositoryItem;

public interface FragmentLinker extends RepositoryItem {

	List<String> convertInputs(List<String> outer);

	List<String> convertOutputs(List<String> inner, List<String> outerinputs,
			String name);

	Fragment link(String name, List<Type> outerinput, List<Type> innerinput,
			List<Type> inneroutput, List<Type> outeroutput, FragmentBase fb,
			FragmentIO io) throws IOException;
	
	Type getInnerType();

}
