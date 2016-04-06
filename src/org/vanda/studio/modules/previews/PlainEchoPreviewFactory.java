package org.vanda.studio.modules.previews;

import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JLabel;
import org.vanda.studio.app.Application;
import org.vanda.studio.core.DefaultPreviewFactory;
import org.vanda.studio.modules.previews.Previews.Preview;

public class PlainEchoPreviewFactory extends DefaultPreviewFactory {

	public PlainEchoPreviewFactory(Application app) {
		super(app);
	}

	private class PreviewLabel extends JLabel implements Preview {
		private static final long serialVersionUID = -6398574080312547323L;
		private int update = 0;
		private FontSizeSelector fontSizeSelector;
		
		public PreviewLabel(String value){
			super(value);
		}
		
		@Override
		public void beginUpdate() {
			update++;
		}

		@Override
		public void endUpdate() {
			update--;
			if (update == 0) {
				updateSizes();
			}
		}

		@Override
		public void setLargeContent(boolean mode) {
			fontSizeSelector = mode ? new LargeFontSelector() : new NormalFontSelector();
		}

		@Override
		public void setLargeUI(boolean mode) {
		}
		
		private void updateSizes() {
			setFont(fontSizeSelector.setFontSize(getFont()));
		}
		
	}
	
	@Override
	public JComponent createPreview(String value) {
		PreviewLabel mp = new PreviewLabel("  Value: " + value);
		previews.add(new WeakReference<Preview>(mp));
		return mp; 
	}

	@Override
	public void openEditor(String value) {
		// No-op.
		// TODO Might want to consider throwing some sort of Exception here? 
	}

}
