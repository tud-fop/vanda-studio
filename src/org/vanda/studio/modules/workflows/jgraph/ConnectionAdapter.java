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
			if (wa.removeConnection(cc) != null)
				wa.workflow.removeConnection(cc);
		}
	}

	@Override
	public void onInsert(mxGraph graph, mxICell parent, mxICell cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(parent);
		if (cc != null) {
			// a previously loaded connection is updated, don't change anything
			if (wa.getConnection(cc) == null) {
				wa.setConnection(cc, cell);
				// propagate value change to selection listeners
				if (graph.getSelectionCell() == cell)
					graph.setSelectionCell(cell);
			}
			assert (wa.getConnection(cc) == cell);
		} else {
			// a new connection has been inserted by the user via GUI
			Object source = model.getTerminal(cell, true);
			Object target = model.getTerminal(cell, false);

			// ignore "unfinished" edges
			if (source != null && target != null) {
				Object sval = model.getValue(source);
				Object tval = model.getValue(target);
				Object sparval = model.getValue(model.getParent(source));
				Object tparval = model.getValue(model.getParent(target));

				assert (sval instanceof PortAdapter
						&& tval instanceof PortAdapter
						&& sparval instanceof JobAdapter && tparval instanceof JobAdapter);

				ConnectionKey cc = new ConnectionKey(
						((JobAdapter) tparval).job.getAddress(),
						((PortAdapter) tval).port);
				wa.putInter(cc, cell);
				cell.setValue(new ConnectionAdapter(cc));
				if (wa.workflow != null)
					wa.workflow.addConnection(cc,
							((JobAdapter) sparval).job.outputs
									.get(((PortAdapter) sval).port));
			}
		}
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
		return cc != null; // FIXME ???? && cc.address != null;
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		// ignore
	}

}
