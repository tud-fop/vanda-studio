package org.vanda.workflows.serialization;

import java.util.Map;

import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Element;

public class JobBuilder {

	WorkflowBuilder parent;
	
	Element element;
	double x;
	double y;
	double width;
	double height;
	Map<String, Token> inPorts;
	Map<String, Token> outPorts;
	
	
}
