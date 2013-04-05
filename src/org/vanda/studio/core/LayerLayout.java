package org.vanda.studio.core;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

import org.vanda.studio.app.LayoutAssortment;
import org.vanda.studio.app.LayoutSelector;

public class LayerLayout implements LayoutManager2 {
	
	private interface LayerOuter {
		void layout(Component comp, Rectangle bounds);
	}
	
	private static class Center implements LayerOuter {
		@Override
		public void layout(Component comp, Rectangle bounds) {
			comp.setBounds(bounds);
		}
	}
	
	private static class North implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x + (bounds.width - d.width) / 2, bounds.y, d.width, d.height);
		}
	}
	
	private static class NorthWest implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x, bounds.y, d.width, d.height);
		}
	}
	
	private static class West implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x, bounds.y + (bounds.height - d.height) / 2, d.width, d.height);
		}
	}

	private static class SouthWest implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x, bounds.y + bounds.height - d.height, d.width, d.height);
		}
	}

	private static class South implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x + (bounds.width - d.width) / 2, bounds.y + bounds.height - d.height, d.width, d.height);
		}
	}

	private static class SouthEast implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x + bounds.width - d.width, bounds.y + bounds.height - d.height, d.width, d.height);
		}
	}
	
	private static class East implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x + bounds.width - d.width, bounds.y + (bounds.height - d.height) / 2, d.width, d.height);
		}
	}
	
	private static class NorthEast implements LayerOuter {
		@Override
		public void layout(Component c, Rectangle bounds) {
			Dimension d = c.getSize();
			c.setBounds(bounds.x + bounds.width - d.width, bounds.y, d.width, d.height);
		}
	}
	
	private static class Assortment implements LayoutAssortment<LayerOuter> {

		private LayerOuter center = new Center();
		private LayerOuter north = new North();
		private LayerOuter northWest = new NorthWest();
		private LayerOuter west = new West();
		private LayerOuter southWest = new SouthWest();
		private LayerOuter south = new South();
		private LayerOuter southEast = new SouthEast();
		private LayerOuter east = new East();
		private LayerOuter northEast = new NorthEast();

		@Override
		public LayerOuter getCenter() {
			return center;
		}

		@Override
		public LayerOuter getNorth() {
			return north;
		}

		@Override
		public LayerOuter getNorthWest() {
			return northWest;
		}

		@Override
		public LayerOuter getWest() {
			return west;
		}

		@Override
		public LayerOuter getSouthWest() {
			return southWest;
		}

		@Override
		public LayerOuter getSouth() {
			return south;
		}

		@Override
		public LayerOuter getSouthEast() {
			return southEast;
		}

		@Override
		public LayerOuter getEast() {
			return east;
		}

		@Override
		public LayerOuter getNorthEast() {
			return northEast;
		}
		
	}
	
	private static Assortment assortment = new Assortment();

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
	}

	@Override
	public void layoutContainer(Container parent) {
		Rectangle bounds = layoutBounds(parent);
		for (Component c : parent.getComponents()) {
			if (c instanceof LayoutSelector) {
				LayerOuter lo = ((LayoutSelector) c).selectLayout(assortment);
				lo.layout(c, bounds);
			}
		}
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

