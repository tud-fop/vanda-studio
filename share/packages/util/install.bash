id="util"
varname="UTIL"
version="2013-06-27"
binpath="$id"

install_me () {
	mkdir -p "$VANDASTUDIO" "$BINDIR" "$FUNCDIR" "$IFACEDIR" "$OUTDIR" "$INDIR" "$PKGDB"
#	if [ ! -f $RCPATH ]; then
#		echo -e "source $FUNCDIR/util.bash\n" >> "$RCPATH"
#	fi
	cp interfaces/* "$IFACEDIR/."
	cp -r examples/* "$INDIR/."
}
