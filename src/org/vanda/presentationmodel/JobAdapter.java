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
	
	JobAdapter(Job job, LayoutManagerInterface layoutManager, Graph graph) {
		setUpCells(layoutManager, graph, job);
	}

	private void setUpCells(LayoutManagerInterface layoutManager, Graph g, Job job) {
		g.getGraph().getModel().beginUpdate();
		try {
			
			jobCell = new JobCell(g, layoutManager, job);
			
			// insert a cell for every input port
			List<Port> in = job.getInputPorts();
			
			for (Port ip : in) {	
				new PortCell(g, layoutManager, jobCell, ip, "InPortCell");
			}

			// insert a cell for every output port
			List<Port> out = job.getOutputPorts();
			for (Port op : out) {
				new PortCell(g, layoutManager, jobCell, op, "OutPortCell");
				new LocationCell(g, layoutManager, jobCell, op, job.bindings.get(op));
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
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (int i = 0; i < jobCell.getVisualization().getChildCount(); ++i)
			cells.add((Cell) jobCell.getVisualization().getChildAt(i).getValue());
		return cells;
	}

	public JobCell getJobCell() {
		return jobCell;
	}
	
	public void destroy(Graph graph) {
		if (jobCell != null) {
			graph.getGraph().removeCells(new Object[] {jobCell.getVisualization()});
		}
	}
}
