package org.vanda.render.jgraph;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.vanda.view.AbstractView;
import org.vanda.view.View;

import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.model.mxGraphModel.mxChildChange;
import com.mxgraph.model.mxGraphModel.mxGeometryChange;
import com.mxgraph.model.mxGraphModel.mxValueChange;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphSelectionModel.mxSelectionChange;
/**
 * 
 * @author kgebhardt
 *
 */
public final class Graph{

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
//					boolean refreshSelection = false;
//					if (cell == getSelectionCell()) {
//						refreshSelection = true;
//						if (model != null)
//							model.setSelection(null);
//					}
					if (cc.getPrevious() != null) {
						// something has been removed
						value.onRemove((mxICell) cc.getPrevious());
					}
					// the second conjunct is necessary for edges
					if (cc.getParent() != null
							&& cell.getParent() == cc.getParent())
						value.onInsert(getGraph());
//					if (refreshSelection)
//						setSelectionCell(cell);
				} else if (c instanceof mxValueChange) {
					// not supposed to happen, or not relevant
				} else if (c instanceof mxGeometryChange) {
					mxICell cell = (mxICell) ((mxGeometryChange) c).getCell();
					Cell value = (Cell) gmodel.getValue(cell);
					if (cell.getParent() != null && value.inModel())
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
	protected final CellSelectionListener cellSelectionListener;
	
	public CellSelectionListener getCellSelectionListener() {
		return cellSelectionListener;
	}

	public View getView() {
		return view;
	}

	public mxGraph getGraph() {
		return graph;
	}
	
	public Graph(View view) {
		this.graph = new mxGraph();
		this.view = view;
		changeListener = new ChangeListener();
		graph.getModel().addListener(mxEvent.CHANGE, changeListener);
		graph.getSelectionModel().addListener(mxEvent.UNDO, changeListener);
		cellSelectionListener = new CellSelectionListener();
	}
	
	public void refresh() {
		// TODO Auto-generated method stub
	}
	
	private void updateSelection(mxIGraphModel gmodel, Object[] cells) {
		List <AbstractView> toUnHighlight = view.getCurrentSelection();
		for (Object o : cells) {
			if (! (gmodel.getValue(o) instanceof Cell))
				continue;
			Cell cell = (Cell) gmodel.getValue(o);
			cell.setSelection(view);
			AbstractView v = cell.getView(view);
			if (v != null && toUnHighlight.contains(v))
					toUnHighlight.remove(v);			
		}
		for (AbstractView v : toUnHighlight)
			v.setSelected(false);
	}
	
	protected class CellSelectionListener implements Observer {

		@Override
		public void update(Observable arg0, Object arg1) {
			Cell cell = (Cell) arg0; 
			setSelection(cell);
		}
		
	}
	public void setSelection(Cell cell) {
		if (cell.getView(view).isSelected())
			graph.addSelectionCell(cell.getVisualization());
		else 
			graph.removeSelectionCell(cell.getVisualization());
		
	}
	
}
