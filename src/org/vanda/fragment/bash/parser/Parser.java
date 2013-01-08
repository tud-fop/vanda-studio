package org.vanda.fragment.bash.parser;

/**
 * More or less acts as a mediator to all the ParserState instances.
 * 
 * @author mbue
 *
 */
public interface Parser {
	Builder getBuilder();

	FieldProcessor[] getFieldProcessors();

	void stateHandleName();

	void stateHandleFields();

	void stateHandleDescription();

	void stateHandleFunction();

}