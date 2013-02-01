package org.vanda.workflows.elements;


public interface ElementVisitor {

	public void visitLiteral(Literal l);
	
	public void visitTool(Tool t);

}
