package org.vanda.workflows.serialization.run;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class PriorityBuilder {
	public String id;
	public int priority;

	private static class Fäctory implements Factory<PriorityBuilder> {

		@Override
		public PriorityBuilder create() {
			return new PriorityBuilder();
		}

	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<PriorityBuilder> createProcessor() {
		return new CompositeFieldProcessor<PriorityBuilder>(new IdProcessor(), new PriorityProcessor()); 
	}

	public static Factory<PriorityBuilder> createFactory() {
		return new Fäctory();
	}
	
	public static final class IdProcessor implements SingleFieldProcessor<PriorityBuilder> {

		@Override
		public void process(String name, String value, PriorityBuilder b) {
			b.id = value;
		}

		@Override
		public String getFieldName() {
			return "id";
		}

	}

	public static final class PriorityProcessor implements SingleFieldProcessor<PriorityBuilder> {

		@Override
		public void process(String name, String value, PriorityBuilder b) {
			b.priority = Integer.parseInt(value, 16);
		}

		@Override
		public String getFieldName() {
			return "priority";
		}

	}
}
