package org.vanda.studio.modules.profile;

import java.util.List;

import org.vanda.studio.model.elements.RepositoryItem;

public interface FragmentLinker extends RepositoryItem {
	
	List<String> convertInputs(List<String> outer);
	List<String> convertOutputs(List<String> inner);

}
