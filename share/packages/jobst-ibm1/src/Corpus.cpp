#include "../include/Corpus.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <wchar.h>
#include <stdio.h>
#include <vector>
#include <string>

#include "../include/Length.h"
#include "../include/Bigram.h"
#include "../include/Dictionary.h"
#include "../include/Timer.h"
#include "../include/utility.h"

#include <boost/lexical_cast.hpp>

using namespace std;

Corpus::Corpus(string e, string f, string base)
{
    wmap* ewords = new wmap;
    wmap* fwords = new wmap;
    this->base = base;
    this->e = e;
    this->f = f;
    #pragma omp parallel sections shared(e,ewords,f,fwords)
    {
        #pragma omp section
        {readCorpus(e, ewords, &elines);}
        #pragma omp section
        {readCorpus(f, fwords, &flines);}
    }
    words = new Words(e, f, base, ewords, fwords);
    #pragma omp parallel sections
    {
        #pragma omp section
        {
            Length *l = new Length(this);
            delete l;
        }
        #pragma omp section
        {
            Bigram *b = new Bigram(this);
            delete b;
        }
    }
    Dictionary *d = new Dictionary(this);
    delete d;
}

Corpus::~Corpus()
{

}

bool Corpus::readCorpus(string lang, boost::bimap<wstring, int>* words, vector<int*>* lines)
{
    string filename = base + "." + lang;
    systemMessage(("Reading Corpus " + filename).c_str());
    words->insert(wpair(L"#",0));
    int i = 1;
    wifstream file(filename.c_str());
    wstring word;
    wstring linestring;
    wstringstream iss;

    std::locale loc("");
    std::ios::sync_with_stdio(false);
    file.imbue(loc);
    std::wcout.imbue(loc);
    std::wcin.imbue(loc);

    if(file)
    {
        while(getline(file, linestring))
        {
            iss << linestring;
            int wordcount = 1;
            wchar_t nextChar;
            for (int j=0; j<int(linestring.length()); j++)
            {
                nextChar = linestring.at(j);
                if (nextChar == L' ')
                    wordcount++;
            }
            lines->push_back(new int[wordcount+3]);
            int* linearray = lines->back();
            linearray[0] = wordcount+3;
            linearray[1] = 0;
            linearray[wordcount+2] = 0;
            int j = 2;
            while(getline(iss, word, (wchar_t)' '))
            {
                if(words->left.find(word)==words->left.end())
                {
                    words->insert(wpair(word,i));
                    i++;
                }
                linearray[j] = words->left.at(word);
                j++;
            }
            iss.clear();
        }
    }
    else
    {
        cerr << "File " << filename << " not found!" << endl;
        return false;
    }
    file.close();
    return true;
}

wmap* Corpus::getEwords()
{
    return words->getEwords();
}

wmap* Corpus::getFwords()
{
    return words->getFwords();
}

vector<int*>* Corpus::getElines()
{
    return &elines;
}

vector<int*>* Corpus::getFlines()
{
    return &flines;
}

