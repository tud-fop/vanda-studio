package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.InsertCellEvent;
import org.vanda.render.jgraph.Cells.MarkChangedEvent;
import org.vanda.render.jgraph.Cells.PropertyChangedEvent;
import org.vanda.render.jgraph.Cells.RemoveCellEvent;
import org.vanda.render.jgraph.Cells.SetSelectionEvent;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxGraph;

public class JobCell extends Cell {
	protected final String label;
	final LayoutManager layoutManager;

	public JobCell(final Graph graph, LayoutManager layoutManager,
			String label, double x, double y, double w, double h) {
		this.layoutManager = layoutManager;

		this.label = label;
		this.observable = new MultiplexObserver<CellEvent<Cell>>();

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
			visualization = new mxCell(this, new mxGeometry(), null);
			setDimensions(new double[] { x, y, w, h });
			JGraphRendering.getRendererAssortment().selectAlgorithmRenderer().render(graph, this);
			graph.getGraph().addCell(visualization,
					graph.getGraph().getDefaultParent());

		} finally {
			graph.getGraph().getModel().endUpdate();
		}
	}

	@Override
	public String getLabel() {
		return label;

	}

	@Override
	public String getType() {
		return "JobCell";
	}

	@ Override
	public void highlight(boolean highlight) {
		if (highlight) {
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization()
							.getStyle(), "highlighted"));
		} else {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(getVisualization()
							.getStyle(), "highlighted"));
		}
		getObservable().notify(new MarkChangedEvent<Cell>(this));

	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		getObservable().notify(new InsertCellEvent<Cell>(this));

	}

	@Override
	public void onRemove() {
		getObservable().notify(new RemoveCellEvent<Cell>(this));
	}

	@Override
	public void onResize(mxGraph graph) {
		if (graph.isAutoSizeCell(visualization))
			graph.updateCellSize(visualization, true); // was:
														// resizeToFitLabel(cell)
		preventTooSmallNested(graph, visualization);
		graph.extendParent(visualization); // was: resizeParentOfCell(cell)

		getObservable().notify(new PropertyChangedEvent<Cell>(this));
	}

	private void preventTooSmallNested(mxGraph graph, mxICell cell) {
		// do nothing
	}

	@Override
	public void setDimensions(double [] dimensions) {
		mxGeometry ng = (mxGeometry) getVisualization().getGeometry().clone();
		ng.setX(dimensions[0]);
		ng.setY(dimensions[1]);
		ng.setWidth(dimensions[2]);
		ng.setHeight(dimensions[3]);
		getVisualization().setGeometry(ng);
		getObservable().notify(new PropertyChangedEvent<Cell>(this));	
	}
	
	@Override
	public void setSelection(boolean selected) {
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

	public void setId(String id) {
		getVisualization().setId(id);
	}

}
