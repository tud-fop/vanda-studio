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
	echo -e "0\n" > "$1/map.e"
	echo -e "0\n" > "$1/map.f"
	runhaskell "$VANDA/programs/XRSToHypergraph.hs" -e "$1/map.e" -f "$1/map.f" -g "$2" -z "$1/zhg"
	runhaskell "$VANDA/programs/XRSTranslate.hs" -e "$1/map.e.new" -f "$1/map.f.new" -z "$1/zhg" < "$3" > "$4"
}

# NGrams
# Version: 2013-01-31
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: language model
# IN n-gram model :: ARPA
# IN english corpus :: SentenceCorpus
# OUT logarithmic scores :: Scores
#
# Evaluates the corpus according to the given model.
NGrams () {
	export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
	N=$(grep "grams:" "$2" | wc -l)
	"runhaskell" "$VANDA/programs/NGrams.hs" -g "$2" -n "$N" "$3" > "$4"
	echo "$3" > "${4}.meta"
}

# KenLM
# Version: 2013-01-31
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
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