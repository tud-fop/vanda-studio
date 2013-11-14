package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.InsertCellEvent;
import org.vanda.render.jgraph.Cells.MarkChangedEvent;
import org.vanda.render.jgraph.Cells.PropertyChangedEvent;
import org.vanda.render.jgraph.Cells.RemoveCellEvent;
import org.vanda.render.jgraph.Cells.SetSelectionEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;

public class JobCell extends Cell {
	protected String label;
	protected int progress = -1;
	protected SupplementalStyle runVis;

	// final LayoutManager layoutManager;

	public JobCell(final Graph graph, Renderer r, String label, double x, double y, double w, double h) {

		// r = null to prevent rendering in supertype
		super(null, null, graph);
		this.label = label;
		setZ(r);
		visualization = new mxCell(this, new mxGeometry(), null);
		setDimensions(new double[] { x, y, w, h });
		r.render(this);
		runVis = null;
	}

	@Override
	public String getLabel() {
		return progress != -1 ? label + "\n(" + progress + "%)" : label;
	}

	@Override
	public void highlight(boolean highlight) {
		if (JGraphRendering.highlightedStyle.updateStyle(getVisualization(), highlight))
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
	public void onResize(Graph graph) {
		// if (graph.getGraph().isAutoSizeCell(visualization))
		// graph.getGraph().updateCellSize(visualization, true); // was:
		// resizeToFitLabel(cell)
		// preventTooSmallNested(graph.getGraph(), visualization);
		// graph.getGraph().extendParent(visualization); // was:
		// resizeParentOfCell(cell)

		getObservable().notify(new PropertyChangedEvent<Cell>(this));
	}

	@Override
	public void setDimensions(double[] dimensions) {
		mxGeometry ng = (mxGeometry) getVisualization().getGeometry().clone();
		ng.setX(dimensions[0]);
		ng.setY(dimensions[1]);
		ng.setWidth(dimensions[2]);
		ng.setHeight(dimensions[3]);
		getVisualization().setGeometry(ng);
		getObservable().notify(new PropertyChangedEvent<Cell>(this));
	}

	public void setLabel(String label) {
		this.label = label == null ? "" : label;
		getObservable().notify(new PropertyChangedEvent<Cell>(this));
	}

	public void setProgress(int runProgress) {
		if (progress != runProgress) {
			progress = runProgress;
			getObservable().notify(new PropertyChangedEvent<Cell>(this));
		}
	}

	@Override
	public void setSelection(boolean selected) {
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.JOBCELL;
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public boolean isValidConnectionSource() {
		return false;
	}

	@Override
	public boolean isValidConnectionTarget() {
		return false;
	}

	@Override
	public boolean isValidDropTarget() {
		return false;
	}

	public void setCancelled() {
		setRunVis(JGraphRendering.cancelledStyle);
	}

	public void setReady() {
		setRunVis(null);
	}

	public void setDone() {
		progress = 100;
		setRunVis(JGraphRendering.doneStyle);
	}

	public void setRunning() {
		setRunVis(JGraphRendering.runningStyle);
	}

	private void setRunVis(SupplementalStyle rv) {
		if (rv != runVis) {
			if (runVis != null)
				runVis.updateStyle(visualization, false);
			runVis = rv;
			if (runVis != null)
				runVis.updateStyle(visualization, true);
			getObservable().notify(new Cells.RunVisualizationChangedEvent<Cell>(this));
		}
	}

}
