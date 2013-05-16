package org.vanda.xml;

public class SimpleElementHandlerFactory<Builder1, Builder2> implements
		SingleElementHandlerFactory<Builder1> {

	private final String tag;
	private final ElementHandlerFactory<Builder2> ehf;
	private final Factory<Builder2> factory;
	private final ComplexFieldProcessor<Builder1, ? super Builder2> cfp;
	private final FieldProcessor<Builder2> fp;
	private final FieldProcessor<Builder2> tfp;

	public SimpleElementHandlerFactory(String tag,
			ElementHandlerFactory<Builder2> ehf, Factory<Builder2> factory,
			ComplexFieldProcessor<Builder1, ? super Builder2> cfp,
			FieldProcessor<Builder2> fp, FieldProcessor<Builder2> tfp) {
		this.tag = tag;
		this.ehf = ehf;
		this.factory = factory;
		this.cfp = cfp;
		this.fp = fp;
		this.tfp = tfp;
	}

	@Override
	public ElementHandler create(String name, Parser<?> p, Builder1 b) {
		return new SimpleElementHandler(p, b);
	}

	private class SimpleElementHandler implements ElementHandler {

		private final Parser<?> p;
		private final Builder1 b1;
		private Builder2 b2;

		public SimpleElementHandler(Parser<?> p, Builder1 b1) {
			this.p = p;
			this.b1 = b1;
		}

		@Override
		public void startElement(String namespace, String name) {
			b2 = factory.create();
		}

		@Override
		public void handleAttribute(String namespace, String name, String value) {
			if (fp != null)
				fp.process(name, value, b2);
		}

		@Override
		public ElementHandler handleChild(String namespace, String name) {
			ElementHandler result = null;
			if (ehf != null)
				result = ehf.create(name, p, b2);
			if (result != null)
				return result;
			else
				throw p.fail(null);
		}

		@Override
		public void endElement(String namespace, String name) {
			if (cfp != null)
				cfp.process(b1, b2);
		}

		@Override
		public void handleText(String text) {
			if (tfp != null)
				tfp.process(null, text, b2);
		}

	}

	@Override
	public String getTag() {
		return tag;
	}
}
