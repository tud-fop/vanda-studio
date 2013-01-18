package org.vanda.fragment.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.vanda.fragment.model.Fragment;
import org.vanda.workflows.elements.Port;

public final class StaticFragment implements Fragment {
	private final String name;
	private final String text;
	private final List<Port> inPorts;
	private final List<Port> outPorts;
	private final Set<String> dependencies;
	private final Set<String> imports;
	
	// workaround for typing difficulties
	private static final Set<String> EMPTY_SET = Collections.emptySet();

	public StaticFragment(String name, List<Port> inPorts, List<Port> outPorts) {
		this(name, inPorts, outPorts, "", EMPTY_SET, EMPTY_SET);
	}

	public StaticFragment(String name, List<Port> inPorts, List<Port> outPorts,
			Set<String> imports) {
		this(name, inPorts, outPorts, "", EMPTY_SET, imports);
	}

	public StaticFragment(String name, List<Port> inPorts, List<Port> outPorts,
			String text, Set<String> dependencies, Set<String> imports) {
		this.name = name;
		this.inPorts = inPorts;
		this.outPorts = outPorts;
		this.text = text;
		this.dependencies = dependencies;
		this.imports = imports;
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public List<Port> getInputPorts() {
		return inPorts;
	}

	@Override
	public List<Port> getOutputPorts() {
		return outPorts;
	}

	@Override
	public Set<String> getDependencies() {
		return dependencies;
	}

	@Override
	public Set<String> getImports() {
		return imports;
	}

}
