package org.vanda.workflows.elements;

public interface ElementReturnVisitor<R> {

	public R visitLiteral(Literal l);
	
	public R visitTool(Tool t);

}
