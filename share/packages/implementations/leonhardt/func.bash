# Hidden-Markov-Model Training with State-Splitting
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# IN soiling :: Double
# IN threshold :: Double
# IN training cycles :: Integer
# IN statesplit cycles :: Integer
# IN number of states :: Integer
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model by using Sate-Splitting
HMM_State-Splitting () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -statesplitting -input "$2" -a "$3" -e "$4" -c "$5" -t "$6" -output "$8" "$9" -n "$7"
}

# Hidden-Markov-Model Training with State-Splitting
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model by using Sate-Splitting with less parameters
HMM_State-Splitting_less () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -statesplitting -input "$2" -output "$3" "$4"
}


# Hidden-Markov-Model Training with State-Splitting
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# IN soiling :: Double
# IN threshold :: Double
# IN training cycles :: Integer
# IN statesplit cycles :: Integer
# IN random seed :: Integer
# IN number of states :: Integer
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model by using Sate-Splitting with fixed seed
HMM_State-Splitting_seed () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -statesplitting -input "$2" -a "$3" -e "$4" -c "$5" -t "$6" -output "$9" "${10}" -s "$7" -n "$8"
}

# Hidden-Markov-Model Training with State-Splitting
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# IN random seed :: Integer
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model by using Sate-Splitting with less parameters and fixed seed
HMM_State-Splitting_less_seed () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -statesplitting -input "$2" -output "$4" "$5" -s "$3"
}

# Hidden-Markov-Model normal training
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# IN training cycles :: Integer
# IN number of states :: Integer
# IN soiling :: Double
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model with Baum-Welch-Algorithm
HMM_Normal () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -normal -input "$2" -output "$6" "$7" -c "$3" -n "$4" -a "$5"
}

# Hidden-Markov-Model normal training
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# IN training cycles :: Integer
# IN number of states :: Integer
# IN soiling :: Double
# IN random seed :: Integer
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model with Baum-Welch-Algorithm with fixed seed
HMM_Normal_seed () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -normal -input "$2" -output "$7" "$8" -c "$3" -n "$4" -s "$6" -a "$5"
}

# Hidden-Markov-Model normal training
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model with Baum-Welch-Algorithm and less parameters
HMM_Normal_less () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -normal -input "$2" -output "$3" "$4"
}

# Hidden-Markov-Model normal training
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN corpus :: SentenceCorpus
# IN random seed :: Integer
# OUT hmm :: HiddenMarkovModel
# OUT wordmap :: Wordmap
#
# Trains a Hidden-Markov-Model with Baum-Welch-Algorithm and less parameters and fixed seed
HMM_Normal_less_seed () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -normal -input "$2" -output "$4" "$5" -s "$3"
}

# Hidden-Markov-Model parsing
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: training
# IN hmm :: HiddenMarkovModel
# IN wordmap :: Wordmap
# IN corpus :: SentenceCorpus
# OUT scores :: Scores
#
# Parses a Hidden-Markov-Model
HMM_Parse () {
	java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -parse -input "$2" "$3" < "$4" | PROGRESS "$4" > "$5"
	echo "$4" > "${5}.meta"
}

# Perplexity Calculating
# Version: 2012-06-02
# Contact: christof.leonhardt@mailbox.tu-dresden.de
# Category: evaluation
# IN scores :: Double
# OUT perplexity :: Double
#
# Parses a Hidden-Markov-Model
HMM_Perplexity () {
	cat "$2" | java -Xmx2048m -jar "$STATE_SPLITTING/HMM-SS.jar" -perplexity > "$3"
}

