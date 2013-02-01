package org.vanda.workflows.serialization;

public class GeometryBuilder {
	public double x;
	public double y;
	public double width;
	public double height;
	
	public double[] build() {
		return new double[] { x, y, width, height };
	}

}
