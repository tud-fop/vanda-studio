id="util"
varname="UTIL"
version="2013-03-05"
binpath="$id"

install_me () {
	mkdir -p "$VANDASTUDIO" "$BINDIR" "$FUNCDIR" "$IFACEDIR" "$OUTDIR" "$INDIR" "$PKGDB"
	echo -e "#!/bin/bash\n"         > "$RCPATH"
	echo    "DATAPATH=$INDIR"      >> "$RCPATH"
	echo    "OUTPATH=$OUTDIR"      >> "$RCPATH"
	echo -e "FUNCDIR=$FUNCDIR\n"   >> "$RCPATH"
	cp interfaces/* "$IFACEDIR/."
	cp -r examples/* "$INDIR/."
}
