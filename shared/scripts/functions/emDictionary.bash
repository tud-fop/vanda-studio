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
	pathAndName "$1" f1 in1
	pathAndName "$2" f2 in2
	out="$OUTPATH/toParallelCorpus($in1,$in2)"
	out1="$OUTPATH/EMDictionary($in1,$in2,$3).0"
	out2="$OUTPATH/EMDictionary($in1,$in2,$3).1"
	$TO_PARALLEL_CORPUS "$f1" "$f2" > "$out"
	$EMDICTIONARY csvAndBest "$3" "$out" "$out2" > "$out1"
	eval $4=\"$out1\"
	eval $5=\"$out2\"
	echo "Done."
}
