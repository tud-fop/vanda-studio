package org.vanda.presentationmodel.palette;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.CellListener;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JGraphRendering;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManager;
import org.vanda.render.jgraph.NaiveLayoutManager;
import org.vanda.render.jgraph.InPortCell;
import org.vanda.render.jgraph.OutPortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;

public class JobAdapter {
	private class JobCellListener implements CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// the following is necessary if the job is not in the model
			// which happens in case of drag&drop (as opposed to render).
			// if (!job.isInserted()) {
			// jobCell.updateDimensions();
			// double[] dim = { jobCell.getX(), jobCell.getY(),
			// jobCell.getWidth(), jobCell.getHeight() };
			// job.setDimensions(dim);
			// view.getWorkflow().addChild(job);
			// }
			//
			// for (Location variable : job.bindings.values()) {
			// locations.get(variable).updateLocation(getJob());
			// }

		}

		@Override
		public void markChanged(Cell c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void propertyChanged(Cell c) {
			if (job.getX() != jobCell.getX() || job.getY() != jobCell.getY()
					|| job.getWidth() != jobCell.getWidth()
					|| job.getHeight() != jobCell.getHeight()) {
				double[] dim = { jobCell.getX(), jobCell.getY(),
						jobCell.getWidth(), jobCell.getHeight() };
				job.setDimensions(dim);
			}
		}

		@Override
		public void removeCell(Cell c) {
			// if (job != null)
			// view.getWorkflow().removeChild(job);
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing

		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			// JobView jv = view.getJobView(job);
			// jv.setSelected(selected);
		}

		@Override
		public void rightClick(MouseEvent e) {
			// do nothing
		}

	}

	private Map<Port, InPortCell> inports;
	private Job job;
	private JobCell jobCell;
	private JobCellListener jobCellListener;
	private Observer<CellEvent<Cell>> jobCellObserver;

	private Map<Port, LocationAdapter> locations;

	private Map<Port, OutPortCell> outports;

	JobAdapter(Job job, Graph graph, WorkflowCell wfc) {
		setUpCells(graph, job, wfc);
		this.jobCellListener = new JobCellListener();

		// register at jobCell
		jobCellObserver = new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(jobCellListener);
			}

		};
		jobCell.getObservable().addObserver(jobCellObserver);
	}

	public void destroy(Graph graph) {
		if (jobCell != null) {
			graph.removeCell(jobCell);
		}
	}

	public List<Cell> getCells() {
		ArrayList<Cell> cells = new ArrayList<Cell>();

		for (LocationAdapter la : locations.values())
			cells.add(la.locationCell);
		cells.addAll(inports.values());
		cells.addAll(outports.values());

		return cells;
	}

	public InPortCell getInPortCell(Port pi) {
		return inports.get(pi);
	}

	public Job getJob() {
		// return jobCell.getJob();
		return job;
	}

	public JobCell getJobCell() {
		return jobCell;
	}

	public OutPortCell getOutPortCell(Port po) {
		return outports.get(po);
	}

	private void setUpCells(Graph g, Job job, WorkflowCell wfc) {
		g.beginUpdate();
		try {
			LayoutManager layoutManager = new NaiveLayoutManager();
			this.job = job;
			inports = new WeakHashMap<Port, InPortCell>();
			outports = new WeakHashMap<Port, OutPortCell>();
			locations = new WeakHashMap<Port, LocationAdapter>();
			jobCell = new JobCell(g, job.selectRenderer(JGraphRendering
					.getRendererAssortment()), job.getName(), job.getX(),
					job.getY(), job.getWidth(), job.getHeight());
			jobCell.setId(job.getElement().getId());
			// insert a cell for every input port
			List<Port> in = job.getInputPorts();

			for (Port ip : in) {
				InPortCell ipc = new InPortCell(g, layoutManager, jobCell,
						"InPortCell");
				inports.put(ip, ipc);
				jobCell.addCell(ipc, null);
			}

			// insert a cell for every output port
			List<Port> out = job.getOutputPorts();
			for (Port op : out) {
				OutPortCell opc = new OutPortCell(g, layoutManager, jobCell,
						"OutPortCell");
				outports.put(op, opc);
				jobCell.addCell(opc, null);
				LocationAdapter locA = new LocationAdapter(g, layoutManager,
						jobCell);
				locations.put(op, locA);
				jobCell.addCell(locA.locationCell, null);

				// set jobID to LocationCell to enable LocationDragging
				locA.locationCell.setId(job.getElement().getId());
			}

			// setup Layout
			layoutManager.setUpLayout(g, jobCell);
//			jobCell.onResize(g.getGraph());

			wfc.addCell(jobCell, null);

		} finally {
			g.endUpdate();
		}
	}

	public double getVisualizationHeight() {
		return jobCell.getHeight();
	}

}
