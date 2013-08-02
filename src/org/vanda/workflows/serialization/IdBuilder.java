package org.vanda.workflows.serialization;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class IdBuilder {
	public String id;
	
	public String build() {
		return id;
	}
	
	public static Factory<IdBuilder> createFactory() {
		return new Fäctory();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<IdBuilder> createProcessor() {
		return new CompositeFieldProcessor<IdBuilder>(new IdProcessor());
	}
	
	public static final class Fäctory implements Factory<IdBuilder> {
		@Override
		public IdBuilder create() {
			return new IdBuilder();
		}
	}

	public static final class IdProcessor implements SingleFieldProcessor<IdBuilder> {
		@Override
		public String getFieldName() {
			return "id";
		}
		
		@Override
		public void process(String name, String value, IdBuilder b) {
			b.id = value;
		}
	}
}
