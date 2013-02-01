source "$FUNCDIR/util.bash"

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
	N=$(grep "grams:" "$1" | wc -l)
	"runhaskell" "$VANDADIR/programs/NGrams.hs" -g "$1" -n "$N" "$2" > "$3"
	echo "$2" > "${3}.meta"
}

