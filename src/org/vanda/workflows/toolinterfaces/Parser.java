package org.vanda.workflows.toolinterfaces;

import org.xmlpull.v1.XmlPullParser;

public interface Parser {
	XmlPullParser getXmlParser();
	ToolBuilder getToolBuilder();
	ToolInterfaceBuilder getToolInterfaceBuilder();
	void buildTool();
	void buildToolInterface();
}
