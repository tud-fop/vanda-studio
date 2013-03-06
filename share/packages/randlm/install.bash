id="randlm"
varname="RANDLM"
version="2013-03-05"
binpath="$id"

install_me () {
	svn co https://randlm.svn.sourceforge.net/svnroot/randlm randlm
	cd randlm/trunk
	./autogen.sh
	mkdir -p "$RANDLMDIR"
	./configure --prefix="$RANDLMDIR"
	make
	make install
	cd ../..
	echo "RANDLM=$RANDLMDIR/bin/" >> ~/.vanda/vandarc
}
