package org.vanda.workflows.toolinterfaces;

import java.util.HashMap;
import java.util.Map;

import org.vanda.types.Types;

public class FieldProcessors {

	public interface RepositoryItemFieldProcessor {
		String getFieldName();

		void process(String value, RepositoryItemBuilder b);
	}

	public interface ToolFieldProcessor {
		String getFieldName();

		void process(String value, ToolBuilder b);
	}

	public interface PortFieldProcessor {
		String getFieldName();

		void process(String value, ToolBuilder tb, PortBuilder pb);
	}

	static private RepositoryItemFieldProcessor[] rifps = { new Name(),
			new Identifier(), new Version(), new Contact(), new Category() };
	static private Map<String, RepositoryItemFieldProcessor> rifpMap;

	static private ToolFieldProcessor[] tfps = { new FragmentType(),
			new Status() };
	static private Map<String, ToolFieldProcessor> tfpMap;

	static private PortFieldProcessor[] pfps = { new PIdent(), new PType() };
	static private Map<String, PortFieldProcessor> pfpMap;

	static {
		rifpMap = new HashMap<String, RepositoryItemFieldProcessor>();
		for (RepositoryItemFieldProcessor fp : rifps)
			rifpMap.put(fp.getFieldName(), fp);
		tfpMap = new HashMap<String, ToolFieldProcessor>();
		for (ToolFieldProcessor fp : tfps)
			tfpMap.put(fp.getFieldName(), fp);
		pfpMap = new HashMap<String, PortFieldProcessor>();
		for (PortFieldProcessor fp : pfps)
			pfpMap.put(fp.getFieldName(), fp);
	}

	public static void processRepositoryItemField(String name, String value,
			RepositoryItemBuilder b) {
		RepositoryItemFieldProcessor fp = rifpMap.get(name);
		if (fp != null)
			fp.process(value, b);
	}

	public static void processToolField(String name, String value,
			ToolBuilder tb) {
		ToolFieldProcessor fp = tfpMap.get(name);
		if (fp != null)
			fp.process(value, tb);
	}

	public static void processPortField(String name, String value,
			ToolBuilder tb, PortBuilder pb) {
		PortFieldProcessor fp = pfpMap.get(name);
		if (fp != null)
			fp.process(value, tb, pb);
	}

	public static class Name implements RepositoryItemFieldProcessor {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.name = line;
		}
	}

	public static class Identifier implements RepositoryItemFieldProcessor {

		@Override
		public String getFieldName() {
			return "id";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.id = line;
		}
	}

	public static class Version implements RepositoryItemFieldProcessor {

		@Override
		public String getFieldName() {
			return "version";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.version = line;
		}
	}

	public static class Contact implements RepositoryItemFieldProcessor {

		@Override
		public String getFieldName() {
			return "contact";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.contact = line;
		}
	}

	public static class Category implements RepositoryItemFieldProcessor {

		@Override
		public String getFieldName() {
			return "category";
		}

		@Override
		public void process(String line, RepositoryItemBuilder b) {
			b.category = line;
		}
	}

	public static class FragmentType implements ToolFieldProcessor {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String value, ToolBuilder b) {
			b.fragmentType = Types.parseType(null, null, value);
		}
	}

	public static class Status implements ToolFieldProcessor {

		@Override
		public String getFieldName() {
			return "status";
		}

		@Override
		public void process(String value, ToolBuilder b) {
			b.status = value;
		}
	}

	public static class PIdent implements PortFieldProcessor {

		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String line, ToolBuilder tb, PortBuilder pb) {
			pb.name = line;
		}
	}

	public static class PType implements PortFieldProcessor {

		@Override
		public String getFieldName() {
			return "type";
		}

		@Override
		public void process(String line, ToolBuilder tb, PortBuilder pb) {
			pb.type = Types.parseType(tb.tVars, tb.ts, line);
		}
	}

}