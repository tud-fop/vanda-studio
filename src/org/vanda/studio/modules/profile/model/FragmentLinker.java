package org.vanda.studio.modules.profile.model;

import java.io.IOException;
import java.util.List;

import org.vanda.studio.model.elements.RepositoryItem;

public interface FragmentLinker extends RepositoryItem {

	List<String> convertInputs(List<String> outer);

	List<String> convertOutputs(List<String> inner, List<String> outerinputs,
			String name);

	Fragment link(String name, FragmentBase fb, FragmentIO io)
			throws IOException;

}
