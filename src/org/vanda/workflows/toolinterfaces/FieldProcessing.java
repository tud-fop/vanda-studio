package org.vanda.workflows.toolinterfaces;

import org.vanda.types.Types;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public final class FieldProcessing {

	public static final FieldProcessor<RepositoryItemBuilder> rifp;
	public static final FieldProcessor<ToolBuilder> tfp;
	public static final FieldProcessor<PortBuilder> pfp;

	static {
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<RepositoryItemBuilder>[] rifps = new SingleFieldProcessor[] {
				new Name(), new Identifier(), new Version(), new Contact(),
				new Category() };
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<ToolBuilder>[] tfps = new SingleFieldProcessor[] {
				new FragmentType(), new Status() };
		@SuppressWarnings("unchecked")
		SingleFieldProcessor<PortBuilder>[] pfps = new SingleFieldProcessor[] {
				new PIdent(), new PType() };

		rifp = new CompositeFieldProcessor<RepositoryItemBuilder>(rifps);
		tfp = new CompositeFieldProcessor<ToolBuilder>(tfps);
		pfp = new CompositeFieldProcessor<PortBuilder>(pfps);
	}

	private static class Name implements
			SingleFieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.name = line;
		}
	}

	private static class Identifier implements
			SingleFieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "id";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.id = line;
		}
	}

	private static class Version implements
			SingleFieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "version";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.version = line;
		}
	}

	private static class Contact implements
			SingleFieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "contact";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.contact = line;
		}
	}

	private static class Category implements
			SingleFieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "category";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.category = line;
		}
	}

	private static class FragmentType implements
			SingleFieldProcessor<ToolBuilder> {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String name, String value, ToolBuilder b) {
			b.fragmentType = Types.parseType(null, null, value);
		}
	}

	private static class Status implements SingleFieldProcessor<ToolBuilder> {

		@Override
		public String getFieldName() {
			return "status";
		}

		@Override
		public void process(String name, String value, ToolBuilder b) {
			b.status = value;
		}
	}

	private static class PIdent implements SingleFieldProcessor<PortBuilder> {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String name, String line, PortBuilder pb) {
			pb.name = line;
		}
	}

	private static class PType implements SingleFieldProcessor<PortBuilder> {

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