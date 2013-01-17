source "$FUNCDIR/util.bash"

# GHKM
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: rule extraction
# IN Alignments :: Alignments
# IN Tree Corpus :: Penn Tree Corpus
# IN Corpus :: Sentence Corpus
# OUT Rules :: GHKM Hypergraph
#
# Extracts GHKM rules from a GIZA alignment, a corpus and a tree corpus
GHKM () {
	echo "Running: GHKM..."
	java -Xmx1g -Xms1g -cp "$GHKM/ghkm.jar:$GHKM/fastutil.jar" edu.stanford.nlp.mt.syntax.ghkm.RuleExtractor -fCorpus "$3" -eParsedCorpus "$2" -align "$1" -joshuaFormat false > "$4"
}