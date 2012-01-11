import Language.Haskell.Parser
import Language.Haskell.Pretty

main = interact $
  \code -> case parseModule code of 
             ParseFailed srcLoc err -> error $ 
                      "Error at " ++ (show srcLoc) ++ ": " ++ err
             ParseOk syntaxTree -> (prettyPrint syntaxTree) ++ "\n"
