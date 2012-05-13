package org.vanda.studio.modules.workflows.jgraph;


import org.vanda.studio.model.hyper.CompositeHyperJob;
import org.vanda.studio.model.hyper.HyperConnection;
import org.vanda.studio.model.hyper.HyperJob;
import org.vanda.studio.model.hyper.HyperWorkflow;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;

import com.mxgraph.view.mxGraph;

// TODO do something about all those unchecked conversions and raw types

public class Adapter {

	protected GraphRenderer renderer;
	protected MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> addObserver;
	protected MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> modifyObserver;
	protected MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> removeObserver;
	protected MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> connectObserver;
	protected MultiplexObserver<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> disconnectObserver;
	
	public <F, V> Adapter(HyperWorkflow<F, V> root) {
		renderer = new GraphRenderer(root);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <F, V> void bind(HyperWorkflow<F, V> hwf) {
		hwf.getAddObservable().addObserver((Observer) addObserver);
		hwf.getModifyObservable().addObserver((Observer) modifyObserver);
		hwf.getRemoveObservable().addObserver((Observer) removeObserver);
		hwf.getConnectObservable().addObserver((Observer) connectObserver);
		hwf.getDisconnectObservable().addObserver((Observer) disconnectObserver);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <F, V> void unbind(HyperWorkflow<F, V> hwf) {
		// XXX this could blow up big time
		hwf.getAddObservable().removeObserver((Observer) addObserver);
		hwf.getModifyObservable().removeObserver((Observer) modifyObserver);
		hwf.getRemoveObservable().removeObserver((Observer) removeObserver);
		hwf.getConnectObservable().removeObserver((Observer) connectObserver);
		hwf.getDisconnectObservable().removeObserver((Observer) disconnectObserver);
		for (HyperJob<V> c : hwf.getChildren()) {
			if (c instanceof CompositeHyperJob<?, ?, ?, ?>) {
				CompositeHyperJob<?, V, ?, ?> chj = (CompositeHyperJob<?, V, ?, ?>) c;
				unbind(chj.getWorkflow());
			}
		}
	}
	
	public mxGraph getGraph() {
		return renderer.graph;
	}
	
//----------------------------------------------------------------------------------------------
	/*
	protected HyperWorkflow<?, ?> root;
	protected Graph graph;
	protected ChangeListener changeListener;
	protected Map<Object, mxICell> translation;

	public <F, V> Adapter(HyperWorkflow<F, V> root) {
		this.root = root;
		translation = new HashMap<Object, mxICell>();
		graph = new Graph();

		// bind defaultParent of the graph and the root hyperworkflow
		// to each other and save them in the node mapping
		((mxCell) graph.getDefaultParent()).setValue(root);
		// translation.put(root, (mxICell) graph.getDefaultParent());
		// translation.put(null, (mxICell) graph.getDefaultParent());
		
		// bind graph; for example, react on new, changed, or deleted elements
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);

		render(null, root);
	}

	@SuppressWarnings({ "unchecked"})
	private <F, V, IV> void render(CompositeHyperJob<F, V, IV, ?> parent,
			HyperWorkflow<F, IV> hwf) {
		if (!translation.containsKey(hwf)) {
			mxCell cell = null;
			if (parent != null) {
				mxICell parentCell = translation.get(parent);
				// cell = graph.insertVertex(parentCell, "", hwf, 0, 0, 1, 1,
				// "");
				mxGeometry geo = null;
				mxGeometry geop = parentCell.getGeometry();
				if (geop != null) {
					geo = new mxGeometry(0, 0, geop.getWidth(),
							geop.getHeight());
					geo.setRelative(true);
				}

				cell = new mxCell(hwf, geo, "");
				cell.setVertex(true);

				graph.addCell(cell, parentCell);
			} else
				cell = (mxCell) graph.getDefaultParent();
			translation.put(hwf, cell);
			// XXX this could blow up big time
			bind(hwf);
			for (HyperJob<IV> c : hwf.getChildren())
				render(hwf, c);
			for (HyperConnection<IV> cc : hwf.getConnections())
				render(hwf, cc);
		}
	}

	@SuppressWarnings("unchecked")
	private <F, V, IV> void render(HyperWorkflow<F, V> parent, HyperJob<V> hj) {
		if (!translation.containsKey(hj)) {
			Object parentCell = translation.get(parent);
			hj.selectRenderer(JobRendering.getRendererAssortment()).render(hj,
					graph, parentCell);
			if (hj instanceof CompositeHyperJob<?, ?, ?, ?>) {
				// render recursively
				render((CompositeHyperJob<F, V, IV, ?>) hj,
						((CompositeHyperJob<F, V, IV, ?>) hj).getWorkflow());
			}
		}
	}

	private <F, V> void render(HyperWorkflow<F, V> parent, HyperConnection<V> cc) {
		Object cell = translation.get(cc);

		if (cell == null) {
			Object parentCell = translation.get(parent);
			assert (parentCell != null);
			if (parentCell == null) {
				parentCell = graph.getDefaultParent();
			}

			mxICell source = (mxICell) translation.get(cc.getSource());
			mxICell target = (mxICell) translation.get(cc.getTarget());

			if (source != null && target != null) {
				assert (source.getValue() == cc.getSource());
				assert (target.getValue() == cc.getTarget());

				// ports are children of a vertex mxCell in the following order:
				// input ports, then output ports
				source = source.getChildAt(cc.getSource().getInputPorts().size() + cc.getSourcePort());
				// target ports are input ports, hence, they come first as children
				target = target.getChildAt(cc.getTargetPort());

				graph.getModel().beginUpdate();
				try {
					// create new edge based on given parent and retrieved
					// source and target
					mxCell edge = new mxCell(cc, new mxGeometry(), null);
					// edge.setId(null);
					edge.setEdge(true);
					edge.setSource(source);
					edge.setTarget(target);

					// add edge to the graph
					graph.addCell(edge, parentCell);

					// NOTE: g.insertEdge(parent, id, value, source, target)
					// does NOT work correctly. It fires too many
					// mxChildChanges
					// and does not insert the edge within the given parent
					// the problem seems to be the method
					// g.createEdge(parent, id, value, source, target,
					// style)
					// FIXME maybe the problem is not the jgraph API but
					// some
					// weird code I wrote concerning inner target ports
				} finally {
					graph.getModel().endUpdate();
				}
			} else
				assert (false);
		}

	}

	Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> addObserver = new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			// XXX type supernova
			render((HyperWorkflow) event.fst, (HyperJob) event.snd);
		}
	};

	Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> modifyObserver = new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {

		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			Object cell = translation.get(event.snd);
			mxIGraphModel model = graph.getModel();
			mxGeometry geo = model.getGeometry(cell);
			if (geo.getX() != event.snd.getX()
					|| geo.getY() != event.snd.getY()
					|| geo.getWidth() != event.snd.getWidth()
					|| geo.getHeight() != event.snd.getHeight()) {
				mxGeometry ng = (mxGeometry) geo.clone();
				ng.setX(event.snd.getX());
				ng.setY(event.snd.getY());
				ng.setWidth(event.snd.getWidth());
				ng.setHeight(event.snd.getHeight());
				model.setGeometry(cell, ng);
			}
		}
	};

	Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>> removeObserver = new Observer<Pair<HyperWorkflow<?, ?>, HyperJob<?>>>() {
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperJob<?>> event) {
			Object cell = translation.get(event.snd);
			if (cell != null) {
				graph.removeCells(new Object[] { cell });
				if (event.snd instanceof CompositeHyperJob<?, ?, ?, ?>) {
					unbind(((CompositeHyperJob<?, ?, ?, ?>) event.snd)
							.getWorkflow());
				}
			}
		}
	};

	Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> connectObserver = new Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>() {
		@SuppressWarnings({ "unchecked" })
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperConnection<?>> event) {
			// XXX type supernova
			render((HyperWorkflow) event.fst, (HyperConnection) event.snd);
		}
	};

	Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>> disconnectObserver = new Observer<Pair<HyperWorkflow<?, ?>, HyperConnection<?>>>() {
		@Override
		public void notify(Pair<HyperWorkflow<?, ?>, HyperConnection<?>> event) {
			mxICell cell = translation.remove(event.snd);
			if (cell != null) {
				assert (cell.getValue() == event.snd);
				assert (graph.isCellDeletable(cell));
				graph.removeCells(new Object[] { cell });
				assert (!graph.getModel().contains(cell));
				graph.refresh(); // XXX necessary?
			}
		}
	};

	@SuppressWarnings({ "unchecked" })
	<F, V> void bind(HyperWorkflow<F, V> hwf) {
		// XXX this could blow up big time
		hwf.getAddObservable().addObserver((Observer) addObserver);
		hwf.getModifyObservable().addObserver((Observer) modifyObserver);
		hwf.getRemoveObservable().addObserver((Observer) removeObserver);
		hwf.getConnectObservable().addObserver((Observer) connectObserver);
		hwf.getDisconnectObservable().addObserver(
				(Observer) disconnectObserver);
	}
	
	@SuppressWarnings({ "unchecked" })
	<F, V> void unbind(HyperWorkflow<F, V> hwf) {
		// XXX this could blow up big time
		hwf.getAddObservable().removeObserver((Observer) addObserver);
		hwf.getModifyObservable().removeObserver((Observer) modifyObserver);
		hwf.getRemoveObservable().removeObserver((Observer) removeObserver);
		hwf.getConnectObservable().removeObserver((Observer) connectObserver);
		hwf.getDisconnectObservable().removeObserver(
				(Observer) disconnectObserver);
		for (HyperJob<V> c : hwf.getChildren()) {
			if (c instanceof CompositeHyperJob<?, ?, ?, ?>) {
				CompositeHyperJob<?, V, ?, ?> chj = (CompositeHyperJob<?, V, ?, ?>) c;
				unbind(chj.getWorkflow());
			}
		}
	}

	@SuppressWarnings({ "unchecked" })
	protected void addNode(mxICell cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof HyperJob<?>);
		HyperJob<?> hj = (HyperJob<?>) value;

		// make sure the node does not already exist (i.e. is in map)
		if (model.getParent(cell) != null && !translation.containsKey(hj)) {
			// add to map
			translation.put(hj, cell);

			// add hyperjob to parent hyperworkflow
			// XXX this could blow up, typewise
			Object parent = ((mxCell) cell).getParent().getValue();
			assert (parent instanceof HyperWorkflow<?, ?>);
			
			// if the current HyperJob exists already as child of its parenting cell
			// because it was loaded from a previously saved file
			// only set the parenting HyperWorkflow correctly
			if (((HyperWorkflow<?, ?>) parent).getChildren().contains(hj)) {
				hj.setParent((HyperWorkflow) parent);
			} else {
				// the HyperJob was just added by the user via the GUI, hence
				// add it to its parent cell's HyperWorkflow
				((HyperWorkflow<?, ?>) parent).addChild((HyperJob) hj);
			}
			
			// set dimensions of to
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			hj.setDimensions(dim);

			System.out.println("JGraphRenderer.addNode(): added "
					+ hj.getName());
		}
	}

	public mxGraph getGraph() {
		return graph;
	}

//	private void preventTooSmallNested(Object cell) {
//		mxIGraphModel model = graph.getModel();
//		Object value = model.getValue(cell);
//		mxGeometry geo = model.getGeometry(cell);
//		assert (model.isVertex(cell) && value instanceof HyperJob<?>);
//		HyperJob<?> hj = (HyperJob<?>) value;
//
//		if (hj instanceof CompositeHyperJob<?, ?, ?, ?>) {
//			double minWidth = 0;
//			double minHeight = 0;
//
//			// determine minimum bounds of cell that contains children
//			for (int i = 0; i < model.getChildCount(cell); i++) {
//				mxCell child = (mxCell) model.getChildAt(cell, i);
//
//				if (child.getValue() instanceof HyperJob<?>) {
//					double childRightBorder = child.getGeometry().getX()
//							+ child.getGeometry().getWidth();
//					double childBottomBorder = child.getGeometry().getY()
//							+ child.getGeometry().getHeight();
//					if (childRightBorder > minWidth) {
//						minWidth = childRightBorder;
//					}
//					if (childBottomBorder > minHeight) {
//						minHeight = childBottomBorder;
//					}
//				}
//			}
//
//			// adjust x coordinate of cell according to appropriate size
//			if (geo.getWidth() < minWidth && !model.isCollapsed(cell)) {
//				geo.setWidth(minWidth);
//				if (geo.getX() > hj.getX()) {
//					geo.setX(hj.getX() + hj.getWidth() - minWidth);
//				}
//			}
//
//			// adjust y coordinate of cell according to appropriate size
//			if (geo.getHeight() < minHeight && !model.isCollapsed(cell)) {
//				geo.setHeight(minHeight);
//				if (geo.getY() > hj.getY()) {
//					geo.setY(hj.getY() + hj.getHeight() - minHeight);
//				}
//			}
//
//			// set the new geometry and refresh graph to make changes visible
//			model.setGeometry(cell, geo);
//			graph.refresh();
//		} else
//			return;
//	}

	@SuppressWarnings({ "unchecked" })
	protected void updateEdge(mxICell cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		HyperConnection<?> conn = null;
		assert (model.isEdge(cell));
		Object source = model.getTerminal(cell, true);
		Object target = model.getTerminal(cell, false);

		// ignore "unfinished" edges
		if (source != null && target != null) {
			Object sval = model.getValue(source);
			Object tval = model.getValue(target);
			Object sparval = model.getValue(model.getParent(source));
			Object tparval = model.getValue(model.getParent(target));

			assert (sval instanceof PortAdapter && tval instanceof PortAdapter
					&& sparval instanceof HyperJob<?> && tparval instanceof HyperJob<?>);

			// a previously loaded connection is updated, don't change anything
			if (value instanceof HyperConnection<?>) {
				conn = (HyperConnection<?>) value;
				if (!translation.containsKey(conn))
					translation.put(conn, cell);
			} else {
				// a new connection has been inserted by the user via GUI
				conn = new HyperConnection((HyperJob<?>) sparval,
						((PortAdapter) sval).index, (HyperJob<?>) tparval,
						((PortAdapter) tval).index);
				
				//assert (conn.getSource().getParent() == conn.getTarget().getParent());
				if (conn.getSource().getParent() != conn.getTarget().getParent()) 
					assert(false);
					
				//assert (conn.getSource().getParent() == cell.getParent().getValue());
				if (conn.getSource().getParent() != cell.getParent().getValue()) 
					assert(false);
				
				translation.put(conn, cell);
				model.setValue(cell, conn);
				((HyperWorkflow) cell.getParent().getValue()).addConnection((HyperConnection) conn);
			}
		}
	}

	protected void updateNode(Object cell) {
		mxIGraphModel model = graph.getModel();
		Object value = model.getValue(cell);
		mxGeometry geo = model.getGeometry(cell);
		assert (model.isVertex(cell) && value instanceof HyperJob<?>);
		HyperJob<?> hj = (HyperJob<?>) value;

		// TODO maybe adjust hj.dimensions within each of the following called
		// functions?

		// prevent shrinking cell too much leading to label being too big
		// resizeToFitLabel(cell);
		// if (graph.isAutoSizeCell(cell))
		//	graph.updateCellSize(cell, true); // XXX was:
												// resizeToFitLabel(cell);

		// prevent resizing a NestedHyperworkflow too much, otherwise
		// its child nodes are moved outside of its bounds
		//preventTooSmallNested(cell);

		// resize parent cells if child nodes are resized over left or top
		// bounds
		//graph.extendParent(cell); // XXX was: resizeParentOfCell(cell);

		// check if changes occurred to the given cell
		if (geo.getX() != hj.getX() || geo.getY() != hj.getY()
				|| geo.getWidth() != hj.getWidth()
				|| geo.getHeight() != hj.getHeight()) {
			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			hj.setDimensions(dim);
		}

		// FIXME does not work properly
		// check if parent changed (only if both hwf and corresponding cell have
		// parents)
		
		// if (model.getParent(cell) != null && to.getParent() != null &&
		// !model.getParent(cell).equals(translation.get(to.getParent()))) {
		 
		// System.out.println("parent of " + to + " has changed to " +
		// ((HyperWorkflow) model.getValue(model.getParent(cell))) .getName());
		 
		// Hyperworkflow newParent = (Hyperworkflow) model.getValue(model
		// .getParent(cell)); objectRemoveObservable.notify(to);
		// to.setParent(newParent); objectAddObservable.notify(to);
		// graph.refresh(); }
		
	}

	protected class ChangeListener implements mxIEventListener {
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			mxIGraphModel model = graph.getModel();
			mxUndoableEdit edit = (mxUndoableEdit) evt.getProperty("edit");
			List<mxUndoableChange> changes = edit.getChanges();
			for (mxUndoableChange c : changes) {
				// process the following changes:
				// - child change (add/remove)
				// - value change
				// - geometry change
				// - ??? terminal change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange) c;
					mxICell cell = (mxICell) cc.getChild();
					Object value = model.getValue(cell);
					if (cc.getParent() == null) {
						// something has been removed
						if (value instanceof HyperJob<?>) {
							if (translation.remove(value) != null) {
								HyperWorkflow
										.removeChildGeneric((HyperJob<?>) value);
								
							}
						} else if (value instanceof HyperConnection<?>) {
							if (translation.remove(value) != null) {
								HyperWorkflow
										.removeConnectionGeneric((HyperConnection<?>) value);
							}
						}
					} else {
						// something has been added
						if (value instanceof HyperJob<?>) {
							addNode(cell);
						} else if (value instanceof HyperConnection<?>) {
							// happens when a loaded nhwf contains connection
							// and they are added to the graph
							updateEdge(cell);
						} else if (value == null || value.equals("")) {
							// check if a new edge is added
							if (model.isEdge(cell)) {
								updateEdge(cell);
							}
						}
					}
				} else if (c instanceof mxValueChange) {
					// fires when a connection was inserted and then any
					// component is moved to change its geometry
					// maybe this is the geometryChange of connections?

					
					// // assert (false);
					// System.out.println("mxValueChange of: " + ((mxCell)
					// ((mxValueChange) c).getCell()) .getValue());
					
				} else if (c instanceof mxGeometryChange) {
					Object cell = ((mxGeometryChange) c).getCell();
					if (model.getValue(cell) instanceof HyperJob<?>)
						updateNode(cell);
				} else if (c instanceof mxTerminalChange) {
					// NOTE this case should not happen
					// creation of a new edge or loading an edge calls
					// updateEdge already and as of now terminals do not
					// change, connection can only be created and/or removed
					
					// FIXME correction: this case should be removed entirely,
					// we do not care about terminal changes at all
					//assert (false);
					
					// just do the same thing as for an added edge
					// Object cell = ((mxTerminalChange) c).getCell();
					// updateEdge(cell);
				}
			}
		}
	}
	*/
}

/**
 * upon increasing a nodes size, also adjust the size of the parent cell
 * 
 * @param cell
 * 
 *            private void resizeParentOfCell(Object cell) { mxIGraphModel model
 *            = graph.getModel(); mxGeometry geo = model.getGeometry(cell);
 * 
 *            // node's left end is no longer inside its parent's bounding box
 *            // -> expand parent's bounds to the left if (geo.getX() < 0 ||
 *            geo.getY() < 0) { mxCell parentCell = (mxCell)
 *            model.getParent(cell); mxGeometry parentGeo = (mxGeometry)
 *            model.getGeometry(parentCell);
 * 
 *            // only update geometry if parent is not the root if (parentGeo !=
 *            null) { parentGeo.setX(Math.min(parentGeo.getX() + geo.getX(),
 *            parentGeo.getX()));
 *            parentGeo.setWidth(Math.max(parentGeo.getWidth() - geo.getX(),
 *            parentGeo.getWidth())); parentGeo.setY(Math.min(parentGeo.getY() +
 *            geo.getY(), parentGeo.getY())); parentGeo.setHeight(Math.max(
 *            parentGeo.getHeight() - geo.getY(), parentGeo.getHeight()));
 *            model.setGeometry(model.getParent(cell), parentGeo);
 * 
 *            // update current cell's geometry geo.setX(Math.max(0,
 *            geo.getX())); geo.setY(Math.max(0, geo.getY()));
 *            model.setGeometry(cell, geo);
 * 
 *            // update parent of currently resized node such that // recent
 *            resize actions are propagated to its parent...
 *            updateNode(model.getParent(cell));
 * 
 *            // update graph graph.refresh(); } else { // a node has been moved
 *            over left or top bounds of the // graph's view, scale the view to
 *            compensate
 *            graph.getView().scaleAndTranslate(graph.getView().getScale(),
 *            Math.max(-geo.getX(), 0), Math.max(-geo.getY(), 0));
 *            graph.getView().reload(); graph.refresh(); } } }
 **/

/**
 * prevents the cell from shrinking too much, ensures that the label still fits
 * in the cell
 * 
 * @param cell
 * 
 *            private void resizeToFitLabel(Object cell) { mxIGraphModel model =
 *            graph.getModel(); Object value = model.getValue(cell); mxGeometry
 *            geo = model.getGeometry(cell); assert (model.isVertex(cell) &&
 *            value instanceof HyperJob<?>); HyperJob<?> hj = (HyperJob<?>)
 *            value;
 * 
 *            // determine font and corresponding width/height of label Font
 *            font = mxUtils.getFont(graph.getCellStyle(cell)); FontMetrics fm =
 *            mxUtils.getFontMetrics(font); int labelWidth =
 *            fm.stringWidth(graph.getLabel(cell)) + 20; int numberOfLines =
 *            graph.getLabel(cell).split("\n").length; int labelHeight =
 *            numberOfLines * font.getSize() + 20;
 * 
 *            // adjust x coordinate of cell according to appropriate size if
 *            (geo.getWidth() < labelWidth) { geo.setWidth(labelWidth); if
 *            (geo.getX() > hj.getX()) { geo.setX(hj.getX() + hj.getWidth() -
 *            labelWidth); } }
 * 
 *            // adjust y coordinate of cell according to appropriate size if
 *            (geo.getHeight() < labelHeight) { geo.setHeight(labelHeight); if
 *            (geo.getY() > hj.getY()) { geo.setY(hj.getY() + hj.getHeight() -
 *            labelHeight); } }
 * 
 *            // reset geometry to changed value and update graph
 *            model.setGeometry(cell, geo); graph.refresh(); }
 */
