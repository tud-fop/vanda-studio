package org.vanda.fragment.model;

import java.util.ArrayList;

import org.vanda.types.Type;
import org.vanda.util.RepositoryItem;

public interface FragmentCompiler extends RepositoryItem {

	Fragment compile(String name, DataflowAnalysis dfa,
			ArrayList<Fragment> fragments, FragmentIO app);

	Type getFragmentType();

}
