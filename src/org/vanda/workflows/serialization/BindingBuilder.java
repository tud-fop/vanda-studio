package org.vanda.workflows.serialization;

import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public final class BindingBuilder {

	JobBuilder parent;

	public String port;

	public String variable;

	public static Factory<BindingBuilder> createFactory() {
		return new Fäctory();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<BindingBuilder> createProcessor() {
		return new CompositeFieldProcessor<BindingBuilder>(new PortProcessor(), new VariableProcessor());
	}

	public static final class Fäctory implements Factory<BindingBuilder> {
		@Override
		public BindingBuilder create() {
			return new BindingBuilder();
		}
	}

	public static final class PortProcessor implements SingleFieldProcessor<BindingBuilder> {

		@Override
		public void process(String name, String value, BindingBuilder b) {
			b.port = value;
		}

		@Override
		public String getFieldName() {
			return "port";
		}

	}

	public static final class VariableProcessor implements SingleFieldProcessor<BindingBuilder> {

		@Override
		public void process(String name, String value, BindingBuilder b) {
			b.variable = value;
		}

		@Override
		public String getFieldName() {
			return "variable";
		}

	}

}
