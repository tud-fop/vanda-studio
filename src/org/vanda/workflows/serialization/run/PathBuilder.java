package org.vanda.workflows.serialization.run;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class PathBuilder {
	public String path;
	
	public String build() {
		return path;
	}
	
	public static Factory<PathBuilder> createFactory() {
		return new Fäctory();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<PathBuilder> createProcessor() {
		return new CompositeFieldProcessor<PathBuilder>(new PathProcessor());
	}
	
	public static final class Fäctory implements Factory<PathBuilder> {
		@Override
		public PathBuilder create() {
			return new PathBuilder();
		}
	}

	public static final class PathProcessor implements SingleFieldProcessor<PathBuilder> {
		@Override
		public String getFieldName() {
			return "path";
		}
		
		@Override
		public void process(String name, String value, PathBuilder b) {
			b.path = value;
		}
	}
	
}
