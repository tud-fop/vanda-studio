package org.vanda.render.jgraph;

import java.util.HashMap;
import java.util.Map;
import org.vanda.workflows.elements.RendererAssortment;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxStyleUtils;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

public class JGraphRendering {

	protected static class AlgorithmRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "algorithm";
		}

	}

	protected static class CancelledStyle extends DefaultSupplementalStyle {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_FILLCOLOR, "#FF0000");
		}

		@Override
		public String getStyleName() {
			return "cancelled";
		}
	}

	protected static class ChoiceNodeRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			super.addStyle(style);
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
			style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.EllipsePerimeter);
			style.put(mxConstants.STYLE_NOLABEL, "true");
		}

		@Override
		public String getStyleName() {
			return "or";
		}
	}

	protected static class CorpusRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "corpus";
		}
	}

	protected static abstract class DefaultRenderer implements Renderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_SPACING, 14);
			style.put(mxConstants.STYLE_SPACING_BOTTOM, -2);
			style.put(mxConstants.STYLE_AUTOSIZE, "1");
			style.put(mxConstants.STYLE_RESIZABLE, "0");
		}

		@Override
		public void render(Cell container) {
			mxCell v = container.getVisualization();

			v.setStyle(getStyleName());
			v.setConnectable(false);
			v.setGeometry(new mxGeometry(container.getX(), container.getY(), container.getWidth(), container
					.getHeight()));
			v.setVertex(true);
		}
	}

	protected static abstract class DefaultSupplementalStyle implements SupplementalStyle {
		@Override
		public boolean updateStyle(mxCell cell, boolean enable) {
			return JGraphRendering.updateStyleWithName(cell, getStyleName(), enable);
		}
	}

	protected static class DoneStyle extends DefaultSupplementalStyle {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_FILLCOLOR, "#55DD55");
		}

		@Override
		public String getStyleName() {
			return "done";
		}
	}

	protected static class ErroneousStyle extends DefaultSupplementalStyle {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_STROKECOLOR, "#DD5555");
			style.put(mxConstants.STYLE_STROKEWIDTH, "3");
		}

		@Override
		public String getStyleName() {
			return "erroneous";
		}
	}

	protected static class GrammarRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			super.addStyle(style);
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
			style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RhombusPerimeter);
		}

		@Override
		public String getStyleName() {
			return "grammar";
		}
	}

	protected static class HighlightedStyle extends DefaultSupplementalStyle {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_STROKECOLOR, "#0099FF");
			style.put(mxConstants.STYLE_STROKEWIDTH, "3");
		}

		@Override
		public String getStyleName() {
			return "highlighted";
		}
	}

	protected static class InPortRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_MOVABLE, "false");
			style.put(mxConstants.STYLE_NOLABEL, "true");
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LINE);
			style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
			style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
			style.put(mxConstants.STYLE_PORT_CONSTRAINT, mxConstants.DIRECTION_WEST);
		}

		@Override
		public String getStyleName() {
			return "inport";
		}

		@Override
		public void render(Cell container) {
			mxCell port = container.visualization;
			port.setStyle("inport");
			port.setVertex(true);
		}
	}

	public static class JGraphRendererAssortment implements RendererAssortment<Renderer> {
		protected JGraphRendererAssortment() {
		}

		@Override
		public Renderer selectAlgorithmRenderer() {
			return algorithmRenderer;
		}

		@Override
		public Renderer selectCorpusRenderer() {
			return corpusRenderer;
		}

		@Override
		public Renderer selectGrammarRenderer() {
			return grammarRenderer;
		}

		@Override
		public Renderer selectLiteralRenderer() {
			return literalRenderer;
		}

		@Override
		public Renderer selectOrRenderer() {
			return orRenderer;
		}

		@Override
		public Renderer selectSinkRenderer() {
			return sinkRenderer;
		}

		@Override
		public Renderer selectWorkflowRenderer() {
			return workflowRenderer;
		}
	}

	protected static class LiteralRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			super.addStyle(style);
			style.remove(mxConstants.STYLE_SPACING);
			style.put(mxConstants.STYLE_SPACING_LEFT, 4);
			style.put(mxConstants.STYLE_SPACING_RIGHT, 8);
			style.put(mxConstants.STYLE_SPACING_TOP, 4);
			style.put(mxConstants.STYLE_SPACING_BOTTOM, 0);
			// style.put(mxConstants.STYLE_SPACING_BOTTOM, -2);
			style.put(mxConstants.STYLE_AUTOSIZE, "1");
			style.put(mxConstants.STYLE_RESIZABLE, "0");
		}

		@Override
		public String getStyleName() {
			return "text";
		}
	}

	protected static class LocationRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_MOVABLE, "false");
			style.put(mxConstants.STYLE_NOLABEL, "true");
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
			style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.EllipsePerimeter);
			style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		}

		@Override
		public String getStyleName() {
			return "location";
		}

		@Override
		public void render(Cell container) {
			mxCell loc = container.visualization;
			loc.setStyle("location");
			loc.setVertex(true);
		}
	}

	protected static class OutPortRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_MOVABLE, "false");
			style.put(mxConstants.STYLE_NOLABEL, "true");
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LINE);
			style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
			style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
			style.put(mxConstants.STYLE_PORT_CONSTRAINT, mxConstants.DIRECTION_EAST);
		}

		@Override
		public String getStyleName() {
			return "outport";
		}

		@Override
		public void render(Cell container) {
			mxCell port = container.visualization;
			port.setStyle("outport");
			port.setVertex(true);
		}
	}

	protected static class RunningStyle extends DefaultSupplementalStyle {
		@Override
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_FILLCOLOR, "#FFD010");
		}

		@Override
		public String getStyleName() {
			return "running";
		}
	}

	protected static class SinkRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "sink";
		}
	}

	protected static class WorkflowRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			super.addStyle(style);
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_SWIMLANE);
			style.put(mxConstants.STYLE_MOVABLE, "false");
			style.put(mxConstants.STYLE_AUTOSIZE, "0");
			style.put(mxConstants.STYLE_STARTSIZE, "23");
		}

		@Override
		public String getStyleName() {
			return "workflow";
		}
	}

	protected static final Renderer algorithmRenderer = new AlgorithmRenderer();
	protected static final Renderer corpusRenderer = new CorpusRenderer();
	protected static final Renderer grammarRenderer = new GrammarRenderer();
	protected static final Renderer literalRenderer = new LiteralRenderer();
	protected static final Renderer orRenderer = new ChoiceNodeRenderer();
	protected static final Renderer sinkRenderer = new SinkRenderer();
	protected static final Renderer workflowRenderer = new WorkflowRenderer();
	protected static final Renderer inPortRenderer = new InPortRenderer();
	protected static final Renderer outPortRenderer = new OutPortRenderer();
	protected static final Renderer locationRenderer = new LocationRenderer();

	public static final SupplementalStyle cancelledStyle = new CancelledStyle();
	public static final SupplementalStyle doneStyle = new DoneStyle();
	public static final SupplementalStyle erroneousStyle = new ErroneousStyle();
	public static final SupplementalStyle highlightedStyle = new HighlightedStyle();
	public static final SupplementalStyle runningStyle = new RunningStyle();

	protected static Renderer[] renderers = { algorithmRenderer, corpusRenderer, grammarRenderer, orRenderer,
			sinkRenderer, workflowRenderer, literalRenderer, inPortRenderer, outPortRenderer, locationRenderer };

	protected static SupplementalStyle[] sstyles = { cancelledStyle, doneStyle, erroneousStyle, highlightedStyle,
			runningStyle };

	protected static mxStylesheet staticStylesheet;

	protected static int refCount = 0;

	protected static RendererAssortment<Renderer> rs = new JGraphRendererAssortment();

	protected static mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();
		Map<String, Object> style;

		for (Renderer r : renderers) {
			style = new HashMap<String, Object>(stylesheet.getDefaultVertexStyle());
			r.addStyle(style);
			stylesheet.putCellStyle(r.getStyleName(), style);
		}

		for (SupplementalStyle s : sstyles) {
			style = new HashMap<String, Object>();
			s.addStyle(style);
			stylesheet.putCellStyle(s.getStyleName(), style);
		}
		
		/*
		 * style = new HashMap<String,
		 * Object>(stylesheet.getDefaultVertexStyle());
		 * style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		 * style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RhombusPerimeter);
		 * //style.put(mxConstants.STYLE_FILLCOLOR, "blue");
		 * stylesheet.putCellStyle("source", style);
		 */

		style = stylesheet.getDefaultEdgeStyle();
		style.put(mxConstants.STYLE_EDGE, mxEdgeStyle.orthConnector); // SideToSide);
		style.put(mxConstants.STYLE_EDGE, mxEdgeStyle.EntityRelation);
		// style.put(mxConstants.STYLE_ROUNDED, "true");
		style.put(mxConstants.STYLE_MOVABLE, "false");

		return stylesheet;
	}

	public static String addRemoveStylename(String style, String styleName, boolean add) {
		return add ? mxStyleUtils.addStylename(style, styleName) : mxStyleUtils.removeStylename(style, styleName);
	}

	public static boolean hasStyleName(String style, String styleName) {
		return mxStyleUtils.indexOfStylename(style, styleName) != -1;
	}

	// return whether the style had to be updated or not
	public static boolean updateStyleWithName(mxCell cell, String styleName, boolean add) {
		String style = cell.getStyle();
		if (add != hasStyleName(style, styleName)) {
			cell.setStyle(addRemoveStylename(style, styleName, add));
			return true;
		} else
			return false;
	}

	public static RendererAssortment<Renderer> getRendererAssortment() {
		return rs;
	}

	public static mxStylesheet getStylesheet() {
		if (staticStylesheet == null)
			staticStylesheet = createStylesheet();
		return staticStylesheet;
	}

	/**
	 * increase reference counter for stylesheet by rc (usually +1 or -1)
	 */
	public static void refStylesheet(int rc) {
		refCount = refCount + rc;
		if (refCount == 0)
			staticStylesheet = null;
	}

}
