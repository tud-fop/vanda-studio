id="europarlTools"
varname="EUROPARL_TOOLS"
version="2013-03-05"
binpath="$id"

install_me () {
	wget http://www.statmt.org/europarl/v7/tools.tgz
	tar xfv tools.tgz
	mv tools/* "$1"
}
