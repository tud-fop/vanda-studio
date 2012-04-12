package org.vanda.studio.modules.terms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.modules.terms.JGraphRendering.Port;
import org.vanda.studio.modules.terms.Term.Connection;
import org.vanda.studio.modules.terms.Term.TermObject;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxTerminalChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

public class JGraphRenderer {
	
	protected JGraphRendering.Graph graph;
	protected ChangeListener changeListener;
	protected Map<TermObject, Object> nodes;
	protected Map<Connection, Object> edges;
	protected MultiplexObserver<Connection> connectionAddObservable;
	protected MultiplexObserver<Connection> connectionModifyObservable;
	protected MultiplexObserver<Connection> connectionRemoveObservable;
	protected MultiplexObserver<TermObject> objectAddObservable;
	protected MultiplexObserver<TermObject> objectModifyObservable;
	protected MultiplexObserver<TermObject> objectRemoveObservable;
	
	public JGraphRenderer() {
		nodes = new HashMap<TermObject, Object>();
		edges = new HashMap<Connection, Object>();
		connectionAddObservable = new MultiplexObserver<Connection>();
		connectionModifyObservable = new MultiplexObserver<Connection>();
		connectionRemoveObservable = new MultiplexObserver<Connection>();
		objectAddObservable = new MultiplexObserver<TermObject>();
		objectModifyObservable = new MultiplexObserver<TermObject>();
		objectRemoveObservable = new MultiplexObserver<TermObject>();
	
		graph = JGraphRendering.createGraph();
		
		// bind graph; for example, react on new, changed, or deleted elements
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
	}
	
	public void ensureAbsence(TermObject to) {
		Object cell = nodes.remove(to);
		if (cell != null) {
			graph.removeCells(new Object[] {cell});
		}
	}
	
	public void ensurePresence(TermObject to) {
		Object cell = nodes.get(to);
		if (cell == null) {
			JGraphRendering.render(to, graph);
		}
		else {
			// make sure the cell has the same properties as the TermObject to
			// for now, we are only interested in the geometry
			mxIGraphModel model = graph.getModel();
			mxGeometry geo = model.getGeometry(cell);
			if (geo.getX() != to.getX()
				|| geo.getY() != to.getY()
				|| geo.getWidth() != to.getWidth()
				|| geo.getHeight() != to.getHeight())
			{
				mxGeometry ng = (mxGeometry)geo.clone();
				ng.setX(to.getX());
				ng.setY(to.getY());
				ng.setWidth(to.getWidth());
				ng.setHeight(to.getHeight());
				model.setGeometry(cell, ng);
			}
		}
	}
	
	public Observable<Connection> getConnectionAddObservable() {
		return connectionAddObservable;
	}
	
	public Observable<Connection> getConnectionModifyObservable() {
		return connectionModifyObservable;
	}
	
	public Observable<Connection> getConnectionRemoveObservable() {
		return connectionRemoveObservable;
	}

	public mxGraph getGraph() {
		return graph;
	}
	
	public Observable<TermObject> getObjectAddObservable() {
		return objectAddObservable;
	}
	
	public Observable<TermObject> getObjectModifyObservable() {
		return objectModifyObservable;
	}
	
	public Observable<TermObject> getObjectRemoveObservable() {
		return objectRemoveObservable;
	}
	
	protected void updateEdge(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		Connection conn = null;
		assert (model.isEdge(cell));
		Object source = model.getTerminal(value, true);
		Object target = model.getTerminal(value, false);
		// ignore "unfinished" edges
		if (source != null && target != null) {
			Object sval = model.getValue(source);
			Object tval = model.getValue(target);
			Object sparval = model.getValue(model.getParent(source));
			Object tparval = model.getValue(model.getParent(target));
			
			assert(sval instanceof Port
				&& tval instanceof Port
				&& sparval instanceof TermObject
				&& tparval instanceof TermObject);
			assert(((Port)sval).index >= 0
				&& ((Port)sval).index < ((TermObject)sparval).getOutputPorts().length
				&& !((Port)sval).input
				&& ((Port)tval).index >= 0
				&& ((Port)tval).index < ((TermObject)tparval).getInputPorts().length
				&& ((Port)tval).input);
			
			if (value == null) {
				// check if a new edge is added
				conn = new Connection();
				if (edges.put(conn, cell) != null) {
					assert(false);
				}
			}
			else {
				conn = (Connection)value;
			}
			conn.source	= (TermObject)sparval;
			conn.target	= (TermObject)tparval;
			conn.sourceIdx = ((Port)sval).index;
			conn.targetIdx = ((Port)tval).index;
			// notify
			if (conn != value)
				connectionAddObservable.notify(conn);
			else
				connectionModifyObservable.notify(conn);
		}
	}
	
	protected void updateNode(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof TermObject);
		TermObject to = (TermObject)value;
		if (geo.getX() != to.getX()
			|| geo.getY() != to.getY()
			|| geo.getWidth() != to.getWidth()
			|| geo.getHeight() != to.getHeight())
		{
			double[] dim
				= { geo.getX(), geo.getY(), geo.getWidth(), geo.getHeight() };
			to.setDimensions(dim);
			// notify
			objectModifyObservable.notify(to);
		}
	}
	
	protected class ChangeListener implements mxIEventListener {
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			mxIGraphModel model = graph.getModel();
			mxUndoableEdit edit = (mxUndoableEdit)evt.getProperty("edit");
			List<mxUndoableChange> changes = edit.getChanges();
			for (mxUndoableChange c : changes) {
				// process the following changes:
				// - child change (add/remove)
				// - value change
				// - TODO geometry change
				// - TODO terminal change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange)c;
					Object cell = cc.getChild();
					Object value = model.getValue(cell);
					if (cc.getParent() == null) {
						// something has been removed
						if (value instanceof TermObject) {
							if (nodes.remove(value) != null) {
								// notify
								objectRemoveObservable.notify((TermObject)value);
							}
						}
						else if (value instanceof Connection) {
							if (edges.remove(value) != null) {
								// notify
								connectionRemoveObservable.notify((Connection)value);
							}
						}
					}
					else {
						// something has been added
						if (value instanceof TermObject) {
							Object oldcell = nodes.put((TermObject)value, cell);
							if (oldcell == null) {
								// check geometry and notify if necessary
								updateNode(cell);
								// FIXME: currently, objectAdd is not used!
							}
							else {
								assert (cell == oldcell);
							}
						}
						else if (value instanceof Connection) {
							// TODO currently this is not supposed to happen
							assert(false);
						}
						else if (value == null) {
							// check if a new edge is added
							if (model.isEdge(cell)) {
								updateEdge(cell);
							}
						}
					}
				}
				else if (c instanceof mxValueChange) {
					assert(false);
				}
				else if (c instanceof mxGeometryChange) {
					Object cell = ((mxGeometryChange)c).getCell();
					if (model.getValue(cell) instanceof TermObject)
						updateNode(cell);
				}
				else if (c instanceof mxTerminalChange) {
					// just do the same thing as for an added edge
					Object cell = ((mxTerminalChange)c).getCell();
					updateEdge(cell);
				}
			}
		}
	}

}
