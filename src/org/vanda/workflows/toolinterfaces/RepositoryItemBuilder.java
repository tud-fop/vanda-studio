package org.vanda.workflows.toolinterfaces;

import org.vanda.xml.SingleFieldProcessor;

public class RepositoryItemBuilder {
	public String id;
	public String name;
	public StringBuilder description;
	public String version;
	public String category;
	public String contact;

	public RepositoryItemBuilder() {
		id = "";
		name = "";
		description = new StringBuilder();
		version = "";
		category = "";
		contact = "";
	}

	public static class NameProcessor implements SingleFieldProcessor<RepositoryItemBuilder> {
		@Override
		public String getFieldName() {
			return "name";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.name = line;
		}
	}

	public static class IdProcessor implements SingleFieldProcessor<RepositoryItemBuilder> {
		@Override
		public String getFieldName() {
			return "id";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.id = line;
		}
	}

	public static class VersionProcessor implements SingleFieldProcessor<RepositoryItemBuilder> {
		@Override
		public String getFieldName() {
			return "version";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.version = line;
		}
	}

	public static class ContactProcessor implements SingleFieldProcessor<RepositoryItemBuilder> {
		@Override
		public String getFieldName() {
			return "contact";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.contact = line;
		}
	}

	public static class CategoryProcessor implements SingleFieldProcessor<RepositoryItemBuilder> {
		@Override
		public String getFieldName() {
			return "category";
		}

		@Override
		public void process(String name, String line, RepositoryItemBuilder b) {
			b.category = line;
		}
	}

}
