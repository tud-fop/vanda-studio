source "$FUNCDIR/util.bash"

# Berkeley Tokenizer
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN Corpus :: Sentence Corpus
# OUT Tokenized Corpus :: Sentence Corpus
#
# Converts some special characters into tokens, such as ( into -LLB-
BerkeleyTokenizer () {
	echo "Running: BerkeleyTokenizer..."
	cat "$1" | java -cp "$BERKELEY_PARSER:$BERKELEY_TOKENIZER" Main > "$2"
	echo "Done."
}

# Berkeley Parser
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: parsing
# IN Corpus :: Sentence Corpus
# IN Grammar :: BerkeleyGrammar.sm6
# OUT Tree Corpus :: Penn Tree Corpus
#
# Berkeley parser using a state-split grammar. Corpus must not contain empty lines.
BerkeleyParser () {
	echo "Running: BerkeleyParser..."
	java -jar "$BERKELEY_PARSER" -gr "$2" -inputFile "$1" -outputFile "$3"
	echo "Done."
}
