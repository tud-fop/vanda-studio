package org.vanda.presentationmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManagerFactoryInterface;
import org.vanda.render.jgraph.LayoutManagerInterface;
import org.vanda.render.jgraph.NaiveLayoutManagerFactory;
import org.vanda.render.jgraph.PortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

public class PresentationModel {
	protected final Graph graph;
	protected final WorkflowCell workflowCell;
	private final WorkflowListener workflowListener;
	List<JobAdapter> jobs;
	Map<ConnectionKey, ConnectionAdapter> connections;
	View view;
	LayoutManagerFactoryInterface layoutManager = new NaiveLayoutManagerFactory();

	/*
	 * map that holds Layouts for all Job-Types, where String is
	 * <code>Job.getName()</code>
	 */
	// static final Map<String, LayoutManager> layouts = null; //TODO define
	// some Layouts

	void beginUpdate() {
	}

	void endUpdate() {
	}

	public Graph getVisualization() {
		return graph;
	}

	public JobAdapter addJobAdapter(Job job) {
		if (!job.isInserted()) {
			view.getWorkflow().addChild(job);
		}
		for (JobAdapter ja : jobs) {
			if (ja.getJob() == job)
				return ja;
		}
		JobAdapter ja = new JobAdapter(job, selectLayout(job), graph, view);
		jobs.add(ja);
		graph.refresh();
		return ja;
	}

	private LayoutManagerInterface selectLayout(Job job) {
		// return layouts.get(job.getName());
		return layoutManager.getLayoutManager(job);
	}

	/**
	 * Listens for changes in Model, i.e. Job, Connections, ... and keeps
	 * Presentation Model up to date and enforces mxGraph updates
	 * 
	 * @author kgebhardt
	 * 
	 */
	protected class WorkflowListener implements
			Workflows.WorkflowListener<MutableWorkflow> {

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
			removeJobAdatper(mwf, j);
		}

		@Override
		public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
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

	public View getView() {
		return view;
	}

	public void removeConnectionAdapter(MutableWorkflow mwf, ConnectionKey cc) {
		if (connections.containsKey(cc)) {
			ConnectionAdapter ca = connections.remove(cc);
			ca.destroy(graph);
		}
	}

	public void addConnectionAdapter(MutableWorkflow mwf, ConnectionKey cc) {
		if (!connections.containsKey(cc))
			connections.put(cc, new ConnectionAdapter(cc, this, mwf, view));
	}

	public void removeJobAdatper(MutableWorkflow mwf, Job j) {
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

	public List<JobAdapter> getJobs() {
		return jobs;
	}

	public PresentationModel(View view) {
		this.view = view;
		this.workflowCell = new WorkflowCell(this);
		graph = new Graph(view, workflowCell);
		jobs = new ArrayList<JobAdapter>();
		connections = new WeakHashMap<ConnectionKey, ConnectionAdapter>();
		workflowListener = new WorkflowListener();
		view.getWorkflow()
				.getObservable()
				.addObserver(
						new Observer<Workflows.WorkflowEvent<MutableWorkflow>>() {

							@Override
							public void notify(
									WorkflowEvent<MutableWorkflow> event) {
								event.doNotify(workflowListener);

							}

						});
		setupPresentationModel();
	}

	private void setupPresentationModel() {
		for (Job j : view.getWorkflow().getChildren()) {
			jobs.add(new JobAdapter(j, selectLayout(j), graph, view));
		}
		for (ConnectionKey ck : view.getWorkflow().getConnections()) {
			connections.put(ck,
					new ConnectionAdapter(ck, this, view.getWorkflow(), view));
		}
	}

	public void addConnectionAdapter(ConnectionCell connectionCell,
			ConnectionKey connectionKey) {
		if (!connections.containsKey(connectionKey))
			connections.put(connectionKey, new ConnectionAdapter(connectionKey,
					connectionCell, view));
	}

	public void addConnectionAdapter(ConnectionCell connectionCell,
			JobCell tparval, PortCell tval) {
		Job j = null;
		Port p = null;
		for (JobAdapter ja : jobs) {
			if (ja.getJobCell() == tparval) {
				j = ja.getJob();
				break;
			}
		}
		for (JobAdapter ja : jobs) {
			if (ja.getJob() == j) {
				for (Port pi : ja.getJob().getInputPorts()) {
					if (ja.getInPortCell(pi) == tval) {
						p = pi;
						break;
					}
				}
				break;
			}
		}
		assert (j != null && p != null);
		ConnectionKey connectionKey = new ConnectionKey(j, p);
		if (!connections.containsKey(connectionKey))
			connections.put(connectionKey, new ConnectionAdapter(connectionKey,
					connectionCell, view));

	}

}
