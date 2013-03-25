package org.vanda.render.jgraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

public class NaiveLayoutManagerFactory implements LayoutManagerFactoryInterface {

	protected static JobLayouter algorithmRenderer = new AlgorithmRenderer();
	protected static JobLayouter corpusRenderer = new CorpusRenderer();
	protected static JobLayouter grammarRenderer = new GrammarRenderer();
	protected static JobLayouter orRenderer = new ChoiceNodeRenderer();
	protected static JobLayouter sinkRenderer = new SinkRenderer();
	protected static JobLayouter workflowRenderer = new WorkflowRenderer();
	protected static JobLayouter literalRenderer = new LiteralRenderer();
	protected static JobLayouter[] renderers = { algorithmRenderer,
			corpusRenderer, grammarRenderer, orRenderer, sinkRenderer,
			workflowRenderer, literalRenderer };
	protected static mxStylesheet staticStylesheet;
	protected static int refCount = 0;
	private static JGraphRendererAssortment rs = new JGraphRendererAssortment();

	protected static mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();
		Map<String, Object> style;

		for (JobLayouter r : renderers) {
			style = new HashMap<String, Object>(
					stylesheet.getDefaultVertexStyle());
			r.addStyle(style);
			stylesheet.putCellStyle(r.getStyleName(), style);
		}

		style = new HashMap<String, Object>();
		style.put(mxConstants.STYLE_STROKECOLOR, "#0099FF");
		style.put(mxConstants.STYLE_STROKEWIDTH, "3");
		stylesheet.putCellStyle("highlighted", style);

		style = new HashMap<String, Object>(stylesheet.getDefaultEdgeStyle());
		style.put(mxConstants.STYLE_STROKECOLOR, "#FF0000");
		style.put(mxConstants.STYLE_STROKEWIDTH, "3");
		stylesheet.putCellStyle("highlightededge", style);

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
		style.put(mxConstants.STYLE_ROUNDED, "true");
		style.put(mxConstants.STYLE_MOVABLE, "false");

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

		style = new HashMap<String, Object>();
		// portStyle.putAll(graph.getStylesheet().getDefaultVertexStyle());
		style.put(mxConstants.STYLE_MOVABLE, "false");
		style.put(mxConstants.STYLE_NOLABEL, "true");
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.EllipsePerimeter);
		style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_EAST);
		stylesheet.putCellStyle("location", style);

		return stylesheet;
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

	public static JGraphRendererAssortment getRendererAssortment() {
		return rs;
	}

	public static class JGraphRendererAssortment implements
			RendererAssortment<JobLayouter> {
		protected JGraphRendererAssortment() {
		}

		@Override
		public JobLayouter selectAlgorithmRenderer() {
			return NaiveLayoutManagerFactory.algorithmRenderer;
		}

		@Override
		public JobLayouter selectCorpusRenderer() {
			return NaiveLayoutManagerFactory.corpusRenderer;
		}

		@Override
		public JobLayouter selectGrammarRenderer() {
			return NaiveLayoutManagerFactory.grammarRenderer;
		}

		@Override
		public JobLayouter selectOrRenderer() {
			return NaiveLayoutManagerFactory.orRenderer;
		}

		@Override
		public JobLayouter selectSinkRenderer() {
			return NaiveLayoutManagerFactory.sinkRenderer;
		}

		@Override
		public JobLayouter selectLiteralRenderer() {
			return NaiveLayoutManagerFactory.literalRenderer;
		}

		@Override
		public JobLayouter selectWorkflowRenderer() {
			return NaiveLayoutManagerFactory.workflowRenderer;
		}
	}

	protected abstract class DefaultRenderer implements LayoutManagerInterface {
		private final JobCell jobCell;
		private Map<Integer, PortCell> inputs;
		private Map<Integer, PortCell> outputs;
		private Map<Integer, LocationCell> locations;
		
		public void addStyle(Map<String, Object> style) {
			// style.put(mxConstants.STYLE_AUTOSIZE, "0");
			style.put(mxConstants.STYLE_SPACING, 14);
			style.put(mxConstants.STYLE_SPACING_BOTTOM, -2);
			style.put(mxConstants.STYLE_AUTOSIZE, "1");
			style.put(mxConstants.STYLE_RESIZABLE, "0");
		}


		@Override
		public void register(Cell cell) {
			if (cell.getType() == "JobCell")
				jobCell = (JobCell) cell;
			else if (cell.getType() == "InPortCell")
				inputs.put(inputs.size() + 1, (PortCell) cell);
			else if (cell.getType() == "OutPortCell")
				outputs.put(outputs.size() + 1, (PortCell) cell);
			else if (cell.getType() == "LocationCell")
				locations.put(locations.size() + 1, (LocationCell) cell);
		}


		@Override
		public void setUpLayout(Graph g) {
			mxCell v = jobCell.getVisualization();
			v.setStyle("bla");
			v.setConnectable(false);
			if (g.getGraph().isAutoSizeCell(v))
				g.getGraph().updateCellSize(v, true);
			for (int i : inputs.keySet()) {
				mxGeometry geo = new mxGeometry(0, (i)
						/ (inputs.size() + 1.0), PORT_DIAMETER, PORT_DIAMETER);
				geo.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
				geo.setRelative(true);

				mxCell port = inputs.get(i).getVisualization(); 
				port.setGeometry(geo); 
				port.setStyle("inport");
				port.setVertex(true);
			}
			for (int i : outputs.keySet()) {
				mxGeometry geo = new mxGeometry(1, (i) / (outputs.size() + 1.0),
						OUTPORT_DIAMETER, OUTPORT_DIAMETER);
				geo.setOffset(new mxPoint(LOCATION_RADIUS, -OUTPORT_RADIUS));
				geo.setRelative(true);
				mxCell port = outputs.get(i).getVisualization();
				port.setGeometry(geo); 
				port.setStyle("inport");
				port.setVertex(true);
			}
			for (int i : locations.keySet()) {
				mxGeometry geo = new mxGeometry(1, (i ) / (locations.size() + 1.0), LOCATION_DIAMETER, LOCATION_DIAMETER);
				geo.setOffset(new mxPoint(-LOCATION_RADIUS, -LOCATION_RADIUS));
				geo.setRelative(true);
				mxCell loc = locations.get(i).getVisualization(); 
				loc.setGeometry(geo);
				loc.setStyle("location");
				loc.setVertex(true);
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

	protected static class LiteralRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "text";
		}
	}

	protected static final int PORT_DIAMETER = 14;
	protected static final int OPORT_DIAMETER = 30;

	protected static final int PORT_RADIUS = PORT_DIAMETER / 2;
	protected static final int OPORT_RADIUS = OPORT_DIAMETER / 2;

	protected static final int OUTPORT_DIAMETER = 14;

	protected static final int OUTPORT_RADIUS = OUTPORT_DIAMETER / 2;

	protected static final int LOCATION_DIAMETER = 16;

	protected static final int LOCATION_RADIUS = LOCATION_DIAMETER / 2;
	
	@Override
	public void getLayoutManager(Job job) {
		// TODO Auto-generated method stub
		
	}
}
