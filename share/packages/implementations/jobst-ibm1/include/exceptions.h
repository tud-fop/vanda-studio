#ifndef EXC_H_INCLUDED
#define EXC_H_INCLUDED

#include <exception>

// Exceptions for the case where not enough options are specified
class notEnoughOptionsException: public std::exception
{
    virtual const char* what() const throw()
    {
        return "not enough options specified";
    }
};
static notEnoughOptionsException notEnoughOptions = notEnoughOptionsException();

class unknownActionException: public std::exception
{
    virtual const char* what() const throw()
    {
        return "unknown action";
    }
};
static unknownActionException unknownAction = unknownActionException();

class wrongFileFormatException: public std::exception
{
    virtual const char* what() const throw()
    {
        return "wrong file format or file not found";
    }
};
static wrongFileFormatException wrongFileFormat = wrongFileFormatException();

#endif // EXC_H_INCLUDED
