id="irstlm"
varname="IRSTLM"
version="2013-06-27"
binpath="$id"

install_me () {
	svn co https://irstlm.svn.sourceforge.net/svnroot/irstlm irstlm
	cd irstlm/trunk/
	sed -i "s/AM_CONFIG_HEADER/AC_CONFIG_HEADERS/g" configure.in
	sh regenerate-makefiles.sh
	./configure --prefix="$1"
	make
	make install
	cd ../..
}
