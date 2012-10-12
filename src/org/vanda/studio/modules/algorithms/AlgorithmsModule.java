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
import org.vanda.studio.model.types.TypeVariable;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.common.LinkerUtil;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.TokenSource;

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
		private static Type haskellType = Types.haskellType;

		private static class Plain2Snt extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("English", new CompositeType(
						"Sentence Corpus")));
				inputPorts.add(new Port("French", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("English_French", new CompositeType(
						"GIZA.snt")));
				outputPorts.add(new Port("French_English", new CompositeType(
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
				inputPorts.add(new Port("English corpus", new CompositeType(
						"Sentence Corpus")));
				inputPorts.add(new Port("French corpus", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("Alignments", new CompositeType(
						"Alignments")));
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
				return "2012-09-12";
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
		
		private static class RemEmptyLines extends Tool {
			
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Corpus", new CompositeType(
						"Sentence Corpus")));
				inputPorts.add(new Port("Corpus", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("Corpus without empty lines", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("Corpus without empty lines", new CompositeType(
						"Sentence Corpus")));
			}
			
			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "corpus tools";
			}

			public String getDescription() {
				return "Removes empty lines from corpora.";
			}

			public String getId() {
				return "remEmptyLines";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "remEmptyLines";
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
				return "2012-09-11";
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
						"Alignments")));
				inputPorts.add(new Port("Tree Corpus",
						new CompositeType("Penn Tree Corpus")));
				inputPorts.add(new Port("Corpus", new CompositeType(
						"Sentence Corpus")));
				outputPorts.add(new Port("Rules", new CompositeType(
						"GHKM Hypergraph")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "rule extraction";
			}

			public String getDescription() {
				return "Extracts GHKM rules from a GIZA alignment, a corpus and a tree corpus";
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
		
		private static class EMDictionary extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("Corpus", new CompositeType(
						"Parallel Sentence Corpus")));
				inputPorts.add(new Port("Threshold",
						new CompositeType("Double")));
				outputPorts.add(new Port("Dictionary", new CompositeType(
						"Dictionary")));
				outputPorts.add(new Port("EM Steps", new CompositeType(
						"EM Steps")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "training";
			}

			public String getDescription() {
				return "Trains a dictionary with a bilingual corpus";
			}

			public String getId() {
				return "EMDictionary";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "EMDictionary";
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
		
		private static class EMDictionaryShowSteps extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("EM Steps", new CompositeType(
						"EM Steps")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "training";
			}

			public String getDescription() {
				return "Shows computation steps of EMDictionary training";
			}

			public String getId() {
				return "EMDictionaryShowSteps";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "EMDictionaryShowSteps";
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

		static class SinkTool extends Tool {
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
				return "cat";
			}

			public List<Port> getInputPorts() {
				Port p = new Port("inputPort", new CompositeType("Text File"));
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
		}

		static class LoadSCFG extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("#1", new CompositeType("SCFG File")));
				outputPorts.add(new Port("#2", new CompositeType("SCFG")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "loadSCFG";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "loadSCFG";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		private static class LoadWeights extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(new CompositeType("Double"));

				inputPorts
						.add(new Port("#1", new CompositeType("WeightsFile")));
				outputPorts.add(new Port("#2", new CompositeType("Vector",
						typeList)));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "loadWeights";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "loadWeights";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		static class LoadText extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("#1", new CompositeType("Text File")));
				outputPorts.add(new Port("#2", new CompositeType("String")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "loadText";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "loadText";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		static class MakeFeature extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(new CompositeType("Double"));

				inputPorts.add(new Port("weights", new CompositeType("Vector",
						typeList)));
				outputPorts.add(new Port("feature",
						new CompositeType("Feature")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "makeFeature";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "makeFeature";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		private static class ToWSA extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("string", new CompositeType("String")));
				outputPorts.add(new Port("wsa", new CompositeType("WSA")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "toWSA";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "toWSA";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		private static class InputProduct extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("WSA", new CompositeType("WSA")));
				inputPorts.add(new Port("SCFG", new CompositeType("SCFG")));
				inputPorts
						.add(new Port("feature", new CompositeType("Feature")));
				outputPorts.add(new Port("SCFG", new CompositeType("SCFG")));
				outputPorts.add(new Port("feature",
						new CompositeType("Feature")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "inputProduct";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "Input Product";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		static class BestDeriv extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(new CompositeType("Double"));

				inputPorts.add(new Port("SCFG", new CompositeType("SCFG")));
				inputPorts
						.add(new Port("Feature", new CompositeType("Feature")));

				List<Type> typeList2 = new ArrayList<Type>();
				typeList2.add(new CompositeType("Deriv"));

				outputPorts.add(new Port("Derivation List", new CompositeType(
						"[]", typeList2)));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "bestDeriv";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "Best Deriv";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		static class GetInputString extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(new CompositeType("Deriv"));

				inputPorts.add(new Port("Derivation List", new CompositeType(
						"[]", typeList)));
				outputPorts
						.add(new Port("string", new CompositeType("String")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "getInputString";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "getInputString";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		static class GetOutputString extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				List<Type> typeList = new ArrayList<Type>();
				typeList.add(new CompositeType("Deriv"));

				inputPorts.add(new Port("Derivation List", new CompositeType(
						"[]", typeList)));
				outputPorts
						.add(new Port("string", new CompositeType("String")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "getOutputString";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "getOutputString";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
			}
		}

		static class SaveText extends Tool {
			static List<Port> inputPorts = new ArrayList<Port>();
			static List<Port> outputPorts = new ArrayList<Port>();
			static {
				inputPorts.add(new Port("#1", new CompositeType("String")));
				outputPorts.add(new Port("#2", new CompositeType("Text File")));
			}

			public String getContact() {
				return "Matthias.Buechse@tu-dresden.de";
			}

			public String getCategory() {
				return "unassigned";
			}

			public String getDescription() {
				return "";
			}

			public String getId() {
				return "saveText";
			}

			public List<Port> getInputPorts() {
				return inputPorts;
			}

			public String getName() {
				return "saveText";
			}

			public List<Port> getOutputPorts() {
				return outputPorts;
			}

			public Type getFragmentType() {
				return haskellType;
			}

			public <R> R selectRenderer(RendererAssortment<R> ra) {
				return ra.selectAlgorithmRenderer();
			}

			public void appendActions(List<Action> as) {
			}

			@Override
			public String getVersion() {
				return "2012-05-24";
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
				ArrayList<Port> result = new ArrayList<Port>(ips.size());
				for (int i = 0; i < ips.size(); i++) {
					Port p = ips.get(i);
					if (p == null)
						result.add(null);
					else
						result.add(new Port(p.getIdentifier(),
								new TypeVariable(TokenSource.getToken(2 * i))));
				}
				return result;
			}

			@Override
			public List<Port> convertOutputPorts(List<Port> ops) {
				ArrayList<Port> result = new ArrayList<Port>(ops.size());
				for (int i = 0; i < ops.size(); i++) {
					Port p = ops.get(i);
					if (p == null)
						result.add(null);
					else
						result.add(new Port(p.getIdentifier(),
								new TypeVariable(TokenSource
										.getToken(2 * i + 1))));
				}
				return result;
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
				return Types.haskellType;
			}

			@Override
			public Type getFragmentType() {
				return Types.shellType;
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
//			tr.addItem(new GIZA());
			tr.addItem(new GIZA());
			tr.addItem(new Berkeley());
			tr.addItem(new RemEmptyLines());
			tr.addItem(new Tokenizer());
			tr.addItem(new PennToInt());
			tr.addItem(new GHKM());
			tr.addItem(new EMDictionary());
			tr.addItem(new EMDictionaryShowSteps());
			tr.addItem(new ToWSA());
			tr.addItem(new InputProduct());
			tr.addItem(new MakeFeature());
			tr.addItem(new BestDeriv());
			tr.addItem(new GetInputString());
			tr.addItem(new GetOutputString());
			tr.addItem(new SinkTool());
			app.getToolMetaRepository().addRepository(tr);

			SimpleRepository<Tool> ctr = new SimpleRepository<Tool>(null);
			ctr.addItem(new LoadText());
			ctr.addItem(new SaveText());
			ctr.addItem(new LoadWeights());
			ctr.addItem(new LoadSCFG());
			app.getConverterToolMetaRepository().addRepository(ctr);

			SimpleRepository<Linker> lr = new SimpleRepository<Linker>(null);
			lr.addItem(new HaskellLinker(app));
			lr.addItem(IdentityLinker.getInstance());
			app.getLinkerMetaRepository().addRepository(lr);

		}
	}

}
