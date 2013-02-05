package org.vanda.fragment.bash;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vanda.fragment.model.Fragment;
import org.vanda.util.RepositoryItem;
import org.vanda.workflows.elements.Port;

public class ShellTool implements Fragment, RepositoryItem {

	private final String id;
	private final String name;
	private final String category;
	private final String version;
	private final String contact;
	private final String description;
	private final Set<String> imports;
	private final List<Port> inPorts;
	private final List<Port> outPorts;

	public ShellTool(String id, String name, String category, String version,
			String contact, String description, Set<String> imports,
			List<Port> inPorts, List<Port> outPorts) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.version = version;
		this.contact = contact;
		this.description = description;
		this.imports = new HashSet<String>(imports);
		this.inPorts = inPorts;
		this.outPorts = outPorts;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getContact() {
		return contact;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public Set<String> getImports() {
		return imports;
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
	public String getText() {
		return "";
	}

	@Override
	public Set<String> getDependencies() {
		return Collections.emptySet();
	}

}
