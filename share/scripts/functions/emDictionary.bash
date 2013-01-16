source "$FUNCDIR/util.bash"

# EMDictionary
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: training
# IN English Corpus :: Sentence Corpus
# IN French Corpus :: Sentence Corpus
# IN Threshold :: Double
# OUT Dictionary :: Dictionary
# OUT EM Steps :: EM Steps
#
# Trains a dictionary with a bilingual corpus.
EMDictionary () {
	echo "Running: EMDictionary..."
	mkdir -p "$OUTPATH/EM_TEMP"
	tmp="$OUTPATH/EM_TEMP/parallelCorpus"
	$TO_PARALLEL_CORPUS "$1" "$2" > "$tmp"
	$EMDICTIONARY csvAndBest "$3" "$tmp" "$5" > "$4"
	rm -r "$OUTPATH/EM_TEMP"
	echo "Done."
}
