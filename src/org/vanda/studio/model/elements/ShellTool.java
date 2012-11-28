package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.util.Action;

public class ShellTool extends Tool {

	private String id;
	private String name;
	private String category;
	private String version;
	private String contact;
	private String description;
	private List<Port> inPorts;
	private List<Port> outPorts;
	
	public ShellTool(String id, String name, String category, String version,
			String contact, String description, List<Port> inPorts,
			List<Port> outPorts) {
		super();
		System.out.println("id: " + id);
		this.id = id;
		this.name = name;
		this.category = category;
		this.version = version;
		this.contact = contact;
		this.description = description;
		this.inPorts = inPorts;
		this.outPorts = outPorts;
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
		return ra.selectAlgorithmRenderer();
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
	public void appendActions(List<Action> as) {
	}

}
