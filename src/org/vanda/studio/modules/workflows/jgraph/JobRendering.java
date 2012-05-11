package org.vanda.studio.modules.workflows.jgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.hyper.Job;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

public class JobRendering {

	protected static Renderer algorithmRenderer = new AlgorithmRenderer();
	protected static Renderer corpusRenderer = new CorpusRenderer();
	protected static Renderer grammarRenderer = new GrammarRenderer();
	protected static Renderer orRenderer = new ChoiceNodeRenderer();
	protected static Renderer sinkRenderer = new SinkRenderer();
	protected static Renderer termRenderer = new TermRenderer();
	protected static Renderer textRenderer = new TextRenderer();
	protected static Renderer[] renderers = { algorithmRenderer,
			corpusRenderer, grammarRenderer, orRenderer, sinkRenderer,
			termRenderer, textRenderer };
	protected static mxStylesheet stylesheet;
	protected static int refCount = 0;
	private static JGraphRendererAssortment rs = new JGraphRendererAssortment();

	// helper class
	private JobRendering() {
	}
	
	public static mxGraph createGraph() {
		return new Graph();
	}

	protected static mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();
		Map<String, Object> style;

		for (Renderer r : renderers) {
			style = new HashMap<String, Object>(
					stylesheet.getDefaultVertexStyle());
			r.addStyle(style);
			stylesheet.putCellStyle(r.getStyleName(), style);
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
		style.put(mxConstants.STYLE_EDGE, mxEdgeStyle.SideToSide);

		style = new HashMap<String, Object>();
		// portStyle.putAll(graph.getStylesheet().getDefaultVertexStyle());
		style.put(mxConstants.STYLE_MOVABLE, "false");
		style.put(mxConstants.STYLE_NOLABEL, "true");
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LINE);
		style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
		style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_WEST);
		style.put(mxConstants.STYLE_PORT_CONSTRAINT, mxConstants.DIRECTION_WEST);
		stylesheet.putCellStyle("inport", style);

		style = new HashMap<String, Object>();
		// portStyle.putAll(graph.getStylesheet().getDefaultVertexStyle());
		style.put(mxConstants.STYLE_MOVABLE, "false");
		style.put(mxConstants.STYLE_NOLABEL, "true");
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LINE);
		style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
		style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		style.put(mxConstants.STYLE_PORT_CONSTRAINT, mxConstants.DIRECTION_EAST);
		stylesheet.putCellStyle("outport", style);
		
		return stylesheet;
	}

	public static mxStylesheet getStylesheet() {
		if (stylesheet == null)
			stylesheet = createStylesheet();
		return stylesheet;
	}

	/**
	 * increase reference counter for stylesheet by rc (usually +1 or -1)
	 */
	public static void refStylesheet(int rc) {
		refCount = refCount + rc;
		if (refCount == 0)
			stylesheet = null;
	}

	public static JGraphRendererAssortment getRendererAssortment() {
		return rs;
	}

	public static class JGraphRendererAssortment implements
			RendererAssortment<Renderer> {
		protected JGraphRendererAssortment() {
		}

		@Override
		public Renderer selectAlgorithmRenderer() {
			return JobRendering.algorithmRenderer;
		}

		@Override
		public Renderer selectCorpusRenderer() {
			return JobRendering.corpusRenderer;
		}

		@Override
		public Renderer selectGrammarRenderer() {
			return JobRendering.grammarRenderer;
		}

		@Override
		public Renderer selectOrRenderer() {
			return JobRendering.orRenderer;
		}

		@Override
		public Renderer selectSinkRenderer() {
			return JobRendering.sinkRenderer;
		}

		@Override
		public Renderer selectTermRenderer() {
			return JobRendering.termRenderer;
		}

		@Override
		public Renderer selectTextRenderer() {
			return JobRendering.textRenderer;
		}
	}

	protected abstract static class DefaultRenderer implements Renderer {
		@Override
		public void addStyle(Map<String, Object> style) {
			//style.put(mxConstants.STYLE_AUTOSIZE, "0");
			style.put(mxConstants.STYLE_SPACING, 10);
			style.put(mxConstants.STYLE_SPACING_BOTTOM, -2);
			style.put(mxConstants.STYLE_AUTOSIZE, "1");
			style.put(mxConstants.STYLE_RESIZABLE, "0");
		}

		@Override
		public void render(Job<?> hj, mxGraph g, Object parentCell) {
			Object parent = parentCell;
			if (parentCell == null) {
				parent = g.getDefaultParent();
			}

			g.getModel().beginUpdate();
			try {
				// insert new node into the graph that has the specified hwf as
				// value and that shared the same dimensions
				mxCell v = (mxCell) g.insertVertex(parent, null, hj, hj.getX(),
						hj.getY(), hj.getWidth(), hj.getHeight(),
						this.getStyleName());
				v.setConnectable(false);

				if (g.isAutoSizeCell(v))
					g.updateCellSize(v, true); // XXX was: g.ensureMinimumCellSize(v);
				//System.out.println(g.isAutoSizeCell(v));

				// insert a cell for every input port
				List<Port> in = hj.getInputPorts();
				for (int i = 0; i < in.size(); i++) {
					mxGeometry geo = new mxGeometry(0, (i + 1.0)
							/ (in.size() + 1.0), PORT_DIAMETER, PORT_DIAMETER);
					geo.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
					geo.setRelative(true);

					mxCell port = new mxCell(new PortAdapter(true, i), geo,
							"inport");
					port.setVertex(true);

					g.addCell(port, v);
				}

				// insert a cell for every output port
				List<Port> out = hj.getOutputPorts();
				for (int i = 0; i < out.size(); i++) {
					mxGeometry geo = new mxGeometry(1, (i + 1.0)
							/ (out.size() + 1.0), PORT_DIAMETER, PORT_DIAMETER);
					geo.setOffset(new mxPoint(0, -PORT_RADIUS));
					geo.setRelative(true);

					mxCell port = new mxCell(new PortAdapter(false, i), geo,
							"outport");
					port.setVertex(true);

					g.addCell(port, v);
				}

				// in case of NestedHyperworkflow also add inner ports, then
				// port children of a node are in the following order:
				// regular input ports, regular output ports, inner source
				// ports,
				// inner target ports
				/*
				 * if (hwf instanceof NestedHyperworkflow) { for (int i = 0; i <
				 * in.size(); i++) { mxGeometry geo = new mxGeometry(0, (i +
				 * 1.0) / (in.size() + 1.0), PORT_DIAMETER, PORT_DIAMETER);
				 * geo.setOffset(new mxPoint(0, -PORT_RADIUS));
				 * geo.setRelative(true);
				 * 
				 * mxCell port = new mxCell(new Port(false, i + in.size()), geo,
				 * "port"); port.setVertex(true);
				 * 
				 * g.addCell(port, v); }
				 * 
				 * for (int i = 0; i < out.size(); i++) { mxGeometry geo = new
				 * mxGeometry(1, (i + 1.0) / (out.size() + 1.0), PORT_DIAMETER,
				 * PORT_DIAMETER); geo.setOffset(new mxPoint(-PORT_DIAMETER,
				 * -PORT_RADIUS)); geo.setRelative(true);
				 * 
				 * mxCell port = new mxCell(new Port(true, i + out.size()), geo,
				 * "port"); port.setVertex(true);
				 * 
				 * g.addCell(port, v); } }
				 */
			} finally {
				g.getModel().endUpdate();
			}
		}

	}

	protected static class AlgorithmRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "algorithm";
		}
	}

	protected static class CorpusRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "corpus";
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

	protected static class SinkRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "sink";
		}
	}

	protected static class TermRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "term";
		}
	}

	protected static class TextRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "text";
		}
	}

	protected static final int PORT_DIAMETER = 14;

	protected static final int PORT_RADIUS = PORT_DIAMETER / 2;

}
