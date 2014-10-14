id="irstlm"
varname="IRSTLM"
version="2013-11-26"
binpath="$id"

download () {
	wget -O - "http://downloads.sourceforge.net/project/irstlm/irstlm/irstlm-5.80/irstlm-5.80.03.tgz?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Firstlm%2Ffiles%2Firstlm%2Firstlm-5.80%2F&ts=1385466171&use_mirror=netcologne" | tar xz
}

install_me () {
	cd irstlm-5.80.03
	sed -i "s/AM_CONFIG_HEADER/AC_CONFIG_HEADERS/g" configure.in
	./regenerate-makefiles.sh
	./configure --prefix="$1"
	make
	make install
	cd ..
}
