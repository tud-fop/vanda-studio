package org.vanda.datasources.serialization;

import org.vanda.datasources.DataSource;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class MountBuilder {
	
	public static DataSourceProcessor dsp = new DataSourceProcessor();

	String prefix;
	DataSource ds;
	
	public static Factory<MountBuilder> createFactory() {
		return new Fäctory();
	}
	
	// @SuppressWarnings("unchecked")
	public static FieldProcessor<MountBuilder> createProcessor() {
		// return new CompositeFieldProcessor<MountBuilder>(new PrefixProcessor());
		return new PrefixProcessor();
	}
	
	public static final class Fäctory implements Factory<MountBuilder> {
		@Override
		public MountBuilder create() {
			return new MountBuilder();
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
		}
	}

}
