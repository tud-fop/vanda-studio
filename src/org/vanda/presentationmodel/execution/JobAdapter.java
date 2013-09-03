package org.vanda.presentationmodel.execution;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.execution.model.Runables.RunStateVisitor;
import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.CellListener;
import org.vanda.render.jgraph.Cells.SelectionChangedEvent;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JGraphRendering;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManager;
import org.vanda.render.jgraph.NaiveLayoutManager;
import org.vanda.render.jgraph.InPortCell;
import org.vanda.render.jgraph.OutPortCell;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.view.JobView;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

public class JobAdapter {
	private class JobCellListener implements CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// the following is necessary if the job is not in the model
			// which happens in case of drag&drop (as opposed to render).
			if (!job.isInserted()) {
				double[] dim = { jobCell.getX(), jobCell.getY(),
						jobCell.getWidth(), jobCell.getHeight() };
				job.setDimensions(dim);
				view.getWorkflow().addChild(job);
			}

			for (Location variable : job.bindings.values()) {
				locations.get(variable).updateLocation(getJob());
			}

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
			// do nothing
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing

		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			JobView jv = view.getJobView(job);
			jv.setSelected(selected);
		}

		@Override
		public void rightClick(MouseEvent e) {
			//do nothing
		}

	}

	private class JobViewListener implements
			AbstractView.ViewListener<AbstractView> {

		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub

		}

		@Override
		public void markChanged(AbstractView v) {
			if (v.isMarked()) {
				jobCell.highlight(true);

			} else {
				jobCell.highlight(false);
			}
		}
		
		@Override
		public void runProgressUpdate(AbstractView v) {
			jobCell.setProgress(((JobView) v).getRunProgress());
		}

		@Override
		public void selectionChanged(AbstractView v) {
			jobCell.getObservable().notify(
					new SelectionChangedEvent<Cell>(jobCell, v.isSelected()));
		}

		@Override
		public void runStateTransition(AbstractView v, RunState from, RunState to) {
			to.visit(new RunStateVisitor() {

				@Override
				public void cancelled() {
					jobCell.setCancelled();
				}

				@Override
				public void done() {
					jobCell.setDone();					
				}

				@Override
				public void ready() {
					jobCell.setReady();
				}

				@Override
				public void running() {
					jobCell.setRunning();					
				}
				
			});
		}
	}

	private Map<Port, InPortCell> inports;
	private Job job;
	private JobCell jobCell;
	private JobCellListener jobCellListener;
	private Observer<CellEvent<Cell>> jobCellObserver;
	private JobViewListener jobViewListener;
	private Observer<ViewEvent<AbstractView>> jobViewObserver;
	
	Map<Location, LocationAdapter> locations;

	Map<Port, OutPortCell> outports;

	View view;

	public JobAdapter(Job job, Graph graph, View view, WorkflowCell wfc) {
		this.view = view;
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

		// register at jobView
		jobViewListener = new JobViewListener();
		jobViewObserver = new Observer<ViewEvent<AbstractView>>() {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				event.doNotify(jobViewListener);
			}
			
		};
		view.getJobView(job).getObservable().addObserver(jobViewObserver);
	}

	public void destroy(Graph graph) {
		if (jobCell != null) {
			graph.removeCell(jobCell);
		}
		for (LocationAdapter la : locations.values()) 
			la.destroy();
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
		return job;
	}

	public JobCell getJobCell() {
		return jobCell;
	}

	public OutPortCell getOutPortCell(Port po) {
		return outports.get(po);
	}

	private void setUpCells(Graph graph, Job job, WorkflowCell wfc) {
		graph.beginUpdate();
		try {
			LayoutManager layoutManager = new NaiveLayoutManager();
			this.job = job;
			inports = new WeakHashMap<Port, InPortCell>();
			outports = new WeakHashMap<Port, OutPortCell>();
			locations = new WeakHashMap<Location, LocationAdapter>();
			jobCell = new JobCell(graph, job.selectRenderer(JGraphRendering
					.getRendererAssortment()), job.getName(), job.getX(),
					job.getY(), job.getWidth(), job.getHeight());

			// insert a cell for every input port
			List<Port> in = job.getInputPorts();

			for (Port ip : in) {
				InPortCell ipc = new InPortCell(graph, layoutManager, jobCell,
						"InPortCell");
				inports.put(ip, ipc);
				jobCell.addCell(ipc, null);
			}

			// insert a cell for every output port
			List<Port> out = job.getOutputPorts();
			for (Port op : out) {
				OutPortCell opc = new OutPortCell(graph, layoutManager,
						jobCell, "OutPortCell");
				outports.put(op, opc);
				jobCell.addCell(opc, null);
				LocationAdapter locA = new LocationAdapter(graph, view,
						layoutManager, jobCell, op, job.bindings.get(op));
				locations.put(job.bindings.get(op), locA);
				jobCell.addCell(locA.locationCell, null);
			}

			// setup Layout
			layoutManager.setUpLayout(graph, jobCell);

			wfc.addCell(jobCell, null);

		} finally {
			graph.endUpdate();
		}
	}
}
