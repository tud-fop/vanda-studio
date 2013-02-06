source "$FUNCDIR/util.bash"

# Berkeley Tokenizer
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN corpus :: SentenceCorpus
# OUT tokenizedCorpus :: SentenceCorpus
#
# Converts some special characters into tokens, such as ( into -LLB-
BerkeleyTokenizer () {
	cat "$2" | java -cp "$BERKELEY_PARSER:$BERKELEY_TOKENIZER" Main > "$3"
}

# Berkeley Parser
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN grammar :: BerkeleyGrammar.sm6
# OUT trees :: PennTreeCorpus
#
# Berkeley parser using a state-split grammar. Corpus must not contain empty lines.
BerkeleyParser () {
	cat "$2" | java -jar "$BERKELEY_PARSER" -nThreads "$(nproc)" -gr "$3" -outputFile "$4"
}
