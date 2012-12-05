package org.vanda.studio.app;

import javax.swing.JComponent;

public interface PreviewFactory {
	JComponent createPreview(String value);
	JComponent createSmallPreview(String absolutePath);
	
	void openEditor(String value);
	
}
