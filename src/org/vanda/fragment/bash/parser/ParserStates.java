package org.vanda.fragment.bash.parser;

/**
 * Four states for parsing tool metadata from a bash script:
 * First parse the name, then the other fields, then the description. Finally
 * parse the id (the name of the function).
 * 
 * @author mbue
 *
 */
public class ParserStates {

	public static class HandleName implements ParserState {
		private final Parser p;

		public HandleName(Parser p) {
			this.p = p;
		}

		@Override
		public boolean handleLine(String line) {
			if (line.startsWith("#")) {
				p.getBuilder().name = line.substring(1).trim();
				p.stateHandleFields();
			}
			return false;
		}

		@Override
		public void lookAhead(String line) {
		}
	}

	public static class HandleFields implements ParserState {
		private final Parser p;

		public HandleFields(Parser p) {
			this.p = p;
		}

		@Override
		public boolean handleLine(String line) {
			line = line.substring(1).trim();
			if ("".equals(line))
				p.stateHandleDescription();
			else
				for (FieldProcessor fp : p.getFieldProcessors()) {
					if (line.toLowerCase().startsWith(fp.getFieldName())) {
						fp.process(line, p.getBuilder());
						break; // only one field processor should match
					}
				}
			return false;
		}

		@Override
		public void lookAhead(String line) {
			if (!line.startsWith("#"))
				p.stateHandleFunction();
		}
	}

	public static class HandleDescription implements ParserState {
		private final Parser p;

		public HandleDescription(Parser p) {
			this.p = p;
		}

		@Override
		public boolean handleLine(String line) {
			p.getBuilder().description.append(line.substring(1).trim());
			return false;
		}

		@Override
		public void lookAhead(String line) {
			if (!line.startsWith("#"))
				p.stateHandleFunction();
		}
	}

	public static class HandleFunction implements ParserState {
		private final Parser p;

		public HandleFunction(Parser p) {
			this.p = p;
		}

		@Override
		public boolean handleLine(String line) {
			if (line.matches(".*\\(\\).*\\{")) {
				p.getBuilder().id = line.trim().split(" ")[0];
				return true;
			} else
				return false;
		}

		@Override
		public void lookAhead(String line) {
			if (line.startsWith("#"))
				p.stateHandleName();
		}
	}

}