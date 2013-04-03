package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.modules.workflows.model.WorkflowDecoration;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.JobSelection;
import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class JobAdapter implements Adapter, Cloneable {
	public final Job job;

	public JobAdapter(Job job) {
		this.job = job;
	}

	@Override
	public String getName() {
		return job.getElement().getName();
	}

	@Override
	public void onRemove(mxICell parent) {
		WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
		if (wa.removeChild(job) != null)
			wa.workflow.removeChild(job);
	}

	@Override
	public void onInsert(mxGraph graph, mxICell parent, mxICell cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
				.getParent(cell));
		mxGeometry geo = model.getGeometry(cell);

		// we are new here, so tell the workflow adapter that we are there
		wa.setChild(job, cell);

		// the following is necessary if the job is not in the model
		// which happens in case of drag&drop (as opposed to render)
		// OR for the palette...
		if (!job.isInserted()) {
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			job.setDimensions(dim);
			if (wa.workflow != null) {
				wa.workflow.addChild(job);
				updateLocations(cell);
			}
		}
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
				.getParent(cell));
		mxGeometry geo = model.getGeometry(cell);
		if (wa.getChild(job) == cell) {
			if (graph.isAutoSizeCell(cell))
				graph.updateCellSize(cell, true); // was: resizeToFitLabel(cell)
			preventTooSmallNested(graph, cell);
			graph.extendParent(cell); // was: resizeParentOfCell(cell)

			if (geo.getX() != job.getX() || geo.getY() != job.getY()
					|| geo.getWidth() != job.getWidth()
					|| geo.getHeight() != job.getHeight()) {

				double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
						geo.getHeight() };
				job.setDimensions(dim);
				sizeChanged(geo, graph, cell);
			}
		}
	}

	protected void preventTooSmallNested(mxGraph graph, Object cell) {
		// do nothing
	}

	public void sizeChanged(mxGeometry geo, mxGraph graph, mxICell cell) {
		// do nothing
	}

	@Override
	public void setSelection(WorkflowDecoration m) {
		m.setSelection(new JobSelection(m.getRoot(), job));
		// TODO no nesting support here because of m.getRoot()
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
		wa.setChild(job, cell);
	}

	@Override
	public boolean inModel() {
		return job.isInserted();
	}

	protected void updateLocations(mxICell cell) {
		for (int i = 0; i < cell.getChildCount(); i++) {
			Object value = cell.getChildAt(i).getValue();
			if (value instanceof LocationAdapter)
				((LocationAdapter) value).updateLocation(job);
		}
	}
}
