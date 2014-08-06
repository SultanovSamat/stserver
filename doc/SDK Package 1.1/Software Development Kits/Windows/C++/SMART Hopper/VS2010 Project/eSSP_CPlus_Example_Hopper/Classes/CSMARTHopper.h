#ifndef C_HOPPER_H
#define C_HOPPER_H

#include <Windows.h>
#include <string>
#include <iostream>
#include <iomanip>
#include <time.h>
#include "SSPInclude.h"
#include "Commands.h"
#include "CLogging.h"

using namespace std;

#define THREAD_LOCK if(!WaitForRelease())return false;
#define THREAD_UNLOCK Release();

class CHopper
{
	// ssp library variables
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

    // variable declarations
    CLogging* m_Log; // The logging class, logs are saved in the directory of the executable under their own dir
    int m_NumberOfChannels; // The number of channels the unit has in its dataset, this is determined in setup request
	ostream* m_Output; // The output stream that should be used
	SUnitData* m_UnitData; // The dataset data, this contains an instance of the SUnitData class for each channel
	unsigned char m_UnitType; // The type of the unit, this is determined in setup request
	// This controls access to the entire instance of the class. If the instance is threadlocked by a thread then no other
	// threads should be allowed access.
	volatile bool m_ThreadLock; 
	// The below variables allow the poll response to be stored so it can't be overwritten by another method during a poll
	// parse
	unsigned char* m_PollResponse;
	unsigned char m_PollResponseLength;

public:
    // constructor
    CHopper()
    {
        cmd = new SSP_COMMAND();
        keys = new SSP_KEYS();
        sspKey = new SSP_FULL_KEY();
        info = new SSP_COMMAND_INFO();

		m_ThreadLock = false;

		// Create ssp library functions
		InitialiseLibrary();
            
        m_Log = new CLogging();
        m_NumberOfChannels = 0;
		m_UnitData = null;
		m_UnitType = 0x03; // This is checked in setup request, 0x03 is SMART Hopper type
		
		m_Output = null;
		m_PollResponseLength = 0;
		m_PollResponse = new unsigned char[255];
    }

	// destructor
	~CHopper()
	{
		WaitForRelease();
		if (m_PollResponse)
			delete[] m_PollResponse;
		if (m_UnitData)
			delete[] m_UnitData;
		delete m_Log;
		delete info;
		delete sspKey;
		delete keys;
		delete cmd;
		THREAD_UNLOCK
	}

    // Inline Variable Access

	// access to ssp handles
    inline SSP_COMMAND* GetCommandStructure(){ return cmd; }
	inline SSP_COMMAND_INFO* GetInfoStructure(){ return info; }

    // access to number of channels
    inline int GetNumChannels(){ return m_NumberOfChannels; }

	// access to the unit type
	inline unsigned char GetUnitType(){ return m_UnitType; }

	// access to the dataset data
	inline SUnitData* GetUnitData(){ return m_UnitData; }

    // get a channel value
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
    inline string GetChannelCurrency(int channelNum)
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
	// Internal functions that do not need to be exposed
private:
	// Internal SSP lib functions
	bool InitialiseLibrary();
	char* FormatComPort(string* comPort);
	// Internal Command Function definitions
	bool OpenComPort(char portNum);
	bool NegotiateKeys();
	bool SetupRequest();
	bool SetProtocolVersion(char pVersion);
	bool SendCommand();
	string DecodeResponseStatus(char responseByte);
	char* GetCommandName(char commandByte);
	bool CheckGenericResponses();
	int ConvertBytesToInt32(unsigned char* bytes, unsigned int index);

	// External functions to be called by the main program
public:
	inline void SetOutputStream(ostream* o)
	{
		if (!WaitForRelease())
			return; 
		m_Output = o; m_Output->precision(2); *m_Output << fixed;
		THREAD_UNLOCK
	}
	inline void CloseComPort(){ ClosePort(); }
	bool ConnectToHopper(const SSP_COMMAND& commandStructure, int protocolVersion, int attempts);
	bool EnableValidator();
	bool DisableValidator();
	bool Payout(int amount, const string& currency);
	bool PayoutByDenomination(int numDenoms, unsigned char* dataArray);
	bool FloatByDenomination(int numDenoms, unsigned char* dataArray);
	bool GetCoinRecycling(unsigned int value, const string& currency, bool* isRecycling);
	bool SendCoinToCashbox(int value, const string& currency);
	bool SendCoinToStorage(int value, const string& currency);
	bool GetCoinLevel(int value, const string& currency, int* returnAmount);
	bool SetCoinLevel(int value, const string& currency, int level);
	bool OutputLevelInfo();
	bool SetCoinMechInhibits();
	bool Empty();
	bool SMARTEmpty();
	bool GetCashboxPayoutOpData();
	bool ResetValidator();
	bool UpdateData();
	bool DoPoll();	

	// Threading
	bool WaitForRelease();
	void Release();
};

#endif

