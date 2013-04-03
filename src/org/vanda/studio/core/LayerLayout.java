package org.vanda.studio.core;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

public class LayerLayout implements LayoutManager2 {
	
	public static final Integer CENTER = 0;
	public static final Integer SOUTHWEST = 1;
	public static final Integer SOUTH = 2;
	public static final Integer SOUTHEAST = 3;
	
	private interface LayerOuter {
		Integer getLayer();
		void layout(Component comp, Rectangle bounds);
	}
	
	private static abstract class AbstractLayerOuter implements LayerOuter {
		protected Integer layer;
		
		private AbstractLayerOuter(Integer layer) {
			this.layer = layer;
		}
		
		@Override
		public Integer getLayer() {
			return layer;
		}
	}
	
	private static class Center extends AbstractLayerOuter {
		public Center() {
			super(CENTER);
		}

		@Override
		public void layout(Component comp, Rectangle bounds) {
			comp.setBounds(bounds);
		}
	}

	private static class SouthWest extends AbstractLayerOuter {
		public SouthWest() {
			super(SOUTHWEST);
		}

		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getPreferredSize();
			c.setBounds(bounds.x, bounds.y + bounds.height - d.height, d.width, d.height);
		}
	}

	private static class South extends AbstractLayerOuter {
		public South() {
			super(SOUTH);
		}

		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getPreferredSize();
			c.setBounds(bounds.x + (bounds.width - d.width) / 2, bounds.y + bounds.height - d.height, d.width, d.height);
		}
	}

	private static class SouthEast extends AbstractLayerOuter {
		public SouthEast() {
			super(SOUTHEAST);
		}

		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getPreferredSize();
			c.setBounds(bounds.x + bounds.width - d.width, bounds.y + bounds.height - d.height, d.width, d.height);
		}
	}
	
	private LayerOuter[] layerOuters = { new Center(), new SouthWest(), new South(), new SouthEast() };
	private HashMap<Component, LayerOuter> damap = new HashMap<Component, LayerOuter>();

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		for (LayerOuter lo : layerOuters)
			if (lo.getLayer().equals(constraints))
				damap.put(comp, lo);
	}

	@Override
	public void layoutContainer(Container parent) {
		Rectangle bounds = layoutBounds(parent);
		for (Map.Entry<Component, LayerOuter> e : damap.entrySet())
			e.getValue().layout(e.getKey(), bounds);
	}

	private Rectangle layoutBounds(Container parent) {
		// Why is this method not in class Container?
		Insets insets = parent.getInsets();
		int x      = insets.left;
		int y      = insets.right;
		int width  = parent.getWidth()  - x - insets.right;
		int height = parent.getHeight() - y - insets.bottom;
		return new Rectangle(x, y, width, height);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		damap.remove(comp);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(0, 0);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(parent.getWidth(), parent.getHeight());
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(Container target) {
		// no cached info.
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		addLayoutComponent(comp, name);
	}

}

