# Stanford POS-tagger
# Version: 2015-06-30
# Contact: Tobias.Denkinger@tu-dresden.de
# Category: Language Models / Tagging
# IN tagger :: StPOStagger
# IN corpus :: SentenceCorpus
# OUT taggedCorpus :: TaggedCorpus
#
# Stanford POS-tagger. Corpus must not contain empty lines.
StanfordTagger () {
  java -mx300m \
    -cp "$STPOSTAGGER/stanford-postagger.jar" edu.stanford.nlp.tagger.maxent.MaxentTagger \
    -encoding utf-8 \
    -model "$2" \
    -textFile "$3" > "$4"
}

# # Stanford POS-tagger training
# # Version: 2015-06-30
# # Contact: Tobias.Denkinger@tu-dresden.de
# # Category: Language Models / Training
# # IN corpus :: TaggedCorpus
# # OUT grammar :: StPOStagger
# #
# # Creates a StPOStagger from a tagged corpus.
# StanfordTaggerTrain () {
# 	java -cp "$STPOSTAGGER/stanford-postagger.jar" edu.stanford.nlp.tagger.maxent.MaxentTagger \
#     -prop propertiesFile \
#     -model "$3" \
#     -trainFile "$2"
# }
