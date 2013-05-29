package org.vanda.render.jgraph;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class mxDropTargetListener extends DropTargetAdapter implements DropTargetListener {

	private final DataInterface di;
	private final mxGraphComponent c;

	public mxDropTargetListener(DataInterface di, mxGraphComponent c) {
		this.di = di;
		this.c = c;
		c.setDropTarget(new DropTarget(c, DnDConstants.ACTION_COPY, this, true, null));
	}

	@Override
	public void drop(DropTargetDropEvent event) {
		try {
			if (event.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String id = (String) event.getTransferable().getTransferData(DataFlavor.stringFlavor);
				event.acceptDrop(DnDConstants.ACTION_COPY);

				mxGraph g = c.getGraph();
				g.getModel().beginUpdate();
				try {
					Point loc = event.getLocation();
					Point view = c.getViewport().getViewPosition();
					mxPoint tr = g.getView().getTranslate();
					double zoom = g.getView().getScale();
	
					double x = (loc.x + view.x) / zoom - tr.getX();
					double y = (loc.y + view.y) / zoom - tr.getY();
					double[] d = { x, y, 100, 80 };
	
					di.createJob(id, d);
				} finally {
					g.getModel().endUpdate();
				}
				event.dropComplete(true);
				return;
			} else {
				event.rejectDrop();
			}
		} catch (Exception e) {
			e.printStackTrace();
			event.rejectDrop();
		}

	}

}
