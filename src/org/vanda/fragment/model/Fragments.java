package org.vanda.fragment.model;

import java.util.Collections;
import java.util.List;

import org.vanda.workflows.elements.Port;

public final class Fragments {

	public static final List<Port> EMPTY_LIST = Collections.emptyList();

	public static String normalize(String name) {
		return name.replace('$', '_').replace(' ', '_');
	}

}
