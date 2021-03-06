# remEmptyLines
# Version: 2015-06-02
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN corpus1 :: t1
# IN corpus2 :: t2
# OUT corpus1 without empty lines :: t1
# OUT corpus2 without empty lines :: t2
#
# Removes empty lines from parallel corpora and alignments.
remEmptyLines2 () {
	ln -frsv "$2" "$1/in1"
	ln -frsv "$3" "$1/in2"
	$REM_EMPTY_LINES/remEmptyLines "$1/in1" "$1/in2"
	mv "$1/in1.nel" "$4"
	mv "$1/in2.nel" "$5"
	unlink "$1/in1"
	unlink "$1/in2"
}

# remEmptyLines
# Version: 2012-12-17
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN corpus1 :: t1
# IN corpus2 :: t2
# IN corpus3 :: t3
# OUT corpus1 without empty lines :: t1
# OUT corpus2 without empty lines :: t2
# OUT corpus3 without empty lines :: t3
#
# Removes empty lines from parallel corpora and alignments.
remEmptyLines3 () {
	ln -frsv "$2" "$1/in1"
	ln -frsv "$3" "$1/in2"
	ln -frsv "$4" "$1/in3"
	$REM_EMPTY_LINES/remEmptyLines "$1/in1" "$1/in2" "$1/in3"
	mv "$1/in1.nel" "$5"
	mv "$1/in2.nel" "$6"
	mv "$1/in3.nel" "$7"
	unlink "$1/in1"
	unlink "$1/in2"
	unlink "$1/in3"
}
