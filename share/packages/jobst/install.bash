id="jobst"
varname="JOBST"
version="2013-05-27"
binpath="$id"

install_me () {
	make
	mv translate "$1/"
}
