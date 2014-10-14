id="berkeley"
varname="BERKELEY_PARSER"
version="2014-10-09"
binpath="$id"

download () {
	wget -N "http://berkeleyparser.googlecode.com/files/berkeleyParser.jar"
	mkdir -p "berkeley_grammars"
	wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/eng_sm6.gr"
	wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/bul_sm5.gr"
	wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/arb_sm5.gr"
	wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/chn_sm5.gr"
	wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/fra_sm5.gr"
	wget -P "berkeley_grammars" -nc "http://berkeleyparser.googlecode.com/files/ger_sm5.gr"
}

install_me () {
	javac -cp "berkeleyParser.jar:src" -d "src" "src/Main.java"
	cp "berleleyParser.jar" "$1/."
	cp "src/Main.class" "$1/."
	cp "berkeley_grammars" "$2/."
}