{--------------------------------------------------------------------------------
Author: Lena Morgenroth

This module is part of my Diplomarbeit (diploma thesis)
"Comparison and Implementation of MT Evaluation Methods" at

Technische Universität Dresden
Fakultät Informatik
Institut für Theoretische Informatik

which was supervised by Prof. Dr.-Ing. habil. Heiko Vogler
and Dipl.-Inf. Matthias Büchse and was handed in on July 11th, 2011.

It implements the BLEU metric as defined by Papineni et.al. in their 2002 paper
"BLEU: a Method for Automatic Evaluation of Machine Translation" as well as some
additional options to compute related n-gram based metrics. 

--------------------------------------------------------------------------------}

-- | This module contains data types and functions for preprocessing files so they
--   can be used for computing (mainly BLEU) scores and functions for actually
--   computing them.
module BLEU
( Options
, optM
, optRs
, optBP
, optComb
, optSegs
, optNVal
, defaultOptions
, BP (Closest, Shortest)
, Combine (GeomAvg, ArithAvg)
, bleuPacker
, bleuHandler
)
where

import Data.Function (on)
import Data.List (transpose, minimumBy, groupBy, sortBy, zip4)
import Data.Char (isSymbol, isNumber, isSpace, isPunctuation)

import qualified Data.MultiSet as M



{---------------------------------------------------------------------------------}

-- * Data Types

{---------------------------------------------------------------------------------}


-- | holds BLEU-specific options for computation
data Options = Options {
        -- | file containing the MT
        optM     :: Maybe FilePath, 
        -- | directory containing the files containing the RTs.
        --   There must not be any other files in this directory.
        optRs    :: Maybe FilePath,
        -- | setting for brevity penalty computation
        optBP    :: BP,
        -- | setting for combination of modified precision scores
        optComb  :: Combine,
        -- | whether segment-level evaluation is enabled or not
        optSegs  :: Bool,
        -- | list of n-gram lengths to be used in the computation
        optNVal  :: [Int]
        } deriving Show


-- | specifies how the brevity penalty is computed
data BP = 
        -- | compute brevity penalty using shortest corresponding reference segment
        Shortest 
        -- | compute brevity penalty using reference segment that is closest in length to the corresponding MT segment
        | Closest
        deriving Show


-- | specifies how the modified precision scores for the various n are combined
data Combine =
        -- | combine modified precision scores using geometric mean (original BLEU definition)
        GeomAvg
        -- | combine modified precision scores using arithmetic mean (NIST definition)
        | ArithAvg
        deriving Show


-- |represents a multiset of n-grams of an MT segment
type MSeg = M.MultiSet String

-- |represents a multisets of n-grams of corresponding reference segments of one or more RTs
type RSegs  = [M.MultiSet String]


-- | contains all data necessary for computation of score
data BLEUInput = BLEUInput { -- | the complete MT in various n-gram representations.
                             --   Say we want to use n-grams of length n = 1,2 and 3, this would be a list
                             --   [1-grams, 2-grams, 3-grams] where e.g. 1-grams is a list of all MT segments
                             --   in 1-gram multiset representation, i.e. 1-gram 'MSeg's
                             mt         :: [[MSeg]]
                             -- | all RTs in various n-gram representations. 
                             --   Say we want to use n-grams of length n = 1,2 and 3, this would be a list
                             --   [1-grams, 2-grams, 3-grams] where e.g. 1-grams is a list of all
                             --   1-gram 'RSegs's
                           , refs       :: [[RSegs]]
                             -- | the length of each MT segment in tokens
                           , mlengths   :: [Int]
                             -- | for each n-gram-length the length of each MT segment in n-grams
                           , mNgramLengths :: [[Int]]
                             -- | the length of each RT segment in tokens
                           , rlengths   :: [[Int]] 
                             -- | the options to be used for the computation
                           , myOpts     :: Options      
                           } deriving (Show) 

-- | contains computation results
data BLEUOutput = BLEUOutput { -- | the corpus level score
                               corpusBLEU         :: Float
                               -- | the list of segment level scores
                             , segBLEUs           :: [Float]
                               -- | the length of the entire MT in tokens
                             , corpusTokenLength  :: Int
                               -- | the list of MT segment lengths in tokens
                             , segTokenLengths    :: [Int]
                               -- | the entire effective reference length
                             , corpusERL          :: Int
                               -- | the list of segment effective reference lengths
                             , segERLs            :: [Int]
                             }

instance Show BLEUOutput where
        show output = "\n" ++ (showSegs [1..] (segTokenLengths output) (segERLs output) (segBLEUs output))
                           ++ "\nTotal number of tokens: " ++ (show $ corpusTokenLength output) 
                           ++ "\nTotal effective reference length: " ++ (show $ corpusERL output)
                           ++ "\n\nBLEU score: "++ (show $ corpusBLEU output) ++ "\n"



