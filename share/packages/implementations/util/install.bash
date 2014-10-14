id="util"
varname="UTIL"
version="2013-06-27"
binpath="$id"

install_me () {
	mkdir -p "$VANDASTUDIO" "$BINDIR" "$FUNCDIR" "$IFACEDIR" "$OUTDIR" "$INDIR" "$PKGDB"
	cp -r examples/* "$INDIR/."
}
