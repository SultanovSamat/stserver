#include "CLogging.h"

CLogging::CLogging()
{
	SYSTEMTIME time;
	GetLocalTime(&time);

	// Turn time into a filename
	string filename = "";
	char* c = new char[6];
	_itoa(time.wHour, c, 10);
	filename.append(c);
	filename += "h ";
	_itoa(time.wMinute, c, 10);
	filename.append(c);
	filename += "m ";
	_itoa(time.wSecond, c, 10);
	filename.append(c);
	filename += "s.txt";

	// Create a logs dir
	_mkdir("Logs\\");
	string dir = "Logs\\";
	_itoa(time.wDay, c, 10);
	dir.append(c);
	dir += ".";
	_itoa(time.wMonth, c, 10);
	dir.append(c);
	dir += ".";
	_itoa(time.wYear, c, 10);
	dir.append(c);
	_mkdir(dir.c_str());
	dir += "\\";
	dir += filename;
	m_LogStream.open(dir, ios::out);

	delete[] c;

	if (m_LogStream.is_open())
		m_LogStream << "Started logging Note Float comms...\n" << endl;

	m_PacketCounter = 0;
}

CLogging::~CLogging()
{
	if (m_LogStream.is_open())
		m_LogStream.close();
}

void CLogging::UpdateLog(const SSP_COMMAND_INFO* info)
{
    string logText = "";
    char len, *buffer;
	buffer = new char[32];

    // NON-ENCRPYTED
    // transmission
    logText += "\nNo Encryption\nSent Packet #";
	logText += itoa(m_PacketCounter, buffer, 10);
	len = info->PreEncryptTransmit.PacketData[2];
    logText += "\nLength: ";
	logText += itoa(len, buffer, 10);
    logText += "\nSync: ";
	logText += itoa((info->PreEncryptTransmit.PacketData[1] >> 7), buffer, 10);
    logText += "\nData: ";
	for (int i = 3; i < len+3; i++)
	{
		logText += itoa(info->PreEncryptTransmit.PacketData[i], buffer, 16);
		logText += " ";
	}
    logText += "\n";

    // received
    logText += "\nReceived Packet #";
	logText += itoa(m_PacketCounter, buffer, 10);
	len = info->PreEncryptRecieve.PacketData[2];
    logText += "\nLength: ";
	logText += itoa(len, buffer, 10);
    logText += "\nSync: ";
	logText += itoa((info->PreEncryptRecieve.PacketData[1] >> 7), buffer, 10);
    logText += "\nData: ";
	for (int i = 3; i < len+3; i++)
	{
		logText += itoa(info->PreEncryptRecieve.PacketData[i], buffer, 16);
		logText += " ";
	}
    logText += "\n";

	m_LogStream << logText.c_str() << endl;
    m_PacketCounter++;
	delete[] buffer;
}