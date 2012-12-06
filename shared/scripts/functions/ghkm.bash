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
HyperGHKM () {
	echo "Running: HyperGHKM..."
	pathAndName "$1" align nAlign
	pathAndName "$2" ecorpus nEcorpus
	pathAndName "$3" fcorpus nFcorpus
	target="$OUTPATH/HyperGHKM($nAlign,$nEcorpus,$nFcorpus).0"
	java -Xmx1g -Xms1g -cp "$GHKM/ghkm.jar:$GHKM/fastutil.jar" edu.stanford.nlp.mt.syntax.ghkm.RuleExtractor -fCorpus "$fcorpus" -eParsedCorpus "$ecorpus" -align "$align" -joshuaFormat false > "$target"
	eval $4=\"$target\"
}
