package org.vanda.view;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.vanda.util.Observer;
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
			jobs.put(j, new JobView());
			for (Location l : j.bindings.values())
			{
				locations.put(l, new LocationView());
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
			connections.put(cc, new ConnectionView());
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
	
	public View (MutableWorkflow workflow) {
		this.workflow = workflow;
		jobs = new WeakHashMap<Job, JobView>();
		connections = new WeakHashMap<ConnectionKey, ConnectionView>();
		locations = new WeakHashMap<Location, LocationView>();
		workflowListener = new WorkflowListener();
		for (Job j : workflow.getChildren())
		{
			jobs.put(j, new JobView());
			for (Location l : j.bindings.values())
			{
				locations.put(l, new LocationView());
			}
		}
		for (ConnectionKey ck : workflow.getConnections())
			connections.put(ck, new ConnectionView());
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
}
