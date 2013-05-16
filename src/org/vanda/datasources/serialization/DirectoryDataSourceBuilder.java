package org.vanda.datasources.serialization;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.DirectoryDataSource;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.xml.Factory;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class DirectoryDataSourceBuilder implements DataSourceBuilder {
	
	String dir;
	String filter;
	Type type;

	@Override
	public DataSource build() {
		return new DirectoryDataSource(type, dir, filter);
	}

	public static Factory<DirectoryDataSourceBuilder> createFactory() {
		return new Fäctory();
	}
	
	@SuppressWarnings("unchecked")
	public static FieldProcessor<DirectoryDataSourceBuilder> createProcessor() {
		return new CompositeFieldProcessor<DirectoryDataSourceBuilder>(new DirProcessor(), new FilterProcessor(), new TypeProcessor());
	}
	
	public static final class Fäctory implements Factory<DirectoryDataSourceBuilder> {
		@Override
		public DirectoryDataSourceBuilder create() {
			return new DirectoryDataSourceBuilder();
		}
	}

	public static final class DirProcessor implements SingleFieldProcessor<DirectoryDataSourceBuilder> {

		@Override
		public void process(String name, String value, DirectoryDataSourceBuilder b) {
			b.dir = value;
		}

		@Override
		public String getFieldName() {
			return "path";
		}

	}

	public static final class FilterProcessor implements SingleFieldProcessor<DirectoryDataSourceBuilder> {

		@Override
		public void process(String name, String value, DirectoryDataSourceBuilder b) {
			b.filter = value;
		}

		@Override
		public String getFieldName() {
			return "filter";
		}

	}

	public static final class TypeProcessor implements SingleFieldProcessor<DirectoryDataSourceBuilder> {

		@Override
		public void process(String name, String value, DirectoryDataSourceBuilder b) {
			b.type = Types.parseType(null, null, value);
		}

		@Override
		public String getFieldName() {
			return "type";
		}

	}

}
