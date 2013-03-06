id="remEmptyLines"
varname="REM_EMPTY_LINES"
version="2013-03-05"
binpath="$id"

install_me () {
	ghc --make -o "$1/remEmptyLines" "remEmptyLines/remEmptyLines.hs"
}