#ifndef UTILITY_H_INCLUDED
#define UTILITY_H_INCLUDED

#include "Timer.h"
#include "Dictionary.h"
#include "Words.h"

#include <iostream>

void lookup(Dictionary* d, Words* w);

template <class T>
void systemMessage(T text)
{
    std::wcout << Timer::getElapsedTime() << " | " << text << std::endl;
}


#endif // UTILITY_H_INCLUDED
