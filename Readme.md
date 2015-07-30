# Vanda Studio
*Vanda Studio* ist ein Workflow-System. Es enthält viele Werkzeuge für SMT.

## Installation aus dem git
1. Das git-Archiv auschecken:
 - ``git clone git@gitlab.tcs.inf.tu-dresden.de:vanda/vanda-studio.git``
2.  In das Verzeichnis ``vanda-studio/share/packages`` wechseln:
 - ``cd vanda-studio/share/packages`` 
3. ``util`` bauen:
 - ``./vandapkg.bash makepkg implementations/util``
4. ``util`` installieren:
 - ``./vandapkg.bash install util.tar.gz interfaces/util.xml``

## Installation von Paketen
1. Toolpaket bauen
 - ``./vandapkg.bash makepkg <PfadZumToolOrdner>``
 - ``<PfadZumToolOrdner>`` ist z.B. ``implementations/berkeley``.
2. Toolinterfaces installieren
 - ``./vandapkg.bash install <InterfacePfad>``
 - Die Toolinterfaces befinden sich im Unterordner interfaces.
 - Es handelt sich um XML-Dateien.
3. Toolpakete installieren:
 - ``./vandapkg.bash install <PfadZumPaket>``
 - Die Toolpakete sind ``tar.gz``-Dateien.

Alternativ sollen eines Tages stabile Versionen der Toolpakete zum Download angeboten werden, bei denen Schritt 1 entfällt.

## Tool-Interfaces und Pakete
1. Tool-Interfaces stellen die Tool-„Kisten” in der GUI von vanda-studio bereit.
2. Tool-Pakete stellen Implementierungen für Tool-Interfaces bereit.
3. Eine „Kiste” kann nur ausgeführt werden, wenn ein entsprechendes Tool-Paket installiert ist.

| **Tool-Interface** | **Tool-Paket** | **Beschreibung** | build-dep | run-dep | libraries |
|:-------------------|:---------------|:-----------------|:----------|:--------|:----------|
| util | util | Kern-Funktionen  || ``pv``||
| berkeley | berkeley | Berkeley-Parser | ``javac`` | ``java`` ||
| egret | egret | Egret-Parser | ``unzip``, ``g++`` |||
| emDictionary | emDictionary | Wörterbuchtraining mit dem EM-Algorithmus | ``ghc`` |||
| ghkm | ghkm | GHKM Regelextraktion || ``java`` ||
| giza | giza | Alignmenttraining | ``make``, ``g++``, ``git`` | ``perl`` | ``boost`` |
|| mgiza | Alignmenttraining (Mehrkernunterstützung) | ``make``, ``g++``, ``git``, ``svn``, ``cmake`` | ``perl``, ``python`` | ``boost`` |
| irstlm | irstlm| Werkzeuge für n-Gramme | ``make``, ``g++`` |||
| jobst-ibm1 | jobst-ibm1| IBM Model 1 | ``make``, ``g++`` || ``boost`` |
| kenlm | kenlm | Werkzeuge für n-Gramme  | ``g++`` || ``boost`` |
| leonhardt | leonhardt | HMM mit State-Splitting | ``javac``, ``jar`` | ``java`` ||
| morgenroth | morgenroth | BLEU-Score | ``ghc`` |||
| remEmptyLines | remEmptyLines | remove empty lines from parallel corpora | ``ghc`` |||
| rparse | rparse | employ the rparse-Parser for LCFRS | ``git``, ``and``, ``javac`` | ``sed``, ``java`` ||
| stanford-postagger | stanford-postagger | Stanford Log-linear Part-Of-Speech Tagger | ``unzip`` | ``java`` ||
| tiburon | tiburon | Werkzeuge für Baumautomaten | ``tiburon.jar`` | ``java`` ||
| vanda | vanda | Werkzeuge aus der Haskell-Bibliothek | ``git``, ``runhaskell``, ``ghc`` |||

## Pakete erstellen
Ein eigenes Paket erstellt man so:
1. Ordner mit dem Paketnamen anlegen
    - Dateien im Ordner erstellen
        - ``install.bash``: verantwortlich für die Installation
        - ``func.bash``: verantwortlich für die Ausführung
    - Quelltext des Programmes hinzufügen (besser: Quelltext im Internet bereitstellen)
2. Interface-Datei erstellen (XML-Datei)

Um ein besseres Bild von der Funktionsweise der Dateien zu bekommen, schaut man sich ein bereits vorhandenes Paket an, zum Beispiel berkeley. Besonders wichtig ist, dass die in den Dateien ``func.bash`` und ``interface.xml`` benutzten Portnamen und -typen sowie Tool-IDs jeweils übereinstimmen, sonst kommt es zu NullPointerExceptions bei der Ausführung.

## Kompilieren und Ausführen

Vanda Studio benutzt das plattformübergreifende Build-Tool ``ant``. Die folgenden Kommandos werden im git-Wurzelverzeichnis ausgeführt:

| Befehl | Beschreibung |
|:-------|:-------------|
| ``ant compile`` | Kompiliert das Projekt |
| ``ant run`` | führt das Projekt aus |
