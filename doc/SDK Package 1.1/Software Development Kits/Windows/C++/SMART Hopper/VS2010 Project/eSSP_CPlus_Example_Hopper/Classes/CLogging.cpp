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
		m_LogStream << "Started logging SMART Hopper comms...\n" << endl;

	m_PacketCounter = 0;
}

CLogging::~CLogging()
{
	if (m_LogStream.is_open())
		m_LogStream.close();
}

void CLogging::UpdateLog(const SSP_COMMAND_INFO* info)
{
    char len, *buffer;
	buffer = new char[32];

    // NON-ENCRPYTED
    // transmission
    m_LogStream << "\nNo Encryption\nSent Packet #";
	m_LogStream << itoa(m_PacketCounter, buffer, 10);
	len = info->PreEncryptTransmit.PacketData[2];
	m_LogStream << "\nCommand Name: ";
	m_LogStream << info->CommandName;
    m_LogStream << "\nLength: ";
	m_LogStream << itoa(len, buffer, 10);
    m_LogStream << "\nSync: ";
	m_LogStream << itoa((info->PreEncryptTransmit.PacketData[1] >> 7), buffer, 10);
    m_LogStream << "\nData: ";
	for (int i = 3; i < len+3; i++)
	{
		m_LogStream << itoa(info->PreEncryptTransmit.PacketData[i], buffer, 16);
		m_LogStream << " ";
	}
    m_LogStream << "\n";

    // received
    m_LogStream << "\nReceived Packet #";
	m_LogStream << itoa(m_PacketCounter, buffer, 10);
	len = info->PreEncryptRecieve.PacketData[2];
    m_LogStream << "\nLength: ";
	m_LogStream << itoa(len, buffer, 10);
    m_LogStream << "\nSync: ";
	m_LogStream << itoa((info->PreEncryptRecieve.PacketData[1] >> 7), buffer, 10);
    m_LogStream << "\nData: ";
	for (int i = 3; i < len+3; i++)
	{
		m_LogStream << itoa(info->PreEncryptRecieve.PacketData[i], buffer, 16);
		m_LogStream << " ";
	}
    m_LogStream << "\n";

    m_PacketCounter++;
	delete[] buffer;
}