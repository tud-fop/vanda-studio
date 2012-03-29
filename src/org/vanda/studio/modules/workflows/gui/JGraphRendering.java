package org.vanda.studio.modules.workflows.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.modules.workflows.Connection;
import org.vanda.studio.modules.workflows.IHyperworkflow;
import org.vanda.studio.modules.workflows.NestedHyperworkflow;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxMultiplicity;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxStylesheet;

public class JGraphRendering {
	
	protected static Renderer algorithmRenderer = new AlgorithmRenderer();
	protected static Renderer corpusRenderer = new CorpusRenderer();
	protected static Renderer grammarRenderer = new GrammarRenderer();
	protected static Renderer orRenderer = new ChoiceNodeRenderer();
	protected static Renderer sinkRenderer = new SinkRenderer();
	protected static Renderer termRenderer = new TermRenderer();
	protected static Renderer textRenderer = new TextRenderer();
	protected static Renderer[] renderers =
	{
		algorithmRenderer,
		corpusRenderer,
		grammarRenderer,
		orRenderer,
		sinkRenderer,
		termRenderer,
		textRenderer
	};
	protected static mxStylesheet stylesheet;
	protected static int refCount = 0;
	
	// helper class
	private JGraphRendering() {
	}
	
	public static void render(IHyperworkflow to, Graph g, Object parentCell) {
		JGraphRendererSelection rs = JGraphRendering.newRendererSelection();
		to.selectRenderer(rs);
		JGraphRendering.Renderer r = rs.getRenderer();
		r.render(to, g, parentCell);
	}
	
	public static void render(Connection conn, Graph g, Object parentCell) {
		JGraphRendererSelection rs = JGraphRendering.newRendererSelection();
		//use arbitrary renderer, style is irrelevant for edge rendring
		rs.selectAlgorithmRenderer();
		JGraphRendering.Renderer r = rs.getRenderer();
		r.render(conn, g, parentCell);
	}
	
	
	public static Graph createGraph() {
		return new Graph();
	}
	
	protected static mxStylesheet createStylesheet() {
		mxStylesheet stylesheet = new mxStylesheet();
		Map<String, Object> style;
		
		for (Renderer r : renderers) {
			style = new HashMap<String, Object>(stylesheet.getDefaultVertexStyle());
			r.addStyle(style);
			stylesheet.putCellStyle(r.getStyleName(), style);
		}
		/*
		style = new HashMap<String, Object>(stylesheet.getDefaultVertexStyle());
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
		style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RhombusPerimeter);
		//style.put(mxConstants.STYLE_FILLCOLOR, "blue");
		stylesheet.putCellStyle("source", style);*/

		style = stylesheet.getDefaultEdgeStyle();
		style.put(mxConstants.STYLE_EDGE, mxEdgeStyle.TopToBottom);
		
		style = new HashMap<String,Object>();
		//portStyle.putAll(graph.getStylesheet().getDefaultVertexStyle());
		style.put(mxConstants.STYLE_MOVABLE, "false");
		style.put(mxConstants.STYLE_NOLABEL, "true");
		style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LINE);
		style.put(mxConstants.STYLE_PERIMETER, mxPerimeter.RectanglePerimeter);
		style.put(mxConstants.STYLE_DIRECTION, mxConstants.DIRECTION_SOUTH);
		style.put(mxConstants.STYLE_PORT_CONSTRAINT, mxConstants.DIRECTION_SOUTH);
		stylesheet.putCellStyle("port", style);
		
