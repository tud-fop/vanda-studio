package org.vanda.workflows.serialization;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public final class AssignmentBuilder {

	public Integer key;

	public String value;

	public static Factory<AssignmentBuilder> createFactory() {
		return new Fäctory();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<AssignmentBuilder> createProcessor() {
		return new CompositeFieldProcessor<AssignmentBuilder>(new KeyProcessor(), new ValueProcessor());
	}

	public static final class Fäctory implements Factory<AssignmentBuilder> {
		@Override
		public AssignmentBuilder create() {
			return new AssignmentBuilder();
		}
	}

	public static final class KeyProcessor implements SingleFieldProcessor<AssignmentBuilder> {

		@Override
		public void process(String name, String value, AssignmentBuilder b) {
			b.key = Integer.parseInt(value, 16);
		}

		@Override
		public String getFieldName() {
			return "key";
		}

	}

	public static final class ValueProcessor implements SingleFieldProcessor<AssignmentBuilder> {

		@Override
		public void process(String name, String value, AssignmentBuilder b) {
			b.value = value;
		}

		@Override
		public String getFieldName() {
			return "value";
		}

	}

}
