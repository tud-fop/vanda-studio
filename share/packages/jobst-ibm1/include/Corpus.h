#ifndef CORPUS_H
#define CORPUS_H

#include <string>
#include <vector>
#include <map>
#include <boost/bimap.hpp>

#include "../include/Words.h"

using namespace std;

typedef boost::bimap< wstring, int > wmap;
typedef wmap::value_type wpair;

class Corpus
{
public:
    Corpus(string e, string f, string base);
    wmap* getEwords();
    wmap* getFwords();
    vector<int*>* getElines();
    vector<int*>* getFlines();
    string getBase(){return base;}
    string getE(){return e;}
    string getF(){return f;}
    virtual ~Corpus();
protected:
private:
    Words* words;
    string base;
    string e;
    string f;
    vector<int*> elines;
    vector<int*> flines;
    bool readCorpus(string lang, boost::bimap<wstring, int>* words, vector<int*>* lines);
};

#endif // CORPUS_H
