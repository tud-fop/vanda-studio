package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;

public interface AbstractPreviewFactory {
	JComponent createPreview(Application app);
	JComponent createButtons(Application app);
}