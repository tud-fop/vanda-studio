package org.vanda.studio.modules.workflows.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.model.VObject;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.workflows.Connection;
import org.vanda.studio.modules.workflows.IHyperworkflow;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;
import org.vanda.studio.util.Observer;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxGraphTransferable;

public class WorkflowEditor implements Editor<VWorkflow>{

	protected Application app;
	protected HashMap<String,WorkflowEditorTab> tabs;
	//protected JSplitPane mainpane;
	//protected JTabbedPane tabpane;
	//protected boolean visible;
	//protected UIModeObserver umo;
	
	public WorkflowEditor(Application a) {
		app = a;
		tabs = new HashMap<String,WorkflowEditorTab>();
		app.getUIModeObservable().addObserver(
			new Observer<Application>() {
				@Override
				public void notify(Application a) {
					for (Entry<String,WorkflowEditorTab> e : tabs.entrySet())
						e.getValue().notifyUIMode(a);
				}
			});
	}
	
	static {
		try
		{
			mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
			 + "; class=com.mxgraph.swing.util.mxGraphTransferable");
		}
		catch (ClassNotFoundException cnfe)
		{
		  // do nothing
		  System.out.println("Problem!");
		}
	}

	
	@Override
	public void open(VWorkflow t) {
		// check if a tab is already open
		WorkflowEditorTab tab = tabs.get(t.getId());
		// if not, open one
		if (tab == null) {
			tab = new WorkflowEditorTab(t);
			tabs.put(t.getId(), tab);
			//tabpane.add(t.getName(), tab.getComponent());
		}
		//// select the tab
		//tabpane.setSelectedComponent(tab.getComponent());
		//// if editor is not visible, make it visible
		//if (!visible) {
		//	updatePalette();
		//	// TODO bind repository
		//	app.getWindowSystem().addContentWindow("", "Term Editor", null,
		//		mainpane);
		//	visible = true;
		//}
		// focus the editor
		app.getWindowSystem().addContentWindow("", t.getName(), null, tab.getComponent());
		app.getWindowSystem().focusContentWindow(tab.getComponent());
		tab.getComponent().requestFocusInWindow();
	}
	
	public void close(VWorkflow t) {
		WorkflowEditorTab tab = tabs.remove(t.getId());
		if (tab == null)
			throw new UnsupportedOperationException("attempt to close nonextant editor");
		//// hide editor if no tabs are open
		//if (tabs.size() == 0) {
		//	app.getWindowSystem().removeContentWindow(mainpane);
		//	visible = false;
		//}
		// remove tab
		app.getWindowSystem().removeContentWindow(tab.getComponent());
		//tabpane.remove(tab.getComponent());
	}
	
	protected class WorkflowEditorTab {
		protected VWorkflow vworkflow;
		protected NestedHyperworkflow nhwf;
		protected mxGraphComponent component;
		protected JGraphRenderer renderer;
		protected JGraphRendering.Graph palettegraph;
		protected mxGraphComponent palette;
		protected JSplitPane mainpane;
		
		public WorkflowEditorTab(VWorkflow t) {			
			vworkflow = t;
			renderer = new JGraphRenderer();
			nhwf = vworkflow.load();
			
			// add listeners to renderer - every change within the graph (renderer) is propagated to the model
			//!!!
			renderer.getObjectAddObservable().addObserver(
					new Observer<IHyperworkflow>() {
						@Override
						public void notify(IHyperworkflow o) {
//							System.out.println("renderer: addObject - " + o.getName());
							nhwf.ensurePresence(o);
						}
					});
			renderer.getObjectModifyObservable().addObserver(
					new Observer<IHyperworkflow>() {
						@Override
						public void notify(IHyperworkflow o) {
//							System.out.println("renderer: modifyObject - " + o.getName());
						}
					});
			renderer.getObjectRemoveObservable().addObserver(
					new Observer<IHyperworkflow>() {
						@Override
						public void notify(IHyperworkflow o) {
//							System.out.println("renderer: removeObject - " + o.getName());
							nhwf.ensureAbsence(o);
						}
					});
			renderer.getConnectionAddObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection c) {
//							System.out.println("renderer: addConnection - " + c);
							nhwf.ensureConnected(c);
						}
					});
			renderer.getConnectionModifyObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection c) {
//							System.out.println("renderer: modifyConnection - " + c);
						}
					});
			renderer.getConnectionRemoveObservable().addObserver(
					new Observer<Connection>() {
						@Override
						public void notify(Connection c) {
//							System.out.println("renderer: removeConnection - " + c);
							nhwf.ensureDisconnected(c);
						}
					});
			
			palettegraph = JGraphRendering.createGraph();
			palettegraph.setCellsLocked(true);
			palette = new mxGraphComponent(palettegraph);
			palette.getGraphControl().addMouseListener(
				new EditMouseAdapter(app, palette));
			component = new mxGraphComponent(renderer.getGraph());
			component.setDragEnabled(false);
			component.getGraphControl().addMouseListener(
				new EditMouseAdapter(app, component));
			component.getGraphControl().addMouseWheelListener(new MouseZoomAdapter(app, component));
			updatePalette();
			mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component, palette);
			mainpane.setOneTouchExpandable(true);
			//mainpane.setDividerLocation(320);
			mainpane.setResizeWeight(1);
			mainpane.setDividerSize(6);
			mainpane.setBorder(null);
			
			//-------------------------------------------------------
			//------ display nodes and edges of a loaded nhwf -------
			//-------------------------------------------------------
			renderRecursively(nhwf);
		}
		
		private void renderRecursively(NestedHyperworkflow nested) {
			
			//create renderer for current NestedHyperworkflow
			nested.getAddObservable().addObserver(		//new children of nhwf are propagated to the renderer
					new Observer<IHyperworkflow>() {
						@Override
						public void notify(IHyperworkflow o) {
							renderer.ensurePresence(o);
						}
					});
			nested.getRemoveObservable().addObserver(		//removed children of nhwf are propagated to the renderer
						new Observer<IHyperworkflow>() {
							@Override
							public void notify(IHyperworkflow o) {
								renderer.ensureAbsence(o);
							}
						});
			nested.getConnectObservable().addObserver(	//new edges of nhwf are propagated to the renderer
						new Observer<Connection>() {
							@Override
							public void notify(Connection conn) {
								renderer.ensureConnected(conn);
							}
						});
			nested.getDisconnectObservable().addObserver(	//removed edges of nhwf are propagated to the renderer
						new Observer<Connection>() {
							@Override
							public void notify(Connection conn) {
								renderer.ensureDisconnected(conn);
							}
						});
			
			//render children recursivley
			for (IHyperworkflow child : nested.getChildren()) {
				nested.ensurePresence(child);
				if (child instanceof NestedHyperworkflow) {
					renderRecursively((NestedHyperworkflow)child);
				}
			}
			//render connections of the current NestedHyperworkflow
			for (Connection conn : nested.getConnections()) {
				nested.ensureConnected(conn);
			}			
		}
		
		public JComponent getComponent() {
			return mainpane;
		}
		
		protected void notifyUIMode(Application app) {
			setZoom(component, app.getUIMode().isLargeContent());
			setZoom(palette, app.getUIMode().isLargeContent());
		}
	
		protected void updatePalette() {
			palettegraph.getModel().beginUpdate();
			try {
				// clear seems to reset the zoom, so we call notify at the end
				((mxGraphModel)palettegraph.getModel()).clear();
				ArrayList<VObject> items
					= new ArrayList<VObject>(app.getGlobalRepository().getItems());
				Collections.sort(items,
					new Comparator<VObject>() {
						@Override
						public int compare(VObject o1, VObject o2) {
							return o1.getCategory().compareTo(o2.getCategory());
						}
					});
				//top left corner of first palette tool, width, height
				double[] d = { 10, 10, 100, 80 };	
				for (VObject item : items) {
					IHyperworkflow to = new VOWorkflowObject(item);
					if (to.getInputPorts().size() > 0)
						d[1] += 20;	//length of a port
					to.setDimensions(d);
					JGraphRendering.render(to, palettegraph, null);
					d[1] += 90;
					if (to.getOutputPorts().size() > 0)
						d[1] += 20;
				}
			}
			finally {
				palettegraph.getModel().endUpdate();
			}
			notifyUIMode(app);
		}
	}
		
	protected static void setZoom(mxGraphComponent component, boolean large) {
		if (large)
			component.zoomTo(1.5, false);
		else
			component.zoomActual();
	}
	
	protected static class UIModeObserver implements Observer<Application> {
		protected mxGraphComponent component;
		
		public UIModeObserver(Application app, mxGraphComponent component) {
			this.component = component;
			notify(app);
		}
		
		@Override
		public void notify(Application a) {
			if (a.getUIMode().isLargeContent())
				component.zoomTo(1.5, false);
			else
				component.zoomActual();
		}
	}
	
	protected static class EditMouseAdapter extends MouseAdapter {
		protected Application app;
		protected mxGraphComponent component;
		
		public EditMouseAdapter(Application app, mxGraphComponent component) {
			this.app = app;
			this.component = component;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			//double click using left mouse button
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				Object cell = component.getCellAt(e.getX(), e.getY());
				Object value = component.getGraph().getModel().getValue(cell);
				
				if (value instanceof IHyperworkflow) {
					ArrayList<Action> as = new ArrayList<Action>();
					((IHyperworkflow)value).appendActions(as);
					if (as.size() > 0) {
						as.get(0).invoke();
					}
				}
			}
		}
	}
	
	/**
	 * enables mouse wheel zooming function within graph editor window
	 * @author afischer
	 *
	 */
	protected static class MouseZoomAdapter implements MouseWheelListener {
		protected Application app;
		protected mxGraphComponent component;
		
		public MouseZoomAdapter(Application app, mxGraphComponent component) {
			this.app = app;
			this.component = component;
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0) component.zoomOut();
			else component.zoomIn();
		}
	}
}
