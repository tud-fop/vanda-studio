# Vanda Studio
*Vanda Studio* is an integrated development environment that allows for rapid incremental design of small-scale machine-translation experiments.

## Installation from git
1. check out the git archive:
 - ``git clone <repo> <target_folder>``
2. go to ``<target_folder>/share/packages``:
 - ``cd <target_folder>/share/packages``
3. build the package ``util``:
 - ``./vandapkg.bash makepkg implementations/util``
4. install the package ``util``:
 - ``./vandapkg.bash install util.tar.gz interfaces/util.xml``

## Installation of packages
1. build the package (usually ``from share/packages``)
 - ``./vandapkg.bash makepkg <path_to_package>``
 - ``<path_to_package>`` is usually something like ``implementations/berkeley``.
2. install tool interface (usually from ``share/packages/interfaces``)
 - ``./vandapkg.bash install <path_to_interface_xml>``
3. install tool implementation
 - ``./vandapkg.bash install <path_to_package_tar.gz>``

We will someday provide a repository of stable package versions to simplify the installation process.

## Tool interfaces and tool implementations
1. tool interfaces provide the *tool boxes* in the gui of Vanda Studio.
2. tool implementations provide the implementation of tool interfaces.
3. a tool box can only be executed of a corresponding tool implementation is installed.

| **Tool-Interface** | **Tool-Paket** | **Beschreibung** | build-dep | run-dep | libraries |
|:-------------------|:---------------|:-----------------|:----------|:--------|:----------|
| util | util | core functionality  || ``pv``||
| berkeley | berkeley | Berkeley parser | ``javac`` | ``java`` ||
| egret | egret | Egret parser | ``unzip``, ``g++`` |||
| emDictionary | emDictionary | dictionary training using the EM algorithm | ``ghc`` |||
| ghkm | ghkm | GHKM rule extraction || ``java`` ||
| giza | giza | alignment training | ``make``, ``g++``, ``git`` | ``perl`` | ``boost`` |
|| mgiza | alignment training (multicore) | ``make``, ``g++``, ``git``, ``svn``, ``cmake`` | ``perl``, ``python`` | ``boost`` |
| irstlm | irstlm| tool for n-grams | ``make``, ``g++`` |||
| jobst-ibm1 | jobst-ibm1| IBM model 1 | ``make``, ``g++`` || ``boost`` |
| kenlm | kenlm | tools for n-grams  | ``g++`` || ``boost`` |
| leonhardt | leonhardt | HMM with state splitting | ``javac``, ``jar`` | ``java`` ||
| morgenroth | morgenroth | BLEU score | ``ghc`` |||
| remEmptyLines | remEmptyLines | remove empty lines from parallel corpora | ``ghc`` |||
| rparse | rparse | employ the rparse-Parser for LCFRS | ``git``, ``ant``, ``javac`` | ``sed``, ``java`` ||
| stanford-postagger | stanford postagger | Stanford Log-linear Part-Of-Speech Tagger | ``unzip`` | ``java`` ||
| tiburon | tiburon |tools for tree automata | ``tiburon.jar`` | ``java`` ||
| vanda | vanda | tools from Vanda Haskell | ``git``, ``runhaskell``, ``ghc`` |||

## Create packages
You can create your own packages with three steps:
1. create a folder for the package (folder name is the package name)
2. create two files (use another package as a template)
    - ``install.bash``: installation script
    - ``func.bash``: is called in the execution of tool boxes
2. create a tool interface xml (use another tool interface as a template)
    - the tool ids, port names, and port types in the tool interface xml and the ``install.bash`` have to match (otherwise, Vanda Haskell will throw NullPointerException upon loading the tool)

## Compilation and execution

Vanda Studio uses the build tool ``ant``. The following commands can be executed in the git root directory:

| Command | Description |
|:-------|:-------------|
| ``ant compile`` | compiles the project |
| ``ant run`` | runs the project |

# Acknowledgements

* This product includes software developed by the Apache Software Foundation (http://www.apache.org/).
* This product includes software developed by the Indiana University Extreme! Lab (http://www.extreme.indiana.edu/).
