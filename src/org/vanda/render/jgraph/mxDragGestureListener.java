package org.vanda.render.jgraph;

import java.awt.Cursor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;


public class mxDragGestureListener implements DragGestureListener {

	private mxGraph g;
	
	public mxDragGestureListener (mxGraph g) {
		this.g = g;
	}
	
	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		Cursor cursor = null;
		String id = ((mxICell) g.getSelectionModel().getCell()).getId();
		if (event.getDragAction() == DnDConstants.ACTION_COPY)
			cursor = DragSource.DefaultCopyDrop;
		event.startDrag(cursor, new StringSelection(id));
	}

}