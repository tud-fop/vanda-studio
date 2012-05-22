package org.vanda.studio.model.elements;

public interface RepositoryItemVisitor {

	void visitChoice(Choice c);
	
	void visitInputPort(InputPort i);
	
	void visitLinker(Linker l);
	
	void visitLiteral(Literal l);
	
	void visitOutputPort(OutputPort o);
	
	void visitTool(Tool t);
	
	
	
}
