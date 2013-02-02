package org.vanda.workflows.toolinterfaces;

import java.util.Map;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.TokenSource;
import org.vanda.workflows.elements.Port;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class PortBuilder {

	public String name;
	public String type;

	public Port build(TokenSource ts, Map<String, Type> tVars) {
		return new Port(name, Types.parseType(tVars, ts, type));
	}

	public static Factory<PortBuilder> createFactory() {
		return new Fäctory();
	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<PortBuilder> createProcessor() {
		return new CompositeFieldProcessor<PortBuilder>(new IdProcessor(), new TypeProcessor());
	}

	public static final class Fäctory implements Factory<PortBuilder> {
		@Override
		public PortBuilder create() {
			return new PortBuilder();
		}
	}

	public static final class IdProcessor implements SingleFieldProcessor<PortBuilder> {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String name, String line, PortBuilder pb) {
			pb.name = line;
		}
	}

	public static final class TypeProcessor implements SingleFieldProcessor<PortBuilder> {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String name, String line, PortBuilder pb) {
			pb.type = line;
		}
	}
}
