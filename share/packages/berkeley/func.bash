# Berkeley Tokenizer
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: corpus tools
# IN corpus :: SentenceCorpus
# OUT tokenizedCorpus :: SentenceCorpus
#
# Converts some special characters into tokens, such as ( into -LLB-
BerkeleyTokenizer () {
	cat "$2" | java -cp "$BERKELEY_PARSER/berkeleyParser.jar:$BERKELEY_PARSER" Main > "$3"
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
	cat "$2" | java -jar "$BERKELEY_PARSER/berkeleyParser.jar" -nThreads "$(nproc)" -gr "$3" -outputFile "$4"
}

# Berkeley Parser n-best
# Version: 2012-05-16
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN n :: Integer
# IN grammar :: BerkeleyGrammar.sm6
# OUT trees :: PennTreeCorpus
#
# Computes n best trees for the sentences in the corpus.
bpnbest () {
	cat "$2" | java -jar "$BERKELEY_PARSER/berkeleyParser.jar" -nThreads "$(nproc)" -gr "$4" -kbest "$3" -outputFile "$5"
}