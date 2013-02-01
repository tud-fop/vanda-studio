source "$FUNCDIR/util.bash"

# remEmptyLines
# Version: 2012-12-17
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN corpus #1 :: t1
# IN corpus #2 :: t2
# OUT corpus #1 without empty lines :: t1
# OUT corpus #2 without empty lines :: t2
#
# Removes empty lines from parallel corpora and alignments.
remEmptyLines2 () {
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
# IN corpus #1 :: t1
# IN corpus #2 :: t2
# IN corpus #3 :: t3
# OUT corpus #1 without empty lines :: t1
# OUT corpus #2 without empty lines :: t2
# OUT corpus #3 without empty lines :: t3
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
