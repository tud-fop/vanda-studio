package org.vanda.presentationmodel.palette;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.List;

import org.vanda.render.jgraph.DataInterface;
import org.vanda.render.jgraph.ConnectionCell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManagerFactoryInterface;
import org.vanda.render.jgraph.LayoutManagerInterface;
import org.vanda.render.jgraph.NaiveLayoutManagerFactory;
import org.vanda.render.jgraph.PortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.render.jgraph.mxDragGestureListener;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

import com.mxgraph.swing.mxGraphComponent;

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
	LayoutManagerFactoryInterface layoutManager = new NaiveLayoutManagerFactory();
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
		graph.setPaletteStyle();
		jobs = new ArrayList<JobAdapter>();
	}

	public double addJobAdapter(Job job) {
		JobAdapter ja = new JobAdapter(job, selectLayout(job), graph);
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

	private LayoutManagerInterface selectLayout(Job job) {
		return job.selectRenderer(layoutManager.getRendererAssortment());
	}

	@Override
	public void createJob(String id, double[] d) {
		// do nothing
	}

	@Override
	public Graph getGraph() {
		return graph;
	}

	@Override
	public void createConnection(ConnectionCell connectionCell,
			JobCell tparval, PortCell tval) {
		// do nothing
	}

	public Component getComponent() {
		if (comp == null) {
			comp = graph.getPaletteComponent(); 
		}
		return comp;
	}

}
