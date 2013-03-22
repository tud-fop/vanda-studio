package org.vanda.render.jgraph;

import java.util.Observable;
import java.util.Observer;

import org.vanda.view.AbstractView;
import org.vanda.view.JobView;
import org.vanda.view.View;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Jobs;
import org.vanda.workflows.hyper.Jobs.JobEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class JobCell extends Cell {
	final LayoutManager layoutManager;
	final Job job;
	protected final org.vanda.workflows.hyper.Jobs.JobListener<Job> jobListerer;
	private class JobViewObserver implements Observer {
		@Override
		public void update(Observable arg0, Object arg1) {
			notify(); // CellSelectionListener in graph
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
			}

			// TODO notify -> graph geo update
//			if (graph.isAutoSizeCell(cell))
//				graph.updateCellSize(cell, true);
//			graph.refresh();
			
		}
		
	}
		
	public JobCell(Graph graph, LayoutManager layoutManager, Job job) {
		this.layoutManager = layoutManager;
		this.job = job;
		this.jobListerer = new JobListener();
		
		graph.getView().getJobView(job).addObserver(new JobViewObserver());
		addObserver(graph.getCellSelectionListener());
		
		job.getObservable().addObserver(new org.vanda.util.Observer<Jobs.JobEvent<Job>> (){

			@Override
			public void notify(JobEvent<Job> event) {
				event.doNotify(jobListerer);
			}
			
		});
		
		//TODO some initial layout of cell?
		
		graph.getGraph().getModel().beginUpdate();
		try {
		
			visualization = (mxCell) graph.getGraph().insertVertex(graph.getGraph().getDefaultParent(), null, this,
				getX(), getY(), getWidth(), getHeight(),
				layoutManager.getStyleName(this));
			visualization.setConnectable(false);
			
			if (graph.getGraph().isAutoSizeCell(visualization))
				graph.getGraph().updateCellSize(visualization, true);

		} finally {
			graph.getGraph().getModel().endUpdate();			
		}
	}

	@Override
	public String getType() {
		return "JobCell";
	}

	@Override
	public void onRemove(mxICell previous) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean inModel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onInsert(mxGraph graph) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize(mxGraph graph) {
		mxIGraphModel model = graph.getModel();
		mxGeometry geo = model.getGeometry(visualization);
		//if (wa.getChild(job) == cell) {
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
				sizeChanged(geo, graph, visualization);
			}
		//}		
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

}
