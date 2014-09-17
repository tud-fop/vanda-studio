id="randlm"
varname="RANDLM"
version="2013-03-05"
binpath="$id"

install_me () {
	svn checkout svn://svn.code.sf.net/p/randlm/code/trunk randlm-code
	cd randlm-code
	./autogen.sh
	mkdir -p "$RANDLMDIR"
	./configure --prefix="$RANDLMDIR"
	make
	make install
	cd ../..
	echo "RANDLM=$RANDLMDIR/bin/" >> ~/.vanda/vandarc
}
