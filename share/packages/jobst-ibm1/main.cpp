#include <boost/program_options.hpp>

using namespace boost;
namespace po = boost::program_options;

#include <iostream>
#include <algorithm>
#include <iterator>
#include <exception>
#include <locale>
#include <string>
using namespace std;

#include "include/Corpus.h"
#include "include/Bigram.h"
#include "include/Dictionary.h"
#include "include/Words.h"
#include "include/Translator.h"
#include "include/exceptions.h"
#include "include/Timer.h"
#include "include/utility.h"


// A helper function to simplify the main part.
template<class T>
ostream& operator<<(ostream& os, const vector<T>& v)
{
    copy(v.begin(), v.end(), ostream_iterator<T>(cout, " "));
    return os;
}

int main(int ac, const char* av[])
{
    po::options_description desc("Allowed options");
    try
    {
        desc.add_options()
        ("help,h", "produce help message")
        ("base", "base name of corpus")
        ("e", "extension of target part of corpus")
        ("f", "extension of foreign part of corpus")
        ("action", "action to perform: 'train', 'lookup', 'decode'")
        ;

        po::positional_options_description p;
        p.add("base", 1).add("e", 1).add("f", 1).add("action", 1);

        po::variables_map vm;
        po::store(po::command_line_parser(ac, av).options(desc).positional(p).run(), vm);
        po::notify(vm);

        if (vm.count("help"))
        {
            cout << "Usage: translate [OPTION] BASE E F ACTION\n";
            cout << desc << "\n";
            return 0;
        }

        if (vm.count("base") && vm.count("e") && vm.count("f") && vm.count("action"))
        {
            string base = vm["base"].as<string>();
            string e = vm["e"].as<string>();
            string f = vm["f"].as<string>();
            // cout << "Target part of corpus: " << base << "." << e << "\n";
            // cout << "Foreign part of corpus: " << base << "." << f  << "\n";
            // cout << "Action: " << vm["action"].as<string>() << "\n";
            if(vm["action"].as<string>() == "train")
            {
                Timer::startTimer();
                systemMessage("Training Dictionary");
                Corpus *c = new Corpus(e, f, base);
                delete c;
                systemMessage("Training successfully finished");
            }
            else if(vm["action"].as<string>() == "lookup")
            {
                cout << "Lookup" << endl;
                Dictionary* d = new Dictionary(e,f,base);
                Words* w = new Words(e,f,base);
                lookup(d,w);
            }
            else if(vm["action"].as<string>() == "translate" || vm["action"].as<string>() == "decode")
            {
                Translator* t = new Translator(e,f,base);
                t->startTranslating();
                delete t;
            }
            else
                throw unknownAction;
        }
        else
        {
            throw notEnoughOptions;
        }
    }
    catch(std::exception& e)
    {
        cerr << "Error: "<< e.what() << endl << endl;
        cout << "Usage: translate [OPTION] BASE E F ACTION" << endl;;
        cout << desc << endl;
        return 1;
    }

    return 0;
}
