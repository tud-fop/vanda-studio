package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

class Graph extends mxGraph {

	public Graph() {
		super(JobRendering.getStylesheet());
		JobRendering.refStylesheet(1);
		setCellsCloneable(false);
		setSplitEnabled(false);
		// graph.setDropEnabled(false);
		// graph.setCellsMovable(false);
		setCellsEditable(false);
		setCollapseToPreferredSize(true);
		// setAutoSizeCells(true);
		setExtendParents(true);
		setExtendParentsOnAdd(true);
		setCellsResizable(true);
		setCellsDisconnectable(false);
		setMultigraph(false); // no effect!
		setAllowLoops(false);
		setAllowDanglingEdges(false);
		setMultiplicities(new mxMultiplicity[] { new mxMultiplicity(false,
				null, null, null, 0, "1", null, ".", "", false) {
			@Override
			public String check(mxGraph graph, Object edge, Object source,
					Object target, int sourceOut, int targetIn) {
				if (targetIn == 0)
					return null;
				else
					return countError;
			}
		} });
	}

	@Override
	public String convertValueToString(Object cell) {

		Object value = model.getValue(cell);

		if (model.isVertex(cell)) {
			if (value instanceof Job<?>)
				return ((Job<?>) value).getName();
			else if (value instanceof Token) {
				Object parentCell = model.getParent(cell);
				Object wa = model.getValue(parentCell);
				if (wa instanceof WorkflowAdapter) {
					return ((WorkflowAdapter) wa).workflow.getChild(
							(Token) value).getName();
				}
			}
		}
		return "";
	}

	@Override
	public Object createVertex(Object parent, String id, Object value,
			double x, double y, double width, double height, String style) {
		mxGeometry geometry = new mxGeometry(x, y, width, height);
		@SuppressWarnings("serial")
		mxCell vertex = new mxCell(value, geometry, style) {
			@Override
			protected Object cloneValue() {
				Object value = getValue();
				if (value instanceof Job<?>) {
					try {
						return ((Job<?>) value).clone();
					} catch (CloneNotSupportedException e) {
						return super.cloneValue();
					}
				} else
					return super.cloneValue();
			}
		};

		vertex.setId(id);
		vertex.setVertex(true);
		vertex.setConnectable(true);

		return vertex;
	}

	@Override
	public void finalize() throws Throwable {
		JobRendering.refStylesheet(-1);
		super.finalize();
	}

	@Override
	public boolean isCellSelectable(Object cell) {
		if (getModel().getValue(cell) instanceof PortAdapter
				|| getModel().getValue(cell) instanceof WorkflowAdapter)
			return false;

		return super.isCellSelectable(cell);
	}

	@Override
	public boolean isValidSource(Object cell) {
		return super.isValidSource(cell)
				&& (cell == null || ((mxCell) cell).getValue() instanceof PortAdapter
						&& !((PortAdapter) ((mxCell) cell).getValue()).input);
	}

	@Override
	public boolean isValidTarget(Object cell) {
		return super.isValidSource(cell) /* sic!! */
				&& (cell == null || ((mxCell) cell).getValue() instanceof PortAdapter
						&& ((PortAdapter) ((mxCell) cell).getValue()).input);
	}

	@Override
	public boolean isValidDropTarget(Object cell, Object[] cells) {
		// works more or less, we just need the right nodes
		return ((mxCell) cell).getValue() instanceof WorkflowAdapter;
	}

	// Removes the folding icon from simple jobs and disables folding
	// Allows folding of NestedHyperworkflows
	@Override
	public boolean isCellFoldable(Object cell, boolean collapse) {
		// FIXME this won't work
		mxCell c = (mxCell) cell;
		return c.getValue() instanceof CompositeJob<?, ?>;
	}

}
