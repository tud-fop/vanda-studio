# BLEU Score
# Version: 2013-07-17
# Contact: tobias.denkinger@tu-dresden.de
# Category: evaluation
# IN reference :: SentenceCorpus
# IN corpus :: SentenceCorpus
# OUT score :: Double
#
# Calculates the BLEU score of a corpus given (only) ONE reference translation.
Bleu () {
	mkdir -p "$1/reference"
	ln -s -T "$2" "$1/reference/r1"
	"$BLEU/bleu" -m "$3" -r "$1/reference/" | grep "BLEU" | sed "s/BLEU score: //g" > "$4"
	unlink "$1/reference/r1"
}

# BLEU Score
# Version: 2013-10-08
# Contact: tobias.denkinger@tu-dresden.de
# Category: evaluation
# IN reference1 :: SentenceCorpus
# IN reference2 :: SentenceCorpus
# IN reference3 :: SentenceCorpus
# IN reference4 :: SentenceCorpus
# IN corpus :: SentenceCorpus
# OUT score :: Double
#
# Calculates the BLEU score of a corpus given (only) FOUR reference translation.
Bleu4 () {
	mkdir -p "$1/reference"
	ln -s -T "$2" "$1/reference/r1"
	ln -s -T "$3" "$1/reference/r2"
	ln -s -T "$4" "$1/reference/r3"
	ln -s -T "$5" "$1/reference/r4"
	"$BLEU/bleu" -m "$6" -r "$1/reference" | grep "BLEU" | sed "s/BLEU score: //g" > "$7"
	unlink "$1/reference/r1"
	unlink "$1/reference/r2"
	unlink "$1/reference/r3"
	unlink "$1/reference/r4"
}
