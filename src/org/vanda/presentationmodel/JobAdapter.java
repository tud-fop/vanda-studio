package org.vanda.presentationmodel;

import java.util.ArrayList;
import java.util.List;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManagerInterface;
import org.vanda.render.jgraph.LocationCell;
import org.vanda.render.jgraph.PortCell;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;

public class JobAdapter {
	JobCell jobCell;
	List<Cell> cells;
	
	JobAdapter(Job job, LayoutManagerInterface layoutManager, Graph graph) {
		setUpCells(layoutManager, graph, job);
	}

	private void setUpCells(LayoutManagerInterface layoutManager, Graph g, Job job) {
		cells = new ArrayList<Cell>();
		g.getGraph().getModel().beginUpdate();
		try {
			
			jobCell = new JobCell(g, layoutManager, job);
			
			// insert a cell for every input port
			List<Port> in = job.getInputPorts();
			
			for (Port ip : in) {	
				PortCell newPort = new PortCell(g, layoutManager, jobCell, ip, "InPortCell");
				cells.add(newPort);
			}

			// insert a cell for every output port
			List<Port> out = job.getOutputPorts();
			for (Port op : out) {
				PortCell newPort = new PortCell(g, layoutManager, jobCell, op, "OutPortCell");
				cells.add(newPort);
				LocationCell newLoc = new LocationCell(g, layoutManager, jobCell, op, job.bindings.get(op));
				cells.add(newLoc);
			}
			
			// setup Layout
			layoutManager.setUpLayout(g);

		} finally {
			g.getGraph().getModel().endUpdate();
		}
	}
	
	public Job getJob() {
		return jobCell.getJob();
	}

	public List<Cell> getCells() {
		return cells;
	}

	public JobCell getJobCell() {
		return jobCell;
	}
	
	public void destroy(Graph graph) {
		if (jobCell != null) {
			Object[] removed = graph.getGraph().removeCells(new Object[] {jobCell.getVisualization()});
			System.out.println(removed);
		}
	}

	public void updateLayout(Graph graph) {
		jobCell.updateLayout(graph);
	}	
	
}
