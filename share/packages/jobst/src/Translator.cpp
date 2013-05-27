#include "../include/Translator.h"
#include "../include/utility.h"

//#include <io>
#include <fstream>
#include <cmath>
#include <cstdlib>
#include <ctime>

using namespace std;

Translator::Translator(string e, string f, string base)
{
    b = new Bigram(e, f, base);
    w = new Words(e, f, base);
    d = new Dictionary(e, f, base);
    l = new Length(e, f, base);
}

Translator::~Translator()
{
    delete b;
    delete w;
    delete d;
    delete l;
}

wstring Translator::translate(wstring sentence)
{
    srand ( time(NULL) );
    vector<int>* ps = parseSentence(sentence); // foreign sentence
    vector<vector<int>*> sentenceList;

    for(int t=0; t<4; t++)
    {
        vector<int>* translation = generateWordToWordTranslation(ps); // own sentence
        float value = evaluate(ps, translation);
        // modify sentence
        for(int i=3; i<3000000; i++)
        {
            vector<int>* temp = new vector<int>(*translation);
            for(int j=0; j<rand()%5+1; j++)
            {
                int var = rand()%3;
                switch(var)
                {
                case 0 :
                    swapWords(temp, rand()%temp->size(), rand()%temp->size());
                    break;
                case 1 :
                    insertWord(temp, rand()%temp->size());
                    break;
                case 2 :
                    if(temp->size()>1)
                        deleteWord(temp, rand()%temp->size());
                    break;
                default :
                    break;
                }
            }
            float vtemp = evaluate(ps, temp);
            if(vtemp>value)
            {
                {
                    value = vtemp;
                    delete translation;
                    translation = new vector<int>(*temp);
                }
            }
            else
            {
                delete temp;
            }
        }
        sentenceList.push_back(translation);
    }

    float value = -1000.0;
    vector<int> translation;
    for(vector<vector<int>*>::iterator it = sentenceList.begin(); it!= sentenceList.end(); it++)
    {
        if(evaluate(ps,*(it))>value)
        {
            translation = *(*(it));
            value = evaluate(ps, &translation);
        }
    }
    //if(translation != 0)
    //    wcout << value << endl << sentenceToString(translation) << endl << endl;
    delete ps;
    wstring transl = sentenceToString(&translation);
    return transl;
}

vector<int>* Translator::parseSentence(wstring sentence)
{
    wstringstream iss;
    iss << sentence;
    wstring word;
    vector<int>* ps = new vector<int>();
    while(getline(iss, word, (wchar_t)' '))
    {
        if(w->containsWord(FROM, word))
            ps->push_back(w->getIdByString(FROM, word));
        else
        {
            //wcerr << word << endl;
            throw wstring(word);
        }
    }
    return ps;
}

float Translator::evaluate(vector<int>* fromSentence, vector<int>* toSentence)
{
    vector<int>::iterator fIt;
    vector<int>::iterator eIt;

    // calculate dictionary values
    float dictValue = 0.0;
    for(fIt= fromSentence->begin(); fIt != fromSentence->end(); fIt++)
    {
        float temp = 0.0;
        for(eIt = toSentence->begin(); eIt != toSentence->end(); eIt++)
        {
            temp += d->getModel()[*fIt][*eIt];
        }
        if(temp <= 0)
            return -1000.0;
        dictValue += log(temp);
    }

    // calculate length values
    int esize = (int) (toSentence->size());
    int fsize = (int) (fromSentence->size());
    float lengthValue;
    if(l->getValue(fsize+2,esize+2) <= 0.0)
        return -1000.0;
    lengthValue = log(l->getValue(fsize+2,esize+2));

    // calculate bigram model
    float bigramValue = 0.0;
    int last = 0;
    for(eIt = toSentence->begin(); eIt != toSentence->end(); eIt++)
    {
        float temp = b->getModel()[last][*eIt];
        if(temp <= 0.0)
            return -1000.0;
        bigramValue+=log(temp);
        last = *eIt;
    }
    if(b->getModel()[last][0] <= 0)
        return -1000;
    bigramValue += 10 * log(b->getModel()[last][0]);
    return 2.5*dictValue+ 0.7*bigramValue + 0.8*(esize+fsize) + 4*lengthValue;
}