-- | returns a string for pretty printing segment level scores
showSegs :: [Int]   -- ^ list of segment indices
         -> [Int]   -- ^ list of segment lengths in tokens
         -> [Int]   -- ^ list of effective reference lengths of segments in tokens
         -> [Float] -- ^ segment scores
         -> String  -- ^ pretty output of all listed segment level scores
showSegs _ [] _ _ = ""
showSegs _ _ [] _ = ""
showSegs _ _ _ [] = ""
showSegs (index:indices)
         (tl:tls)
         (erl:erls)
         (score:scores) = "segment " ++ (show index) ++ ": " ++
                          (show tl) ++ " tokens, " ++
                          "effective reference lenth is " ++ (show erl) ++ " tokens, " ++
                          "score: " ++ (show score) ++ "\n" ++
                          (showSegs indices tls erls scores)



{---------------------------------------------------------------------------------}

-- * Constants

{---------------------------------------------------------------------------------}


-- | returns an 'Options' record of BLEU-specific options with default values
defaultOptions :: Options
defaultOptions = Options {
        optM      = Nothing,
        optRs     = Nothing,
        optBP     = BLEU.Closest,
        optComb   = BLEU.GeomAvg,
        optSegs   = True,
        optNVal   = [1,2,3,4]
        }



{---------------------------------------------------------------------------------}

-- * Preprocessing Input

{---------------------------------------------------------------------------------}


-- | preprocesses and packages all input necessary for computation 
bleuPacker :: Options   -- ^ the computation options  
           -> String    -- ^ the contents of the MT file
           -> [String]  -- ^ a list of Strings, each the contents of an RT file
           -> BLEUInput -- ^ the preprocessed input to the computation
bleuPacker opts mRaw rRaw =  BLEUInput {mt = m, mlengths = mls, mNgramLengths = mNGLs, refs = rs, rlengths = rls, myOpts = opts }
        where m           = zipWith ($) nGramizeM (repeat tokensM)  ::  [[M.MultiSet String]]
              rs          = zipWith ($) nGramizeR (repeat tokensR)  :: [[[M.MultiSet String]]]              
              nGramizeM   = map (map . makeNgrams) (optNVal opts)   :: [ [[String]]  ->  [M.MultiSet String] ]
              nGramizeR   = map (map) nGramizeM                     :: [[[[String]]] -> [[M.MultiSet String]]]
              mNGLs       = nGramLengths (optNVal opts) mls         :: [[Int]]
              mls         = map length tokensM                      ::  [Int]
              rls         = map (map length) tokensR                :: [[Int]]
              tokensM     = map tokenize m'                         ::  [[String]]
              tokensR     = map (map tokenize) rs'                  :: [[[String]]]                   
              m'          = lines mRaw                              ::  [String]
              rs'         = transpose $ map lines rRaw              :: [[String]]


-- | Tokenization using unicode categories. This is equivalent to the 
--   'international-tokenization' option in NIST's mteval-v13a.pl script.
--   Whitespace is dropped. Symbols are treated as a separate token each. 
--   Punctuation each a separate token as well unless followed and preceeded by a number.
tokenize :: String   -- ^ the segment to be tokenized
         -> [String] -- ^ the tokenized segment
tokenize string = tokenize' False [""] string
        where tokenize' _ intermediate "" = map reverse (reverse (filter (/="") intermediate))
              tokenize' lastNum (string:listofstrings) (char:rest)
                | isSymbol char      = tokenize' False ("":[char]:string:listofstrings) rest
                | isPunctuation char = if lastNum && (rest /= "") && (isNumber (head rest))
                                          then tokenize' False ((char:string):listofstrings) rest
                                          else tokenize' False ("":[char]:string:listofstrings) rest
                | isSpace char       = tokenize' False ("":string:listofstrings) rest
                | otherwise          = tokenize' (isNumber char) ((char:string):listofstrings) rest



-- | creates multiset n-gram representation of a segment according to specified n-gram length
makeNgrams :: Int                -- ^ the desired n-gram length
           -> [String]           -- ^ a tokenized segment
           -> M.MultiSet String  -- ^ the segment's multiset n-gram representation
makeNgrams n tokenizedString = makeNgrams' M.empty n tokenizedString
        where makeNgrams' intermediate n tokenizedString
                | (length tokenizedString) < n = intermediate
                | otherwise                    = makeNgrams' newInter n (tail tokenizedString)
                where newInter = M.insert (unwords (take n tokenizedString)) intermediate


-- | computes the lengths of a list of segments in n-grams  various n.
--   (This is more efficient than just calling M.size on the various n-gram multiset representations.)
nGramLengths :: [Int]   -- ^ the list of values of n for which the length in n-grams is to be computed
             -> [Int]   -- ^ a list of segment lengths in tokens
             -> [[Int]] -- ^ list containing for each n a list of the segment lengths in n-grams
nGramLengths nList lengthList = zipWith ($) mapSubtractFuncs (repeat lengthList)
       where mapSubtractFuncs = map (map) subtractFuncs         :: [[Int] -> [Int]]
             subtractFuncs    = map (\n x -> x-n+1) nList       :: [Int -> Int]



