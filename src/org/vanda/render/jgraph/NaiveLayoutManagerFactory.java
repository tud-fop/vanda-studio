package org.vanda.render.jgraph;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

public class NaiveLayoutManagerFactory implements LayoutManagerFactoryInterface {

	protected LayoutManagerInterface algorithmRenderer = new AlgorithmRenderer();
	protected LayoutManagerInterface corpusRenderer = new CorpusRenderer();
	protected LayoutManagerInterface grammarRenderer = new GrammarRenderer();
	protected LayoutManagerInterface orRenderer = new ChoiceNodeRenderer();
	protected LayoutManagerInterface sinkRenderer = new SinkRenderer();
	protected LayoutManagerInterface workflowRenderer = new WorkflowRenderer();
	protected LayoutManagerInterface literalRenderer = new LiteralRenderer();
	protected LayoutManagerInterface[] renderers  = { algorithmRenderer, corpusRenderer, grammarRenderer, orRenderer, sinkRenderer,
			workflowRenderer, literalRenderer };
	protected mxStylesheet staticStylesheet;
	protected int refCount = 0;
	private JGraphRendererAssortment rs = new JGraphRendererAssortment();

	protected mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();
		Map<String, Object> style;

		for (LayoutManagerInterface r : renderers) {
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

	public mxStylesheet getStylesheet() {
		if (staticStylesheet == null)
			staticStylesheet = createStylesheet();
		return staticStylesheet;
	}

	/**
	 * increase reference counter for stylesheet by rc (usually +1 or -1)
	 */
	public void refStylesheet(int rc) {
		refCount = refCount + rc;
		if (refCount == 0)
			staticStylesheet = null;
	}

	public JGraphRendererAssortment getRendererAssortment() {
		return rs;
	}

	public class JGraphRendererAssortment implements
			RendererAssortment<LayoutManagerInterface> {
		protected JGraphRendererAssortment() {
		}

		@Override
		public LayoutManagerInterface selectAlgorithmRenderer() {
			return new AlgorithmRenderer();
		}

		@Override
		public LayoutManagerInterface selectCorpusRenderer() {
			return new CorpusRenderer();
		}

		@Override
		public LayoutManagerInterface selectGrammarRenderer() {
			return new GrammarRenderer();
		}

		@Override
		public LayoutManagerInterface selectOrRenderer() {
			return orRenderer;
		}

		@Override
		public LayoutManagerInterface selectSinkRenderer() {
			return new SinkRenderer();
		}

		@Override
		public LayoutManagerInterface selectLiteralRenderer() {
			return new LiteralRenderer();
		}

		@Override
		public LayoutManagerInterface selectWorkflowRenderer() {
			return new WorkflowRenderer();
		}
	}

	protected abstract class DefaultRenderer implements LayoutManagerInterface {
		protected JobCell jobCell;
		//protected WeakHashMap<Integer, PortCell> inputs = new WeakHashMap<Integer, PortCell>();
		//protected WeakHashMap<Integer, PortCell> outputs = new WeakHashMap<Integer,PortCell>();
		//protected WeakHashMap<Integer, LocationCell> locations = new WeakHashMap<Integer, LocationCell>();
		int inputs = 0, outputs = 0, locations = 0; 
		public void addStyle(Map<String, Object> style) {
			style.put(mxConstants.STYLE_SPACING, 14);
			style.put(mxConstants.STYLE_SPACING_BOTTOM, -2);
			style.put(mxConstants.STYLE_AUTOSIZE, "1");
			style.put(mxConstants.STYLE_RESIZABLE, "0");
		}


		@Override
		public void register(Cell cell) {
			if (cell.getType().equals("JobCell"))
				jobCell = (JobCell) cell;
			else if (cell.getType().equals("InPortCell"))
			{	inputs++;
				cell.setZ(inputs);
				//inputs.put(inputs.size() + 1, (PortCell) cell);
			}
			else if (cell.getType().equals("OutPortCell"))
			{
				outputs++;
				cell.setZ(outputs);
			//	outputs.put(outputs.size() + 1, (PortCell) cell);
			}
			else if (cell.getType().equals("LocationCell"))
			{
				locations++;
				cell.setZ(locations);
				//locations.put(locations.size() + 1, (LocationCell) cell);
			}
		}


		@Override
		public void setUpLayout(Graph g) {
			mxCell v = jobCell.getVisualization();
			v.setStyle(getStyleName());
			v.setConnectable(false);
			v.setGeometry(new mxGeometry(jobCell.getX(), jobCell.getY(), jobCell.getWidth(), jobCell.getHeight()));
			v.setVertex(true);
			if (g.getGraph().isAutoSizeCell(v))
				g.getGraph().updateCellSize(v, true);
			
			for (int j = 0; j < jobCell.getVisualization().getChildCount(); ++j) {
				mxICell vis = jobCell.getVisualization().getChildAt(j);
				Cell cell = (Cell) vis.getValue();
				
				// Inports
				if (cell.getType().equals("InPortCell")) {
					mxGeometry geo = new mxGeometry(0, (int) cell.getZ()
							/ (inputs + 1.0), PORT_DIAMETER, PORT_DIAMETER);
					geo.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
					geo.setRelative(true);

					mxCell port = (mxCell) vis;
					port.setGeometry(geo); 
					port.setStyle("inport");
					port.setVertex(true);
				}  
			    
			    // OutPorts
				else if (cell.getType().equals("OutPortCell")) {
					mxGeometry geo = new mxGeometry(1, (int) cell.getZ() / (outputs + 1.0),
							OUTPORT_DIAMETER, OUTPORT_DIAMETER);
					geo.setOffset(new mxPoint(LOCATION_RADIUS, -OUTPORT_RADIUS));
					geo.setRelative(true);
					mxCell port = (mxCell) vis;
					port.setGeometry(geo); 
					port.setStyle("outport");
					port.setVertex(true);
			    }  
			    	
			    // Locations
				else if (cell.getType().equals("LocationCell")) {
					mxGeometry geo = new mxGeometry(1, (int) cell.getZ()/ (locations + 1.0), 
							LOCATION_DIAMETER, LOCATION_DIAMETER);
					geo.setOffset(new mxPoint(-LOCATION_RADIUS, -LOCATION_RADIUS));
					geo.setRelative(true);
					mxCell loc = (mxCell) vis; 
					loc.setGeometry(geo);
					loc.setStyle("location");
					loc.setVertex(true);
			    }
			}
			

		}

	}

	protected class AlgorithmRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "algorithm";
		}

	}

	protected class CorpusRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "corpus";
		}
	}

	protected class ChoiceNodeRenderer extends DefaultRenderer {
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

	protected class GrammarRenderer extends DefaultRenderer {
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

	protected class SinkRenderer extends DefaultRenderer {
		@Override
		public String getStyleName() {
			return "sink";
		}
	}

	protected class WorkflowRenderer extends DefaultRenderer {
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

	protected class LiteralRenderer extends DefaultRenderer {
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
	public LayoutManagerInterface getLayoutManager(Job job) {
		return job.selectRenderer(rs);
	}
}