vector<int>* Translator::generateRandomSentence(std::vector<int>* fromSentence)
{
    vector<int>* sentence = new vector<int>();
    vector<int>::iterator fIt;
    int word;
    for(int i=rand()%(LMAX-4); i>=0; i--)
    {
        bool ok = false;
        int count = 0;
        do
        {
            count++;
            word = rand()%w->getEwords()->size();
            if(word == 0)
            {
                continue;
            }
            for(fIt= fromSentence->begin(); fIt != fromSentence->end(); fIt++)
            {
                if(d->getModel()[*fIt][word]>0.15)
                {
                    ok = true;
                    break;
                }
            }
        }
        while(!ok&&count<200);
        if(ok)
            sentence->push_back(word);
    }
    return sentence;
}

vector<int>* Translator::generateWordToWordTranslation(std::vector<int>* fromSentence)
{
    vector<int>* toSentence = new vector<int>();
    vector<int>::iterator fIt;
    for(fIt = fromSentence->begin(); fIt != fromSentence->end(); fIt++)
    {
        int word = d->getBestTranslation(*fIt);
        toSentence->push_back(word);
    }
    return toSentence;
}

void Translator::swapWords(std::vector<int>* sentence, int pos1, int pos2)
{
    int temp = (*sentence)[pos1];
    (*sentence)[pos1] = (*sentence)[pos2];
    (*sentence)[pos2] = temp;
}

void Translator::insertWord(std::vector<int>* sentence, int pos)
{
    sentence->insert(sentence->begin()+pos, rand()%w->getEwords()->size());
}

void Translator::deleteWord(std::vector<int>* sentence, int pos)
{
    sentence->erase(sentence->begin()+pos);
}

wstring Translator::sentenceToString(std::vector<int>* sentence)
{
    wstring out;
    for(vector<int>::iterator it = sentence->begin(); it!=sentence->end(); it++)
    {
        out += w->getStringById(TO, *it) + L" ";
    }
    return out;
}

wstring Translator::splitTranslate(wstring sentence)
{
    wstring rest = sentence;
    wstring translation;
    wstring temp;
    while(rest.find(L",") != string::npos)
    {
        int pos = rest.find(L",");
        temp = translate(rest.substr(0,pos) + L",");
        translation += temp.substr(0, temp.size()-3) + L" , ";
        rest = rest.substr(pos+2);
    }
    translation += translate(rest);
    return translation;
}

void Translator::startTranslating()
{
    std::locale loc("");
    std::locale::global(loc);
    std::ios::sync_with_stdio(false);
    std::wcin.imbue(loc);

    if (isatty(fileno(stdin)))
    {
        while(true)
        {
            try
            {
                wcerr << "Please enter the sentence which shall be translated:" << endl;
                wstring sentence;
                getline(wcin,sentence);
                if(wcin.eof())
                {
                    wcout << endl;
                    break;
                }
                wcout << splitTranslate(sentence) <<endl;
            }
            catch(wstring word)
            {
                wcerr << "Word not found: " << word << endl;
            }
        }
    }
    else
    {
        //wcin.unsetf(ios::skipws);
        vector<wstring> sentences;
        while(!wcin.eof())
        {
            wstring sentence;
            getline(wcin,sentence);
            if(!wcin.eof())
                sentences.push_back(sentence);
        }
        for(vector<wstring>::iterator it = sentences.begin(); it != sentences.end(); it++)
        {
            try
            {
                wstring sentence(*it);
                wstring test = sentence;
                wstring translation;
                wcout << sentence << endl;
                translation = splitTranslate(sentence);
                wcout << translation <<endl;
            }
            catch(wstring word)
            {
                wcerr << "Word not found: " << word << endl;
            }
        }
    }
}

