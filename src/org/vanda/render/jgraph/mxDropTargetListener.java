package org.vanda.render.jgraph;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.types.CompositeType;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.ToolAdapter;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxPoint;

public class mxDropTargetListener extends DropTargetAdapter implements
DropTargetListener {
	private final WorkflowEditor wfe;
	private final PresentationModel pm;
	private final mxGraphComponent c;
	public mxDropTargetListener(WorkflowEditor wfe, PresentationModel pm, mxGraphComponent c) {
		this.wfe = wfe;
		this.pm = pm;
		this.c = c;
		c.setDropTarget(new DropTarget(c, DnDConstants.ACTION_COPY, this, true,
				null));
	}
	
	@Override
	public void drop(DropTargetDropEvent event) {
		try {
			if (event.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String id = (String) event.getTransferable().getTransferData(DataFlavor.stringFlavor);
				event.acceptDrop(DnDConstants.ACTION_COPY);
				
				Graph g = pm.getVisualization();
				g.getGraph().getModel().beginUpdate();
				Point loc = event.getLocation();
				Point view = c.getViewport().getViewPosition();
				mxPoint tr = g.getGraph().getView().getTranslate();
				double zoom = g.getGraph().getView().getScale();

				double x = (loc.x + view.x) / zoom - tr.getX();
				double y = (loc.y + view.y) / zoom - tr.getY();
				double[] d = { x, y, 100, 80 };

				ElementAdapter ele;
				// TODO literal should be recognized otherwise
				if (id.equals("literal"))
					ele = new LiteralAdapter(new Literal(new CompositeType(
							"String"), ""));
				else
					ele = new ToolAdapter(wfe.getApplication()
							.getToolMetaRepository().getRepository()
							.getItem(id));
				Job j = new Job(ele);
				j.setDimensions(d);
				
				pm.addJobAdapter(j);
				g.getGraph().getModel().endUpdate();
				
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
