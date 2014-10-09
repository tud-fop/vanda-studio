#include "../include/utility.h"

#include <iostream>
#include <iomanip>

using namespace std;

void lookup(Dictionary* d, Words* w)
{
    while(true)
    {
        wstring x;
        wcout << "Enter a word of the to-language: ";
        wcin >> x;
        if(wcin.eof())
        {
            wcout << endl;
            break;
        }
        if(!w->containsWord(TO, x))
        {
            wcout << "Word " << x << " not found!" << endl;
            continue;
        }
        map<float, int>* translations = d->getNBestTranslations(w->getIdByString(TO, x), 10);
        map<float,int>::reverse_iterator it;
        for(it = translations->rbegin(); it != translations->rend(); it++)
            wcout << fixed << setprecision (3) << right << setw(22) << w->getStringById(FROM, it->second) << "   " << it->first << endl;
        wcout << endl;
        delete translations;
        wcout << endl;
    }
}
