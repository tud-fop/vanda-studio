package org.vanda.workflows.serialization;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class GeometryBuilder {
	public double x;
	public double y;
	public double width;
	public double height;

	public double[] build() {
		return new double[] { x, y, width, height };
	}
	
	public static Factory<GeometryBuilder> createFactory() {
		return new Fäctory();
	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<GeometryBuilder> createProcessor() {
		return new CompositeFieldProcessor<GeometryBuilder>(new XProcessor(), new YProcessor(), new WidthProcessor(),
				new HeightProcessor());
	}
	
	public static final class Fäctory implements Factory<GeometryBuilder> {
		@Override
		public GeometryBuilder create() {
			return new GeometryBuilder();
		}
	}

	public static final class XProcessor implements SingleFieldProcessor<GeometryBuilder> {
		@Override
		public String getFieldName() {
			return "x";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.x = Double.parseDouble(value);
		}
	}

	public static final class YProcessor implements SingleFieldProcessor<GeometryBuilder> {
		@Override
		public String getFieldName() {
			return "y";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.y = Double.parseDouble(value);
		}
	}

	public static final class WidthProcessor implements SingleFieldProcessor<GeometryBuilder> {
		@Override
		public String getFieldName() {
			return "width";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.width = Double.parseDouble(value);
		}
	}

	public static final class HeightProcessor implements SingleFieldProcessor<GeometryBuilder> {
		@Override
		public String getFieldName() {
			return "height";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.height = Double.parseDouble(value);
		}
	}

}
