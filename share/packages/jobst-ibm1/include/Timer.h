#ifndef TIMER_H
#define TIMER_H

#include "boost/date_time/posix_time/posix_time_types.hpp"
#include "boost/date_time/posix_time/time_formatters.hpp"

class Timer
{
    public:
        Timer();
        static void startTimer();
        static std::wstring getElapsedTime();
        virtual ~Timer();
    protected:
    private:
        static boost::posix_time::ptime start;
};

#endif // TIMER_H
