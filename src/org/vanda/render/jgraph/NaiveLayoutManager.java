package org.vanda.render.jgraph;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxPoint;

public class NaiveLayoutManager implements LayoutManager {

	@Override
	public void setUpLayout(Graph g, Cell container) {
		int inputs = 0, outputs = 0, locations = 0; 
		for (int j = 0; j < container.getVisualization().getChildCount(); ++j) {
			mxICell vis = container.getVisualization().getChildAt(j);
			Cell cell = (Cell) vis.getValue();
			if (cell.getType().equals("InPortCell")) {
				inputs++;
				cell.setZ(inputs);
			} else if (cell.getType().equals("OutPortCell")) {
				outputs++;
				cell.setZ(outputs);
			} else if (cell.getType().equals("LocationCell")) {
				locations++;
				cell.setZ(locations);
			}

		}
		
		for (int j = 0; j < container.getVisualization().getChildCount(); ++j) {
			mxICell vis = container.getVisualization().getChildAt(j);
			Cell cell = (Cell) vis.getValue();
			
			// Inports
			if (cell.getType().equals("InPortCell")) {
				mxGeometry geo = new mxGeometry(0, (Integer) cell.getZ()
						/ (inputs + 1.0), PORT_DIAMETER, PORT_DIAMETER);
				geo.setOffset(new mxPoint(-PORT_DIAMETER, -PORT_RADIUS));
				geo.setRelative(true);

				mxCell port = (mxCell) vis;
				port.setGeometry(geo); 
			}  
		    
		    // OutPorts
			else if (cell.getType().equals("OutPortCell")) {
				mxGeometry geo = new mxGeometry(1, (Integer) cell.getZ() / (outputs + 1.0),
						OUTPORT_DIAMETER, OUTPORT_DIAMETER);
				geo.setOffset(new mxPoint(LOCATION_RADIUS, -OUTPORT_RADIUS));
				geo.setRelative(true);
				
				mxCell port = (mxCell) vis;
				port.setGeometry(geo); 
		    }  
		    	
		    // Locations
			else if (cell.getType().equals("LocationCell")) {
				mxGeometry geo = new mxGeometry(1, (Integer) cell.getZ()/ (locations + 1.0), 
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

	protected static final int PORT_DIAMETER = 14;
	protected static final int OPORT_DIAMETER = 30;

	protected static final int PORT_RADIUS = PORT_DIAMETER / 2;
	protected static final int OPORT_RADIUS = OPORT_DIAMETER / 2;

	protected static final int OUTPORT_DIAMETER = 14;

	protected static final int OUTPORT_RADIUS = OUTPORT_DIAMETER / 2;

	protected static final int LOCATION_DIAMETER = 16;

	protected static final int LOCATION_RADIUS = LOCATION_DIAMETER / 2;

}
