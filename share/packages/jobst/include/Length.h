#ifndef LENGTH_H
#define LENGTH_H

#include "Corpus.h"
#include "Model.h"

class Length : public Model<Length>
{
    public:
        Length(Corpus* co);
        Length(string e, string f, string base) : Model<Length>(e,f,base){};
        float** getLengthmodel();
        static string ending;
    protected:
    private:
};

#endif // LENGTH_H
