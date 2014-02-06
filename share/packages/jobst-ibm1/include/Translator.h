#ifndef TRANSLATOR_H
#define TRANSLATOR_H

#include <vector>

#include "Bigram.h"
#include "Words.h"
#include "Length.h"
#include "Dictionary.h"

class Translator
{
    public:
        Translator(std::string e, std::string f, std::string base);
        wstring translate(std::wstring sentence);
        wstring splitTranslate(wstring sentence);
        void startTranslating();
        virtual ~Translator();
    protected:
    private:
        std::vector<int>* generateRandomSentence(std::vector<int>* fromSentence);
        std::vector<int>* generateWordToWordTranslation(std::vector<int>* fromSentence);
        float evaluate(vector<int>* fromSentence, vector<int>* toSentence);
        std::vector<int>* parseSentence(std::wstring sentence);
        void swapWords(std::vector<int>* sentence, int pos1, int pos2);
        void insertWord(std::vector<int>* sentence, int pos);
        //void insertGoodWord(std::vector<int>* sentence, int pos);
        void deleteWord(std::vector<int>* sentence, int pos);
        wstring sentenceToString(std::vector<int>* sentence);
        Bigram* b;
        Words* w;
        Length* l;
        Dictionary* d;
};

#endif // TRANSLATOR_H
