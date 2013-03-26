package org.vanda.view;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.view.AbstractView.ViewListener;
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
	MutableWorkflow workflow;
	public MutableWorkflow getWorkflow() {
		return workflow;
	}
	protected class WorkflowListener implements
	Workflows.WorkflowListener<MutableWorkflow> {

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			addJobView(j);
			for (Location l : j.bindings.values())
			{
				addLocationView(l);
			}
		}

		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
			// do nothing
		}

		@Override
		public void childRemoved(MutableWorkflow mwf, Job j) {
			for (Location l : j.bindings.values()) {
				locations.remove(l);
			}
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
	WeakHashMap<Job, JobView> jobs;
	WeakHashMap<ConnectionKey, ConnectionView> connections;
	WeakHashMap<Location, LocationView> locations;
	WorkflowListener workflowListener;
	ViewListener<AbstractView> selectionChangeListener;
	
	public View (MutableWorkflow workflow) {
		this.workflow = workflow;
		this.observable = new MultiplexObserver<GlobalViewEvent<View>>();
		jobs = new WeakHashMap<Job, JobView>();
		connections = new WeakHashMap<ConnectionKey, ConnectionView>();
		locations = new WeakHashMap<Location, LocationView>();
		workflowListener = new WorkflowListener();
		selectionChangeListener = new ViewListener<AbstractView>() {

			@Override
			public void selectionChanged(AbstractView v) {
				// TODO this will cause multiple notifications for one selection change
				observable.notify(new SelectionChangedEvent<View>(View.this));
			}
			
		};
		for (Job j : workflow.getChildren())
		{
			addJobView(j);
			for (Location l : j.bindings.values())
			{
				addLocationView(l);
			}
		}
		for (ConnectionKey ck : workflow.getConnections())
			addConnectionView(ck);
		workflow.getObservable().addObserver(new Observer<WorkflowEvent<MutableWorkflow>>() {

			@Override
			public void notify(WorkflowEvent<MutableWorkflow> event) {
				event.doNotify(workflowListener);				
			}
			
		});
	}
	
	public JobView getJobView(Job job) {
		return jobs.get(job);
	}
	
	public ConnectionView getConnectionView(ConnectionKey ck) {
		return connections.get(ck);
	}
	
	public LocationView getLocationView(Location loc) {
		return locations.get(loc);
	}
	
	public List<AbstractView> getCurrentSelection() {
		List<AbstractView> currentSelection = new ArrayList<AbstractView>();
		addSelected(jobs, currentSelection);
		addSelected(connections, currentSelection);
		addSelected(locations, currentSelection);
		return currentSelection;
	}
	public <T, T2 extends AbstractView> void addSelected(WeakHashMap<T,T2> whm, List<AbstractView> selection) {
		for (T2 v : whm.values())
			if (v.isSelected())
				selection.add(v);
	}
	
	public void clearSelection() {
		 clearSelected(jobs);
		 clearSelected(connections);
		 clearSelected(locations);
	}
	public <T, T2 extends AbstractView> void clearSelected(WeakHashMap<T,T2> whm) {
		for (T2 v : whm.values())
			v.setSelected(false);
	}

	private MultiplexObserver<GlobalViewEvent<View>> observable;
	
	public MultiplexObserver<GlobalViewEvent<View>> getObservable() {
		return observable;
	}
	private void addLocationView(Location l) {
		locations.put(l, new LocationView());
		locations.get(l).getObservable().addObserver(new Observer<ViewEvent<AbstractView>>() {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(selectionChangeListener);
			}
			
		});
	}
	
	private void addJobView(Job j) {
		jobs.put(j, new JobView());
		jobs.get(j).getObservable().addObserver(new Observer<ViewEvent<AbstractView>>() {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(selectionChangeListener);
			}
			
		});
	}
	
	private void addConnectionView(ConnectionKey cc) {
		connections.put(cc, new ConnectionView());
		connections.get(cc).getObservable().addObserver(new Observer<ViewEvent<AbstractView>>() {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(selectionChangeListener);
			}
			
		});
	}
	public static interface GlobalViewEvent<V> {
		void doNotify(GlobalViewListener<V> vl);
	}

	public static interface GlobalViewListener<V> {
		void selectionChanged(V v);
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
	
}
