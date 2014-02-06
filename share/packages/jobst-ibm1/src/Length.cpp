#include "../include/Length.h"

#include <vector>
#include <fstream>
#include <iostream>

#include "../include/utility.h"
#include "../include/Timer.h"
#include "../include/exceptions.h"

#include <boost/lexical_cast.hpp>
#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/copy.hpp>
#include <boost/iostreams/filter/zlib.hpp>
//#include "Model.cpp"

using namespace boost;

string Length::ending = "length";

Length::Length(Corpus* co) : Model<Length>(co)
{
    size1 = LMAX;
    size2 = LMAX;
    systemMessage("Creating Length Model");
    model = new float*[LMAX];
    float norm[LMAX];
    for(int i = 0; i < LMAX; i++)
    {
        model[i] = new float[LMAX];
        norm[i] = SMOOTH * LMAX;
        for(int j = 0; j < LMAX; j++)
        {
            model[i][j] = SMOOTH;
        }
    }

    vector<int*>::iterator eIt;
    vector<int*>::iterator fIt;

    int m = 0;
    int n = 0;

    for(eIt = co->getElines()->begin(), fIt = co->getFlines()->begin(); eIt != co->getElines()->end(), fIt != co->getFlines()->end(); eIt++, fIt++)
    {
        m = (*eIt)[0];
        n = (*fIt)[0];
        if(m < LMAX && n < LMAX)
        {
            model[n][m] += 1.0;
            norm[m] += 1.0;
        }
    }

    for(int i = 0; i < LMAX; i++)
    {
        for(int j = 0; j < LMAX; j++)
        {
            model[i][j] = model[i][j]/norm[j];
        }
    }
    saveModel();
}

float** Length::getLengthmodel()
{
    return model;
}
