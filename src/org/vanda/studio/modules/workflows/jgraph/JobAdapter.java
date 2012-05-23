package org.vanda.studio.modules.workflows.jgraph;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.modules.workflows.Model.JobSelection;
import org.vanda.studio.util.TokenSource.Token;

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
	public JobAdapter clone() throws CloneNotSupportedException {
		return new JobAdapter(job.clone());

	}

	@Override
	public String getName() {
		return job.getItem().getName();
	}

	@Override
	public void remove(mxICell parent) {
		WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
		Token address = job.getAddress();
		if (address != null && wa.removeChild(address) != null)
			wa.workflow.removeChild(address);
	}

	@Override
	public void update(mxGraph graph, mxICell parent, mxICell cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
				.getParent(cell));
		mxGeometry geo = model.getGeometry(cell);

		if (job.getAddress() == null) {
			// set dimensions of job
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			job.setDimensions(dim);
			wa.putInter(job, (mxICell) cell);
			if (wa.workflow != null)
				wa.workflow.addChild(job);
		} else {
			if (wa.getChild(job.getAddress()) == null)
				wa.setChild(job.getAddress(), cell);
			// the following condition can be violated when dragging stuff
			if (wa.getChild(job.getAddress()) == cell) {
				if (graph.isAutoSizeCell(cell))
					graph.updateCellSize(cell, true); // was:
														// resizeToFitLabel(cell)
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
	}

	protected void preventTooSmallNested(mxGraph graph, Object cell) {
		// do nothing
	}

	public void sizeChanged(mxGeometry geo, mxGraph graph, mxICell cell) {
		// do nothing
	}

	@Override
	public void prependPath(LinkedList<Token> path) {
		path.addFirst(job.getAddress());

	}

	@Override
	public void setSelection(Model m, List<Token> path) {
		m.setSelection(new JobSelection(path, job.getAddress()));
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
		wa.setChild(job.getAddress(), cell);
	}

	@Override
	public mxICell dereference(ListIterator<Token> path, mxICell current) {
		return null;
	}
}
