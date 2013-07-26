package org.vanda.workflows.hyper;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;


public interface JobVisitor {

	public void visitLiteral(Job j, Literal l);
	
	public void visitTool(Job j, Tool t);

}
