package org.vanda.workflows.serialization;

import org.vanda.types.Types;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public final class FieldProcessing {

	public static final FieldProcessor<WorkflowBuilder> wfp;
	public static final FieldProcessor<LiteralBuilder> lfp;
	public static final FieldProcessor<ToolBuilder> tfp;
	public static final FieldProcessor<BindingBuilder> bfp;
	public static final FieldProcessor<GeometryBuilder> gfp;

	static {
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<WorkflowBuilder>[] wfps = new SingleFieldProcessor[] {
				new WName() };
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<LiteralBuilder>[] lfps = new SingleFieldProcessor[] {
				new LType(), new LValue() };
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<ToolBuilder>[] tfps = new SingleFieldProcessor[] {
				new TId() };
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<BindingBuilder>[] bfps = new SingleFieldProcessor[] {
				new BPort(), new BVariable() };
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<GeometryBuilder>[] gfps = new SingleFieldProcessor[] {
				new GX(), new GY(), new GWidth(), new GHeight() };

		wfp = new CompositeFieldProcessor<WorkflowBuilder>(wfps);
		lfp = new CompositeFieldProcessor<LiteralBuilder>(lfps);
		tfp = new CompositeFieldProcessor<ToolBuilder>(tfps);
		bfp = new CompositeFieldProcessor<BindingBuilder>(bfps);
		gfp = new CompositeFieldProcessor<GeometryBuilder>(gfps);
	}

	private static final class WName implements
			SingleFieldProcessor<WorkflowBuilder> {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String name, String value, WorkflowBuilder b) {
			b.name = value;
		}
	}

	private static final class LType implements
			SingleFieldProcessor<LiteralBuilder> {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.type = Types.parseType(null, null, value);
		}
	}

	private static final class LValue implements
			SingleFieldProcessor<LiteralBuilder> {

		@Override
		public String getFieldName() {
			return "value";
		}

		@Override
		public void process(String name, String value, LiteralBuilder b) {
			b.value = value;
		}
	}

	private static final class TId implements
			SingleFieldProcessor<ToolBuilder> {

		@Override
		public String getFieldName() {
			return "id";
		}

		@Override
		public void process(String name, String value, ToolBuilder b) {
			b.id = value;
		}
	}

	private static final class BPort implements
			SingleFieldProcessor<BindingBuilder> {

		@Override
		public String getFieldName() {
			return "port";
		}

		@Override
		public void process(String name, String value, BindingBuilder b) {
			b.port = value;
		}
	}

	private static final class BVariable implements
			SingleFieldProcessor<BindingBuilder> {

		@Override
		public String getFieldName() {
			return "variable";
		}

		@Override
		public void process(String name, String value, BindingBuilder b) {
			b.variable = value;
		}
	}

	private static final class GX implements
			SingleFieldProcessor<GeometryBuilder> {

		@Override
		public String getFieldName() {
			return "x";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.x = Double.parseDouble(value);
		}
	}

	private static final class GY implements
			SingleFieldProcessor<GeometryBuilder> {

		@Override
		public String getFieldName() {
			return "y";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.y = Double.parseDouble(value);
		}
	}

	private static final class GWidth implements
			SingleFieldProcessor<GeometryBuilder> {

		@Override
		public String getFieldName() {
			return "width";
		}

		@Override
		public void process(String name, String value, GeometryBuilder b) {
			b.width = Double.parseDouble(value);
		}
	}

	private static final class GHeight implements
			SingleFieldProcessor<GeometryBuilder> {

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
