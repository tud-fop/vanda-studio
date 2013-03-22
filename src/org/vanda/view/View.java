package org.vanda.view;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

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
	WeakHashMap<Job, JobView> jobs;
	WeakHashMap<ConnectionKey, ConnectionView> connections;
	WeakHashMap<Location, LocationView> locations;
	
	public View (MutableWorkflow workflow) {
		this.workflow = workflow;
		jobs = new WeakHashMap<Job, JobView>();
		connections = new WeakHashMap<ConnectionKey, ConnectionView>();
		locations = new WeakHashMap<Location, LocationView>();
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
