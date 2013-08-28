
package org.vanda.studio.core;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;

import org.vanda.studio.app.LayoutAssortment;
import org.vanda.studio.app.LayoutSelector;

@SuppressWarnings("serial")
class ToolFrame extends JInternalFrame implements LayoutSelector {
	private final LayoutSelector layout;

	public ToolFrame(JComponent c, LayoutSelector layout) {
		super(c.getName(), true);
		this.layout = layout;
		add(c);
		pack();
		setVisible(true);
		setIconifiable(true);
	}

	@Override
	public <L> L selectLayout(LayoutAssortment<L> la) {
		return layout.selectLayout(la);
	}

	@Override
	protected void fireInternalFrameEvent(int id) {
		switch (id) {
		case InternalFrameEvent.INTERNAL_FRAME_ICONIFIED:
			setNormalBounds((Rectangle) getBounds().clone());
			setBounds(getDesktopIcon().getBounds());
			setResizable(false);
			break;
		case InternalFrameEvent.INTERNAL_FRAME_DEICONIFIED:
			setBounds(getNormalBounds());
			setResizable(true);
			break;
		default:
			super.fireInternalFrameEvent(id);
		}

	}
}