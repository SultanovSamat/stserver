#ifndef C_PAYOUT_H
#define C_PAYOUT_H

#include <Windows.h>
#include <string>
#include <iostream>
#include <time.h>
#include "SSPInclude.h"
#include "Commands.h"
#include "CLogging.h"

using namespace std;

#define THREAD_LOCK if(!WaitForRelease())return false;
#define THREAD_UNLOCK Release();

class CPayout
{
	// SSP library variables
    SSP_COMMAND* cmd;
    SSP_KEYS* keys;
    SSP_FULL_KEY* sspKey;
    SSP_COMMAND_INFO* info;

	// Function pointers
	LPFNDLLFUNC1 OpenPort;
	LPFNDLLFUNC2 ClosePort;
	LPFNDLLFUNC3 SSPSendCommand;
	LPFNDLLFUNC4 InitiateSSPHostKeys;
	LPFNDLLFUNC5 CreateSSPHostEncryptionKey;

    // Variable declarations
    CLogging* m_Log;
    int m_NumberOfChannels;
    int m_ValueMultiplier, m_ProtocolVersion;
	ostream* m_Output;
	SUnitData* m_UnitData;
	unsigned char m_UnitType;
	unsigned char* m_PollResponse;
	unsigned char m_PollResponseLength;

	// Threading variables
	bool m_ThreadLock;

public:
    // constructor
    CPayout()
    {
        cmd = new SSP_COMMAND();
        keys = new SSP_KEYS();
        sspKey = new SSP_FULL_KEY();
        info = new SSP_COMMAND_INFO();

		m_ThreadLock = false; // Instance not locked at start

		// Create ssp library functions
		InitialiseLibrary();
            
        m_Log = new CLogging();
        m_NumberOfChannels = 0;
        m_ValueMultiplier = 1;
		m_ProtocolVersion = 0;
		m_UnitData = null;
		m_UnitType = 0x06; // 0x06 is SMART Payout - this is checked in Setup Request
		m_PollResponseLength = 0;
		m_PollResponse = new unsigned char[255];
		
    }

	// destructor
	~CPayout()
	{
		if (m_PollResponse)
			delete[] m_PollResponse;
		if (m_UnitData)
			delete[] m_UnitData;
		delete m_Log;
		delete info;
		delete sspKey;
		delete keys;
		delete cmd;
	}

    // Inline Variable Access

	// access to ssp handles
    inline SSP_COMMAND* GetCommandStructure(){ return cmd; }
	inline SSP_COMMAND_INFO* GetInfoStructure(){ return info; }

    // access to number of channels
    inline int GetNumChannels(){ return m_NumberOfChannels; }

    // access to value multiplier
    inline int GetValueMultiplier(){ return m_ValueMultiplier; }

	// access to the data of the dataset
	inline SUnitData* GetUnitData(){ return m_UnitData; }

	// access to the type of unit
	inline unsigned char GetUnitType(){ return m_UnitType; }

    // get a channel value
	// Critical Section: NO
    inline int GetChannelValue(int channelNum)
	{
		if (channelNum > 0 && channelNum <= m_NumberOfChannels)
		{
			for (int i = 0; i < m_NumberOfChannels; ++i)
			{
				if (m_UnitData[i].Channel == channelNum)
					return m_UnitData[i].Value;
			}
		}
		return 0;
	}

	// get a channel currency
	// Critical Section: NO
    inline char* GetChannelCurrency(int channelNum)
	{
		if (channelNum > 0 && channelNum <= m_NumberOfChannels)
		{
			for (int i = 0; i < m_NumberOfChannels; ++i)
			{
				if (m_UnitData[i].Channel == channelNum)
					return m_UnitData[i].Currency;
			}
		}
		return 0;
	}


private:
	// Internal SSP lib functions
	bool InitialiseLibrary();
	char* FormatComPort(string* comPort);
	// Internal Command Function definitions
	bool NegotiateKeys();
	bool SetupRequest();
	bool SetProtocolVersion(char pVersion);
	bool SendCommand();
	string DecodeResponseStatus(char responseByte);
	char* GetCommandName(char commandByte);
	bool CheckGenericResponses();
	void UpdateData();

	// External functions to be called by the main program
public:
	inline void SetOutputStream(ostream* o){WaitForRelease(); m_Output = o; m_Output->precision(2); *m_Output << fixed; THREAD_UNLOCK}
	bool OpenComPort(char portNum);
	inline void CloseComPort(){WaitForRelease(); ClosePort(); THREAD_UNLOCK}
	bool ConnectToPayout(const SSP_COMMAND& command, int protocolVersion, int attempts);
	bool EnableValidator();
	bool DisableValidator();
	bool SetInhibits();
	bool EnablePayout();
	bool DisablePayout();
	bool Payout(int coinValue, const string& currency);
	bool PayoutByDenomination(int numDenoms, unsigned char* denomData);
	bool QueryRejection();
	bool GetNoteRecycling(unsigned int note, const string& currency, bool* isRecycling);
	bool OutputLevelInfo();
	bool SendNoteToCashbox(int value, const string& currency);
	bool SendNoteToStorage(int value, const string& currency);
	bool Empty();
	bool SMARTEmpty();
	bool GetCashboxPayoutOpData();
	bool ResetValidator();
	bool DoPoll();	

	// Threading
	bool WaitForRelease();
	void Release();
};

#endif

