id="irstlm"
varname="IRSTLM"
version="2013-03-05"
binpath="$id"

install_me () {
	svn co https://irstlm.svn.sourceforge.net/svnroot/irstlm irstlm
	cd irstlm/trunk/
	sh regenerate-makefiles.sh
	./configure --prefix="$1"
	make
	make install
	cd ../..
}