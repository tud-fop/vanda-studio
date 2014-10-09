id="berkeley"
varname="BERKELEY_PARSER"
version="2014-10-09"
binpath="$id"

install_me () {
	wget -P "$1" -nc "http://berkeleyparser.googlecode.com/files/berkeleyParser.jar"
	javac -cp "$1/berkeleyParser.jar:src" -d "src" "src/Main.java"
	mv "src/Main.class" "$1/."
	read -p "Download grammar files (≈80MiB) [Y/n]? " choice
	if [ "$choice" != "n" ]; then
		mkdir -p "$2/berkeley_grammars"
		wget -P "$2/berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/eng_sm6.gr"
		wget -P "$2/berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/bul_sm5.gr"
		wget -P "$2/berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/arb_sm5.gr"
		wget -P "$2/berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/chn_sm5.gr"
		wget -P "$2/berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/fra_sm5.gr"
		wget -P "$2/berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/ger_sm5.gr"
	fi
}