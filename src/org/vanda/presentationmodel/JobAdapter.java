package org.vanda.presentationmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cell.CellEvent;
import org.vanda.render.jgraph.Cell.SelectionChangedEvent;
import org.vanda.render.jgraph.Graph;
import org.vanda.render.jgraph.JobCell;
import org.vanda.render.jgraph.LayoutManagerInterface;
import org.vanda.render.jgraph.PortCell;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.JobView;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Jobs;
import org.vanda.workflows.hyper.Jobs.JobEvent;
import org.vanda.workflows.hyper.Location;

public class JobAdapter {
	private class JobCellListener implements Cell.CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// the following is necessary if the job is not in the model
			// which happens in case of drag&drop (as opposed to render).
			if (!job.isInserted()) {
				jobCell.updateDimensions();
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
			if (job != null)
				view.getWorkflow().removeChild(job);
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

	}

	private class JobListener implements Jobs.JobListener<Job> {
		@Override
		public void propertyChanged(Job j) {
			if (jobCell.getX() != j.getX() || jobCell.getY() != j.getY()
					|| jobCell.getWidth() != j.getWidth()
					|| jobCell.getHeight() != j.getHeight()) {

				jobCell.setDimensions(new double[] { job.getX(), job.getY(),
						job.getWidth(), job.getHeight() });
				jobCell.sizeChanged();

			}
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
		public void selectionChanged(AbstractView v) {
			jobCell.getObservable().notify(
					new SelectionChangedEvent<Cell>(jobCell, v.isSelected()));
		}
	}

	Map<Port, PortCell> inports;
	Job job;
	JobCell jobCell;
	JobCellListener jobCellListener;
	JobListener jobListener;
	JobViewListener jobViewListener;

	Map<Location, LocationAdapter> locations;

	Map<Port, PortCell> outports;

	View view;

	JobAdapter(Job job, LayoutManagerInterface layoutManager, Graph graph,
			View view) {
		this.view = view;
		setUpCells(layoutManager, graph, job);
		this.jobListener = new JobListener();
		this.jobCellListener = new JobCellListener();

		// Register at Job
		if (job.getObservable() != null)
			job.getObservable().addObserver(new Observer<Jobs.JobEvent<Job>>() {

				@Override
				public void notify(JobEvent<Job> event) {
					event.doNotify(jobListener);
				}

			});

		// register at jobCell
		jobCell.getObservable().addObserver(new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(jobCellListener);
			}

		});

		// register at jobView
		this.jobViewListener = new JobViewListener();

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

	public PortCell getInPortCell(Port pi) {
		return inports.get(pi);
	}

	public Job getJob() {
		// return jobCell.getJob();
		return job;
	}

	public JobCell getJobCell() {
		return jobCell;
	}

	public PortCell getOutPortCell(Port po) {
		return outports.get(po);
	}

	private void setUpCells(LayoutManagerInterface layoutManager, Graph g,
			Job job) {
		g.beginUpdate();
		try {
			this.job = job;
			inports = new WeakHashMap<Port, PortCell>();
			outports = new WeakHashMap<Port, PortCell>();
			locations = new WeakHashMap<Location, LocationAdapter>();
			jobCell = new JobCell(g, layoutManager, job.getName(), job.getX(),
					job.getY(), job.getWidth(), job.getHeight());

			// insert a cell for every input port
			List<Port> in = job.getInputPorts();

			for (Port ip : in) {
				inports.put(ip, new PortCell(g, layoutManager, jobCell,
						"InPortCell"));
			}

			// insert a cell for every output port
			List<Port> out = job.getOutputPorts();
			for (Port op : out) {
				outports.put(op, new PortCell(g, layoutManager, jobCell,
						"OutPortCell"));
				locations
						.put(job.bindings.get(op),
								new LocationAdapter(g, view, layoutManager,
										jobCell, op, job.bindings.get(op)));

			}

			// setup Layout
			layoutManager.setUpLayout(g);

		} finally {
			g.endUpdate();
		}
	}

}
