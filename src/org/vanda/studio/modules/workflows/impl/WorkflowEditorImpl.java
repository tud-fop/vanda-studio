package org.vanda.studio.modules.workflows.impl;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.render.jgraph.Cell;
import org.vanda.render.jgraph.mxDropTargetListener;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.HasActions;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.util.Util;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;

import com.mxgraph.swing.util.mxGraphTransferable;

import java.io.File;

public class WorkflowEditorImpl extends DefaultWorkflowEditorImpl {
	public static class CheckWorkflowAction implements Action {
		private WorkflowEditor wfe;

		public CheckWorkflowAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}

		@Override
		public String getName() {
			return "Check Workflow";
		}

		@Override
		public void invoke() {
			try {
				wfe.getSyntaxAnalysis().checkWorkflow();
			} catch (TypeCheckingException e) {

			} catch (Exception e) {
				// do nothing e.printStackTrace();
			}
		}
	}

	public static class ClearWorkflowDirectoryAction implements Action {
		private WorkflowEditor wfe;

		public ClearWorkflowDirectoryAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}

		@Override
		public String getName() {
			return "Clear Workflow directory";
		}

		@Override
		public void invoke() {
			File d = new File(wfe.getApplication().getProperty("outputPath"));
			if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(wfe
					.getApplication().getWindowSystem().getMainWindow(), 
			        "Do you want to empty " + d.getAbsolutePath() + "?", 
			        "Empty working directory", 
			        JOptionPane.OK_CANCEL_OPTION, 
			        JOptionPane.INFORMATION_MESSAGE, 
			        null, 
			        new String[]{"Empty directory!", "No, leave it."},
			        "default")) {
				if (!emptyDirectory(d))
					JOptionPane.showMessageDialog(wfe.getApplication()
							.getWindowSystem().getMainWindow(),
							"Deletion failed.");
			}
		}

		static public boolean emptyDirectory(File dir) {
			boolean success = true;
			if (dir == null)
				return false;
			if (dir.exists()) {
				for (File f : dir.listFiles()) {
					if (f.isDirectory()) {
						success = success & emptyDirectory(f);
						success = success & f.delete();
					} else {
						success = success & f.delete();
					}
				}
			}
			return success;
		}

	}

	protected final PresentationModel presentationModel;

	protected JComponent palette;

	protected final SyntaxUpdater synUp;

	private final Observer<WorkflowEvent<MutableWorkflow>> mwfObserver;

	private final Collection<Object> tools;

	public WorkflowEditorImpl(Application app, List<ToolFactory> toolFactories,
			Pair<MutableWorkflow, Database> phd) {
		super(app, phd);
		presentationModel = new PresentationModel(view, this);

		mwfObserver = new Observer<WorkflowEvent<MutableWorkflow>>() {
			@Override
			public void notify(WorkflowEvent<MutableWorkflow> event) {
				event.doNotify(WorkflowEditorImpl.this);
			}
		};
		view.getWorkflow().getObservable().addObserver(mwfObserver);

		synA = new SyntaxAnalysis(phd.fst);
		synUp = new SyntaxUpdater(app, synA, view);
		view.getWorkflow().getObservable().addObserver(synUp);

		semA = new SemanticAnalysis(synA, database);

		graphComponent = new MyMxGraphComponent(presentationModel.getVisualization()
				.getGraph());
		new mxDropTargetListener(presentationModel, graphComponent);
		configureComponent();
		graphComponent.getGraphControl().addMouseListener(new EditMouseAdapter());
		graphComponent.addKeyListener(new DelKeyListener());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				graphComponent.getVerticalScrollBar()
						.setValue(
								(int) (graphComponent.getVerticalScrollBar()
										.getMaximum() * 0.35));
			}
		});
		// component.setPanning(true); // too complic: must press SHIFT+CONTROL
		// (component.getGraph().getDefaultParent());
		// mainpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component,
		// outline);
		// mainpane.setOneTouchExpandable(true);
		// mainpane.setResizeWeight(0.9);
		// mainpane.setDividerSize(6);
		// mainpane.setBorder(null);
		// mainpane.setDividerLocation(0.7);
		graphComponent.setName(view.getWorkflow().getName());
		app.getWindowSystem().addContentWindow(null, graphComponent, null);

		tools = new ArrayList<Object>();
		for (ToolFactory tf : toolFactories) {
			tools.add(tf.instantiate(this));
		}

		// The comments are all added in different files :(
		// Still, here is the one list:
		// 00 -> Save
		addAction(new CloseWorkflowAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK), 10);
		// 20 -> Export to PDF
		addSeparator(30);
		// 40 -> Previous assignment
		// 50 -> Next assignment
		addSeparator(60);
		addAction(new CheckWorkflowAction(this), "document-preview",
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), 70);
		addAction(new ClearWorkflowDirectoryAction(this), "run-build-clean",
				KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), 80);
		// 90 -> Run
		addSeparator(100);
		addAction(new ResetZoomAction(),
				KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK), 110);
		// 120 -> (Seperator has to be added after the...)
		// 130 -> Open assignment table
		
		//setupOutline();

		// send some initial event ("updated" will be sent)
		view.getWorkflow().beginUpdate();
		view.getWorkflow().endUpdate();

		// focus window
		app.getWindowSystem().focusContentWindow(graphComponent);
		graphComponent.requestFocusInWindow();
		// init outline painting
		//outline.updateScaleAndTranslate();
	}

	static {
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ "; class=com.mxgraph.swing.util.mxGraphTransferable");
		} catch (ClassNotFoundException cnfe) {
			// do nothing
			System.out.println("Problem!");
		}
	}

	/**
	 * Handles KeyEvents such as removing cells when focused and pressing DEL
	 * 
	 * @author afischer
	 * 
	 */
	protected class DelKeyListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {

			// check if KeyEvent occurred on graph component,
			// e.getSource().equals(component) &&
			// only handle DELETE-key
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				removeSelectedCell();
			}

		}
	}

	private void removeSelectedCell() {
		// WorkflowSelection ws = model.getSelection();
		// if (ws instanceof SingleObjectSelection)
		// ((SingleObjectSelection) ws).remove();
		view.removeSelectedCell();
	}

	/**
	 * Handles mouse actions: opens cell-specific views/editors on double-click,
	 * opens context menu on mouse right-click
	 * 
	 * @author buechse, afischer
	 * 
	 */
	protected class EditMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == 1 && e.getClickCount() == 2) {
				// double click using left mouse button
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				Object value = graphComponent.getGraph().getModel().getValue(cell);

				if (value instanceof HasActions) {
					Action def = Util.getDefaultAction((HasActions) value);
					if (def != null)
						def.invoke();
				}
			} else if (e.getButton() == 3) {
				// show context menu when right clicking a node or an edge
				Object cell = graphComponent.getCellAt(e.getX(), e.getY());
				final Object value = graphComponent.getGraph().getModel()
						.getValue(cell);

				if (value != null)
					((Cell) value).rightMouseClick(e);

				// if (menu != null) {
				// if (value instanceof HasActions) {
				// HasActions ha = (HasActions) value;
				// LinkedList<Action> as = new LinkedList<Action>();
				// ha.appendActions(as);
				// for (final Action a : as) {
				// @SuppressWarnings("serial")
				// JMenuItem item = new JMenuItem(a.getName()) {
				// @Override
				// public void fireActionPerformed(ActionEvent _) {
				// a.invoke();
				// }
				// };
				// menu.add(item);
				// }
				// }
				// menu.show(e.getComponent(), e.getX(), e.getY());
				// }

			}
		}
	}

	/**
	 * a context popup menu that displays a components title
	 * 
	 * @author afischer
	 * 
	 */
	@SuppressWarnings("serial")
	public static class PopupMenu extends JPopupMenu {

		public PopupMenu(String title) {
			add(new JLabel("<html><b>" + title + "</b></html>"));
			addSeparator();
		}
	}

	@Override
	public void setPalette(JComponent c) {
		if (palette != c) {
			if (palette != null)
				removeSideSplit(palette);
			palette = c;
			if (palette != null)
				addSideSplit(palette, WindowSystem.Side.EAST, 300);
			// mainpane.setRightComponent(c);
		}
	}

}
