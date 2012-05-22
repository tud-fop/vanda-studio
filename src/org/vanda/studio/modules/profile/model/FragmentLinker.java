package org.vanda.studio.modules.profile.model;

import java.util.List;

import org.vanda.studio.model.elements.RepositoryItem;

public interface FragmentLinker extends RepositoryItem {
	
	List<String> convertInputs(List<String> outer);
	
	List<String> convertOutputs(List<String> inner);
	
	Fragment link(Fragment inner, FragmentIO io);

}
