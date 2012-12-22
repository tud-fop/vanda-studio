package org.vanda.studio.modules.profile;

import org.vanda.studio.model.elements.RendererAssortment;

public class RendererSelectors {

	private static class SelectAlgorithmRenderer implements RendererSelector {

		@Override
		public String getIdentifier() {
			return "algorithm";
		}

		@Override
		public <R> R selectRenderer(RendererAssortment<R> ra) {
			return ra.selectAlgorithmRenderer();
		}

	}

	private static class SelectOrRenderer implements RendererSelector {

		@Override
		public String getIdentifier() {
			return "choice";
		}

		@Override
		public <R> R selectRenderer(RendererAssortment<R> ra) {
			return ra.selectOrRenderer();
		}

	}

	public static RendererSelector selectAlgorithmRenderer = new SelectAlgorithmRenderer();
	public static RendererSelector selectOrRenderer = new SelectOrRenderer();

	public static RendererSelector[] selectors = { selectAlgorithmRenderer,
			selectOrRenderer };

}
