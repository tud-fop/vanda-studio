source "$FUNCDIR/util.bash"

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
	"runhaskell" "$VANDADIR/programs/NGrams_KenLM.hs" -g "$2" -i "$3" -o "$4"
	echo "$3" > "${4}.meta"
}

