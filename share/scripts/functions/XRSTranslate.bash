source "$FUNCDIR/util.bash"

# XRSTranslate
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: translation
# IN Rules :: GHKM Hypergraph
# IN Corpus :: Sentence Corpus
# OUT Tree Corpus :: Penn Tree Corpus
#
# Generates a Tree Corpus given a GHKM Hypergraph and a Sentence Corpus
XRSTranslate () {
	echo "Running: XRSTranslate..."
	TMP="$OUTPATH/XRS_TMP"
	mkdir -p "$TMP"
	cd "$TMP"
	echo -e "0\n" > "map.e"
	echo -e "0\n" > "map.f"
	cd "$VANDADIR"
	runhaskell "programs/XRSToHypergraph.hs" -e "$TMP/map.e" -f "$TMP/map.f" -g "$1" -z "$TMP/zhg"
	runhaskell "programs/XRSTranslate.hs" -e "$TMP/map.e.new" -f "$TMP/map.f.new" -z "$TMP/zhg" < "$2" > "$3"
	cd $OUTPATH
	rm -rf "$TMP"
	echo "Done."
}
