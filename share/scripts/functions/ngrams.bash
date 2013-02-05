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
	N=$(grep "grams:" "$2" | wc -l)
	"runhaskell" "$VANDADIR/programs/NGrams.hs" -g "$2" -n "$N" "$3" > "$4"
	echo "$3" > "${4}.meta"
}

