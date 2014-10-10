# Egret parser
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Parsing
# IN grammar :: EgretGrammar
# IN corpus :: SentenceCorpus
# OUT trees :: PennTreeCorpus
#
# Egret parser using a LAPCFG. Corpus must not contain empty lines.
egret () {
	"$EGRET/egret" -lapcfg "-i=$3" "-data=$2" "-n=1" | sed "/^[(]/!d" | PROGRESS "$3" > "$4"
}

# Egret n-best trees
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Parsing
# IN grammar :: EgretGrammar
# IN n :: Integer
# IN corpus :: SentenceCorpus
# OUT trees :: PennTreeCorpus
#
# Computes n best trees for the sentences in the corpus.
egretnbest () {
	"$EGRET/egret" -lapcfg "-i=$4" "-data=$2" "-n=$3" | sed "/^[(]/!d" | sed "0~${3}G" | sed -e :a -e '/^\n*$/{$d;N;ba' -e '}' | PROGRESSX "$4" $(expr $3 + 1) > "$5"
}

# Egret n-best forest
# Version: 2014-10-08
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Parsing
# IN grammar :: EgretGrammar
# IN n :: Integer
# IN corpus :: SentenceCorpus
# OUT pcfgs :: PCFGs
#
# Computes n best trees for the sentences in the corpus.
egretnbestforest () {
	"$EGRET/egret" -lapcfg "-i=$4" "-data=$2" "-nbest4threshold=$3" -printForest | sed "/^$/{n;N;N;d}" | tail -n+3 > "$5"
}
