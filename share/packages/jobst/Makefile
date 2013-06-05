CC=g++
CFLAGS=-c -Wall -fopenmp -O3 -s
LDFLAGS=-lboost_program_options -lgomp -lboost_iostreams
SOURCES=main.cpp src/Bigram.cpp  src/Corpus.cpp  src/Dictionary.cpp  src/kbhit.cpp  src/Length.cpp  src/Model.cpp  src/Timer.cpp  src/Translator.cpp  src/utility.cpp  src/Words.cpp
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=translate

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) -o $@

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -f $(OBJECTS) translate
