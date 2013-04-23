package org.vanda.render.jgraph;

import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.JobView;
import org.vanda.view.View;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Jobs;
import org.vanda.workflows.hyper.Jobs.JobEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;

public class JobCell extends Cell {
	final LayoutManagerInterface layoutManager;
	final Job job;
	protected final org.vanda.workflows.hyper.Jobs.JobListener<Job> jobListerer;
	protected final JobViewListener jobViewListener;
	private class JobViewListener implements AbstractView.ViewListener<AbstractView> {

		@Override
		public void selectionChanged(AbstractView v) {
			getObservable().notify(new SelectionChangedEvent<Cell>(JobCell.this)); 
			
		}

		@Override
		public void markChanged(AbstractView v) {
			if (v.isMarked()) {
				visualization.setStyle(mxStyleUtils.addStylename(visualization.getStyle(),
					"highlighted"));
			} else {
				visualization.setStyle(mxStyleUtils.removeStylename(visualization.getStyle(),
					"highlighted"));
			}
			getObservable().notify(new MarkChangedEvent<Cell> (JobCell.this));
		}

		@Override
		public void highlightingChanged(AbstractView v) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private class JobListener implements Jobs.JobListener<Job> {
		@Override
		public void propertyChanged(Job j) {
			if (getX() != j.getX() || getY() != j.getY()
					|| getWidth() != j.getWidth()
					|| getHeight() != j.getHeight()) {
				double[] dim = { j.getX(), j.getY(), j.getWidth(), j.getHeight() };
				setDimensions(dim);
				mxGeometry ng = (mxGeometry) visualization.getGeometry().clone();
				ng.setX(j.getX());
				ng.setY(j.getY());
				ng.setWidth(j.getWidth());
				ng.setHeight(j.getHeight());
				visualization.setGeometry(ng);
				getObservable().notify(new Cell.PropertyChangedEvent<Cell>(JobCell.this));
			}
		}
		
	}

	public JobCell(final Graph graph, LayoutManagerInterface layoutManager, Job job) {
		this.layoutManager = layoutManager;
		this.job = job;
		this.jobListerer = new JobListener();
		this.jobViewListener = new JobViewListener();
		this.observable = new CellObservable();
		setDimensions(new double [] {job.getX(), job.getY(), job.getWidth(), job.getHeight()});
		// Register at JobView
		graph.getView().getJobView(job).getObservable().addObserver(new Observer<ViewEvent<AbstractView>> () {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				// TODO Auto-generated method stub
				event.doNotify(jobViewListener);
			}
			
		});
		
		// Register at Graph
		getObservable().addObserver(new Observer<CellEvent<Cell>> () {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(graph.getCellChangeListener());
			}
			
		});
		
		// Register at Job
		if (job.getObservable() != null)
			job.getObservable().addObserver(new Observer<Jobs.JobEvent<Job>> (){

			@Override
			public void notify(JobEvent<Job> event) {
				event.doNotify(jobListerer);
			}
			
		});
		
		// Create mxCell and add it to Graph
		graph.getGraph().getModel().beginUpdate();
		try {
		
			visualization = new mxCell(this);
			graph.getGraph().addCell(visualization, graph.getGraph().getDefaultParent());


		} finally {
			graph.getGraph().getModel().endUpdate();			
		}
		
		// Register at LayoutManager
		layoutManager.register(this);
	}

	@Override
	public String getType() {
		return "JobCell";
	}

	@Override
	public void onRemove(View view) {
		if (job != null)
			System.out.println("Job Removed JC");
			view.getWorkflow().removeChild(job);
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// the following is necessary if the job is not in the model
		// which happens in case of drag&drop (as opposed to render).
		if (!job.isInserted()) {
			mxGeometry geo = cell.getGeometry();
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			setDimensions(dim);
			job.setDimensions(dim);
			graph.getView().getWorkflow().addChild(job);
		}
		for (int i = 0; i < getVisualization().getChildCount(); ++i ) {
			((Cell) getVisualization().getChildAt(i).getValue()).updateLocation(job);
		}
	
	}

	@Override
	public void onResize(mxGraph graph) {
		mxIGraphModel model = graph.getModel();
		mxGeometry geo = model.getGeometry(visualization);
		if (graph.isAutoSizeCell(visualization))
			graph.updateCellSize(visualization, true); // was: resizeToFitLabel(cell)
		preventTooSmallNested(graph, visualization);
		graph.extendParent(visualization); // was: resizeParentOfCell(cell)

		if (geo.getX() != getX() || geo.getY() != getY()
				|| geo.getWidth() != getWidth()
				|| geo.getHeight() != getHeight()) {

			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			setDimensions(dim);
			job.setDimensions(dim);
			sizeChanged(geo, graph, visualization);
		}
	}

	private void sizeChanged(mxGeometry geo, mxGraph graph, mxICell cell) {
		// do nothing
		
	}

	private void preventTooSmallNested(mxGraph graph, mxICell cell) {
		// do nothing
		
	}

	@Override
	public void setSelection(View view) {
		JobView jv = view.getJobView(job);
		jv.setSelected(true);
	}

	public Job getJob() {
		return job;
	}

	@Override
	public AbstractView getView(View view) {
		return view.getJobView(job);
	}
	
	@Override 
	public String getLabel() {
		return job.getName();
		
	}

}
