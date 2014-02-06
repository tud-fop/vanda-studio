#ifndef DICTIONARY_H
#define DICTIONARY_H

#include "Corpus.h"
#include "Model.h"

class Dictionary : public Model<Dictionary>
{
public:
    Dictionary(Corpus* co);
    Dictionary(string e, string f, string base) : Model<Dictionary>(e,f,base){};
    int getBestTranslation(int f);
    map<float, int>* getNBestTranslations(int word, unsigned int n=10);
    static string ending;
    float lookup(int f, int e){ return getValue(f,e); };
protected:
private:
};

#endif // DICTIONARY_H
