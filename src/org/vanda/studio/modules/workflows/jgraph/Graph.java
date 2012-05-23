package org.vanda.studio.modules.workflows.jgraph;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;

class Graph extends mxGraph {

	public Graph() {
		super(JobRendering.getStylesheet());
		JobRendering.refStylesheet(1);
		setCellsCloneable(false);
		setSplitEnabled(false);
		// setDropEnabled(false);
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
		setMultiplicities(new mxMultiplicity[] {
				new mxMultiplicity(false, null, null, null, 0, "1", null, ".",
						"", false) {
					@Override
					public String check(mxGraph graph, Object edge,
							Object source, Object target, int sourceOut,
							int targetIn) {
						if (targetIn == 0)
							return null;
						else
							return countError;
					}
				},
				new mxMultiplicity(false, null, null, null, 0, "1", null, ".",
						"", false) {
					@Override
					public String check(mxGraph graph, Object edge,
							Object source, Object target, int sourceOut,
							int targetIn) {
						mxIGraphModel model = graph.getModel();
						if (model.getParent(model.getParent(source)) == model
								.getParent(model.getParent(target)))
							return null;
						else
							return countError;
					}
				} });
	}

	@Override
	public String convertValueToString(Object cell) {
		Object value = model.getValue(cell);
		if (value instanceof Adapter)
			return ((Adapter) value).getName();
		else
			return "";
	}

	@Override
	public Object createEdge(Object parent, String id, Object value,
			Object source, Object target, String style) {
		if (value == null || "".equals(value))
			value = new ConnectionAdapter(null);
		return super.createEdge(parent, id, value, source, target, style);
	}

	@Override
	public void finalize() throws Throwable {
		JobRendering.refStylesheet(-1);
		super.finalize();
	}
	
	@Override
	public boolean isCellSelectable(Object cell) {
		if (getModel().getValue(cell) instanceof PortAdapter)
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
		// return ((mxCell) cell).getValue() instanceof WorkflowAdapter;
		// return super.isValidDropTarget(cell, cells);
		return ((mxCell) cell).getValue() instanceof WorkflowAdapter
				&& super.isValidDropTarget(cell, cells);
	}

	// Removes the folding icon from simple jobs and disables folding
	// Allows folding of NestedHyperworkflows
	@Override
	public boolean isCellFoldable(Object cell, boolean collapse) {
		// FIXME this won't work
		mxCell c = (mxCell) cell;
		return c.getValue() instanceof CompositeJobAdapter;
	}

}
