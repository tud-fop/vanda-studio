package org.vanda.fragment.bash;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vanda.fragment.model.FragmentTool;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.elements.ToolInterface;
import org.vanda.workflows.toolinterfaces.RendererSelector;

// TODO this is going to go
public class ShellTool extends Tool implements FragmentTool {

	private final String id;
	private final String name;
	private final String category;
	private final String version;
	private final String contact;
	private final String description;
	private final List<Port> inPorts;
	private final List<Port> outPorts;
	private final Set<String> imports;
	private final RendererSelector rs;
	private final ToolInterface ti;

	public ShellTool(String id, String name, String category, String version,
			String contact, String description, List<Port> inPorts,
			List<Port> outPorts, Set<String> imports, RendererSelector rs,
			ToolInterface ti) {
		super();
		this.id = id;
		this.name = name;
		this.category = category;
		this.version = version;
		this.contact = contact;
		this.description = description;
		this.inPorts = inPorts;
		this.outPorts = outPorts;
		this.imports = new HashSet<String>(imports);
		this.rs = rs;
		this.ti = ti;
	}

	@Override
	public Type getFragmentType() {
		return Types.shellType;
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
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return rs.selectRenderer(ra);
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
	public ToolInterface getInterface() {
		return ti;
	}

	@Override
	public String getStatus() {
		return "";
	}
}
