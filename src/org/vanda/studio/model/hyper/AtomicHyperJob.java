package org.vanda.studio.model.hyper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.workflows.AtomicJob;
import org.vanda.studio.model.workflows.InputPort;
import org.vanda.studio.model.workflows.Job;
import org.vanda.studio.model.workflows.OutputPort;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;
import org.vanda.studio.util.Action;

public class AtomicHyperJob<V, I extends ToolInstance> extends HyperJob<V> {
	private final Tool<V, I> tool;

	private I toolInstance;

	public AtomicHyperJob(Tool<V, I> tool) {
		this(tool, tool.createInstance());
	}

	public AtomicHyperJob(Tool<V, I> tool, I toolInstance) {
		this.tool = tool;
		this.toolInstance = toolInstance;
	}
	
	public static <V, I extends ToolInstance> AtomicHyperJob<V, I> create(Tool<V, I> tool) {
		return new AtomicHyperJob<V, I>(tool);
	}
	
	@Override
	public AtomicHyperJob<V, I> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		AtomicHyperJob<V, I> cl = (AtomicHyperJob<V, I>) super.clone();
		cl.toolInstance = tool.createInstance();
		Map<String, Object> map = new HashMap<String,Object>();
		toolInstance.saveToMap(map);
		cl.toolInstance.loadFromMap(map);
		return cl;
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
	public boolean isInputPort() {
		return tool instanceof InputPort;
	}

	@Override
	public boolean isOutputPort() {
		return tool instanceof OutputPort;
	}

	@Override
	public List<Job<V>> unfold() {
		return Collections.singletonList((Job<V>) new AtomicJob<V, I>(tool,
				toolInstance, this));
	}

	@Override
	public Class<V> getViewType() {
		return tool.getViewType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return tool.selectRenderer(ra);
	}

	@Override
	public String getName() {
		return tool.getName();
	}

	@Override
	public void appendActions(List<Action> as) {
		tool.appendActions(as);
	}
}
