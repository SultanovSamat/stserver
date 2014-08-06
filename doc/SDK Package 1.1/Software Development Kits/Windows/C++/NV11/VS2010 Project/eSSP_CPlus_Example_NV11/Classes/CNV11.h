#ifndef C_NV11_H
#define C_NV11_H

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

class CNV11
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
    CLogging* m_Log;
    int m_NumStackedNotes, m_NumStoredNotes;
    int m_NumberOfChannels;
	SUnitData* m_UnitData;
    int m_ValueMultiplier;
	ostream* m_Output;
	unsigned char m_UnitType;
	volatile bool m_ThreadLock;
	unsigned char m_PollResponseLength;
	unsigned char* m_PollResponse;

public:
    // constructor
    CNV11()
    {
		m_ThreadLock = false;
        cmd = new SSP_COMMAND();
        keys = new SSP_KEYS();
        sspKey = new SSP_FULL_KEY();
        info = new SSP_COMMAND_INFO();

		// Create ssp library functions
		InitialiseLibrary();
            
        m_Log = new CLogging();
		m_UnitData = null;
        m_NumberOfChannels = 0;
        m_ValueMultiplier = 1;
		m_NumStackedNotes = 0;
		m_NumStoredNotes = 0;
		m_UnitData = 0;
		m_PollResponseLength = 0;
		m_PollResponse = new unsigned char[255];
    }

	// destructor
	~CNV11()
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
    inline SSP_COMMAND* GetCommandStructure(){return cmd;}
	inline SSP_COMMAND_INFO* GetInfoStructure(){return info;}

    // access to number of channels
    inline int GetNumChannels(){return m_NumberOfChannels;}

    // access to number of notes stacked
    inline int GetNumNotesStacked(){return m_NumStackedNotes;}

	// access to number of notes stored
    inline int GetNumNotesStored(){return m_NumStoredNotes;}

    // access to value multiplier
    inline int GetValueMultiplier(){return m_ValueMultiplier;}

    // get a channel value
    inline int GetChannelValue(int channelNum)
	{
		if (channelNum > 0 && channelNum <= m_NumberOfChannels)
		{
			for(int i = 0; i < m_NumberOfChannels; ++i)
			{
				if (m_UnitData[i].Channel == channelNum)
				{
					return m_UnitData[i].Value;
				}
			}
		}
		return 0;
	}

	// get a channel currency
    inline char* GetChannelCurrency(int channelNum)
	{
		if (channelNum > 0 && channelNum <= m_NumberOfChannels)
		{
			for(int i = 0; i < m_NumberOfChannels; ++i)
			{
				if (m_UnitData[i].Channel == channelNum)
				{
					return m_UnitData[i].Currency;
				}
			}
		}
		return 0;
	}

private:
	// Internal SSP lib functions
	bool InitialiseLibrary();
	char* FormatComPort(string* comPort);
	unsigned int ConvertBytesToInt(unsigned char* c, unsigned int index);
	// Internal Command Function definitions
	bool NegotiateKeys();
	bool SetupRequest();
	bool QueryRejection();
	bool SetProtocolVersion(char pVersion);
	bool DisableAllRecycling();
	bool SendCommand();
	bool CheckGenericResponses();
	bool UpdateData();
	bool UnitCheck() { if (m_UnitType == 0x07)return true; return false; }
	bool WaitForRelease();
	void Release();

	// External functions to be called by the main program
public:
	inline void SetOutputStream(ostream* o){ WaitForRelease(); m_Output = o; m_Output->precision(2); *m_Output << fixed; THREAD_UNLOCK}
	bool OpenComPort(char portNum);
	void CloseComPort(){ ClosePort(); }
	bool ConnectToValidator(const SSP_COMMAND& commandStructure, int protocolVersion, int attempts);
	bool EnableValidator();
	bool DisableValidator();
	bool EnablePayout();
	bool DisablePayout();
	bool SetInhibits();
	bool GetLastNoteRejectionInfo();
	bool ResetValidator();
	bool DoPoll();
	bool SetValueReportingType(bool byChannel);
	bool ChangeRouting(int noteValue, char* currency, bool stack);
	bool PayoutNextNote();
	bool StackNextNote();
	bool EmptyPayout();
	bool OutputNoteLevelInfo();
	bool GetChannelRecycling (int channelNum, bool* recycling);
	bool OutputCurrentRecycling();
};

#endif