		return stylesheet;
	}
	
	public static mxStylesheet getStylesheet() {
		if (stylesheet == null)
			stylesheet = createStylesheet();
		return stylesheet;
	}
	
	public static JGraphRendererSelection newRendererSelection() {
		return new JGraphRendererSelection();
	}
	
	/** increase reference counter for stylesheet by rc
	 * (usually +1 or -1)
	 */
	public static void refStylesheet(int rc) {
		refCount = refCount + rc;
		if (refCount == 0)
			stylesheet = null;
	}
	
	public static class JGraphRendererSelection implements RendererSelection {		
		protected Renderer renderer;
		
		protected JGraphRendererSelection() {
			renderer = null;
		}
		
		protected Renderer getRenderer() {
			if (renderer == null) {
				throw new RuntimeException(
					"Someone did not select his renderer properly!");
			}
			return renderer;
		}
		
		@Override
		public void selectAlgorithmRenderer() {
			renderer = JGraphRendering.algorithmRenderer;
		}
		
		@Override
		public void selectCorpusRenderer() {
			renderer = JGraphRendering.corpusRenderer;
		}
		
		@Override
		public void selectGrammarRenderer() {
			renderer = JGraphRendering.grammarRenderer;
		}
		
		@Override
		public void selectOrRenderer() {
			renderer = JGraphRendering.orRenderer;
		}
		
		@Override
		public void selectSinkRenderer() {
			renderer = JGraphRendering.sinkRenderer;
		}
		
		@Override
		public void selectTermRenderer() {
			renderer = JGraphRendering.termRenderer;
		}
	
		@Override
		public void selectTextRenderer() {
			renderer = JGraphRendering.textRenderer;
		}
	}
	
	protected static interface Renderer {
		void addStyle(Map<String,Object> style);
		
		String getStyleName();
		
		void render(IHyperworkflow to, Graph g, Object parentCell);
		
		void render(Connection c, Graph g, Object parentCell);
	}
	
	protected abstract static class DefaultRenderer implements Renderer {
		@Override
		public void addStyle(Map<String,Object> style) {
		}
		
		@Override
		public void render(IHyperworkflow hwf, Graph g, Object parentCell) {
			Object parent = parentCell;
			if (parentCell == null) parent = g.getDefaultParent();
			
			g.getModel().beginUpdate();
			try
			{				
				mxCell v = (mxCell) g.insertVertex(parent, null, hwf, hwf.getX(),
						hwf.getY(), hwf.getWidth(), hwf.getHeight(), this.getStyleName());
				v.setConnectable(false);
				
				List<org.vanda.studio.modules.workflows.Port> in = hwf.getInputPorts();
				for (int i = 0; i < in.size(); i++) {
					mxGeometry geo = new mxGeometry((i+1.0)/(in.size()+1.0), 0,
							PORT_DIAMETER, PORT_DIAMETER);
					geo.setOffset(new mxPoint(-PORT_RADIUS, -PORT_DIAMETER));
					geo.setRelative(true);
					
					mxCell port = new mxCell(new Port(true, i), geo, "port");
					port.setVertex(true);
					
					g.addCell(port, v);
				}
				
				List<org.vanda.studio.modules.workflows.Port> out = hwf.getOutputPorts();
				for (int i = 0; i < out.size(); i++) {
					mxGeometry geo = new mxGeometry((i+1.0)/(out.size()+1.0), 1,
							PORT_DIAMETER, PORT_DIAMETER);
					geo.setOffset(new mxPoint(-PORT_RADIUS, 0));
					geo.setRelative(true);
					
					mxCell port = new mxCell(new Port(false, i), geo, "port");
					port.setVertex(true);
					
					g.addCell(port, v);
				}
			}
			finally {
				g.getModel().endUpdate();
			}
		}
		
		@Override
		public void render(Connection c, Graph g, Object parentCell) {
			Object parent = parentCell;
			if (parentCell == null) parent = g.getDefaultParent();
			
			g.getModel().beginUpdate();
			try {
				mxICell source = null;
				mxICell target = null;
				
				boolean innerSource = false;
				boolean innerTarget = false;
				
				//check if source or target equal the parent (i.e. inner ports are used)
				if (parent instanceof mxCell && ((mxCell)parent).getValue() != null && ((mxCell)parent).getValue().equals(c.getSource())) {
					source = (mxICell)parent;
					innerSource = true;
				}
				if (parent instanceof mxCell && ((mxCell)parent).getValue() != null && ((mxCell)parent).getValue().equals(c.getTarget())) {
					target = (mxICell)parent;
					innerTarget = true;
				}
				
				//get all child vertices of the graph
				Object[] childVertices = g.getChildCells(parent, true, false);
				for (Object o : childVertices) {
					//check if vertice's values equal the source or target of the connection 
					if (!innerSource && ((mxCell)o).getValue().equals(c.getSource())) source = (mxICell)o;
					if (!innerTarget && ((mxCell)o).getValue().equals(c.getTarget())) target = (mxICell)o;
				}
				
				if (source != null && target != null) {
					IHyperworkflow src = (IHyperworkflow)source.getValue();
					IHyperworkflow trg = (IHyperworkflow)target.getValue();
					
					//determine port id of srcPort
					List<org.vanda.studio.modules.workflows.Port> portList = src.getOutputPorts();
					if (innerSource) portList = src.getInputPorts();
					for (int i = 0; i < portList.size(); i++) {
						if (portList.get(i).equals(c.getSrcPort())) {
							//a vertice's ports are children of that node, first input ports, then output ports
							if (innerSource) source = source.getChildAt(i);
							else source = source.getChildAt(i + src.getInputPorts().size());
							break;
						}
					}
					//determine port id of targPort
					portList = trg.getInputPorts();
					if (innerTarget) portList = trg.getOutputPorts();
					for (int i = 0; i < portList.size(); i++) {
						if (portList.get(i).equals(c.getTargPort())) {
							if (innerTarget) target = target.getChildAt(trg.getInputPorts().size());
							else target = target.getChildAt(i);
							break;
						}
					}
				
					System.out.println(source.getClass());
					System.out.println(target.getClass());
					
					//add edge to the graph
					System.out.println(g);
					g.insertEdge(parent, null, c, source, target);
				}
			}
			finally {
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
		public void addStyle(Map<String,Object> style) {
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		}
		
		@Override
		public String getStyleName() {
			return "or";
		}
	}
	
	protected static class GrammarRenderer extends DefaultRenderer {
		@Override
		public void addStyle(Map<String,Object> style) {
			style.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RHOMBUS);
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

	protected static final int PORT_DIAMETER = 20;

	protected static final int PORT_RADIUS = PORT_DIAMETER / 2;
	
	protected static class Port implements Cloneable {
		public boolean input;
		public int index;
		
		public Port(boolean input, int index) {
			this.input = input;
			this.index = index;
		}
	}
	
	protected static class Graph extends mxGraph {
		
		protected Graph() {
			super(JGraphRendering.getStylesheet());
			JGraphRendering.refStylesheet(1);
			setCellsCloneable(false);
			setSplitEnabled(false);
			//graph.setDropEnabled(false);
			//graph.setCellsMovable(false);
			setCellsEditable(false);
			setCellsResizable(true);
			setCellsDisconnectable(false);
			setMultigraph(false); // no effect!
			setAllowLoops(false);
			setAllowDanglingEdges(false);
			setMultiplicities(new mxMultiplicity[] {
					new mxMultiplicity(
						false, null, null, null, 0, "1", null, ".", "", false)
					{
						@Override
						public String check(mxGraph graph, Object edge, Object source,
								Object target, int sourceOut, int targetIn)
						{
							if (targetIn == 0)
								return null;
							else
								return countError;
						}
			}});
		}
		
		@Override
		public String convertValueToString(Object cell) {

			Object value = model.getValue(cell);
			
			if (value instanceof IHyperworkflow)
				return ((IHyperworkflow)value).getName();
			else
				return "";
		}
		
		@Override
		public Object createVertex(Object parent, String id, Object value,
				double x, double y, double width, double height, String style)
		{
			mxGeometry geometry = new mxGeometry(x, y, width, height);
			mxCell vertex = new mxCell(value, geometry, style) {
				@Override
				protected Object cloneValue()
				{
					Object value = getValue();
					if (value instanceof IHyperworkflow) {
						try {
							return ((IHyperworkflow)value).clone();
						}
						catch (CloneNotSupportedException e) {
							return super.cloneValue();
						}
					}
					else
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
			JGraphRendering.refStylesheet(-1);
			super.finalize();
		}
		
		@Override
		public boolean isCellSelectable(Object cell) {
			if (getModel().getValue(cell) instanceof Port)
				return false;
			return super.isCellSelectable(cell);
		}
		
		@Override
		public boolean isValidSource(Object cell)
		{
			return super.isValidSource(cell)
				&& (cell == null
					|| ((mxCell)cell).getValue() instanceof Port
					&& !((Port)((mxCell)cell).getValue()).input);
		}
	
		@Override
		public boolean isValidTarget(Object cell)
		{
			return super.isValidSource(cell) /*sic!!*/
				&& (cell == null
					|| ((mxCell)cell).getValue() instanceof Port
					&& ((Port)((mxCell)cell).getValue()).input);
		}
		
		@Override
		public boolean isValidDropTarget(Object cell, Object[] cells)
		{
			return false;
		}

		// Removes the folding icon and disables any folding
		@Override
		public boolean isCellFoldable(Object cell, boolean collapse)
		{
			//allow NestedHyperworkflow with children to be foldable
			mxCell c = (mxCell)cell;
			if (c.getValue() instanceof NestedHyperworkflow) {
				if (!((NestedHyperworkflow)c.getValue()).getChildren().isEmpty()) return true;
			}
			
			
			return false;
		}
	}
}
