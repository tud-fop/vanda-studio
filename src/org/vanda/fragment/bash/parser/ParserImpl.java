package org.vanda.fragment.bash.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

import org.vanda.fragment.bash.ShellTool;
import org.vanda.util.Observer;

public final class ParserImpl implements Parser {

	private final Builder b;
	private Scanner sc;
	private ParserState st;
	private final Observer<? super ShellTool> o;
	private final FieldProcessor[] fieldProcessors = { new FieldProcessors.Version(),
			new FieldProcessors.Contact(), new FieldProcessors.Category(),
			new FieldProcessors.Renderer(), new FieldProcessors.InPort(),
			new FieldProcessors.OutPort() };
	private final ParserState stHandleName = new ParserStates.HandleName(this);
	private final ParserState stHandleFields = new ParserStates.HandleFields(this);
	private final ParserState stHandleDescription = new ParserStates.HandleDescription(this);
	private final ParserState stHandleFunction = new ParserStates.HandleFunction(this);

	public ParserImpl(Observer<? super ShellTool> o) {
		b = new Builder();
		this.o = o;
	}

	public void init(File file) throws FileNotFoundException {
		b.imports = new HashSet<String>();
		b.imports.add(file.getAbsolutePath());
		st = stHandleName;
		sc = new Scanner(file);
	}

	public void process() {
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			st.lookAhead(line);
			if (st.handleLine(line)) {
				ShellTool t = b.build();
				o.notify(t);
				b.reset();
			}
		}
	}

	public void done() {
		if (sc != null) {
			sc.close();
			sc = null;
		}
	}

	@Override
	public void finalize() {
		done();
	}

	@Override
	public Builder getBuilder() {
		return b;
	}

	@Override
	public FieldProcessor[] getFieldProcessors() {
		return fieldProcessors;
	}

	@Override
	public void stateHandleName() {
		st = stHandleName;
	}

	@Override
	public void stateHandleFields() {
		st = stHandleFields;
	}

	@Override
	public void stateHandleDescription() {
		st = stHandleDescription;
	}

	@Override
	public void stateHandleFunction() {
		st = stHandleFunction;
	}

}