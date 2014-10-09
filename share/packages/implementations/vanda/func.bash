# XRSTranslate
# Version: 2012-05-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: translation
# IN rules :: GHKMHypergraph
# IN sentence corpus :: SentenceCorpus
# OUT tree corpus :: PennTreeCorpus
#
# Generates a Tree Corpus given a GHKM Hypergraph and a Sentence Corpus
XRSTranslate () {
	"$VANDA/programs/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	"$VANDA/programs/XRSTranslate" -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" --complicated < "$3" > "$4"
}

# PennToSentenceCorpus
# Version: 2013-10-10
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Corpus Tools
# IN tree corpus :: PennTreeCorpus
# OUT sentence corpus :: SentenceCorpus
#
# Reads of the yield of trees in a PennTreeCorpus.
PennToSentenceCorpus () {
	"$VANDA/programs/PennToSentenceCorpus" < "$2" > "$3"
}

# XRSNGrams
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: intersection
# IN rulesIn :: GHKMHypergraph
# IN ngrams :: ARPA
# OUT rulesOut :: GHKMHypergraph
#
# Intersects a language model in ARPA format with a GHKM hypergraph.
XRSNGrams () {
	"$VANDA/programs/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	"$VANDA/programs/XRSNGrams" -p "Pruning" -b "100" -e "$1/map.e" -z "$1/zhg" -l "$3"
	"$VANDA/programs/XRSToHypergraph" b2t -e "$1/map.e" -f "$1/map.f" -z "$1/zhg.new" > "$4"
}

# XRSNGramsTranslate
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: translation
# IN rules :: GHKMHypergraph
# IN ngrams :: ARPA
# IN beam :: Integer
# IN corpus :: SentenceCorpus
# OUT translation :: SentenceCorpus
#
# Translates a SentenceCorpus using a GHKM hypergraph and a language model.
XRSNGramsTranslate () {
	"$VANDA/programs/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	if [ "$4" -eq "0" ]; then
		"$VANDA/programs/XRSNGrams" -t "NoBackoff" -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" -l "$3" < "$5" > "$6"
	else
		"$VANDA/programs/XRSNGrams" -t "Pruning" -b "$4" -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" -l "$3" < "$5" > "$6"
	fi
}

# NGrams
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN model :: ARPA
# IN corpus :: SentenceCorpus
# OUT scores :: Scores
#
# Evaluates the corpus according to the given model.
NGrams () {
	"$VANDA/programs/NGrams" "$2" < "$3" > "$4"
	echo "$3" > "${4}.meta"
}

# NGramsTrain
# Version: 2014-10-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN degree :: Integer
# IN minReliableCount :: Integer
# IN corpus :: SentenceCorpus
# OUT model :: ARPA
#
# Trains an n-gram model.
NGramsTrain () {
	"$VANDA/programs/NGrams" --train -n "$2" -k "$3" < "$4" > "$5"
}