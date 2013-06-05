#ifndef BIGRAM_H
#define BIGRAM_H

#include <boost/bimap.hpp>

#include "Corpus.h"
#include "Model.h"

class Bigram : public Model<Bigram>
{
public:
    Bigram(Corpus* co);
    Bigram(string e, string f, string base) : Model<Bigram>(e,f,base){};
    static string ending;
protected:
private:
};

#endif // BIGRAM_H
