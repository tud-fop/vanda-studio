/**
 * 
 */
package org.vanda.studio.modules.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.Ports;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.CompositeType;
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

		private static class Plain2Snt extends Tool<Object> {
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

			public String getDate() {
				return "date";
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

			public Class<Object> getFragmentType() {
				return Object.class;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}
		}

		private static class GIZA extends Tool<Object> {
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

			public String getDate() {
				return "date";
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

			public Class<Object> getFragmentType() {
				return Object.class;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}
		}

		private static class Berkeley extends Tool<Object> {
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

			public String getDate() {
				return "date";
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

			public Class<Object> getFragmentType() {
				return Object.class;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}
		}

		private static class Tokenizer extends Tool<Object> {
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

			public String getDate() {
				return "date";
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

			public Class<Object> getFragmentType() {
				return Object.class;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}
		}

		private static class PennToInt extends Tool<Object> {
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

			public String getDate() {
				return "date";
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

			public Class<Object> getFragmentType() {
				return Object.class;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}
		}

		private static class GHKM extends Tool<Object> {
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

			public String getDate() {
				return "date";
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

			public Class<Object> getFragmentType() {
				return Object.class;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}
		}

		public WorkflowModuleInstance(Application a) {
			app = a;

			SimpleRepository<Object, Tool<Object>> tr = new SimpleRepository<Object, Tool<Object>>(
					null);

			tr.addItem(new Plain2Snt());
			tr.addItem(new GIZA());
			tr.addItem(new Berkeley());
			tr.addItem(new Tokenizer());
			tr.addItem(new PennToInt());
			tr.addItem(new GHKM());

			Tool<Object> sinkTool = new Tool<Object>() {
				public String getContact() {
					return "Anja.Fischer@mailbox.tu-dresden.de";
				}

				public String getCategory() {
					return "testCategory";
				}

				public String getDate() {
					return "date";
				}

				public String getDescription() {
					return "TestSink";
				}

				public String getId() {
					return "sinkId";
				}

				public List<Port> getInputPorts() {
					Port p = new Port("inputPort", Ports.typeVariable);
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

				public Class<Object> getFragmentType() {
					return Object.class;
				}

				public <R> R selectRenderer(RendererAssortment<R> ra) {
					return ra.selectSinkRenderer();
				}

				public void appendActions(List<Action> as) {
				}
			};

			tr.addItem(sinkTool);

			app.getToolMetaRepository().addRepository(tr);
		}
	}

}