{---------------------------------------------------------------------------------}

-- * Computing (BLEU-) Scores

{---------------------------------------------------------------------------------}


-- | handles computation of (mainly BLEU) scores as specified by the options provided
bleuHandler :: BLEUInput  -- ^ preprocessed input
            -> BLEUOutput -- ^ corpus (and if so specified by the options) segment level computation results
bleuHandler input = BLEUOutput { corpusBLEU        = corpusLevel         :: Float
                               , segBLEUs          = segLevel            :: [Float]
                               , corpusTokenLength = totalMtTokenLength  :: Int
                               , segTokenLengths   = mtTokenLengths      :: [Int]
                               , corpusERL         = totalERL            :: Int
                               , segERLs           = effectiveRefLengths :: [Int]
                               }
        where corpusLevel              = bleu (optComb $ myOpts input) (aggregateMatchCounts,
                                                                        aggregateMtNgramLengths,
                                                                        totalMtTokenLength,
                                                                        totalERL
                                                                        )
              segLevel                 = if (optSegs $ myOpts input)
                                           then map (bleu (optComb $ myOpts input))
                                                    (zip4 (transpose matchCounts)
                                                          (transpose (mNgramLengths input))
                                                          (mlengths input)
                                                          effectiveRefLengths
                                                    ) 
                                           else []
              totalMtTokenLength       = sum (mlengths input)                                 :: Int
              mtTokenLengths           = if (optSegs $ myOpts input)
                                           then (mlengths input)
                                           else []
              totalERL                 = sum effectiveRefLengths                              :: Int
              effectiveRefLengths      = zipWith (effectiveRefLength (optBP $ myOpts input))
                                                 (mlengths input)
                                                 (rlengths input)                             :: [Int]
              aggregateMatchCounts     = map sum matchCounts                                  :: [Int]
              matchCounts              = zipWith (zipWith matchCount) (mt input) (refs input) :: [[Int]]
              aggregateMtNgramLengths  = map sum (mNgramLengths input)                        :: [Int]
              


-- | computes a single (BLEU) score according to the options specified
bleu :: Combine                   -- ^ combination option for the modified precision scores
     -> ([Int], [Int], Int, Int)  -- ^ quadruple containing 1. a list of match counts for the different n-gram lengths.
                                  --   These can arise either from a single segment or as an aggregate for the whole corpus. 
                                  --   2. a corresponding list of MT lengths (either segment or aggregated for the whole corpus)
                                  --   in n-grams for the different n-gram lengths.
                                  --   3. the length of the MT segment or the aggregate length of the whole MT corpus in tokens
                                  --   4. the corresponding effective reference length in tokens
     -> Float                     -- ^ the resulting score
bleu comb (matchCounts, lengths_in_ngrams, mLength, efRefLength) = brevityPenalty * combinedPrecs
        where combinedPrecs  = case comb of 
                                 GeomAvg  -> geomAvg precisions
                                 ArithAvg -> arithAvg precisions
              precisions     = zipWith (/)
                                   (map fromIntegral matchCounts)
                                   (map fromIntegral lengths_in_ngrams)
              brevityPenalty = exp (min 0 (1 - (fromIntegral efRefLength) /
                                           (fromIntegral mLength)
                                          )
                                   )             


-- | computes effective reference length according to the brevity penalty option provided
effectiveRefLength :: BP    -- ^ brevity penalty option (use reference closest in length or use shortest reference)
                   -> Int   -- ^ length of MT segment in tokens
                   -> [Int] -- ^ list of lengths of corresponding RT segments in tokens
                   -> Int   -- ^ the segment's effective reference length in tokens
effectiveRefLength Shortest _ rlengths      = minimum rlengths                     
effectiveRefLength Closest mlength rlengths = minimum (head lengthsByDifference)   
        where lengthsByDifference = groupBy equal' (sortBy compare' rlengths)
              compare'            = compare `on` (abs . (\x -> x - mlength))
              equal' x y          = (compare' x y) == EQ


-- | computes number of n-gram matches between an MT segment and corresponding RT segments. 
matchCount :: MSeg   -- ^ an MT segment in n-gram multiset representation
           -> RSegs  -- ^ corresponding RT segments in n-gram multiset representation
           -> Int    -- ^ number of matches between the MT segment and the RT segments
matchCount m refs = M.size matches
        where matches          = M.intersection m potentialMatches
              potentialMatches = foldr M.maxUnion M.empty refs


-- | computes the geometric average of a list of floating point values
geomAvg :: [Float] -> Float
geomAvg list = exp logAvg
        where logAvg = sum (map ((factor*) . log) list)
              factor = 1.0 / (fromIntegral $ length list)


-- | computes the arithmetic average of a list of floating point values
arithAvg :: [Float] -> Float
arithAvg list = (sum list) / (fromIntegral $ length list)
