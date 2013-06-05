#include "../include/Words.h"

#include <fstream>

#include <boost/lexical_cast.hpp>
using namespace boost;

Words::Words(string e, string f, string base, wmap* ewords, wmap* fwords)
{
    this->ewords = ewords;
    this->fwords = fwords;
    saveWordMap(e, base, ewords);
    saveWordMap(f, base, fwords);
}

Words::Words(string e, string f, string base)
{
    ewords = new wmap;
    fwords = new wmap;
    loadWordMap(e, base, ewords);
    loadWordMap(f, base, fwords);
}

Words::~Words()
{

}


bool Words::saveWordMap(string lang, string base, boost::bimap<wstring, int>* words)
{
    string filename = base + "_" + lang + ".words";
    wofstream file(filename.c_str());
    file.imbue(std::wcout.getloc());
    if(file)
    {
        for(boost::bimap<wstring, int>::right_map::const_iterator it=words->right.begin(); it != words->right.end(); it++)
        {
            file << (*it).first << "|" << (*it).second << endl;
        }
    }
    else
    {
        cerr << "Could not save to file " << filename << "!" << endl;
        return false;
    }
    file.close();
    return true;
}

bool Words::loadWordMap(string lang, string base, boost::bimap<wstring, int>* words)
{
    string filename = base + "_" + lang + ".words";
    wifstream file(filename.c_str());
    wstring word;
    wstring linestring;
    wstringstream iss;

    std::locale loc("");
    std::locale::global(loc);
    std::ios::sync_with_stdio(false);
    file.imbue(loc);
    std::wcout.imbue(loc);
    std::wcin.imbue(loc);

    if(file)
    {
        while(getline(file, linestring))
        {
            int pos = linestring.find(L"|");
            int i = lexical_cast<int>(linestring.substr(0,pos));
            wstring word = linestring.substr(pos+1);
            words->insert(wpair(word,i));
        }
    }
    file.close();
    return true;
}

wmap* Words::getEwords()
{
    return ewords;
}
wmap* Words::getFwords()
{
    return fwords;
}
