source "$FUNCDIR/util.bash"

# remEmptyLines
# Version: 2012-09-11
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN English Corpus :: Sentence Corpus
# IN French Corpus :: Sentence Corpus
# OUT English Corpus without empty lines :: Sentence Corpus
# OUT French Corpus without empty lines :: Sentence Corpus
#
# Removes empty lines from parallel corpora.
remEmptyLines () {
	echo "Running: remEmptyLines..."
	pathAndName "$1" f1 in1
	pathAndName "$2" f2 in2
	out1="$OUTPATH/remEmptyLines($in1,$in2).0"
	out2="$OUTPATH/remEmptyLines($in1,$in2).1"
	$REM_EMPTY_LINES "$f1" "$f2" "$out1" "$out2"
	eval $3=\"$out1\"
	eval $4=\"$out2\"
	echo "Done."
}
