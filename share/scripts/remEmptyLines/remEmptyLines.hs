-- (c) 2010 Linda Leuschner <Leuschner.Linda@mailbox.tu-dresden.de>
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
-- Maintainer  :  Linda Leuschner
-- Stability   :  unbekannt
-- Portability :  portable
--
-- This module computes 'Hypergraph' out of a 'Hypergraph' and a 'WSA'.
-- The resulting 'Hypergraph' will only recognize the given word.
-- This implementation uses the Early and the Bar-Hille algorithm.

-- The input 'Hypergraph' represents a synchronous contet-free grammar.
-- Variables in a production should start with 0.
-- The list of nonterminals belonging to the variables is ordered by the index
-- of the variables.

-- Left : nonterminals
-- Right: terminals

-- TODO: Resulting pscfg accepts the empty word

module Main  where

import qualified Data.Text.Lazy as TIO
import qualified Data.Text.Lazy.IO as TIO
import qualified Data.Text as T
import System.IO
import System.Environment ( getArgs )
import Control.Applicative
import Control.Arrow

remEmptyLines3 :: FilePath -> FilePath -> FilePath -> IO ()
remEmptyLines3 file_en file_fn file_a
  = do
      en <- TIO.readFile file_en
      fn <- TIO.readFile file_fn
      a <- TIO.readFile file_a
      handle_e <- openFile (file_en ++ ".nel") WriteMode
      handle_f <- openFile (file_fn ++ ".nel") WriteMode
      handle_a <- openFile (file_a ++ ".nel") WriteMode
      let p (e, f, a) = TIO.length e > 6 && TIO.length f > 3 && TIO.length a > 6
          xs = filter p $ zip3 (TIO.lines en) (TIO.lines fn) (TIO.lines a)
      sequence [ TIO.hPutStrLn handle_e e
                 >> TIO.hPutStrLn handle_f f
                 >> TIO.hPutStrLn handle_a a
               | (e, f, a) <- xs
               ]
      hClose handle_e
      hClose handle_f
      hClose handle_a

remEmptyLines2 :: FilePath -> FilePath -> IO ()
remEmptyLines2 file_en file_fn
  = do
      en <- TIO.readFile file_en
      fn <- TIO.readFile file_fn
      handle_e <- openFile (file_en ++ ".nel") WriteMode
      handle_f <- openFile (file_fn ++ ".nel") WriteMode
      let p (e, f) = TIO.length e > 6 && TIO.length f > 3
          xs = filter p $ zip (TIO.lines en) (TIO.lines fn)
      sequence [ TIO.hPutStrLn handle_e e
                 >> TIO.hPutStrLn handle_f f
               | (e, f) <- xs
               ]
      hClose handle_e
      hClose handle_f

main :: IO ()
main = do
  args <- getArgs
  case args of
    [eFile, fFile, aFile] -> remEmptyLines3 eFile fFile aFile
    [eFile, fFile] -> remEmptyLines2 eFile fFile

