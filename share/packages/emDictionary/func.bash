# EMDictionary
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: training
# IN english corpus :: SentenceCorpus
# IN french corpus :: SentenceCorpus
# IN threshold :: Double
# OUT dictionary :: Dictionary
# OUT em steps :: EMSteps
#
# Trains a dictionary with a bilingual corpus.
EMDictionary () {
	tmp="$1/corpus.txt"
	"$EMDICTIONARY/toParallelCorpus" "$2" "$3" > "$tmp"
	"$EMDICTIONARY/EMDictionary" csvAndBest "$4" "$tmp" "$6" > "$5"
}
