id="kenlm"
varname="KENLM"
version="2013-10-17"
binpath="$id"

download () {
	wget -O - http://kheafield.com/code/kenlm.tar.gz | tar xz
}

install_me () {
	cd kenlm
	./bjam
	cp -r bin "$1/."
	cd ..
}
