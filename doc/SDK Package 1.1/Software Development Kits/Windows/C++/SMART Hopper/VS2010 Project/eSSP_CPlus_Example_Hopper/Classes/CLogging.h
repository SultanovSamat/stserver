#ifndef C_LOGGING_H
#define C_LOGGING_H

#include <Windows.h>
#include <iostream>
#include <fstream>
#include <direct.h>

#include "SSPInclude.h"

using namespace std;

class CLogging
{
	ofstream m_LogStream;
	unsigned int m_PacketCounter;
public:
	CLogging();
	~CLogging();

	void UpdateLog(const SSP_COMMAND_INFO* info);
};

#endif