id="kenlm"
varname="KENLM"
version="2013-10-17"
binpath="$id"

install_me () {
	wget -O - http://kheafield.com/code/kenlm.tar.gz |tar xz
	cd kenlm
	./bjam
	cp -r bin "$1/."
	cd ..
}
