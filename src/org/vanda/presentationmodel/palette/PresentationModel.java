package org.vanda.presentationmodel.palette;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import org.vanda.render.jgraph.DataInterface;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.InPortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

public class PresentationModel implements DataInterface {
	/**
	 * Listens for changes in Model, i.e. Job, Connections, ... and keeps
	 * Presentation Model up to date and enforces mxGraph updates
	 * 
	 * @author kgebhardt
	 * 
	 */

	protected final Graph graph;
	List<JobAdapter> jobs;
	// LayoutManagerFactoryInterface layoutManager = new JGraphRendering();
	protected final WorkflowCell workflowCell;
	Component comp;

	/*
	 * map that holds Layouts for all Job-Types, where String is
	 * <code>Job.getName()</code>
	 */
	// static final Map<String, LayoutManager> layouts = null; //TODO define
	// some Layouts

	public PresentationModel() {
		this.workflowCell = new WorkflowCell(this);
		graph = new Graph(workflowCell);
		graph.setGraphImmutable();
		jobs = new ArrayList<JobAdapter>();
	}

	public double addJobAdapter(Job job) {
		JobAdapter ja = new JobAdapter(job, graph, workflowCell);
		jobs.add(ja);
		graph.refresh();
		return ja.getVisualizationHeight();
	}

	public List<JobAdapter> getJobs() {
		return jobs;
	}

	public Graph getVisualization() {
		return graph;
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

	@Override
	public void createJob(String id, double[] d) {
		// do nothing
	}

	@Override
	public void createConnection(ConnectionCell connectionCell,
			JobCell tparval, InPortCell tval) {
		// do nothing
	}

	public Component getComponent() {
		if (comp == null) {
			comp = graph.getGraphComponent(); 
		}
		return comp;
	}

}
