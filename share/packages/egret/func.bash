# Egret
# Version: 2013-03-11
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN grammar :: LAPCFG-Grammar
# OUT trees :: PennTreeCorpus
#
# Computes the best tree for every sentence in the corpus.
egret () {
	"$EGRET/egret" -lapcfg "-i=$2" "-data=$3" "-n=1" | sed "/^[(]/!d" > "$4"
}

# Egret n-best
# Version: 2013-03-08
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN n :: Integer
# IN grammar :: LAPCFG-Grammar
# OUT trees :: PennTreeCorpus
#
# Computes n best trees for the sentences in the corpus.
egretnbest () {
	"$EGRET/egret" -lapcfg "-i=$2" "-data=$4" "-n=$3" | sed "/^[(]/!d" | sed "0~${3}G" | sed -e :a -e '/^\n*$/{$d;N;ba' -e '}' > "$5"
}

# Egret n-best forest
# Version: 2013-03-08
# Contact: Tobias.Denkinger@mailbox.tu-dresden.de
# Category: parsing
# IN corpus :: SentenceCorpus
# IN n :: Integer
# IN grammar :: LAPCFG-Grammar
# OUT pcfgs :: PCFGs
#
# Computes n best trees for the sentences in the corpus.
egretnbestforest () {
	"$EGRET/egret" -lapcfg "-i=$2" "-data=$4" "-nbest4threshold=$3" -printForest | sed "/^$/{n;N;N;d}" | tail -n+3 > "$5"
}
