id="randlm"
varname="RANDLM"
version="2013-03-05"
binpath="$id"

install_me () {
	svn checkout svn://svn.code.sf.net/p/randlm/code/trunk randlm-code
	cd randlm-code
	./autogen.sh
	./configure --prefix="${1}"
	make
	make install
	echo "RANDLM=${1}/bin/" >> ~/.vanda/vandarc
}
