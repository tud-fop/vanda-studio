source "$FUNCDIR/util.bash"

# remEmptyLines
# Version: 2012-12-17
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN English Corpus :: t1
# IN French Corpus :: t2
# OUT English Corpus without empty lines :: t1
# OUT French Corpus without empty lines :: t2
#
# Removes empty lines from parallel corpora and alignments.
remEmptyLines () {
	echo "Running: remEmptyLines..."
	pathAndName "$1" f1 in1
	pathAndName "$2" f2 in2
	out1="$OUTPATH/remEmptyLines($in1,$in2).0"
	out2="$OUTPATH/remEmptyLines($in1,$in2).1"
	$REM_EMPTY_LINES "$f1" "$f2" "$f1"
	mv "${f1}.nel" "$out1"
	mv "${f2}.nel" "$out2"
	rm "${f3}.nel"
	eval $3=\"$out1\"
	eval $4=\"$out2\"
	echo "Done."
}

# remEmptyLines
# Version: 2012-12-17
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN English Tree Corpus :: t1
# IN French Corpus :: t2
# IN Alignments :: t3
# OUT English Tree Corpus without empty lines :: t1
# OUT French Corpus without empty lines :: t2
# OUT Alignments without empty lines :: t3
#
# Removes empty lines from parallel corpora and alignments.
remEmptyLines3 () {
	echo "Running: remEmptyLines..."
	pathAndName "$1" f1 in1
	pathAndName "$2" f2 in2
	pathAndName "$3" f3 in3
	out1="$OUTPATH/remEmptyLines3($in1,$in2,$in3).0"
	out2="$OUTPATH/remEmptyLines3($in1,$in2,$in3).1"
	out3="$OUTPATH/remEmptyLines3($in1,$in2,$in3).2"
	$REM_EMPTY_LINES "$f1" "$f2" "$f3"
	mv "${f1}.nel" "$out1"
	mv "${f2}.nel" "$out2"
	mv "${f3}.nel" "$out3"
	eval $4=\"$out1\"
	eval $5=\"$out2\"
	eval $6=\"$out3\"
	echo "Done."
}
