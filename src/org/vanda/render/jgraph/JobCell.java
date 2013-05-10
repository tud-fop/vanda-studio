package org.vanda.render.jgraph;

import org.vanda.util.Observer;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

public class JobCell extends Cell {
	final LayoutManagerInterface layoutManager;
	protected final String label;

	



	public JobCell(final Graph graph, LayoutManagerInterface layoutManager, String label, double x, double y, double w, double h) {
		this.layoutManager = layoutManager;
		//this.job = job;


		this.label = label;
		this.observable = new CellObservable();
		setDimensions(new double[] { x, y, w, h });

		// Register at Graph
		getObservable().addObserver(new Observer<CellEvent<Cell>>() {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(graph.getCellChangeListener());
			}

		});

		// Create mxCell and add it to Graph
		graph.getGraph().getModel().beginUpdate();
		try {

			visualization = new mxCell(this);
			graph.getGraph().addCell(visualization,
					graph.getGraph().getDefaultParent());

		} finally {
			graph.getGraph().getModel().endUpdate();
		}

		// Register at LayoutManager
		layoutManager.register(this);
	}

	@Override
	public String getType() {
		return "JobCell";
	}

	@Override
	public void onRemove() {
		getObservable().notify(new RemoveCellEvent<Cell>(this));
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		getObservable().notify(new InsertCellEvent<Cell>(this));

	}

	@Override
	public void onResize(mxGraph graph) {
		mxIGraphModel model = graph.getModel();
		mxGeometry geo = model.getGeometry(visualization);
		if (graph.isAutoSizeCell(visualization))
			graph.updateCellSize(visualization, true); // was:
														// resizeToFitLabel(cell)
		preventTooSmallNested(graph, visualization);
		graph.extendParent(visualization); // was: resizeParentOfCell(cell)

		if (geo.getX() != getX() || geo.getY() != getY()
				|| geo.getWidth() != getWidth()
				|| geo.getHeight() != getHeight()) {

			double[] dim = { geo.getX(), geo.getY(), geo.getWidth(),
					geo.getHeight() };
			setDimensions(dim);
			//job.setDimensions(dim);
			sizeChanged(geo, graph, visualization);
		}
	}

	private void sizeChanged(mxGeometry geo, mxGraph graph, mxICell cell) {
		// do nothing
	}

	private void preventTooSmallNested(mxGraph graph, mxICell cell) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

	@Override
	public String getLabel() {
		return label;

	}

}
