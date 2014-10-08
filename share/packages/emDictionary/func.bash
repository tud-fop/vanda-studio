# EMDictionary
# Version: 2014-10-08
# Contact: Matthias.Buechse@tu-dresden.de
# Category: Translation Models / Training
# IN threshold :: Double
# IN english corpus :: SentenceCorpus
# IN french corpus :: SentenceCorpus
# OUT dictionary :: Dictionary
# OUT em steps :: EMSteps
#
# Trains a dictionary with a bilingual corpus.
EMDictionary () {
	tmp="$1/corpus.txt"
	"$EMDICTIONARY/toParallelCorpus" "$2" "$3" > "$tmp"
	"$EMDICTIONARY/EMDictionary" csvAndBest "$4" "$tmp" "$6" > "$5"
}
