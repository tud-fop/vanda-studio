package org.vanda.workflows.hyper;

import java.util.List;

import org.vanda.types.Type;
import org.vanda.util.Observable;
import org.vanda.workflows.elements.ElementReturnVisitor;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ElementAdapters.ElementAdapterEvent;

public final class ToolAdapter implements ElementAdapter {
	
	private Tool tool;
	
	public ToolAdapter(Tool tool) {
		this.tool = tool;
	}

	@Override
	public String getCategory() {
		return tool.getCategory();
	}

	@Override
	public String getContact() {
		return tool.getContact();
	}

	@Override
	public String getDescription() {
		return tool.getDescription();
	}

	@Override
	public String getId() {
		return tool.getId();
	}

	@Override
	public String getName() {
		return tool.getName();
	}

	@Override
	public String getVersion() {
		return tool.getVersion();
	}

	@Override
	public Type getFragmentType() {
		return tool.getFragmentType();
	}

	@Override
	public List<Port> getInputPorts() {
		return tool.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return tool.getOutputPorts();
	}

	@Override
	public Observable<ElementAdapterEvent<ElementAdapter>> getObservable() {
		return null;
	}

	@Override
	public void rebind() {

	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return tool.selectRenderer(ra);
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visitTool(tool);
	}

	@Override
	public <R> R visitReturn(ElementReturnVisitor<R> v) {
		return v.visitTool(tool);
	}

}
