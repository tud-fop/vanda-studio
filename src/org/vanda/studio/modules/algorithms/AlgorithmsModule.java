/**
 * 
 */
package org.vanda.studio.modules.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.elements.IdentityLinker;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.common.LinkerUtil;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.util.Action;

/**
 * @author buechse
 * 
 */
public class AlgorithmsModule implements Module {

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Algorithms"; // Module for Vanda Studio";
	}

	protected static final class WorkflowModuleInstance {

		private final Application app;

		private static Type shellType = Types.shellType;

		private static class Plain2Snt extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("English", new CompositeType(
						"Sentence Corpus")));
				inputPorts.add(new Port("French", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("Parallel", new CompositeType(
						"GIZA.snt")));
				outputPorts.add(new Port("English.vcb", new CompositeType(
						"GIZA.vcb")));
				outputPorts.add(new Port("French.vcb", new CompositeType(
						"GIZA.vcb")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "alignments";
			}

			public String getDescription() {
				return "Converts two plain text files into GIZA++ files";
			}

			public String getId() {
				return "plain2snt";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "plain2snt";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return shellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-16";
			}
		}

		private static class GIZA extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Parallel", new CompositeType(
						"GIZA.snt")));
				inputPorts.add(new Port("English.vcb", new CompositeType(
						"GIZA.vcb")));
				inputPorts.add(new Port("French.vcb", new CompositeType(
						"GIZA.vcb")));
				outputPorts.add(new Port("Alignments", new CompositeType(
						"GIZA Alignments")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "alignments";
			}

			public String getDescription() {
				return "computes alignments and translation tables";
			}

			public String getId() {
				return "GIZA";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "GIZA";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return shellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-16";
			}
		}

		private static class Berkeley extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Corpus", new CompositeType(
						"Sentence Corpus")));
				inputPorts.add(new Port("Grammar", new CompositeType(
						"BerkeleyGrammar.sm6")));
				outputPorts.add(new Port("Tree Corpus", new CompositeType(
						"Penn Tree Corpus")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "parsing";
			}

			public String getDescription() {
				return "Berkeley parser using a state-split grammar";
			}

			public String getId() {
				return "BerkeleyParser";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "Berkeley Parser";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return shellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-16";
			}
		}

		private static class Tokenizer extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Corpus", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("Tokenized Corpus", new CompositeType(
						"Sentence Corpus")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "corpus tools";
			}

			public String getDescription() {
				return "Converts some special characters into tokens, such as ( into -LLB-";
			}

			public String getId() {
				return "BerkeleyTokenizer";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "Berkeley Tokenizer";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return shellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-16";
			}
		}

		private static class PennToInt extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Tree Corpus", new CompositeType(
						"Penn Tree Corpus")));
				outputPorts.add(new Port("Integerized Tree Corpus",
						new CompositeType("Penn Tree Corpus")));
				outputPorts.add(new Port("Conversion Table", new CompositeType(
						"TokenMap")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "corpus tools";
			}

			public String getDescription() {
				return "Converts words into integers, creates a conversion table";
			}

			public String getId() {
				return "PennToInt";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "PennToInt";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return shellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-16";
			}
		}

		private static class GHKM extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Alignment", new CompositeType(
						"GIZA Alignments")));
				inputPorts.add(new Port("Integerized Tree Corpus",
						new CompositeType("Penn Tree Corpus")));
				inputPorts.add(new Port("Conversion Table", new CompositeType(
						"TokenMap")));
				outputPorts.add(new Port("Rules", new CompositeType(
						"GHKM Hypergraph")));
				outputPorts.add(new Port("Conversion Table", new CompositeType(
						"TokenMap")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "rule extraction";
			}

			public String getDescription() {
				return "Extracts GHKM rules from a GIZA alignment and an integizered tree corpus";
			}

			public String getId() {
				return "HyperGHKM";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "GHKM";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return shellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-16";
			}
		}

		private static class HaskellLinker implements Linker {

			private final Application app;

			public HaskellLinker(Application app) {
				this.app = app;
			}

			@Override
			public void appendActions(List<Action> as) {

			}

			@Override
			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			@Override
			public String getId() {
				return "haskell-linker";
			}

			@Override
			public String getName() {
				return "Haskell Box";
			}

			@Override
			public String getVersion() {
				return "n/a";
			}

			@Override
			public boolean checkInputTypes(List<Type> outer, List<Type> inner) {
				return LinkerUtil.checkTypes(app
						.getConverterToolMetaRepository().getRepository(),
						outer, inner);
			}

			@Override
			public boolean checkOutputTypes(List<Type> outer, List<Type> inner) {
				return LinkerUtil.checkTypes(app
						.getConverterToolMetaRepository().getRepository(),
						inner, outer);
			}

			@Override
			public List<Port> convertInputPorts(List<Port> ips) {
				return ips;
			}

			@Override
			public List<Port> convertOutputPorts(List<Port> ops) {
				return ops;
			}

			@Override
			public String getCategory() {
				return "Boxes";
			}

			@Override
			public String getDescription() {
				return "Converts a Haskell workflow into a shell tool.";
			}

			@Override
			public Type getInnerFragmentType() {
				return Types.genericType;
			}

			@Override
			public Type getFragmentType() {
				return Types.genericType;
			}

			@Override
			public void visit(RepositoryItemVisitor v) {
				v.visitLinker(this);
			}

		}

		public WorkflowModuleInstance(Application a) {
			app = a;

			SimpleRepository<Tool> tr = new SimpleRepository<Tool>(null);

			tr.addItem(new Plain2Snt());
			tr.addItem(new GIZA());
			tr.addItem(new Berkeley());
			tr.addItem(new Tokenizer());
			tr.addItem(new PennToInt());
			tr.addItem(new GHKM());

			Tool sinkTool = new Tool() {
				public String getContact() {
					return "Anja.Fischer@mailbox.tu-dresden.de";
				}

				public String getCategory() {
					return "testCategory";
				}

				public String getDescription() {
					return "TestSink";
				}

				public String getId() {
					return "sinkId";
				}

				public List<Port> getInputPorts() {
					Port p = new Port("inputPort", Types.genericType);
					List<Port> list = new ArrayList<Port>();
					list.add(p);
					return list;
				}

				public String getName() {
					return "Sink";
				}

				public List<Port> getOutputPorts() {
					return new ArrayList<Port>();
				}

				public Type getFragmentType() {
					return shellType;
				}

				public <R> R selectRenderer(RendererAssortment<R> ra) {
					return ra.selectSinkRenderer();
				}

				public void appendActions(List<Action> as) {
				}

				@Override
				public String getVersion() {
					return "n/a";
				}
			};

			tr.addItem(sinkTool);
			app.getToolMetaRepository().addRepository(tr);

			SimpleRepository<Linker> lr = new SimpleRepository<Linker>(null);
			lr.addItem(new HaskellLinker(app));
			lr.addItem(IdentityLinker.getInstance());
			app.getLinkerMetaRepository().addRepository(lr);

		}
	}

}
