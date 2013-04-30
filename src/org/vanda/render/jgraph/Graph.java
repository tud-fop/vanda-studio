package org.vanda.render.jgraph;

import java.util.Hashtable;
import java.util.List;

import org.vanda.render.jgraph.Cell.CellListener;
import org.vanda.view.AbstractView;
import org.vanda.view.View;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;
import com.mxgraph.view.mxGraphSelectionModel.mxSelectionChange;
import com.mxgraph.view.mxStylesheet;
/**
 * 
 * @author kgebhardt
 *
 */
public final class Graph{
	private class customMxGraph extends mxGraph {
		public customMxGraph(mxStylesheet styleSheet, WorkflowCell workflowCell) {
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
					new mxMultiplicity(false, null, null, null, 0, "1", null, ".",
							"", false) {
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
					new mxMultiplicity(false, null, null, null, 0, "1", null, ".",
							"", false) {
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
			if (value instanceof Cell)
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
			if (value == null || "".equals(value))
				value = new ConnectionCell();
			// XXX don't call with a constant
			return super.createEdge(parent, id, value, source, target, "ROUNDED");
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

		@Override
		public boolean isCellSelectable(Object cell) {
			if (((Cell) getModel().getValue(cell)).getType().equals("InPortCell"))
				return false;
			return super.isCellSelectable(cell);
		}

		@Override
		public boolean isValidSource(Object cell) {
			return super.isValidSource(cell)
					&& (cell == null || ((Cell) ((mxCell) cell).getValue()).getType().equals("OutPortCell"));
		}

		@Override
		public boolean isValidTarget(Object cell) {
			return super.isValidSource(cell) /* sic!! */
					&& (cell == null || ((Cell) ((mxCell) cell).getValue()).getType().equals("InPortCell"));
		}

		@Override
		public boolean isValidDropTarget(Object cell, Object[] cells) {
			// return ((mxCell) cell).getValue() instanceof WorkflowAdapter;
			// return super.isValidDropTarget(cell, cells);
			return ((Cell) ((mxCell) cell).getValue()).getType().equals("WorkflowCell")
					&& super.isValidDropTarget(cell, cells);
		}

		// Removes the folding icon from simple jobs and disables folding
		// Allows folding of NestedHyperworkflows
		@Override
		public boolean isCellFoldable(Object cell, boolean collapse) {
			return false;
//			mxCell c = (mxCell) cell;
//			return c.getValue() instanceof CompositeJobAdapter;
		}

		
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
				// Object value = getValue();
				if (value instanceof Cell && value instanceof Cloneable) {
					// try {
						return value;
						// return ((Adapter) value).clone();
					// } catch (CloneNotSupportedException e) {
					// 	return super.cloneValue();
					// }
				} else
					return super.cloneValue();
			}
		}	
	}
	
	protected class ChangeListener implements mxIEventListener{
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
//						if (model != null)
//							model.setSelection(null);
						view.clearSelection();
					}
					if (cc.getPrevious() != null) {
						// something has been removed
						value.onRemove(view);
					}
					// the second conjunct is necessary for edges
					if (cc.getParent() != null
							&& cell.getParent() == cc.getParent())
						value.onInsert(Graph.this, cell.getParent(), cell);
					if (refreshSelection)
//						setSelectionCell(cell);
						graph.setSelectionCell(cell);
				} else if (c instanceof mxValueChange) {
					// not supposed to happen, or not relevant
				} else if (c instanceof mxGeometryChange) {
					mxICell cell = (mxICell) ((mxGeometryChange) c).getCell();
					Cell value = (Cell) gmodel.getValue(cell);
					if (cell.getParent() != null /*&& value.inModel()*/)
						value.onResize(getGraph());
				} else if (c instanceof mxSelectionChange) {
					Object[] cells = graph.getSelectionCells();
					if (cells != null) {
						updateSelection(gmodel, cells);
					} 
					else
						view.clearSelection();
				}
			}
		}
	}
	protected final ChangeListener changeListener;
	protected final mxGraph graph;
	protected final View view;
	protected final CellChangeListener cellChangeListener;
	
	public CellListener<Cell> getCellChangeListener() {
		return cellChangeListener;
	}

	public View getView() {
		return view;
	}

	public mxGraph getGraph() {
		return graph;
	}
	
	public Graph(View view, WorkflowCell workflowCell) {
		// Create graph and set graph properties
		LayoutManagerFactoryInterface layoutFactory = new NaiveLayoutManagerFactory();
		this.view = view;
		this.graph = new customMxGraph(layoutFactory.getStylesheet(), workflowCell);
		cellChangeListener = new CellChangeListener();
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
		graph.getSelectionModel().addListener(mxEvent.UNDO, changeListener);
	
	}
	
	public void refresh() {
		graph.refresh();
	}
	
	private void updateSelection(mxIGraphModel gmodel, Object[] cells) {
		List <AbstractView> toUnHighlight = view.getCurrentSelection();
		for (Object o : cells) {
//			if (! (gmodel.getValue(o) instanceof Cell))
//				continue;
			Cell cell = (Cell) gmodel.getValue(o);
			AbstractView v = cell.getView(view);
			if (v != null)
				cell.setSelection(view);
			if (v != null && toUnHighlight.contains(v))
				toUnHighlight.remove(v);			
		}
		for (AbstractView v : toUnHighlight)
			v.setSelected(false);
	}
	

	protected class CellChangeListener implements Cell.CellListener<Cell> {

		@Override
		public void propertyChanged(Cell c) {
			graph.getModel().setGeometry(c.getVisualization(), c.getVisualization().getGeometry());
			if (graph.isAutoSizeCell(c.getVisualization()))
				graph.updateCellSize(c.getVisualization(), true);
			refresh();
		}

		@Override
		public void selectionChanged(Cell c) {
			setSelection(c);
		}

		@Override
		public void markChanged(Cell c) {
			refresh();
		}

		@Override
		public void removeCell(Cell c) {
//			mxCell cell = c.getVisualization();
//			c.setVisualization(null);
//			if (cell != null) {
//				//graph.getModel().beginUpdate();
//				try {
//					mxICell parent = cell.getParent();
//					System.out.println("Graph removes cell " + cell);
//					Object [] removed = graph.removeCells(new Object[] { cell });
//					System.out.println(removed.length);
//					System.out.println("Model contains cell: " + graph.getModel().contains(cell));
//					boolean child = false;
//					for (int i = 0; i < parent.getChildCount(); ++i) 
//						if (parent.getChildAt(i) == cell )
//							child = true;
//					System.out.println("Parent has child cell: " + child );
//					
//					//graph.getModel().remove(cell);	
//				} finally {
//					//graph.getModel().endUpdate();
//					graph.refresh();
//				}
//			}
		}
		
	}
	
	public void setSelection(Cell cell) {
		if (cell.getView(view).isSelected())
			graph.addSelectionCell(cell.getVisualization());
		else 
			graph.removeSelectionCell(cell.getVisualization());
		
	}
	
}
