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
	"$VANDA/.cabal-sandbox/bin/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	"$VANDA/.cabal-sandbox/bin/XRSTranslate" -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" --complicated < "$3" > "$4"
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
	"$VANDA/.cabal-sandbox/bin/PennToSentenceCorpus" < "$2" > "$3"
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
	"$VANDA/.cabal-sandbox/bin/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	"$VANDA/.cabal-sandbox/bin/vanda" xrsngrams product --no-backoff "$1/zhg" "$1/map.e" "$1/map.f" "$3"
	"$VANDA/.cabal-sandbox/bin/XRSToHypergraph" b2t -e "$1/map.e" -f "$1/map.f" -z "$1/zhg.new" > "$4"
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
	"$VANDA/.cabal-sandbox/bin/XRSToHypergraph" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	if [ "$4" -eq "0" ]; then
		"$VANDA/.cabal-sandbox/bin/vanda" xrsngrams translate --no-backoff "$1/zhg" "$1/map.e" "$1/map.f" "$3" < "$5" > "$6"
	else
		"$VANDA/.cabal-sandbox/bin/vanda" xrsngrams translate --prune="$4" "$1/zhg" "$1/map.e" "$1/map.f" "$3" < "$5" > "$6"
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
	"$VANDA/.cabal-sandbox/bin/vanda" ngrams evaluate "$2" < "$3" > "$4"
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
	"$VANDA/.cabal-sandbox/bin/vanda" ngrams train --bound="$3" --degree="$2" < "$4" > "$5"
}

# ExtragtPLCFRS
# Version: 2015-07-09
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN corpus :: NeGraCorpus
# OUT plcfrs :: PLCFRS
#
# Extract a probabilistic LCFRS from a corpus (NEGRA export format)
ExtractPLCFRS () {
  iconv "--from-code=$(file --brief --mime-encoding "$2")" "--to-code=utf-8" "$2" | "$VANDA/.cabal-sandbox/bin/vanda" lcfrs extract "$3"
}

# BinarizeLCFRSNaively
# Version: 2015-07-09
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarizes a probabilistic LCFRS naively
BinarizeLCFRSNaively () {
	"$VANDA/.cabal-sandbox/bin/vanda" lcfrs binarize --naive "$2" "$3"
}

# BinarizeLCFRSLowMaxFo
# Version: 2015-07-09
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarizes a probabilistic LCFRS optimally (lowest maximal fanout)
BinarizeLCFRSLowMaxFo () {
	"$VANDA/.cabal-sandbox/bin/vanda" lcfrs binarize --optimal "$2" "$3"
}

# BinarizeLCFRSHybrid
# Version: 2015-07-31
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarize rules up to rank 5 optimally and the rest naively.
BinarizeLCFRSHybrid () {
	"$VANDA/.cabal-sandbox/bin/vanda" lcfrs binarize --hybrid=5 "$2" "$3"
}

# BinarizeLCFRSHybrid
# Version: 2015-07-31
# Contact: sebastian.mielke@tu-dresden.de
# Category: Language Model
# IN bound :: Integer
# IN plcfrs :: PLCFRS
# OUT binarizedplcfrs :: PLCFRS
#
# Binarize rules up to rank "bound" optimally and the rest naively.
BinarizeLCFRSHybrid2 () {
	"$VANDA/.cabal-sandbox/bin/vanda" lcfrs binarize --hybrid="$2" "$3" "$4"
}

# Vanda-pcfg-extract
# Version: 2016-03-31
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Training
# IN trees :: PennTreeCorpus
# OUT pcfg :: VandaPCFG
#
# Extract a pcfg from treebank.
Vanda-pcfg-extract () {
	"${VANDA}/.cabal-sandbox/bin/vanda" pcfg extract "$3" "$2"
}

# Vanda-pcfg-train
# Version: 2016-03-31
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Training
# IN pcfg-in :: VandaPCFG
# IN sentences :: SentenceCorpus
# IN em-iterations :: Integer
# OUT pcfg-out :: VandaPCFG
#
# Estimate the rule probabilities of a pcfg with unsupervised training.
Vanda-pcfg-train () {
	"${VANDA}/.cabal-sandbox/bin/vanda" pcfg train "$2" "$5" "$3" "$4"
}

# Vanda-pcfg-n-best
# Version: 2016-03-31
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Generation
# IN pcfg :: VandaPCFG
# IN count :: Integer
# OUT trees :: PennTreeCorpus
#
# Find the most probable parse trees of a pcfg.
Vanda-pcfg-bests () {
	"${VANDA}/.cabal-sandbox/bin/vanda" pcfg bests "$2" "$3" > "$4"
}

# Vanda-pcfg-intersect
# Version: 2016-03-31
# Contact: Toni.Dietze@tu-dresden.de
# Category: Language Models::Parsing
# IN pcfg-in :: VandaPCFG
# IN sentence :: SingleSentence
# OUT pcfg-out :: VandaPCFG
#
# Intersect a pcfg with a sentence resulting in a pcfg that allows exactly those derivations that produce the given sentence.
Vanda-pcfg-intersect () {
	"${VANDA}/.cabal-sandbox/bin/vanda" pcfg intersect "$2" "$4" "$(cat "$3")"
}

# lcfrs-parse-cyk
# Version: 2017-03-05
# Contact: thomas.ruprecht@tu-dresden.de
# Category: Language Models::Parsing
# IN grammar :: PLCFRS
# IN sentence :: SingleSentence
# OUT parse-trees :: NeGraCorpus
#
# Parse a sentence probabilisticly using an LCFRS.
lcfrs-parse-cyk () {
	cat "$3" | "${VANDA}/.cabal-sandbox/bin/vanda" lcfrs parse --algorithm=CYK --bw=2500 --ts=1 "$2" > "$4"
}

# lcfrs-parse-naive
# Version: 2017-03-05
# Contact: thomas.ruprecht@tu-dresden.de
# Category: Language Models::Parsing
# IN grammar :: PLCFRS
# IN sentence :: SingleSentence
# OUT parse-trees :: NeGraCorpus
#
# Parse a sentence probabilisticly using an LCFRS.
lcfrs-parse-naive () {
	cat "$3" | "${VANDA}/.cabal-sandbox/bin/vanda" lcfrs parse --algorithm=NaiveActive --bw=2500 --ts=1 "$2" > "$4"
}

# lcfrs-parse-active
# Version: 2017-03-05
# Contact: thomas.ruprecht@tu-dresden.de
# Category: Language Models::Parsing
# IN grammar :: PLCFRS
# IN sentence :: SingleSentence
# OUT parse-trees :: NeGraCorpus
#
# Parse a sentence probabilisticly using an LCFRS.
lcfrs-parse-active () {
	cat "$3" | "${VANDA}/.cabal-sandbox/bin/vanda" lcfrs parse --algorithm=Active --bw=2500 --ts=1 "$2" > "$4"
}

# lcfrs-parse-incremental
# Version: 2017-07-27
# Contact: thomas.ruprecht@tu-dresden.de
# Category: Language Models::Parsing
# IN grammar :: PLCFRS
# IN sentence :: SingleSentence
# OUT parse-trees :: NeGraCorpus
#
# Parse a sentence probabilisticly using an LCFRS.
lcfrs-parse-incremental () {
	cat "$3" | "${VANDA}/.cabal-sandbox/bin/vanda" lcfrs parse --algorithm=Incremental --bw=2500 --ts=1 "$2" > "$4"
}