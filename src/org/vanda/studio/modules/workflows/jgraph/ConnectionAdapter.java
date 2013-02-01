package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.Model.ConnectionSelection;
import org.vanda.workflows.hyper.ConnectionKey;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class ConnectionAdapter implements Adapter {
	public ConnectionKey cc;

	// public Token variable;

	public ConnectionAdapter(ConnectionKey cc) {
		this.cc = cc;
	}

	@Override
	public ConnectionAdapter clone() {
		return new ConnectionAdapter(cc);
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void onRemove(mxICell parent) {
		if (cc != null) {
			WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
			if (wa.removeConnection(cc) != null && cc.target.isInserted())
				wa.workflow.removeConnection(cc);
		}
	}

	@Override
	public void onInsert(mxGraph graph, mxICell parent, mxICell cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(parent);
		
		// not in the model -> hand-drawn edge
		if (cc == null) {
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				PortAdapter sval = (PortAdapter) model.getValue(source);
				PortAdapter tval = (PortAdapter) model.getValue(target);
				JobAdapter sparval = (JobAdapter) model.getValue(model
						.getParent(source));
				JobAdapter tparval = (JobAdapter) model.getValue(model
						.getParent(target));

				cc = new ConnectionKey(tparval.job, tval.port);
				wa.setConnection(cc, cell);
				if (wa.workflow != null)
					wa.workflow.addConnection(cc,
							sparval.job.bindings.get(sval.port));
			}
		} else
			wa.setConnection(cc, cell);
	}

	@Override
	public void setSelection(Model m) {
		if (cc != null)
			m.setSelection(new ConnectionSelection(m.getRoot(), cc));
		else
			m.setSelection(null);
		// TODO no nesting support here because of m.getRoot()
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
		wa.setConnection(cc, cell);
	}

	@Override
	public boolean inModel() {
		return cc != null && cc.target.isInserted();
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		// ignore
	}

}
