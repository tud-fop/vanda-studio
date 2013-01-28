package org.vanda.studio.modules.workflows.jgraph;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;

import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;

public class mxDropTargetListener extends DropTargetAdapter implements
		DropTargetListener {
	private WorkflowEditor wfe;
	private DrecksAdapter da;
	private mxGraphComponent c;

	public mxDropTargetListener(WorkflowEditor wfe, DrecksAdapter da,
			mxGraphComponent c) {
		this.wfe = wfe;
		this.da = da;
		this.c = c;
		c.setDropTarget(new DropTarget(c, DnDConstants.ACTION_COPY, this, true,
				null));
	}

	public void drop(DropTargetDropEvent event) {
		try {
			if (event.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String id = (String) event.getTransferable().getTransferData(
						DataFlavor.stringFlavor);
				event.acceptDrop(DnDConstants.ACTION_COPY);

				da.getGraph().getModel().beginUpdate();

				Point loc = event.getLocation();
				Point view = c.getViewport().getViewPosition();
				double zoom = da.getGraph().getView().getScale();
				mxPoint tr = da.getGraph().getView().getTranslate();
				Tool t = wfe.getApplication().getToolMetaRepository()
						.getRepository().getItem(id);
				System.out.println("loc:  " + loc);
				System.out.println("view: " + view);
				System.out.println("tr:   " + tr);
				System.out.println("zoom: " + zoom);
				double x = (loc.x + view.x) / zoom - tr.getX();
				double y = (loc.y + view.y) / zoom - tr.getY();
				double[] d = { x, y, 100, 80 };
				Job j = new Job(t);
				j.setDimensions(d);
				da.renderChild(wfe.getModel().getRoot(), j);
				
				da.getGraph().getModel().endUpdate();
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