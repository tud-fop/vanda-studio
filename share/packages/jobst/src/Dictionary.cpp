#include "../include/Dictionary.h"
#include "../include/kbhit.h"

#include <fstream>
#include <omp.h>

#include "../include/exceptions.h"
#include "../include/Words.h"
#include "../include/Timer.h"
#include "../include/utility.h"

#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/copy.hpp>
#include <boost/iostreams/filter/zlib.hpp>

using namespace std;
using namespace boost;

typedef vector< wstring > splitVec;

string Dictionary::ending = "dictionary";

Dictionary::Dictionary(Corpus* co) : Model<Dictionary>(co)
{
    systemMessage("Creating dictionary");

    // create dictionary parts t and c: f -> e
    size2 = co->getEwords()->size();
    size1 = co->getFwords()->size();
    int esize = size2;
    int fsize = size1;
    model = new float*[fsize];
    float** c;
    c = new float*[fsize];
    for (int i = 0; i < fsize; i++)
    {
        model[i] = new float[esize];
        c[i] = new float[esize];
        for (int j = 0; j < esize; j++)
        {
            c[i][j] = SMOOTH;
            model[i][j] = 0.0005;
        }
    }

    int loops = 0;
    int** fit;
    int** eit;
    vector<wstring>::iterator ewit;
    vector<wstring>::iterator fwit;
    splitVec ew;
    splitVec fw;
    vector<int*>* flines = co->getFlines();
    vector<int*>* elines = co->getElines();
    float normalize[esize];
    float norm = SMOOTH*fsize;
    while (!kbhit())
    {
        for(int i = 0; i<esize; i++)
        {
            normalize[i] = norm;
        }
        loops++;
        stringstream s;
        s << loops << ". loop";
        systemMessage(s.str().c_str());

#pragma omp parallel for
        for(unsigned int x=0; x<elines->size(); x++)
        {
            fit = &(flines->at(x));
            eit = &(elines->at(x));
            for(int i=1; i<(*fit)[0]; i++)
            {
                float s = 0.0;
                float* tpointer = model[(*fit)[i]];
                float* cpointer = c[(*fit)[i]];
                for(int j=1; j<(*eit)[0]; j++)
                {
                    s += tpointer[(*eit)[j]];
                }

                for(int j=1; j<(*eit)[0]; j++)
                {
                    float tmp = tpointer[(*eit)[j]]/s;
                    cpointer[(*eit)[j]] += tmp;
                    normalize[(*eit)[j]] += tmp;
                }
            }
        }
#pragma omp parallel for
        for(int i = 0; i<fsize; i++)
        {
            for(int j = 0; j<esize; j++)
            {
                model[i][j] = c[i][j]/normalize[j];
                c[i][j] = SMOOTH;
            }

        }
    }
    for (int j = 0; j < fsize ; j++)
    {
        delete [] c[j];
    }
    delete [] c;
    saveModel();
}

map<float, int>* Dictionary::getNBestTranslations(int word, unsigned int n)
{
    float last = 0.0;
    map<float, int>* tr = new map<float,int>();
    for(int i=0; i<size1; i++)
    {
        float p = lookup(i,word);
        if(tr->size()<n)
        {
            tr->insert(pair<float,int>(p, i));
            if(tr->size()==n)
                last = tr->begin()->first;
        }
        else if(p > last)
        {
            tr->insert(pair<float,int>(p, i));
            if(tr->size()>n)
                tr->erase(last);
            last = tr->begin()->first;
        }
    }
    return tr;
}

int Dictionary::getBestTranslation(int f)
{
    float last = 0.0;
    int tr = 0;
    for(int i=0; i<size2; i++)
    {
        float p = lookup(f,i);
        if(p > last)
        {
            tr=i;
            last = p;
        }
    }
    return tr;
}
