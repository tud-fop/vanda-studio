package org.vanda.studio.modules.profile.fragments;

import java.util.ArrayList;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.types.Type;

public interface FragmentCompiler extends RepositoryItem {

	Fragment compile(String name, DataflowAnalysis dfa,
			ArrayList<String> fragments, FragmentIO app);

	Type getFragmentType();

}
