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
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	runhaskell "$VANDA/programs/XRSToHypergraph.hs" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	runhaskell "$VANDA/programs/XRSTranslate.hs" -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" --complicated < "$3" > "$4"
}

# XRSNGrams
# Version: 2013-03-07
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: intersection
# IN rulesIn :: GHKMHypergraph
# IN ngrams :: ARPA
# OUT rulesOut :: GHKMHypergraph
#
# Intersects a language model in ARPA format with a GHKM hypergraph.
XRSNGrams () {
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	runhaskell "$VANDA/programs/XRSToHypergraph.hs" t2b -e "$1/map.e" -f "$1/map.f" -z "$1/zhg" < "$2"
	runhaskell "$VANDA/programs/XRSNGrams.hs" -f "$1/map.f" -z "$1/zhg" -l "$3"
	runhaskell "$VANDA/programs/XRSToHypergraph.hs" b2t -e "$1/map.e" -f "$1/map.f" -z "$1/zhg.new" > "$4"
}

# NGrams
# Version: 2013-01-31
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN n-gram model :: ARPA
# IN english corpus :: SentenceCorpus
# OUT scores :: Scores
#
# Evaluates the corpus according to the given model.
NGrams () {
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	runhaskell "$VANDA/programs/NGrams.hs" -l "$2" < "$3" > "$4"
	echo "$3" > "${4}.meta"
}

# KenLM
# Version: 2013-01-31
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: language model
# IN n-gram model :: ARPA
# IN english corpus :: SentenceCorpus
# OUT logarithmic scores :: Scores
#
# Evaluates the corpus according to the given model.
KenLM () {
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	"runhaskell" "$VANDA/programs/NGrams_KenLM.hs" -g "$2" -i "$3" -o "$4"
	echo "$3" > "${4}.meta"
}
