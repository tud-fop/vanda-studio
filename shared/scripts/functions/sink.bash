source "$FUNCDIR/util.bash"

# Sink
# Version: n/a
# Contact: Matthias.Buechse@tu-dresden.de
# Category: basics
# IN inputPort :: t
#
# Sink
SinkTool () {
	pathAndName "$1" f1 i1
	echo "$i1"
}
