package org.vanda.render.jgraph;

import java.util.HashMap;
import java.util.Map;

import org.vanda.util.Pair;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxPoint;

public class NaiveLayoutManager implements LayoutManager {

	private static Assortment assortment = new Assortment();

	@Override
	public void setUpLayout(Graph g, Cell container) {
		Map<LayoutSelector, Pair<Integer, Integer>> layout = new HashMap<LayoutSelector, Pair<Integer, Integer>>();
		layout.put(INPORT, new Pair<Integer, Integer>(0, 0));
		layout.put(OUTPORT, new Pair<Integer, Integer>(0, 0));
		layout.put(LOCATION, new Pair<Integer, Integer>(0, 0));
		
		for (int j = 0; j < container.getVisualization().getChildCount(); ++j) {
			mxICell vis = container.getVisualization().getChildAt(j);
			Cell cell = (Cell) vis.getValue();
			LayoutSelector ls = cell.getLayoutSelector();
			if (layout.containsKey(ls))
				layout.put(ls, new Pair<Integer, Integer>((Integer) 0,
						(Integer) layout.get(ls).snd + 1));
		}

		for (int j = 0; j < container.getVisualization().getChildCount(); ++j) {
			mxICell vis = container.getVisualization().getChildAt(j);
			Cell cell = (Cell) vis.getValue();

			cell.getLayoutSelector().selectLayout(assortment)
					.layout(cell, layout);
		}

	}

	private interface LayOuter {
		void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout);
	}

	private static class InPort implements LayOuter {

		@Override
		public void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout) {
			mxGeometry geo = new mxGeometry(0, (layout.get(INPORT).fst + 1)
					/ (layout.get(INPORT).snd + 1.0), PORT_DIAMETER,
					PORT_DIAMETER);
			geo.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
			geo.setRelative(true);
			
			cell.getVisualization().setGeometry(geo);

			// increment counter
			layout.put(INPORT, new Pair<Integer, Integer>(
					layout.get(INPORT).fst + 1, layout.get(INPORT).snd));
		}

	}

	private static class OutPort implements LayOuter {

		@Override
		public void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout) {
			mxGeometry geo = new mxGeometry(1, (layout.get(OUTPORT).fst + 1)
					/ (layout.get(OUTPORT).snd + 1.0), OUTPORT_DIAMETER,
					OUTPORT_DIAMETER);
			geo.setOffset(new mxPoint(LOCATION_RADIUS, -OUTPORT_RADIUS));
			geo.setRelative(true);
			
			cell.getVisualization().setGeometry(geo);
			
			// increment counter
			layout.put(OUTPORT, new Pair<Integer, Integer>(
					layout.get(OUTPORT).fst + 1, layout.get(INPORT).snd));

		}

	}

	private static class Location implements LayOuter {

		@Override
		public void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout) {
			mxGeometry geo = new mxGeometry(1, (layout.get(LOCATION).fst + 1)
					/ (layout.get(LOCATION).snd + 1.0), LOCATION_DIAMETER,
					LOCATION_DIAMETER);
			geo.setOffset(new mxPoint(-LOCATION_RADIUS, -LOCATION_RADIUS));
			geo.setRelative(true);
			
			cell.getVisualization().setGeometry(geo);
			// increment counter
			layout.put(LOCATION,
					new Pair<Integer, Integer>(layout.get(LOCATION).fst + 1,
							layout.get(LOCATION).snd));
		}

	}

	private static class Job implements LayOuter {

		@Override
		public void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout) {
			// do nothing ?!
		}

	}

	private static class Connection implements LayOuter {

		@Override
		public void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout) {
			// do nothing ?!
		}

	}
	
	private static class Workflow implements LayOuter {

		@Override
		public void layout(Cell cell,
				Map<LayoutSelector, Pair<Integer, Integer>> layout) {
			// do nothing ?!
		}

	}

	private static class Assortment implements LayoutAssortment<LayOuter> {
		private LayOuter inport = new InPort();
		private LayOuter outport = new OutPort();
		private LayOuter location = new Location();
		private LayOuter job = new Job();
		private LayOuter connection = new Connection();
		private LayOuter workflow = new Workflow(); 

		@Override
		public LayOuter getInport() {
			return inport;
		}

		@Override
		public LayOuter getOutport() {
			return outport;
		}

		@Override
		public LayOuter getLocation() {
			return location;
		}

		@Override
		public LayOuter getJob() {
			return job;
		}

		@Override
		public LayOuter getConnection() {
			return connection;
		}

		@Override
		public LayOuter getWorkflow() {
			return workflow;
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

}
