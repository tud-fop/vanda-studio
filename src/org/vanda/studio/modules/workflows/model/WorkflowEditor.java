package org.vanda.studio.modules.workflows.model;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.LayoutSelector;
import org.vanda.studio.app.WindowSystem;
import org.vanda.util.Action;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;


public interface WorkflowEditor {
	void addAction(Action a, KeyStroke keyStroke, int pos);
	void addAction(Action a, String imageName, KeyStroke keyStroke, int pos);
	void addSeparator(int pos);
	void addSideSplit(JComponent c, WindowSystem.Side side, int size);
	void removeSideSplit(JComponent c);
	void addToolBarPanel(JComponent c, int pos);
	Application getApplication();
	Database getDatabase();

	void setPalette(JComponent c);
	View getView();
	SyntaxAnalysis getSyntaxAnalysis();
	SemanticAnalysis getSemanticAnalysis();
	void enableAction(Action a);
	void disableAction(Action a);
}
