source "$FUNCDIR/util.bash"

# XRSTranslate
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: rule extraction
# IN Rules :: GHKM Hypergraph
# IN Corpus :: Sentence Corpus
# OUT Tree Corpus :: Penn Tree Corpus
#
# Generates a Tree Corpus given a GHKM Hypergraph and a Sentence Corpus
XRSTranslate () {
	echo "Running: XRSTranslate..."
	pathAndName "$1" f1 i1
	pathAndName "$2" f2 i2
	target="$OUTPATH/XRSTranslate($i1,$i2).0"
	TMP="$OUTPATH/XRS_TMP"
	mkdir -p "$TMP"
	cd "$TMP"
	echo -e "0\n" > "map.e"
	echo -e "0\n" > "map.f"
	cd "$VANDADIR"
	runhaskell "programs/XRSToHypergraph.hs" -e "$TMP/map.e" -f "$TMP/map.f" -g "$f1" -z "$TMP/zhg"
	runhaskell "programs/XRSTranslate.hs" -e "$TMP/map.e.new" -f "$TMP/map.f.new" -z "$TMP/zhg" < "$f2" > "$target"
	cd $OUTPATH
	# rm -rf "$TMP"
	eval $3=\"$target\"
	echo "Done."
}
