package org.vanda.studio.modules.workflows.jgraph;

import java.util.Hashtable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxConstants;
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
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ROUNDED, true);
		getStylesheet().putCellStyle("ROUNDED", style);
		setMultiplicities(new mxMultiplicity[] {
				new mxMultiplicity(false, null, null, null, 0, "1", null, ".",
						"", false) {
					@Override
					public String check(mxGraph graph, Object edge,
							Object source, Object target, int sourceOut,
							int targetIn) {
						return targetIn == 0 ? null : countError;
					}
				},
				new mxMultiplicity(false, null, null, null, 0, "1", null, ".",
						"", false) {
					@Override
					public String check(mxGraph graph, Object edge,
							Object source, Object target, int sourceOut,
							int targetIn) {
						mxIGraphModel m = graph.getModel();
						if (m.getParent(m.getParent(source)) == m
								.getParent(m.getParent(target)))
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
	
	public mxCell createCell(Object value, mxGeometry geometry, String style) {
		return new Cell(value, geometry, style);
	}

	@Override
	public Object createEdge(Object parent, String id, Object value,
			Object source, Object target, String style) {
		if (value == null || "".equals(value))
			value = new ConnectionAdapter(null);
		// XXX don't call with a constant
		return super.createEdge(parent, id, value, source, target, "ROUNDED");
	}

	@Override
	public Object createVertex(Object parent, String id, Object value,
			double x, double y, double width, double height, String style,
			boolean relative) {
		mxGeometry geometry = new mxGeometry(x, y, width, height);
		geometry.setRelative(relative);
		mxCell vertex = createCell(value, geometry, style);

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
				&& !(getModel().getValue(cell) instanceof LocationAdapter))
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
		mxCell c = (mxCell) cell;
		return c.getValue() instanceof CompositeJobAdapter;
	}

	
	private static class Cell extends mxCell {
		/**
		 * 
		 */
		private static final long serialVersionUID = 39927174614076724L;

		public Cell(Object value, mxGeometry geometry, String style) {
			super(value, geometry, style);
		}

		@Override
		protected Object cloneValue() {
			// Object value = getValue();
			if (value instanceof Adapter && value instanceof Cloneable) {
				// try {
					return value;
					// return ((Adapter) value).clone();
				// } catch (CloneNotSupportedException e) {
				// 	return super.cloneValue();
				// }
			} else
				return super.cloneValue();
		}
	}	
	
}
