package org.vanda.workflows.serialization.run;

import java.util.Map;

import org.vanda.studio.modules.workflows.run2.RunConfig;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.FieldProcessor;
import org.vanda.xml.SingleFieldProcessor;

public class RunConfigBuilder {
	public String path;
	public Map<String, Integer> priorities;

	public RunConfigBuilder() {
	}

	public RunConfig build() {
		return new RunConfig(path, priorities);
	}

	public static Factory<RunConfigBuilder> createFactory() {
		return new Fäctory();
	}

	public static class Fäctory implements Factory<RunConfigBuilder> {

		@Override
		public RunConfigBuilder create() {
			return new RunConfigBuilder();
		}

	}

	@SuppressWarnings("unchecked")
	public static FieldProcessor<RunConfigBuilder> createProcessor() {
		return new CompositeFieldProcessor<RunConfigBuilder>(new PathProcessor());
	}

	public static final class PathProcessor implements SingleFieldProcessor<RunConfigBuilder> {
		@Override
		public String getFieldName() {
			return "path";
		}

		@Override
		public void process(String name, String value, RunConfigBuilder b) {
			b.path = value;
		}
	}
}
