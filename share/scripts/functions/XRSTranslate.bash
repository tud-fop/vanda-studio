source "$FUNCDIR/util.bash"

# XRSTranslate
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: translation
# IN rules :: GHKMHypergraph
# IN sentence corpus :: SentenceCorpus
# OUT tree corpus :: PennTreeCorpus
#
# Generates a Tree Corpus given a GHKM Hypergraph and a Sentence Corpus
XRSTranslate () {
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	TMP="$1"
	echo -e "0\n" > "$TMP/map.e"
	echo -e "0\n" > "$TMP/map.f"
	runhaskell "$VANDADIR/programs/XRSToHypergraph.hs" -e "$TMP/map.e" -f "$TMP/map.f" -g "$2" -z "$TMP/zhg"
	runhaskell "$VANDADIR/programs/XRSTranslate.hs" -e "$TMP/map.e.new" -f "$TMP/map.f.new" -z "$TMP/zhg" < "$3" > "$4"
}
