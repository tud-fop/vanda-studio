# rparse parser
# Version: 2015-07-29
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Parsing
# IN grammar :: RparseLCFRS
# IN corpus :: TaggedCorpus
# OUT trees :: NeGraCorpus
#
# rparse parser. Corpus must not contain empty lines.
rparseParser () {
  sed 's/$/\n/g; s/ /\n/g; s/_/\//g' "$3" > "$1/convertedCorpus"
  java -jar "$RPARSE/rparse.jar" \
    -doParse \
    -test "$1/convertedCorpus" \
    -readModel "$2" > "$4"
}

# rparse training
# Version: 2015-06-29
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Training
# IN corpus :: NeGraCorpus
# OUT grammar :: RparseLCFRS
#
# Creates an LCFRS from a NeGra corpus.
rparseTrain () {
	java -jar "$RPARSE/rparse.jar" \
       -doTrain \
       -train "$2" \
       -headFinder negra \
       -saveModel "$3"
}
