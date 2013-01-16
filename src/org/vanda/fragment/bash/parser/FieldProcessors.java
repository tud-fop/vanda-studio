package org.vanda.fragment.bash.parser;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.toolinterfaces.RendererSelector;
import org.vanda.workflows.toolinterfaces.RendererSelectors;

public class FieldProcessors {

	public static class Version implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "version:";
		}

		@Override
		public void process(String line, Builder b) {
			b.version = line.substring(getFieldName().length()).trim();
		}
	}

	public static class Contact implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "contact:";
		}

		@Override
		public void process(String line, Builder b) {
			b.contact = line.substring(getFieldName().length()).trim();
		}
	}

	public static class Category implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "category:";
		}

		@Override
		public void process(String line, Builder b) {
			b.category = line.substring(getFieldName().length()).trim();
		}
	}

	public static class Renderer implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "renderer:";
		}

		@Override
		public void process(String line, Builder b) {
			String renderer = line.substring(getFieldName().length())
					.trim();
			for (RendererSelector r : RendererSelectors.selectors)
				if (r.getIdentifier().equals(renderer)) {
					b.rs = r;
					break;
				}
		}
	}

	public static class InPort implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "in ";
		}

		@Override
		public void process(String line, Builder b) {
			String valtype = line.substring(getFieldName().length()).trim();
			String[] arr = valtype.split("::");
			Type t = Types.parseType(b.tVars, b.ts, arr[1].trim());
			b.inPorts.add(new Port(arr[0].trim(), t));
		}
	}

	public static class OutPort implements FieldProcessor {

		@Override
		public String getFieldName() {
			return "out ";
		}

		@Override
		public void process(String line, Builder b) {
			String valtype = line.substring(getFieldName().length()).trim();
			String[] arr = valtype.split("::");
			Type t = Types.parseType(b.tVars, b.ts, arr[1].trim());
			b.outPorts.add(new Port(arr[0].trim(), t));
		}
	}

}