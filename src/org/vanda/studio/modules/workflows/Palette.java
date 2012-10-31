package org.vanda.studio.modules.workflows;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.modules.workflows.jgraph.DrecksAdapter;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;

public class Palette {
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

	protected final Application app;
	protected JXTaskPaneContainer taskPaneContainer;
	protected JScrollPane paletteComponent;
	protected mxGraphComponent searchGraph;
	protected JTextField textField;
	protected JXTaskPane searchPane;
	protected final ArrayList<Job> templates;

	public Palette(Application app) {
		this.app = app;
		taskPaneContainer = new JXTaskPaneContainer();
		paletteComponent = new JScrollPane();
		templates = new ArrayList<Job>();
	}

	public JComponent getComponent() {
		return paletteComponent;
	}

	public void update() {
		textField = new JTextField();
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
		for (Tool t : app.getToolMetaRepository().getRepository().getItems())
			templates.add(new AtomicJob(t));
		for (Linker l : app.getLinkerMetaRepository().getRepository()
				.getItems())
			templates.add(new CompositeJob(l, new MutableWorkflow(
					"inner workflow")));
		// templates.add(new AtomicJob(new Choice()));
		// templates.add(new AtomicJob(new InputPort()));
		// templates.add(new AtomicJob(new OutputPort()));
		templates.add(new AtomicJob(
				new Literal(new CompositeType("String"), "")));
		Collections.sort(templates, new Comparator<Job>() {
			@Override
			public int compare(Job o1, Job o2) {
				return o1.getItem().getName().compareTo(o2.getItem().getName());
			}
		});

		// partition items into categories
		Map<String, List<Job>> catMap = new HashMap<String, List<Job>>();
		for (Job template : templates) {
			List<Job> catList = catMap.get(template.getItem().getCategory());
			if (catList == null) {
				catList = new ArrayList<Job>();
				catMap.put(template.getItem().getCategory(), catList);
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
			mxGraphComponent graphComp = renderTemplates(catMap.get(category));
			categoryPane.add(graphComp);
			categoryPane.setCollapsed(true);
			taskPaneContainer.add(categoryPane);
		}

		paletteComponent.getViewport().add(taskPaneContainer);
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
				if (template.getItem().getName().toLowerCase().contains(text))
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
				d[1] += cell.getGeometry().getHeight() + 10;
			}
		} finally {
			da.getGraph().getModel().endUpdate();
		}
		mxGraphComponent c = new mxGraphComponent(da.getGraph());
		c.setConnectable(false);
		c.setDropTarget(null);
		return c;
	}

}