package org.vanda.studio.model.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowEvent;
import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowListener;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class CompositeJob extends Job implements WorkflowListener {

	private final Linker linker;

	private MutableWorkflow workflow; // not final because of clone()
	
	private final MultiplexObserver<JobEvent> observable;

	public CompositeJob(Linker linker, MutableWorkflow workflow) {
		address = null;
		this.linker = linker;
		this.workflow = workflow;
		observable = new MultiplexObserver<Job.JobEvent>();
		rebind();
	}

	@Override
	public CompositeJob clone() throws CloneNotSupportedException {
		return new CompositeJob(linker, workflow.clone());
	}

	@Override
	public List<Port> getInputPorts() {
		return linker.convertInputPorts(workflow.getInputPorts());
	}

	@Override
	public List<Port> getOutputPorts() {
		return linker.convertOutputPorts(workflow.getOutputPorts());
	}

	@Override
	public ImmutableJob freeze() throws Exception {
		return new CompositeImmutableJob(address, linker, workflow.freeze());
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
	public Type getFragmentType() {
		return linker.getFragmentType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectBoxRenderer();
	}

	public MutableWorkflow getWorkflow() {
		return workflow;
	}

	@Override
	public void appendActions(List<Action> as) {
		linker.appendActions(as);
	}

	@Override
	public MutableWorkflow dereference(ListIterator<Token> address) {
		return workflow.dereference(address);
	}

	public Linker getLinker() {
		return linker;
	}

	@Override
	public void rebind() {
		workflow.getObservable().addObserver(new Observer<WorkflowEvent>() {

			@Override
			public void notify(WorkflowEvent event) {
				event.doNotify(CompositeJob.this);
			}
			
		});
	}

	@Override
	public RepositoryItem getItem() {
		return linker;
	}

	@Override
	public void visit(JobVisitor v) {
		v.visitCompositeJob(this);
	}

	@Override
	public Observable<JobEvent> getObservable() {
		return observable;
	}

	@Override
	public void inputPortAdded(MutableWorkflow mwf, int index) {
		observable.notify(new Jobs.InputPortAddedEvent(this, index));
	}

	@Override
	public void inputPortRemoved(MutableWorkflow mwf, int index) {
		observable.notify(new Jobs.InputPortRemovedEvent(this, index));
	}

	@Override
	public void outputPortAdded(MutableWorkflow mwf, int index) {
		observable.notify(new Jobs.OutputPortAddedEvent(this, index));
	}

	@Override
	public void outputPortRemoved(MutableWorkflow mwf, int index) {
		observable.notify(new Jobs.OutputPortRemovedEvent(this, index));
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		// ignore
	}

}
