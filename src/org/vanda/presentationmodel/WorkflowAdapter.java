package org.vanda.presentationmodel;

import java.awt.event.MouseEvent;

import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.DataInterface;
import org.vanda.render.jgraph.WorkflowCell;
import org.vanda.render.jgraph.Cells.CellListener;
import org.vanda.util.Observer;
import org.vanda.view.View;

public class WorkflowAdapter {

	private final View view; 
	private final WorkflowCellListener wfcl;
	private final Observer<CellEvent<Cell>> workflowCellObserver;
	private final WorkflowCell visualization;
	private class WorkflowCellListener implements CellListener<Cell> {

		@Override
		public void insertCell(Cell c) {
			// do nothing
		}

		@Override
		public void markChanged(Cell c) {
			// do nothing
		}

		@Override
		public void propertyChanged(Cell c) {
			// do nothing			
		}

		@Override
		public void removeCell(Cell c) {
			// do nothing			
		}

		@Override
		public void selectionChanged(Cell c, boolean selected) {
			// do nothing
		}

		@Override
		public void setSelection(Cell c, boolean selected) {
			view.getWorkflowView().setSelected(selected);			
		}

		@Override
		public void rightClick(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public WorkflowAdapter(DataInterface di, View view) {
		this.view = view;
		this.wfcl = new WorkflowCellListener();
		this.visualization = new WorkflowCell(di);
		workflowCellObserver = new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(wfcl);
			}
		};
		visualization.getObservable().addObserver(workflowCellObserver);
		
		//initial selection of Workflow
		view.getWorkflowView().setSelected(true);
	}
	
	public WorkflowCell getWorkflowCell() {
		return visualization;
	}
	
}
