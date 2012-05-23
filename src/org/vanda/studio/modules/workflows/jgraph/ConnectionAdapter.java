package org.vanda.studio.modules.workflows.jgraph;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.modules.workflows.Model.ConnectionSelection;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class ConnectionAdapter implements Adapter {
	public Connection cc;

	public ConnectionAdapter(Connection cc) {
		this.cc = cc;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void remove(mxICell parent) {
		if (cc != null) {
			WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
			Token address = cc.address;
			if (address != null && wa.removeConnection(address) != null)
				wa.workflow.removeConnection(address);
		}
	}

	@Override
	public void update(mxGraph graph, mxICell parent, mxICell cell) {
		mxIGraphModel model = graph.getModel();
		WorkflowAdapter wa = (WorkflowAdapter) model.getValue(parent);
		if (cc != null) {
			// a previously loaded connection is updated, don't change anything
			Token address = cc.address;
			if (wa.getConnection(address) == null) {
				wa.setConnection(address, cell);
				// propagate value change to selection listeners
				if (graph.getSelectionCell() == cell)
					graph.setSelectionCell(cell);
			}
			assert (wa.getConnection(address) == cell);
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

				Connection cc = new Connection(
						((JobAdapter) sparval).job.getAddress(),
						((PortAdapter) sval).port,
						((JobAdapter) tparval).job.getAddress(),
						((PortAdapter) tval).port);
				wa.putInter(cc, cell);
				cell.setValue(new ConnectionAdapter(cc));
				if (wa.workflow != null)
					wa.workflow.addConnection(cc);
			}
		}
	}

	@Override
	public void prependPath(LinkedList<Token> path) {
		// do nothing
	}

	@Override
	public void setSelection(Model m, List<Token> path) {
		if (cc != null)
			m.setSelection(new ConnectionSelection(path, cc.address));
		else
			m.setSelection(null);
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		WorkflowAdapter wa = (WorkflowAdapter) parent.getValue();
		wa.setConnection(cc.address, cell);
	}

	@Override
	public mxICell dereference(ListIterator<Token> path, mxICell current) {
		return null;
	}

	@Override
	public boolean inModel() {
		return cc != null && cc.address != null;
	}

}
