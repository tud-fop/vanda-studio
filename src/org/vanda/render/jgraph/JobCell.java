package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.InsertCellEvent;
import org.vanda.render.jgraph.Cells.MarkChangedEvent;
import org.vanda.render.jgraph.Cells.PropertyChangedEvent;
import org.vanda.render.jgraph.Cells.RemoveCellEvent;
import org.vanda.render.jgraph.Cells.SetSelectionEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxStyleUtils;

public class JobCell extends Cell {
	protected String label;
	protected RunVis runVis;

	// final LayoutManager layoutManager;

	public JobCell(final Graph graph, Renderer r, String label, double x,
			double y, double w, double h) {

		// r = null to prevent rendering in supertype
		super(null, null, graph);
		this.label = label;
		setZ(r);
		visualization = new mxCell(this, new mxGeometry(), null);
		setDimensions(new double[] { x, y, w, h });
		r.render(this);
		runVis = new Ready();
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void highlight(boolean highlight) {
		if (highlight) {
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization().getStyle(),
							"highlighted"));
		} else {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(getVisualization().getStyle(),
							"highlighted"));
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
		if (label != null)
			this.label = label;
		else
			this.label = "";
		getObservable().notify(new PropertyChangedEvent<Cell>(this));
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
		runVis.cancelled();
	}

	public void setReady() {
		runVis.ready();
	}

	public void setDone() {
		runVis.done();
	}

	public void setRunning() {
		runVis.running();
	}

	private abstract class RunVis {
		void cancelled() {
			removeCurrentStyle();
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization().getStyle(),
							"cancelled"));
			runVis = new Cancelled();
			getObservable().notify(
					new Cells.RunVisualizationChangedEvent<Cell>(JobCell.this));
		};

		void running() {
			removeCurrentStyle();
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization().getStyle(),
							"running"));
			runVis = new Running();
			getObservable().notify(
					new Cells.RunVisualizationChangedEvent<Cell>(JobCell.this));

		};

		void done() {
			removeCurrentStyle();
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization().getStyle(),
							"done"));
			runVis = new Done();
			getObservable().notify(
					new Cells.RunVisualizationChangedEvent<Cell>(JobCell.this));

		};

		void ready() {
			removeCurrentStyle();
			getVisualization().setStyle(
					mxStyleUtils.addStylename(getVisualization().getStyle(),
							"ready"));
			runVis = new Ready();
			getObservable().notify(
					new Cells.RunVisualizationChangedEvent<Cell>(JobCell.this));
		};

		public abstract void removeCurrentStyle();
	}

	private class Ready extends RunVis {

		@Override
		public void removeCurrentStyle() {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(getVisualization().getStyle(),
							"ready"));
		}
	}

	private class Running extends RunVis {

		@Override
		public void removeCurrentStyle() {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(getVisualization().getStyle(),
							"running"));
		}
	}

	private class Done extends RunVis {

		@Override
		public void removeCurrentStyle() {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(getVisualization().getStyle(),
							"done"));
		}
	}

	private class Cancelled extends RunVis {

		@Override
		public void removeCurrentStyle() {
			getVisualization().setStyle(
					mxStyleUtils.removeStylename(getVisualization().getStyle(),
							"cancelled"));
		}
	}

}
