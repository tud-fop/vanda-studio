#ifndef WORDS_H
#define WORDS_H

#include <string>
#include <boost/bimap.hpp>

#define FROM false
#define TO true

using namespace std;

typedef boost::bimap< wstring, int > wmap;
typedef wmap::value_type wpair;

class Words
{
    public:
        Words(string e, string f, string base);
        Words(string e, string f, string base, wmap* ewords, wmap* fwords);
        wmap* getEwords();
        wmap* getFwords();
        bool containsWord(bool language, wstring word){ return language ? ewords->left.count(word) == 1 : fwords->left.count(word) == 1; };
        int getIdByString(bool language, wstring word){ return language ? ewords->left.at(word) : fwords->left.at(word); };
        wstring getStringById(bool language, int word){ return language ? ewords->right.at(word) : fwords->right.at(word); };
        virtual ~Words();
    protected:
    private:
        wmap* fwords;
        wmap* ewords;
        bool saveWordMap(string lang, string base, boost::bimap<wstring, int>* words);
        bool loadWordMap(string lang, string base, boost::bimap<wstring, int>* words);
};

#endif // WORDS_H
