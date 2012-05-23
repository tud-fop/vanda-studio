package org.vanda.studio.modules.workflows.jgraph;

import java.util.ListIterator;

import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class CompositeJobAdapter extends JobAdapter {

	public CompositeJobAdapter(Job job) {
		super(job);
	}

	@Override
	public CompositeJobAdapter clone() throws CloneNotSupportedException {
		return new CompositeJobAdapter(job.clone());
	}

	/**
	 * keeps the size of a cell big enough to contain all its children properly
	 * 
	 * @param cell
	 */
	@Override
	protected void preventTooSmallNested(mxGraph graph, Object cell) {
		mxIGraphModel model = graph.getModel();
		// Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);

		double minWidth = 0;
		double minHeight = 0;

		// determine minimum bounds of cell that contains children
		for (int i = 0; i < model.getChildCount(cell); i++) {
			mxCell child = (mxCell) model.getChildAt(cell, i);

			if (child.getValue() instanceof JobAdapter) {
				double childRightBorder = child.getGeometry().getX()
						+ child.getGeometry().getWidth();
				double childBottomBorder = child.getGeometry().getY()
						+ child.getGeometry().getHeight();
				if (childRightBorder > minWidth) {
					minWidth = childRightBorder;
				}
				if (childBottomBorder > minHeight) {
					minHeight = childBottomBorder;
				}
			}
		}

		// adjust x coordinate of cell according to appropriate size
		if (geo.getWidth() < minWidth && !model.isCollapsed(cell)) {
			geo.setWidth(minWidth);
			/*
			 * if (geo.getX() > hj.getX()) { geo.setX(hj.getX() +
			 * hj.getWidth() - minWidth); }
			 */
		}

		// adjust y coordinate of cell according to appropriate size
		if (geo.getHeight() < minHeight && !model.isCollapsed(cell)) {
			geo.setHeight(minHeight);
			/*
			 * if (geo.getY() > hj.getY()) { geo.setY(hj.getY() +
			 * hj.getHeight() - minHeight); }
			 */
		}

		// set the new geometry and refresh graph to make changes visible
		model.setGeometry(cell, geo);
		graph.refresh();
	}

	protected mxICell findWorkflow(mxICell cell) {
		for (int i = 0; i < cell.getChildCount(); i++) {
			mxICell child = cell.getChildAt(i);
			Object value = child.getValue();
			if (value instanceof WorkflowAdapter)
				return child;
		}
		return null;
	}

	@Override
	public void sizeChanged(mxGeometry geo, mxGraph graph, mxICell cell) {
		mxICell child = findWorkflow(cell);
		if (child != null) {
			child.setGeometry(new mxGeometry(2, 2, job.getWidth() - 4, job
					.getHeight() - 4));
			graph.refresh();
		}
	}

	@Override
	public mxICell dereference(ListIterator<Token> path, mxICell current) {
		mxICell child = findWorkflow(current);
		if (child != null)
			return ((Adapter) child.getValue()).dereference(path, child);
		else
			return null;
	}
	
	public mxICell renderWorkflowCell(mxGraph graph, mxICell cell, MutableWorkflow hwf) {
		// two reasons why we might not know about hwf:
		// a) it is not in the graph
		// b) it is in the graph, but because of (complex) drag'n'drop
		// check whether we have case b)
		mxICell wfcell = findWorkflow(cell);
		if (wfcell != null) {
			// case b) -- make new WorkflowAdapter as the old one is
			// most likely obsolete
			wfcell.setValue(new WorkflowAdapter(hwf));
			for (int i = 0; i < cell.getChildCount(); i++) {
				mxICell cl = cell.getChildAt(i);
				((Adapter) cl.getValue()).register(cell, cl);
			}
		} else {
			// case a)
			mxGeometry geo = null;
			mxGeometry geop = cell.getGeometry();
			if (geop != null) {
				geo = new mxGeometry(2, 2, geop.getWidth() - 4,
						geop.getHeight() - 4);
				geo.setRelative(false);
			}

			wfcell = new mxCell(new WorkflowAdapter(hwf), geo, "workflow");
			((mxCell) wfcell).setVertex(true);

			graph.addCell(wfcell, cell);
		}
		return wfcell;
	}
}
