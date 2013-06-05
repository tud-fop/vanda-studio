package org.vanda.view;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.view.AbstractView.ViewListener;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

/**
 * 
 * @author kgebhardt
 * 
 */
public class View {
	public static interface GlobalViewEvent<V> {
		void doNotify(GlobalViewListener<V> vl);
	}

	public static interface GlobalViewListener<V> {
		void markChanged(V v);

		void selectionChanged(V v);
	}

	public static class MarkChangedEvent<V> implements GlobalViewEvent<V> {
		private final V v;

		public MarkChangedEvent(V v) {
			this.v = v;
		}

		@Override
		public void doNotify(GlobalViewListener<V> vl) {
			vl.markChanged(v);
		}
	}

	public static class SelectionChangedEvent<V> implements GlobalViewEvent<V> {
		private final V v;

		public SelectionChangedEvent(V v) {
			this.v = v;
		}

		@Override
		public void doNotify(GlobalViewListener<V> vl) {
			vl.selectionChanged(v);
		}
	}

	public class WorkflowListener implements
			Workflows.WorkflowListener<MutableWorkflow> {

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			addJobView(j);
			for (Port p : j.getOutputPorts()) {
				addLocationView(j.bindings.get(p));
			}
		}

		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
			// do nothing
		}

		@Override
		public void childRemoved(MutableWorkflow mwf, Job j) {
			if (j.bindings != null)
				for (Location l : j.bindings.values())
					variables.remove(l);
			jobs.remove(j);
		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
			addConnectionView(cc);
		}

		@Override
		public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
			connections.remove(cc);
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

	WeakHashMap<ConnectionKey, ConnectionView> connections;
	WeakHashMap<Job, JobView> jobs;
	private MultiplexObserver<GlobalViewEvent<View>> observable;
	WeakHashMap<Location, LocationView> variables;

	ViewListener<AbstractView> viewEventListener;

	MutableWorkflow workflow;

	private WorkflowListener workflowListener;

	private WorkflowView workflowView;

	public View(MutableWorkflow workflow) {
		this.workflow = workflow;
		this.observable = new MultiplexObserver<GlobalViewEvent<View>>();
		jobs = new WeakHashMap<Job, JobView>();
		connections = new WeakHashMap<ConnectionKey, ConnectionView>();
		variables = new WeakHashMap<Location, LocationView>();
		workflowListener = new WorkflowListener();
		viewEventListener = new ViewListener<AbstractView>() {

			@Override
			public void highlightingChanged(AbstractView v) {
				// TODO Auto-generated method stub

			}

			@Override
			public void markChanged(AbstractView v) {
				observable.notify(new MarkChangedEvent<View>(View.this));
			}

			@Override
			public void selectionChanged(AbstractView v) {
				// TODO this will cause multiple notifications for one selection
				// change
				observable.notify(new SelectionChangedEvent<View>(View.this));
			}

		};

		setWorkflowView(new WorkflowView());

		for (Job j : workflow.getChildren()) {
			addJobView(j);
			for (Port p : j.getOutputPorts()) {
				addLocationView(j.bindings.get(p));
			}
		}
		for (ConnectionKey ck : workflow.getConnections())
			addConnectionView(ck);
		workflow.getObservable().addObserver(
				new Observer<WorkflowEvent<MutableWorkflow>>() {

					@Override
					public void notify(WorkflowEvent<MutableWorkflow> event) {
						event.doNotify(workflowListener);
					}

				});
	}

	public void addConnectionView(ConnectionKey cc) {
		if (!connections.containsKey(cc)) {
			connections.put(cc, new ConnectionView());
			connections.get(cc).getObservable()
					.addObserver(new Observer<ViewEvent<AbstractView>>() {

						@Override
						public void notify(ViewEvent<AbstractView> event) {
							event.doNotify(viewEventListener);
						}

					});
		}
	}

	private void addJobView(Job j) {
		JobView jv = new JobView();
		jobs.put(j, jv);
		jv.getObservable().addObserver(new Observer<ViewEvent<AbstractView>>() {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(viewEventListener);
			}

		});
	}

	private void addLocationView(Location l) {
		variables.put(l, new LocationView());
		variables.get(l).getObservable()
				.addObserver(new Observer<ViewEvent<AbstractView>>() {

					@Override
					public void notify(ViewEvent<AbstractView> event) {
						event.doNotify(viewEventListener);
					}

				});
	}

	public <T, T2 extends AbstractView> void addMarked(WeakHashMap<T, T2> whm,
			List<AbstractView> marked) {
		for (T2 v : whm.values())
			if (v.isMarked())
				marked.add(v);
	}

	public <T, T2 extends AbstractView> void addSelected(
			WeakHashMap<T, T2> whm, List<AbstractView> selection) {
		for (T2 v : whm.values())
			if (v.isSelected())
				selection.add(v);
	}

	public void clearMarked() {
		clearMarked(jobs);
		clearMarked(connections);
		clearMarked(variables);
	}

	public <T, T2 extends AbstractView> void clearMarked(WeakHashMap<T, T2> whm) {
		for (T2 v : whm.values())
			v.setMarked(false);
	}

	public <T, T2 extends AbstractView> void clearSelected(
			WeakHashMap<T, T2> whm) {
		for (T2 v : whm.values())
			v.setSelected(false);
	}

	public void clearSelection() {
		clearSelected(jobs);
		clearSelected(connections);
		clearSelected(variables);
	}

	public ConnectionView getConnectionView(ConnectionKey ck) {
		return connections.get(ck);
	}

	public List<AbstractView> getCurrentSelection() {
		List<AbstractView> currentSelection = new ArrayList<AbstractView>();
		addSelected(jobs, currentSelection);
		addSelected(connections, currentSelection);
		addSelected(variables, currentSelection);
		if (workflowView.isSelected())
			currentSelection.add(workflowView);
		return currentSelection;
	}

	public JobView getJobView(Job job) {
		return jobs.get(job);
	}

	public LocationView getLocationView(Location l) {
		return variables.get(l);
	}

	public List<AbstractView> getMarked() {
		List<AbstractView> markedViews = new ArrayList<AbstractView>();
		addMarked(jobs, markedViews);
		addMarked(connections, markedViews);
		addMarked(variables, markedViews);
		return markedViews;
	}

	public MultiplexObserver<GlobalViewEvent<View>> getObservable() {
		return observable;
	}

	public MutableWorkflow getWorkflow() {
		return workflow;
	}

	public WorkflowView getWorkflowView() {
		return workflowView;
	}

	private void setWorkflowView(WorkflowView workflowView) {
		this.workflowView = workflowView;
		workflowView.getObservable().addObserver(
				new Observer<ViewEvent<AbstractView>>() {

					@Override
					public void notify(ViewEvent<AbstractView> event) {
						event.doNotify(viewEventListener);
					}

				});
	}

	public void removeSelectedCell() {
		List<AbstractView> selection = getCurrentSelection();
		// System.out.print("Selection: ");
		for (AbstractView v : selection) {
			// System.out.print(v);
			v.remove(this);
		}
		// System.out.println();
	}

}
