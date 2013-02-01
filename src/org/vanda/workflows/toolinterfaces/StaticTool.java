package org.vanda.workflows.toolinterfaces;

import java.util.List;

import org.vanda.types.Type;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.elements.ToolInterface;

public final class StaticTool implements Tool {
	private final String id;
	private final String name;
	private final String description;
	private final String version;
	private final String category;
	private final String contact;
	private final RendererSelector rs;
	private final List<Port> inPorts;
	private final List<Port> outPorts;
	private final String status;
	private final Type fragmentType;
	private final ToolInterface ti;
	
	public StaticTool(String id, String name, String category, String version,
			String contact, String description, List<Port> inPorts,
			List<Port> outPorts, RendererSelector rs, Type fragmentType,
			String status, ToolInterface ti) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.version = version;
		this.category = category;
		this.contact = contact;
		this.rs = rs;
		this.inPorts = inPorts;
		this.outPorts = outPorts;
		this.fragmentType = fragmentType;
		this.status = status;
		this.ti = ti;
	}

	@Override
	public Type getFragmentType() {
		return fragmentType;
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
	public ToolInterface getInterface() {
		return ti;
	}

	@Override
	public String getStatus() {
		return status;
	}
	
}
