package org.vanda.studio.model.hyper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.workflows.CompositeJob;
import org.vanda.studio.model.workflows.Job;
import org.vanda.studio.model.workflows.JobWorkflow;
import org.vanda.studio.model.workflows.Linker;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.model.workflows.ToolInstance;
import org.vanda.studio.util.Action;

public class CompositeHyperJob<IF, V, IV, I extends ToolInstance> extends HyperJob<V> {

	private I instance;
	
	private final Linker<IF, V, I> linker;

	private HyperWorkflow<IF, IV> workflow; // not final because of clone()

	public CompositeHyperJob(Linker<IF, V, I> linker, I instance,
			HyperWorkflow<IF, IV> workflow) {
		this.linker = linker;
		this.instance = instance;
		this.workflow = workflow;
	}
	
	@Override
	public CompositeHyperJob<IF, V, IV, I> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		CompositeHyperJob<IF, V, IV, I> cl = (CompositeHyperJob<IF, V, IV, I>) super.clone();
		cl.instance = linker.createInstance();
		Map<String, Object> map = new HashMap<String,Object>();
		instance.saveToMap(map);
		cl.instance.loadFromMap(map);
		cl.workflow = workflow.clone();
		return cl;
	}

	@Override
	public List<Port> getInputPorts() {
		return workflow.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return workflow.getOutputPorts();
	}

	@Override
	public List<Job<V>> unfold() {
		List<JobWorkflow<IF, IV>> workflows = workflow.unfold();
		List<Job<V>> jobs = new LinkedList<Job<V>>();
		for (JobWorkflow<IF, IV> w : workflows)
			jobs.add(new CompositeJob<IF, V, IV, I>(linker, instance, w, this));
		return jobs;
	}

	@Override
	public boolean isInputPort() {
		return false;
	}

	@Override
	public boolean isOutputPort() {
		return false;
	}

	@Override
	public Class<V> getViewType() {
		return linker.getViewType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectAlgorithmRenderer();
	}

	@Override
	public String getName() {
		return "";
	}
	
	public HyperWorkflow<IF, IV> getWorkflow() {
		return workflow;
	}

	@Override
	public void appendActions(List<Action> as) {
		linker.appendActions(as);
	}

}
