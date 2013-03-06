-- (c) 2012 Tobias Denkinger <s7480817@mail.zih.tu-dresden.de>
--
-- Technische Universität Dresden / Faculty of Computer Science / Institute
-- of Theoretical Computer Science / Chair of Foundations of Programming
--
-- Redistribution and use in source and binary forms, with or without
-- modification, is ONLY permitted for teaching purposes at Technische
-- Universität Dresden AND IN COORDINATION with the Chair of Foundations
-- of Programming.
-- ---------------------------------------------------------------------------

-- |
-- Maintainer  :  Tobias Denkinger
-- Stability   :  unbekannt
-- Portability :  portable
--
-- Given two sentence corpora, generates a parallel corpus file.

module Main where

import qualified Data.Text.Lazy as TIO
import qualified Data.Text.Lazy.IO as TIO
import Control.Monad
import System.Environment ( getArgs )

toParallelCorpus :: FilePath -> FilePath -> IO()
toParallelCorpus file_en file_fn
  = do
      en <- TIO.readFile file_en
      fn <- TIO.readFile file_fn
      let p (e,f) = not (TIO.null e) && not (TIO.null f)
          xs = filter p $ zip (TIO.lines en) (TIO.lines fn)
          (he, hf) = head xs
      TIO.putStrLn he >> TIO.putStrLn hf
      forM_ (tail xs) (\ (e,f) -> TIO.putStrLn TIO.empty
                               >> TIO.putStrLn e
                               >> TIO.putStrLn f)

main :: IO ()
main = do 
  args <- getArgs
  case args of
    [eFile, fFile] -> toParallelCorpus eFile fFile

