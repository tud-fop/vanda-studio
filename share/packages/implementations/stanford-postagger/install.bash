id="stanford-postagger"
varname="STPOSTAGGER"
version="2015-06-30"
binpath="$id"

download () {
	wget -N "http://nlp.stanford.edu/software/stanford-postagger-full-2015-04-20.zip"
	unzip "stanford-postagger-full-2015-04-20.zip"
}

install_me () {
	cp "stanford-postagger-full-2015-04-20/stanford-postagger.jar" -t "$1/."
	mkdir -p "$2/stanford-taggers"
	cp --recursive "stanford-postagger-full-2015-04-20/models" -t "$2/stanford-taggers"
}

