package org.vanda.datasources.serialization;

import org.vanda.datasources.DataSource;
import org.vanda.studio.app.Application;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class MountBuilder {
	
	public static DataSourceProcessor dsp = new DataSourceProcessor();

	private Application app;
	String prefix;
	DataSource ds;
	
	public MountBuilder(Application app) {
		this.app = app;
	}

	public static Factory<MountBuilder> createFactory(Application app) {
		return new Fäctory(app);
	}
	
	// @SuppressWarnings("unchecked")
	public static FieldProcessor<MountBuilder> createProcessor() {
		// return new CompositeFieldProcessor<MountBuilder>(new PrefixProcessor());
		return new PrefixProcessor();
	}
	
	public static final class Fäctory implements Factory<MountBuilder> {
		private Application app;
		
		private Fäctory(Application app) {
			this.app = app;
		}
		
		@Override
		public MountBuilder create() {
			return new MountBuilder(app);
		}
	}

	public static final class PrefixProcessor implements SingleFieldProcessor<MountBuilder> {

		@Override
		public void process(String name, String value, MountBuilder b) {
			b.prefix = value;
		}

		@Override
		public String getFieldName() {
			return "prefix";
		}

	}

	public static final class DataSourceProcessor implements ComplexFieldProcessor<MountBuilder, DataSourceBuilder> {
		@Override
		public void process(MountBuilder b1, DataSourceBuilder b2) {
			b1.ds = b2.build();
			b1.registerTypeWithApp();
		}
	}
	
	public void registerTypeWithApp() {
		ds.getType(null).getSubTypes(app.getTypes());
	}
}
