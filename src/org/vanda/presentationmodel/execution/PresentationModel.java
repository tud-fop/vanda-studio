package org.vanda.presentationmodel.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.render.jgraph.Graph;
import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

public class PresentationModel {
	/**
	 * Listens for changes in Model, i.e. Job, Connections, ... and keeps
	 * Presentation Model up to date and enforces mxGraph updates
	 * 
	 * @author kgebhardt
	 * 
	 */
	protected class WorkflowListener implements Workflows.WorkflowListener<MutableWorkflow> {

		@Override
		public void childAdded(MutableWorkflow mwf, Job j) {
			addJobAdapter(j);
		}

		@Override
		public void childModified(MutableWorkflow mwf, Job j) {
			// do nothing
		}

		@Override
		public void childRemoved(MutableWorkflow mwf, Job j) {
			removeJobAdapter(mwf, j);
		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
			if (update == 0)
				addConnectionAdapter(mwf, cc);
		}

		@Override
		public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
			removeConnectionAdapter(mwf, cc);
		}

		@Override
		public void propertyChanged(MutableWorkflow mwf) {
			// TODO improve
			if (mwf != PresentationModel.this.getView().getWorkflow())
				graph.refresh();
		}

		@Override
		public void updated(MutableWorkflow mwf) {
		}

	}

	private Map<ConnectionKey, ConnectionAdapter> connections;
	protected final Graph graph;
	private List<JobAdapter> jobs;
	// LayoutManagerFactoryInterface layoutManager = new JGraphRendering();
	private View view;
	private int update = 0;
	private final WorkflowAdapter wfa;

	/*
	 * map that holds Layouts for all Job-Types, where String is
	 * <code>Job.getName()</code>
	 */
	// static final Map<String, LayoutManager> layouts = null; //TODO define
	// some Layouts

	private final WorkflowListener workflowListener;
	private final Observer<Workflows.WorkflowEvent<MutableWorkflow>> workflowObserver;

	public PresentationModel(View view) {
		this.view = view;
		this.wfa = new WorkflowAdapter(view);
		graph = new Graph(wfa.getWorkflowCell());
		graph.setGraphImmutable();
		jobs = new ArrayList<JobAdapter>();
		connections = new WeakHashMap<ConnectionKey, ConnectionAdapter>();
		workflowListener = new WorkflowListener();
		workflowObserver = new Observer<Workflows.WorkflowEvent<MutableWorkflow>>() {

			@Override
			public void notify(WorkflowEvent<MutableWorkflow> event) {
				event.doNotify(workflowListener);

			}

		};
		view.getWorkflow().getObservable().addObserver(workflowObserver);
		setupPresentationModel();
	}

	/**
	 * called in case of edge insertion by workflow (never ???)
	 * 
	 * @param mwf
	 * @param cc
	 */
	public void addConnectionAdapter(MutableWorkflow mwf, ConnectionKey cc) {
		if (!connections.containsKey(cc))
			connections.put(cc, new ConnectionAdapter(cc, this, mwf, view));
	}

	public JobAdapter addJobAdapter(Job job) {
		if (!job.isInserted()) {
			view.getWorkflow().addChild(job);
		}
		for (JobAdapter ja : jobs) {
			if (ja.getJob() == job)
				return ja;
		}
		JobAdapter ja = new JobAdapter(job, graph, view, wfa.getWorkflowCell());
		jobs.add(ja);
		graph.refresh();
		return ja;
	}

	void beginUpdate() {
		update++;
	}

	void endUpdate() {
		update--;
	}

	public List<JobAdapter> getJobs() {
		return jobs;
	}

	public View getView() {
		return view;
	}

	public Graph getVisualization() {
		return graph;
	}

	public void removeConnectionAdapter(MutableWorkflow mwf, ConnectionKey cc) {
		if (connections.containsKey(cc)) {
			ConnectionAdapter ca = connections.remove(cc);
			ca.destroy(graph);
		}
	}

	public void removeJobAdapter(MutableWorkflow mwf, Job j) {
		JobAdapter toDelete = null;
		for (JobAdapter ja : jobs) {
			if (ja.getJob() == j)
				toDelete = ja;
		}
		if (toDelete != null) {
			jobs.remove(toDelete);
			toDelete.destroy(graph);
		}
	}

	private void setupPresentationModel() {
		for (Job j : view.getWorkflow().getChildren()) {
			jobs.add(new JobAdapter(j, graph, view, wfa.getWorkflowCell()));
		}
		for (ConnectionKey ck : view.getWorkflow().getConnections()) {
			if (connections.get(ck) == null)
				connections.put(ck, new ConnectionAdapter(ck, this, view.getWorkflow(), view));
		}
	}

}
