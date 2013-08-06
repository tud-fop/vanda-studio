package org.vanda.workflows.serialization.run;

import java.util.Map;

import org.vanda.studio.modules.workflows.run2.RunConfig;
import org.vanda.xml.Factory;


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
}
