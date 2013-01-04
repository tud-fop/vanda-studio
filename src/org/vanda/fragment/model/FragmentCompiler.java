package org.vanda.fragment.model;

import java.util.ArrayList;

import org.vanda.types.Type;
import org.vanda.workflows.elements.RepositoryItem;

public interface FragmentCompiler extends RepositoryItem {

	Fragment compile(String name, DataflowAnalysis dfa,
			ArrayList<String> fragments, FragmentIO app);

	Type getFragmentType();

}
