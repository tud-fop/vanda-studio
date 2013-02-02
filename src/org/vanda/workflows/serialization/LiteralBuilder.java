package org.vanda.workflows.serialization;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Literal;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class LiteralBuilder {

	public Type type;

	public String value;

	public Literal build() {
		return new Literal(type, value);
	}

	public static Factory<LiteralBuilder> createFactory() {
		return new Fäctory();
	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<LiteralBuilder> createProcessor() {
		return new CompositeFieldProcessor<LiteralBuilder>(new TypeProcessor(), new ValueProcessor());
	}

	public static final class Fäctory implements Factory<LiteralBuilder> {
		@Override
		public LiteralBuilder create() {
			return new LiteralBuilder();
		}
	}

	public static final class TypeProcessor implements SingleFieldProcessor<LiteralBuilder> {
		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.type = Types.parseType(null, null, value);
		}

		@Override
		public String getFieldName() {
			return "type";
		}
	}

	public static final class ValueProcessor implements SingleFieldProcessor<LiteralBuilder> {
		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.value = value;
		}

		@Override
		public String getFieldName() {
			return "value";
		}
	}
}
