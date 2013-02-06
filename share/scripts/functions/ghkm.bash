source "$FUNCDIR/util.bash"

# GHKM
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: rule extraction
# IN alignments :: Alignments
# IN tree corpus :: PennTreeCorpus
# IN sentence corpus :: SentenceCorpus
# OUT rules :: GHKMHypergraph
#
# Extracts GHKM rules from a GIZA alignment, a corpus and a tree corpus
GHKM () {
	java -Xmx1g -Xms1g -cp "$GHKM/ghkm.jar:$GHKM/fastutil.jar" edu.stanford.nlp.mt.syntax.ghkm.RuleExtractor -threads "$(nproc)" -fCorpus "$4" -eParsedCorpus "$3" -align "$2" -joshuaFormat false > "$5"
}
