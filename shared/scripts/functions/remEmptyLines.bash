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
	$REM_EMPTY_LINES "$1" "$2" "$1"
	mv "${1}.nel" "$3"
	mv "${2}.nel" "$4"
	rm "${3}.nel"
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
	$REM_EMPTY_LINES "$1" "$2" "$3"
	mv "${1}.nel" "$4"
	mv "${2}.nel" "$5"
	mv "${3}.nel" "$6"
	echo "Done."
}
