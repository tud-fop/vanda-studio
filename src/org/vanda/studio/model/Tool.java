/**
 * 
 */
package org.vanda.studio.model;

import java.util.List;


/**
 * Represents a single vanda object.
 * <p>
 * This class serves as common base for vanda objects of any kind.
 * 
 * @author buechse, rmueller
 * 
 */
public interface Tool {
	/**
	 * Append actions to a list. Do not forget to call super.
	 */
	void appendActions(List<Action> as);

	ToolInstance createInstance();

	String getAuthor();
	
	/**
	 * The category is used like a path in a file system.
	 * The separator is a period.
	 */
	String getCategory();
	
	String getDate();

	String getDescription();

	String getId();

	//public StringBuilder generateCode(String[] args);

	Port[] getInputPorts();

	String getName();

	Port[] getOutputPorts();
	
	void selectRenderer(RendererSelection rs);
}
