package org.vanda.studio.app;

import javax.swing.JComponent;

public interface PreviewFactory {
	JComponent createPreview(String value);
	
	void openEditor(String value);
}
