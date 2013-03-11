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
if [ 2 -gt "$3" ]; then
	"$EGRET/egret" -lapcfg "-i=$2" "-data=$4" "-n=1" | sed "/^[(]/!d" > "$5"
else
	"$EGRET/egret" -lapcfg "-i=$2" "-data=$4" "-n=$3" | sed "/^[(]/!d" | sed "0~${3}G" | sed -e :a -e '/^\n*$/{$d;N;ba' -e '}' > "$5"
fi
}

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
	egretnbest "$1" "$2" "1" "$3" "$4"
}
