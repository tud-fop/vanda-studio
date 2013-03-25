package org.vanda.render.jgraph;

import java.util.HashMap;

import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;

public class NaiveLayoutManagerFactory2 implements LayoutManagerFactoryInterface {
	private static HashMap<String, String> layouts = new HashMap<String,String>();
	static {
		layouts.put("LocationCell", "location");
		layouts.put("InPortCell", "inport");
		layouts.put("OutPortCell", "outport");
		layouts.put("JobCell", "job");
		layouts.put("ConnectionCell", "connection");
	}
	
	
	public String getStyleName(Cell cell) {
		return layouts.get(cell.getType());
	}

	
	public mxGeometry getGeometry(Cell cell) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private interface GeometryInterface {
		public mxGeometry getGeometry(Cell cell);
	}
	
	private class PortGeometry implements GeometryInterface {

		@Override
		public mxGeometry getGeometry(Cell cell) {
			int i = 1, size = 5;
			mxGeometry geo = new mxGeometry(0, (i + 1.0)/ (size + 1.0), NaiveLayoutManagerFactory.PORT_DIAMETER, NaiveLayoutManagerFactory.PORT_DIAMETER);
			geo.setOffset(new mxPoint(-NaiveLayoutManagerFactory.PORT_DIAMETER, -NaiveLayoutManagerFactory.PORT_RADIUS));
			geo.setRelative(true);
			return geo;
		}
	}
	
	private class LocationGeometry implements GeometryInterface {

		@Override
		public mxGeometry getGeometry(Cell cell) {
			int i = 1, size = 5;
			mxGeometry geo = new mxGeometry(1, (i + 1.0)
					/ (size + 1.0), NaiveLayoutManagerFactory.LOCATION_DIAMETER,
					NaiveLayoutManagerFactory.LOCATION_DIAMETER);
			geo.setOffset(new mxPoint(-NaiveLayoutManagerFactory.LOCATION_RADIUS,
					-NaiveLayoutManagerFactory.LOCATION_RADIUS));
			geo.setRelative(true);
			return geo;
		}
		
	}

	@Override
	public void getLayoutManager(Job job) {
		// TODO Auto-generated method stub
		
	}


}
