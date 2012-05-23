package org.vanda.studio.modules.profile.model;

import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.JobInfo;
import org.vanda.studio.model.types.Type;

public interface FragmentCompiler extends RepositoryItem {

	Fragment compile(String name, List<Port> inputPorts,
			List<Port> outputPorts, ArrayList<JobInfo> jobs,
			ArrayList<String> fragments);

	Type getFragmentType();

}
