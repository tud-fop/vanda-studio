package org.vanda.render.jgraph;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.List;

import org.vanda.render.jgraph.Cells.CellListener;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel.mxSelectionChange;
import com.mxgraph.view.mxMultiplicity;
import com.mxgraph.view.mxStylesheet;

/**
 * 
 * @author kgebhardt
 * 
 */
public final class Graph {
	protected class CellChangeListener implements CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// do nothing

		}

		@Override
		public void markChanged(Cell c) {
			refresh();
		}

		@Override
		public void propertyChanged(Cell c) {
			graph.getModel().setGeometry(c.getVisualization(),
					c.getVisualization().getGeometry());
			if (graph.isAutoSizeCell(c.getVisualization()))
				graph.updateCellSize(c.getVisualization(), true);
			refresh();
		}

		@Override
		public void removeCell(Cell c) {
			// do nothing
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			Graph.this.setSelection(c, selected);

		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			// do nothing
		}

		@Override
		public void rightClick(MouseEvent e) {
			// do nothing
		}

	}

	protected class ChangeListener implements mxIEventListener {
		// edges: childChange, terminalChange, terminalChange, geometryChange,
		// terminalChange
		// the we are only interested in the final childChange!
		// the first one is ignored using an additional conjunct (see below)
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			mxIGraphModel gmodel = graph.getModel();
			mxUndoableEdit edit = (mxUndoableEdit) evt.getProperty("edit");
			List<mxUndoableChange> changes = edit.getChanges();
			for (mxUndoableChange c : changes) {
				// process the following changes:
				// - child change (add/remove)
				// - value change
				// - geometry change
				if (c instanceof mxChildChange) {
					mxChildChange cc = (mxChildChange) c;
					mxICell cell = (mxICell) cc.getChild();
					Cell value = (Cell) gmodel.getValue(cell);
					boolean refreshSelection = false;
					if (cell == graph.getSelectionCell()) {
						refreshSelection = true;
						clearSelection((Cell) ((mxCell) graph
								.getDefaultParent()).getValue());
					}
					if (cc.getPrevious() != null) {
						// something has been removed
						value.onRemove();
					}
					// the second conjunct is necessary for edges
					if (cc.getParent() != null
							&& cell.getParent() == cc.getParent())
						value.onInsert(Graph.this, cell.getParent(), cell);
					if (refreshSelection)
						graph.setSelectionCell(cell);
				} else if (c instanceof mxValueChange) {
					// not supposed to happen, or not relevant
				} else if (c instanceof mxGeometryChange) {
					mxICell cell = (mxICell) ((mxGeometryChange) c).getCell();
					Cell value = (Cell) gmodel.getValue(cell);
					if (cell.getParent() != null /* && value.inModel() */)
						value.onResize(Graph.this);
				} else if (c instanceof mxSelectionChange) {
					Object[] cells = graph.getSelectionCells();

					if (cells != null) {
						updateSelection(gmodel, cells);
					} else
						clearSelection((Cell) ((mxCell) graph
								.getDefaultParent()).getValue());
				}
			}
		}

	}

	private class CustomMxGraph extends mxGraph {
		private class customMxCell extends mxCell {
			/**
			 * 
			 */
			private static final long serialVersionUID = 39927174614076724L;

			public customMxCell(Object value, mxGeometry geometry, String style) {
				super(value, geometry, style);
			}

			@Override
			protected Object cloneValue() {
				if (value instanceof Cell && value instanceof Cloneable) {
					return value;
				} else
					return super.cloneValue();
			}
		}

		public CustomMxGraph(mxStylesheet styleSheet, WorkflowCell workflowCell) {
			super(styleSheet);
			setCellsCloneable(false);
			setSplitEnabled(false);
			// setDropEnabled(false);
			// graph.setCellsMovable(false);
			setCellsEditable(false);
			setCollapseToPreferredSize(true);
			// setAutoSizeCells(true);
			setExtendParents(true);
			setExtendParentsOnAdd(true);
			setCellsResizable(true);
			setCellsDisconnectable(false);
			setMultigraph(false); // no effect!
			setAllowLoops(false);
			setAllowDanglingEdges(false);
			Hashtable<String, Object> style = new Hashtable<String, Object>();
			style.put(mxConstants.STYLE_ROUNDED, true);
			getStylesheet().putCellStyle("ROUNDED", style);
			setMultiplicities(new mxMultiplicity[] {
					new mxMultiplicity(false, null, null, null, 0, "1", null,
							".", "", false) {
						@Override
						public String check(mxGraph graph, Object edge,
								Object source, Object target, int sourceOut,
								int targetIn) {
							if (targetIn == 0)
								return null;
							else
								return countError;
						}
					},
					new mxMultiplicity(false, null, null, null, 0, "1", null,
							".", "", false) {
						@Override
						public String check(mxGraph graph, Object edge,
								Object source, Object target, int sourceOut,
								int targetIn) {
							mxIGraphModel m = graph.getModel();
							if (m.getParent(m.getParent(source)) == m
									.getParent(m.getParent(target)))
								return null;
							else
								return countError;
						}
					} });
			((mxICell) getDefaultParent()).setValue(workflowCell);
			workflowCell.visualization = (mxCell) getDefaultParent();
		}

		@Override
		public String convertValueToString(Object cell) {
			Object value = model.getValue(cell);
			if (value != null)
				return ((Cell) value).getLabel();
			else
				return "";
		}

		public mxCell createCell(Object value, mxGeometry geometry, String style) {
			return new customMxCell(value, geometry, style);
		}

		@Override
		public Object createEdge(Object parent, String id, Object value,
				Object source, Object target, String style) {
			if (value == null || "".equals(value)) {
				value = new ConnectionCell();

			}
			// XXX don't call with a constant
			return super.createEdge(parent, id, value, source, target,
					"ROUNDED");
		}

		@Override
		public Object createVertex(Object parent, String id, Object value,
				double x, double y, double width, double height, String style,
				boolean relative) {
			mxGeometry geometry = new mxGeometry(x, y, width, height);
			geometry.setRelative(relative);
			mxCell vertex = createCell(value, geometry, style);

			vertex.setId(id);
			vertex.setVertex(true);
			vertex.setConnectable(true);

			return vertex;
		}

		@Override
		public void finalize() throws Throwable {
			super.finalize();
		}

		// Removes the folding icon from simple jobs and disables folding
		// Allows folding of NestedHyperworkflows
		@Override
		public boolean isCellFoldable(Object cell, boolean collapse) {
			return false;
			// mxCell c = (mxCell) cell;
			// return c.getValue() instanceof CompositeJobAdapter;
		}

		@Override
		public boolean isCellSelectable(Object cell) {
			return (((Cell) getModel().getValue(cell)).isSelectable() && super
					.isCellSelectable(cell));
		}

		@Override
		public boolean isValidDropTarget(Object cell, Object[] cells) {
			return ((Cell) ((mxCell) cell).getValue()).isValidDropTarget()
					&& super.isValidDropTarget(cell, cells);
		}

		@Override
		public boolean isValidSource(Object cell) {
			return super.isValidSource(cell)
					&& (cell == null || ((Cell) ((mxCell) cell).getValue())
							.isValidConnectionSource());
		}

		@Override
		public boolean isValidTarget(Object cell) {
			return super.isValidSource(cell) /* sic!! */
					&& (cell == null || ((Cell) ((mxCell) cell).getValue())
							.isValidConnectionTarget());
		}
	}

	protected final CellChangeListener cellChangeListener;
	protected final ChangeListener changeListener;
	protected final mxGraph graph;
	private int selectionUpdate = 0;

	public Graph(WorkflowCell workflowCell) {
		// Create graph and set graph properties
		// LayoutManagerFactoryInterface layoutFactory = new JGraphRendering();
		this.graph = new CustomMxGraph(JGraphRendering.getStylesheet(),
				workflowCell);
		JGraphRendering.refStylesheet(1);
		cellChangeListener = new CellChangeListener();
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
		graph.getSelectionModel().addListener(mxEvent.UNDO, changeListener);

	}

	public void beginUpdate() {
		getGraph().getModel().beginUpdate();
	}

	private void clearSelection(Cell container) {
		container.setSelection(false);
		for (int i = 0; i < container.getChildCount(); ++i) {
			clearSelection(container.getChildAt(i));
		}
	}

	public void endUpdate() {
		getGraph().getModel().endUpdate();
	}

	@Override
	public void finalize() throws Throwable {
		JGraphRendering.refStylesheet(-1);
		super.finalize();
	}

	public CellListener<Cell> getCellChangeListener() {
		return cellChangeListener;
	}

	public mxGraph getGraph() {
		return graph;
	}

	public void refresh() {
		graph.refresh();
	}

	public void setSelection(Cell cell, boolean selected) {
		if (selectionUpdate > 0)
			return;
		if (selected)
			graph.addSelectionCell(cell.getVisualization());
		else
			graph.removeSelectionCell(cell.getVisualization());

	}

	private void updateSelection(mxIGraphModel gmodel, Object[] cells) {
		selectionUpdate++;
		clearSelection((Cell) ((mxCell) graph.getDefaultParent()).getValue());

		// set selection in View
		for (Object o : cells) {
			Cell cell = (Cell) gmodel.getValue(o);
			cell.setSelection(true);
		}
		selectionUpdate--;
	}

	public void removeCell(Cell cell) {
		getGraph().removeCells(new Object[] { cell.getVisualization() });

	}

	/**
	 * Make the visualization immutable (used for palette)
	 */
	public void setPaletteStyle() {
		getGraph().setCellsLocked(true);
		getGraph().setDropEnabled(false);
	}

	/**
	 * @return
	 */
	public Component getPaletteComponent() {
		mxGraphComponent c = new mxGraphComponent(getGraph());
		c.setConnectable(false);
		c.setDragEnabled(false);
		DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(c.getGraphControl(),
				DnDConstants.ACTION_COPY_OR_MOVE,
				new mxDragGestureListener(c.getGraph()));
		return c;
	}

}
