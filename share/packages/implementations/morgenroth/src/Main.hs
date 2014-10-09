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


-- | This is the main module. It mainly deals with I/O. It contains some functions
--   for dealing with command line arguments and with file I/O, but does not export 
--   anything except main.

module Main (main) where 

import System.Environment (getArgs)
import System.Console.GetOpt
import System.Directory (getDirectoryContents)
import System.Exit (exitFailure, exitSuccess)
import Data.Maybe (fromJust)
import qualified BLEU as BLEU


-- | the main function. It deals with I/O and invokes functions that handle
--   the actual preprocessing and computation
main :: IO()
main = do
        args <- getArgs
        printUsage args
        myOptions <- setOpts args
        checkNGram (BLEU.optNVal myOptions)

        mRaw <- getMFile (BLEU.optM myOptions)
        rDirContents <- getRDirContents (BLEU.optRs myOptions)
        rRaw <- getRFiles rDirContents (BLEU.optRs myOptions)
        
        let input = BLEU.bleuPacker myOptions mRaw rRaw
        print (BLEU.bleuHandler input)


-- | returns the list of available command line options
options :: [OptDescr (BLEU.Options -> BLEU.Options)]
options =
        [ 
        Option ['h'] ["help"] 
                (NoArg id)
                "print usage information",

        Option ['m'] [] 
               (ReqArg (\ mFile opts -> opts{ BLEU.optM = Just mFile }) "FILE")
               "file containing the MT",

        Option ['r'] []
               (ReqArg (\ rDir opts -> opts{ BLEU.optRs = Just rDir }) "DIR")
               "directory containing the RTs",

        Option [] ["shortest"]
               (NoArg (\ opts -> opts{ BLEU.optBP = BLEU.Shortest }))
               "use shortest ref. for brevity penalty (default: ref. closest in length)",
              
        Option [] ["arithmetic"]
               (NoArg (\ opts -> opts{ BLEU.optComb = BLEU.ArithAvg }))
               "use arithmetic averaging (default: geometric averaging)",

        Option [] ["nosegments"]
               (NoArg (\ opts -> opts{ BLEU.optSegs = False }))
               "no segment-level scoring",              

        Option ['n'] ["ngrams"]
               (ReqArg (\ nString opts -> opts{ BLEU.optNVal = map read (words nString)} ) "[N1,N2,N3...]")
               "space-separated list of n-gram lengths used for scoring (default \"1 2 3 4\")"
        ]


-- | sets the options according to the command line arguments that have been passed to the main function
setOpts :: [String]          -- ^ the list of command line arguments
        -> IO (BLEU.Options)
setOpts args = case getOpt RequireOrder options args of
           ([], [], [])      -> do
                                putStrLn (usageInfo header options)
                                exitFailure
           (opts, [], [])    -> return (foldl (flip id) BLEU.defaultOptions opts) 
           (_, nonOpts, [])  -> do
                                putStrLn ("! Error: unrecognized arguments: " ++ unwords nonOpts ++ "\n")
                                exitFailure
           (_, _, errs)      -> do
                                putStrLn ("! Error: " ++ concat errs ++ "\n")
                                exitFailure


-- | checks whether the n-gram list specified by command line argument contains only positive integers
--   causes a program failure if this is not the case
checkNGram :: [Int] -> IO ()
checkNGram nList = case (filter (<1) nList) of
                        [] -> return()
                        _  -> do
                              putStrLn "! Error: n-grams must be at least of length 1\n"
                              exitFailure


-- | reads the MT file specified by command line argument
--   and causes a program failure if none is specified
getMFile :: Maybe FilePath -> IO String
getMFile Nothing          = do
                            putStrLn "! Error: no MT file specified\n"
                            exitFailure
getMFile (Just filepath)  = readFile filepath


-- | returns the filepaths to all files in the RT directory specified by command line argument
--   and causes a program failure if none is specified
getRDirContents :: Maybe FilePath -> IO [String]
getRDirContents Nothing         = do
                                  putStrLn "! Error: no directory of RT files specified\n"
                                  exitFailure
getRDirContents (Just dirpath)  = getDirectoryContents dirpath


-- | reads all files contained in the reference translation directory that do not start with "."
--   and returns them as a list of Strings
getRFiles :: [String]           -- ^ a list of filenames
          -> Maybe FilePath     -- ^ the path of the directory containing the files
          -> IO [String]
getRFiles rDirContents dirPath = mapM readFile rFilePaths
              where rFilePaths = map ((dirPath'++"/")++) rDirFiles
                    rDirFiles  = filter ((/='.').head) rDirContents
                    -- dirPath is never Nothing at this point, because then the program
                    -- would have failed in getRDirContents
                    dirPath'   = fromJust dirPath 


-- | returns the header used for generating the command line usage info
header :: String
header = "\n Usage: BLEU -m MTFILE -r REFDIR [OPTION]... \n REFDIR must contain"++
         " the reference translations in a separate file each and must not contain any other files. \n\n Options:"


-- | prints usage info if indicated by command line argument
printUsage :: [String] -> IO ()
printUsage ["-h"]       = do
                          putStrLn (usageInfo header options)
                          exitSuccess        
printUsage ["--help"]   = do
                          putStrLn (usageInfo header options)
                          exitSuccess 
printUsage _            = return ()
