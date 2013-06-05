#include "../include/Bigram.h"

#include <sstream>
#include <iostream>
#include <fstream>

#include <boost/lexical_cast.hpp>

#include "../include/exceptions.h"
#include "../include/Timer.h"
#include "../include/utility.h"

using namespace boost;

string Bigram::ending = "bigram";

Bigram::Bigram(Corpus* co) : Model<Bigram>(co)
{
    size1 = co->getEwords()->size();
    size2 = size1;
    model = new float*[size1];
    for (int i = 0; i < size1; i++)
    {
        model[i] = new float[size2];
        for (int j = 0; j < size2; j++)
        {
            model[i][j] = SMOOTH;
        }
    }

    int word;
    int prevword;
    int count[size1];
    for(int i=0; i<size1; i++)
    {
        count[i] = SMOOTH*size1;
    }
    vector<int*>* lines = co->getElines();
    systemMessage("Counting Bigrams");
    for(vector<int*>::iterator it = lines->begin(); it != lines->end(); it++)
    {
        prevword = (*it)[1];
        for(int i=2; i< (*it)[0]; i++)
        {
            word = (*it)[i];
            count[prevword]++;
            model[prevword][word]++;
            prevword = word;
        }
    }
    float* ptr;
    systemMessage("Normalizing Bigram Model");
    for(int i = 0; i<size1; i++)
    {
        for(int j = 0; j<size2; j++)
        {
            ptr = &model[i][j];
            if(*ptr != 0.0)
            {
                (*ptr) = (*ptr)/count[i];
            }
        }
    }
    saveModel();
}
