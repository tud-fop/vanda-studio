#include <string>

#include "../include/Timer.h"

using namespace std;
using namespace boost::posix_time;

ptime Timer::start = microsec_clock::local_time();

Timer::Timer()
{

}

Timer::~Timer()
{
    //dtor
}

void Timer::startTimer()
{
    start = microsec_clock::local_time();
}

wstring Timer::getElapsedTime()
{
    return to_simple_wstring(microsec_clock::local_time()-start);
}
