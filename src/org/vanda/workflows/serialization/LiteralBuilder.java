package org.vanda.workflows.serialization;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Literal;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class LiteralBuilder {

	public Integer key;

	public Type type;

	public String name;

	public Literal build() {
		return new Literal(type, name, key);
	}

	public static Factory<LiteralBuilder> createFactory() {
		return new Fäctory();
	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<LiteralBuilder> createProcessor() {
		return new CompositeFieldProcessor<LiteralBuilder>(new TypeProcessor(), new ValueProcessor(),
				new NameProcessor(), new KeyProcessor());
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

	public static final class KeyProcessor implements SingleFieldProcessor<LiteralBuilder> {
		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.key = Integer.parseInt(value, 16);
		}

		@Override
		public String getFieldName() {
			return "key";
		}
	}

	public static final class NameProcessor implements SingleFieldProcessor<LiteralBuilder> {
		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.name = value;
		}

		@Override
		public String getFieldName() {
			return "name";
		}
	}

	public static final class ValueProcessor implements SingleFieldProcessor<LiteralBuilder> {
		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.key = null;
			b.name = value;
		}

		@Override
		public String getFieldName() {
			return "value";
		}
	}
}
