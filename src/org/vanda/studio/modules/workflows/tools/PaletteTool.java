package org.vanda.studio.modules.workflows.tools;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;
import org.vanda.studio.modules.workflows.jgraph.mxDragGestureListener;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.types.CompositeType;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.ToolAdapter;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;

public class PaletteTool implements ToolFactory {
	// TODO see below: beamer mode and stuff
	// public Palette(Application app) {
	// app.getUIModeObservable().addObserver(new Observer<Application>() {
	// @Override
	// public void notify(Application a) {
	// if (a.getUIMode().isLargeContent())
	// component.zoomTo(1.5, false);
	// else
	// component.zoomActual();
	// }
	// });
	// }

	// protected final Application app;
	// protected SemanticsModule sm;

	public static class Palette {
		protected final WorkflowEditor wfe;
		protected mxGraphComponent searchGraph;
		protected JXTaskPaneContainer taskPaneContainer;
		protected JTextField textField;
		protected JSplitPane paletteComponent;
		protected JScrollPane scrollPane;
		protected JXTaskPane searchPane;
		protected final ArrayList<Job> templates;

		public Palette(WorkflowEditor wfe) {
			this.wfe = wfe;
			taskPaneContainer = new JXTaskPaneContainer();
			scrollPane = new JScrollPane();
			templates = new ArrayList<Job>();
			paletteComponent = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					scrollPane, new JPanel());
			paletteComponent.setName("Palette");
			update();
			wfe.setPalette(paletteComponent);
		}

		public void update() {
			textField = new JTextField();
			textField.setDragEnabled(true);
			searchPane = new JXTaskPane("Search");
			searchPane.add(textField);
			if (searchGraph != null)
				searchPane.add(searchGraph);
			taskPaneContainer.add(searchPane);

			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					updateSearch(textField.getText());
				}
			});

			// get all palette items
			for (Tool t : wfe.getApplication().getToolMetaRepository()
					.getRepository().getItems()) {
				// if ("".equals(t.getStatus()))
				templates.add(new Job(new ToolAdapter(t)));
			}
			// templates.add(new AtomicJob(new Choice()));
			// templates.add(new AtomicJob(new InputPort()));
			// templates.add(new AtomicJob(new OutputPort()));
			templates.add(new Job(new LiteralAdapter(new Literal(
					Types.undefined, ""))));
			Collections.sort(templates, new Comparator<Job>() {
				@Override
				public int compare(Job o1, Job o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			// partition items into categories
			Map<String, List<Job>> catMap = new HashMap<String, List<Job>>();
			for (Job template : templates) {
				List<Job> catList = catMap.get(template.getElement()
						.getCategory());
				if (catList == null) {
					catList = new ArrayList<Job>();
					catMap.put(template.getElement().getCategory(), catList);
				}
				catList.add(template);
			}

			// create a new TaskPane for every category and fill it with
			// an mxGraphComponent that contains all tools of this category
			List<String> categories = new ArrayList<String>();
			categories.addAll(catMap.keySet());
			Collections.sort(categories);
			for (String category : categories) {
				JXTaskPane categoryPane = new JXTaskPane(category);
				mxGraphComponent graphComp = renderTemplates(catMap
						.get(category));
				categoryPane.add(graphComp);
				categoryPane.setCollapsed(true);
				taskPaneContainer.add(categoryPane);
			}

			scrollPane.getViewport().add(taskPaneContainer);
		}

		private void updateSearch(String text) {

			// remove previous results
			if (searchGraph != null) {
				searchPane.remove(searchGraph);
				searchGraph = null;
			}

			// find all items that contain the search string
			if (!text.isEmpty()) {
				List<Job> searchResults = new LinkedList<Job>();
				text = text.toLowerCase();
				for (Job template : templates) {
					if (template.getName().toLowerCase().contains(text))
						searchResults.add(template);
				}

				// create graph with search results and add it to display
				if (!searchResults.isEmpty()) {
					searchGraph = renderTemplates(searchResults);
					searchPane.add(searchGraph);
				}
			}
			searchPane.revalidate(); // in particular if searchResults.isEmpty()
			searchPane.setCollapsed(false);
		}

	}

	protected static mxGraphComponent renderTemplates(List<Job> ts) {
		DrecksAdapter da = new DrecksAdapter(null);
		da.getGraph().setCellsLocked(true);
		da.getGraph().setDropEnabled(false);
		da.getGraph().getModel().beginUpdate();
		try {
			// top left corner of first palette tool, width, height
			double[] d = { 20, 10, 100, 80 };
			for (Job template : ts) {
				template.setDimensions(d);
				mxICell cell = da.renderChild(null, template);
				cell.setId(template.getElement().getId());
				d[1] += cell.getGeometry().getHeight() + 10;
			}
		} finally {
			da.getGraph().getModel().endUpdate();
		}
		mxGraphComponent c = new mxGraphComponent(da.getGraph());
		c.setConnectable(false);
		c.setDragEnabled(false);
		DragSource ds = new DragSource();
		ds.createDefaultDragGestureRecognizer(c.getGraphControl(),
				DnDConstants.ACTION_COPY_OR_MOVE,
				new mxDragGestureListener(c.getGraph()));
		return c;
	}

	@Override
	public String getCategory() {
		return "Workflow Editing";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Displays tools for dragging into workflow editor";
	}

	@Override
	public String getId() {
		return "palette";
	}

	@Override
	public String getName() {
		return "Palette Tool";
	}

	@Override
	public String getVersion() {
		return "2012-12-19";
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Palette(wfe);
	}

}