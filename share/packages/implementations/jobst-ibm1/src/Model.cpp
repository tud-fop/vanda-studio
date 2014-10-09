#include "../include/Model.h"
#include "../include/Length.h"
#include "../include/Dictionary.h"
#include "../include/Bigram.h"

#include <iostream>
#include <fstream>

#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/copy.hpp>
#include <boost/iostreams/filter/zlib.hpp>
#include <boost/iostreams/stream.hpp>

using namespace std;
using namespace boost;

template <typename T> Model<T>::Model(Corpus* co)
{
    this->e = co->getE();
    this->f = co->getF();
    this->base = co->getBase();
}

template <typename T> Model<T>::Model(string e, string f, string base)
{
    this->e = e;
    this->f = f;
    this->base = base;
    loadModel();
}

template <typename T> Model<T>::~Model()
{
    for(int i=0; i<size1; i++)
    {
        delete[] model[i];
    }
    delete[] model;
}

template <typename T> void Model<T>::saveModel()
{
    string filename = base + "_" + f + "_" + e + "." + fileEnding;
    ofstream outfile(filename.c_str(),ios_base::out | ios_base::binary);
    if(!outfile)
        throw wrongFileFormat;

    stringstream out;
    iostreams::filtering_streambuf<iostreams::input> outbuf;
    outbuf.push(iostreams::zlib_compressor());

    out.write((char *) &size1, sizeof(int));
    out.write((char *) &size2, sizeof(int));
    for(int i = 0; i<size1; i++)
    {
        out.write((char *) model[i], size2*sizeof(float));
    }

    outbuf.push(out);
    iostreams::copy(outbuf, outfile);

    outfile.close();
}

template <typename T> void Model<T>::loadModel()
{
    string filename = base + "_" + f + "_" + e + "." + fileEnding;
    ifstream infile(filename.c_str(),ios_base::in | ios_base::binary);
    if(!infile)
        throw wrongFileFormat;

    // zlib decompression
    iostreams::filtering_streambuf<iostreams::input> inbuf;
    inbuf.push(iostreams::zlib_decompressor());
    inbuf.push(infile);

    stringstream in;
    boost::iostreams::copy(inbuf, in);

    in.read((char *) &size1, sizeof(int));
    in.read((char *) &size2, sizeof(int));
    model = new float*[size1];
    for(int i = 0; i<size1; i++)
    {
        model[i] = new float[size2];
        in.read((char *) model[i], size2*sizeof(float));
    }
    infile.close();
}

template class Model<Length>;
template class Model<Dictionary>;
template class Model<Bigram>;
