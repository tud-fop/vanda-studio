package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class InputPortAdapter extends JobAdapter {
	
	final static double HALF = 5;
	final static double SIZE = 2 * HALF;

	public InputPortAdapter(Job job) {
		super(job);
	}

	@Override
	public InputPortAdapter clone() throws CloneNotSupportedException {
		return new InputPortAdapter(job.clone());
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		// NOT NECESSARY super.onResize(graph, parent, cell);
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
				.getParent(cell));
		mxGeometry geo = model.getGeometry(cell);
		if (wa.getChild(job.getAddress()) == cell) {
			if (geo.getX() != 0 || geo.getWidth() != SIZE) {
				geo.setX(0);
				geo.setWidth(SIZE);
				geo.setHeight(SIZE);
				cell.setGeometry(geo);
			}
			mxICell gparent = parent.getParent();
			if (gparent != null) {
				// TODO optimize
				int index = wa.workflow.getInputPorts().indexOf(
						job.getOutputPorts().get(0));
				for (int i = 0; i < gparent.getChildCount(); i++) {
					mxICell ch = gparent.getChildAt(i);
					if (ch.getValue() instanceof PortAdapter
							&& ((PortAdapter) ch.getValue()).input
							&& ((PortAdapter) ch.getValue()).port == index) {
						mxGeometry geo2 = ch.getGeometry();
						if (geo2.getY() != geo.getY()) {
							geo2.setY((geo.getY() + 0.5 * geo.getHeight() + 2) / gparent.getGeometry().getHeight());
							ch.setGeometry(geo2);
						}
						break;
					}
				}
			}
			graph.refresh();
		}
	}

}
