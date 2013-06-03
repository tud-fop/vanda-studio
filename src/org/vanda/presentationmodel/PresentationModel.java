package org.vanda.presentationmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.render.jgraph.DataInterface;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.InPortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.types.Types;
import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.ToolAdapter;
import org.vanda.workflows.hyper.Workflows;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

public class PresentationModel implements DataInterface {
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

	Map<ConnectionKey, ConnectionAdapter> connections;
	protected final Graph graph;
	List<JobAdapter> jobs;
	// LayoutManagerFactoryInterface layoutManager = new JGraphRendering();
	View view;
	protected final WorkflowCell workflowCell;
	private int update = 0;
	protected final WorkflowEditor wfe;

	/*
	 * map that holds Layouts for all Job-Types, where String is
	 * <code>Job.getName()</code>
	 */
	// static final Map<String, LayoutManager> layouts = null; //TODO define
	// some Layouts

	private final WorkflowListener workflowListener;

	public PresentationModel(View view, WorkflowEditor wfe) {
		this.view = view;
		this.wfe = wfe;
		this.workflowCell = new WorkflowCell(this);
		graph = new Graph(workflowCell);
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

	/**
	 * called in case of hand-drawn edge insertion
	 * 
	 * @param connectionCell
	 * @param tparval
	 * @param tval
	 */
	private void addConnectionAdapter(ConnectionCell connectionCell,
			JobCell tparval, InPortCell tval) {
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
		beginUpdate();
		if (!connections.containsKey(connectionKey))
			connections.put(connectionKey, new ConnectionAdapter(connectionKey,
					connectionCell, view));
		endUpdate();

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
		JobAdapter ja = new JobAdapter(job, graph, view, workflowCell);
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
			jobs.add(new JobAdapter(j, graph, view, workflowCell));
		}
		for (ConnectionKey ck : view.getWorkflow().getConnections()) {
			if (connections.get(ck) == null)
				connections.put(ck,
						new ConnectionAdapter(ck, this, view.getWorkflow(),
								view));
		}
	}

	@Override
	public void createJob(String id, double[] d) {
		ElementAdapter ele;
		// TODO literal should be recognized otherwise
		if (id.equals("literal"))
			ele = new LiteralAdapter(new Literal(Types.undefined, "literal", null));
		else
			ele = new ToolAdapter(wfe.getApplication().getToolMetaRepository()
					.getRepository().getItem(id));
		Job j = new Job(ele);
		j.setDimensions(d);
		addJobAdapter(j);
	}

	@Override
	public void createConnection(ConnectionCell connectionCell,
			JobCell tparval, InPortCell tval) {
		addConnectionAdapter(connectionCell, tparval, tval);
	}

}
