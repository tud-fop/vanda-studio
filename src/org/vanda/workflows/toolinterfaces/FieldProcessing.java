package org.vanda.workflows.toolinterfaces;

import java.util.HashMap;
import java.util.Map;

import org.vanda.types.Types;

public final class FieldProcessing {

	private interface FieldProcessor<Builder> {
		String getFieldName();

		void process(String value, Builder b);
	}

	private static Map<String, FieldProcessor<RepositoryItemBuilder>> rifpMap;
	private static Map<String, FieldProcessor<ToolBuilder>> tfpMap;
	private static Map<String, FieldProcessor<PortBuilder>> pfpMap;

	static {
		@SuppressWarnings("unchecked")
		FieldProcessor<RepositoryItemBuilder>[] rifps = new FieldProcessor[] {
				new Name(), new Identifier(), new Version(), new Contact(),
				new Category() };
		@SuppressWarnings("unchecked")
		FieldProcessor<ToolBuilder>[] tfps = new FieldProcessor[] {
				new FragmentType(), new Status() };
		@SuppressWarnings("unchecked")
		FieldProcessor<PortBuilder>[] pfps = new FieldProcessor[] {
				new PIdent(), new PType() };

		rifpMap = mkMap(rifps);
		tfpMap = mkMap(tfps);
		pfpMap = mkMap(pfps);
	}

	public static void processRepositoryItemField(String name, String value,
			RepositoryItemBuilder b) {
		FieldProcessor<RepositoryItemBuilder> fp = rifpMap.get(name);
		if (fp != null)
			fp.process(value, b);
	}

	public static void processToolField(String name, String value,
			ToolBuilder tb) {
		FieldProcessor<ToolBuilder> fp = tfpMap.get(name);
		if (fp != null)
			fp.process(value, tb);
	}

	public static void processPortField(String name, String value,
			PortBuilder pb) {
		FieldProcessor<PortBuilder> fp = pfpMap.get(name);
		if (fp != null)
			fp.process(value, pb);
	}

	private static <Builder> Map<String, FieldProcessor<Builder>> mkMap(
			FieldProcessor<Builder>[] fps) {
		Map<String, FieldProcessor<Builder>> result = new HashMap<String, FieldProcessor<Builder>>();
		for (FieldProcessor<Builder> fp : fps)
			result.put(fp.getFieldName(), fp);
		return result;
	}

	private static class Name implements FieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.name = line;
		}
	}

	private static class Identifier implements
			FieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "id";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.id = line;
		}
	}

	private static class Version implements
			FieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "version";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.version = line;
		}
	}

	private static class Contact implements
			FieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "contact";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.contact = line;
		}
	}

	private static class Category implements
			FieldProcessor<RepositoryItemBuilder> {

		@Override
		public String getFieldName() {
			return "category";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.category = line;
		}
	}

	private static class FragmentType implements FieldProcessor<ToolBuilder> {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String value, ToolBuilder b) {
			b.fragmentType = Types.parseType(null, null, value);
		}
	}

	private static class Status implements FieldProcessor<ToolBuilder> {

		@Override
		public String getFieldName() {
			return "status";
		}

		@Override
		public void process(String value, ToolBuilder b) {
			b.status = value;
		}
	}

	private static class PIdent implements FieldProcessor<PortBuilder> {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String line, PortBuilder pb) {
			pb.name = line;
		}
	}

	private static class PType implements FieldProcessor<PortBuilder> {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String line, PortBuilder pb) {
			ToolBuilder tb = pb.parent;
			pb.type = Types.parseType(tb.tVars, tb.ts, line);
		}
	}
}