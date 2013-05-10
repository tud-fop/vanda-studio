package org.vanda.presentationmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cell.CellEvent;
import org.vanda.render.jgraph.Cell.MarkChangedEvent;
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

import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxStyleUtils;

public class JobAdapter {
	JobCell jobCell;
	Job job;
	JobListener jobListener;
	JobViewListener jobViewListener;
	JobCellListener jobCellListener;
	View view;
	Map<Port, PortCell> inports;
	Map<Port, PortCell> outports;
	Map<Location, LocationAdapter> locations;

	private class JobListener implements Jobs.JobListener<Job> {
		@Override
		public void propertyChanged(Job j) {
			if (jobCell.getX() != j.getX() || jobCell.getY() != j.getY()
					|| jobCell.getWidth() != j.getWidth()
					|| jobCell.getHeight() != j.getHeight()) {
				mxGeometry ng = (mxGeometry) jobCell.getVisualization()
						.getGeometry().clone();
				ng.setX(j.getX());
				ng.setY(j.getY());
				ng.setWidth(j.getWidth());
				ng.setHeight(j.getHeight());
				jobCell.setDimensions(new double[] { job.getX(), job.getY(),
						job.getWidth(), job.getHeight() });
				jobCell.getVisualization().setGeometry(ng);
				jobCell.getObservable().notify(
						new Cell.PropertyChangedEvent<Cell>(jobCell));
			}
		}
	}

	private class JobViewListener implements
			AbstractView.ViewListener<AbstractView> {

		@Override
		public void selectionChanged(AbstractView v) {
			jobCell.getObservable().notify(
					new SelectionChangedEvent<Cell>(jobCell, v.isSelected()));
		}

		@Override
		public void markChanged(AbstractView v) {
			if (v.isMarked()) {
				jobCell.getVisualization().setStyle(
						mxStyleUtils.addStylename(jobCell.getVisualization()
								.getStyle(), "highlighted"));
			} else {
				jobCell.getVisualization().setStyle(
						mxStyleUtils.removeStylename(jobCell.getVisualization()
								.getStyle(), "highlighted"));
			}
			jobCell.getObservable().notify(new MarkChangedEvent<Cell>(jobCell));
		}

		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub

		}
	}

	private class JobCellListener implements Cell.CellListener<Cell> {

		@Override
		public void propertyChanged(Cell c) {
			// do nothing
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing

		}

		@Override
		public void markChanged(Cell c) {
			// TODO Auto-generated method stub

		}

		@Override
		public void removeCell(Cell c) {
			if (job != null)
				view.getWorkflow().removeChild(job);
		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			JobView jv = view.getJobView(job);
			jv.setSelected(selected);
		}

		@Override
		public void insertCell(Cell c) {
			// the following is necessary if the job is not in the model
			// which happens in case of drag&drop (as opposed to render).
			if (!job.isInserted()) {
				mxGeometry geo = jobCell.getVisualization().getGeometry();
				double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
						geo.getHeight() };
				jobCell.setDimensions(dim);
				// job.setDimensions(dim);
				view.getWorkflow().addChild(job);
			}

			for (Location variable : job.bindings.values()) {
				locations.get(variable).updateLocation(getJob());
			}

		}

	}

	JobAdapter(Job job, LayoutManagerInterface layoutManager, Graph graph,
			View view) {
		setUpCells(layoutManager, graph, job);
		this.jobListener = new JobListener();
		this.jobCellListener = new JobCellListener();
		this.view = view;

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

	private void setUpCells(LayoutManagerInterface layoutManager, Graph g,
			Job job) {
		g.getGraph().getModel().beginUpdate();
		try {
			this.job = job;
			jobCell = new JobCell(g, layoutManager, job.getName(), job.getX(),
					job.getY(), job.getWidth(), job.getHeight());

			// insert a cell for every input port
			List<Port> in = job.getInputPorts();

			for (Port ip : in) {
				inports.put(ip, new PortCell(g, layoutManager, jobCell, ip,
						"InPortCell"));
			}

			// insert a cell for every output port
			List<Port> out = job.getOutputPorts();
			for (Port op : out) {
				outports.put(op, new PortCell(g, layoutManager, jobCell, op,
						"OutPortCell"));
				locations.put(job.bindings.get(op), new LocationAdapter(g,
						layoutManager, jobCell, op, job.bindings.get(op)));

			}

			// setup Layout
			layoutManager.setUpLayout(g);

		} finally {
			g.getGraph().getModel().endUpdate();
		}
	}

	public Job getJob() {
		// return jobCell.getJob();
		return job;
	}

	public List<Cell> getCells() {
		ArrayList<Cell> cells = new ArrayList<Cell>();
		for (int i = 0; i < jobCell.getVisualization().getChildCount(); ++i)
			cells.add((Cell) jobCell.getVisualization().getChildAt(i)
					.getValue());
		return cells;
	}

	public JobCell getJobCell() {
		return jobCell;
	}

	public void destroy(Graph graph) {
		if (jobCell != null) {
			graph.getGraph().removeCells(
					new Object[] { jobCell.getVisualization() });
		}
	}

	public PortCell getInPortCell(Port pi) {
		return inports.get(pi);
	}

	public PortCell getOutPortCell(Port po) {
		return outports.get(po);
	}

}
