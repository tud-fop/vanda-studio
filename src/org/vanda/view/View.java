package org.vanda.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.view.Views.*;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;
import org.vanda.workflows.hyper.Workflows.*;

/**
 * Holds ViewObject for the Workflow, Jobs, Locations and Cells. ViewObjects are
 * only weakly referenced.
 * 
 * @author kgebhardt
 * 
 */
public class View implements WorkflowListener<MutableWorkflow> {

	private WeakHashMap<ConnectionKey, ConnectionView> connections;
	private WeakHashMap<Job, JobView> jobs;
	private MultiplexObserver<ViewEvent<View>> observable;
	private WeakHashMap<Location, LocationView> variables;

	private final ViewListener<AbstractView<?>> viewEventListener;

	private MutableWorkflow workflow;
	private final Observer<WorkflowEvent<MutableWorkflow>> workflowObserver = new Observer<WorkflowEvent<MutableWorkflow>>() {

		@Override
		public void notify(WorkflowEvent<MutableWorkflow> event) {
			event.doNotify(View.this);
		}
	};
	private WorkflowView workflowView;

	public View(MutableWorkflow workflow) {
		this.workflow = workflow;
		this.observable = new MultiplexObserver<ViewEvent<View>>();
		jobs = new WeakHashMap<Job, JobView>();
		connections = new WeakHashMap<ConnectionKey, ConnectionView>();
		variables = new WeakHashMap<Location, LocationView>();
		viewEventListener = new ViewListener<AbstractView<?>>() {

			@Override
			public void highlightingChanged(AbstractView<?> v) {
			}

			@Override
			public void markChanged(AbstractView<?> v) {
				observable.notify(new MarkChangedEvent<View>(View.this));
			}

			@Override
			public void selectionChanged(AbstractView<?> v) {
				// TODO this will cause multiple notifications for one selection
				// change
				observable.notify(new SelectionChangedEvent<View>(View.this));
			}

		};

		workflowView = new WorkflowView(viewEventListener);

		workflow.getObservable().addObserver(workflowObserver);
	}

	public <T, T2 extends AbstractView<T>> void addMarked(WeakHashMap<T, T2> whm, List<AbstractView<?>> marked) {
		for (T2 v : whm.values())
			if (v.isMarked())
				marked.add(v);
	}

	public <T, T2 extends AbstractView<T>> void addSelected(WeakHashMap<T, T2> whm, List<SelectionObject> selection) {
		for (Map.Entry<T, T2> e : whm.entrySet())
			if (e.getValue().isSelected())
				selection.add(e.getValue().createSelectionObject(e.getKey()));
	}

	public void clearMarked() {
		clearMarked(jobs);
		clearMarked(connections);
		clearMarked(variables);
	}

	public <T, T2 extends AbstractView<T>> void clearMarked(WeakHashMap<T, T2> whm) {
		for (T2 v : whm.values())
			v.setMarked(false);
	}

	public <T, T2 extends AbstractView<T>> void clearSelected(WeakHashMap<T, T2> whm) {
		for (T2 v : whm.values())
			v.setSelected(false);
	}

	public void clearSelection() {
		clearSelected(jobs);
		clearSelected(connections);
		clearSelected(variables);
	}

	public ConnectionView getConnectionView(ConnectionKey ck) {
		ConnectionView cv = connections.get(ck);
		if (cv == null) {
			cv = new ConnectionView(viewEventListener);
			connections.put(ck, cv);
		}
		return cv;
	}

	public List<SelectionObject> getCurrentSelection() {
		List<SelectionObject> result = new ArrayList<SelectionObject>();
		addSelected(connections, result);
		addSelected(variables, result);
		addSelected(jobs, result);
		if (workflowView.isSelected())
			result.add(workflowView.createSelectionObject(workflow));
		return result;
	}

	public JobView getJobView(Job job) {
		JobView jv = jobs.get(job);
		if (jv == null) {
			jv = new JobView(viewEventListener);
			jobs.put(job, jv);
		}
		return jv;
	}

	public LocationView getLocationView(Location l) {
		LocationView lv = variables.get(l);
		if (lv == null) {
			lv = new LocationView(viewEventListener);
			variables.put(l, lv);
		}
		return lv;
	}

	public List<AbstractView<?>> getMarked() {
		List<AbstractView<?>> markedViews = new ArrayList<AbstractView<?>>();
		addMarked(connections, markedViews);
		addMarked(variables, markedViews);
		addMarked(jobs, markedViews);
		return markedViews;
	}

	public MultiplexObserver<ViewEvent<View>> getObservable() {
		return observable;
	}

	public MutableWorkflow getWorkflow() {
		return workflow;
	}

	public WorkflowView getWorkflowView() {
		return workflowView;
	}

	public void removeSelectedCell() {
		List<SelectionObject> selection = getCurrentSelection();
		for (SelectionObject so : selection) {
			so.remove(workflow);
		}
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
		// do nothing
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
		// do nothing
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
		getJobView(j).setSelected(false);
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
		// do nothing
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
		getConnectionView(cc).setSelected(false);
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		// do nothing
	}

	@Override
	public void updated(MutableWorkflow mwf) {
		// do nothing
	}

}
