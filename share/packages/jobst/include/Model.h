/**
This class is a parent class for all the models which store their data in a 2D-Array.
**/

#ifndef MODEL_H
#define MODEL_H

#include "Corpus.h"
#include "exceptions.h"

// set the smoothing factor
#define SMOOTH 0.0005
#define LMAX 50

template <typename T>
class Model
{
    public:
        // constructor for training
        Model(Corpus* co);
        // constructor for loading
        Model(string e, string f, string base);
        //virtual ~Model();
        float** getModel() { return model; }
        float getValue(int i, int j) { return model[i][j]; }
        ~Model();
    protected:
        void loadModel();
        void saveModel();
        // 2D-array which contains the model data
        float** model;
        int size1;
        int size2;
    private:
        string e, f, base;
        static string const fileEnding;
};

// let each derived class store its own file ending (curiously recurring template pattern (CRTP))
template <typename T> string const Model<T>::fileEnding(T::ending);

#endif // MODEL_H
