package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class OutputPortAdapter extends JobAdapter {

	public OutputPortAdapter(Job job) {
		super(job);
	}

	@Override
	public OutputPortAdapter clone() throws CloneNotSupportedException {
		return new OutputPortAdapter(job.clone());
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		// NOT NECESSARY super.onResize(graph, parent, cell);
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(model
				.getParent(cell));
		mxGeometry geo = model.getGeometry(cell);
		if (wa.getChild(job.getAddress()) == cell
				&& parent.getGeometry() != null) {
			double diff = parent.getGeometry().getWidth()
					- InputPortAdapter.SIZE;
			if (geo.getX() != diff || geo.getWidth() != InputPortAdapter.SIZE) {
				geo.setX(diff);
				geo.setHeight(InputPortAdapter.SIZE);
				geo.setWidth(InputPortAdapter.SIZE);
				// geo.setRelative(true);
				cell.setGeometry(geo);
			}
			mxICell gparent = parent.getParent();
			if (gparent != null) {
				// TODO optimize
				int index = wa.workflow.getOutputPorts().indexOf(
						job.getInputPorts().get(0));
				for (int i = 0; i < gparent.getChildCount(); i++) {
					mxICell ch = gparent.getChildAt(i);
					if (ch.getValue() instanceof PortAdapter
							&& !((PortAdapter) ch.getValue()).input
							&& ((PortAdapter) ch.getValue()).port == index) {
						mxGeometry geo2 = ch.getGeometry();
						if (geo2.getY() != geo.getY()) {
							geo2.setY((geo.getY() + 0.5 * geo.getHeight() + 2)
									/ gparent.getGeometry().getHeight());
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
