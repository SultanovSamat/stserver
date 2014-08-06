#ifndef C_NOTE_VALIDATOR_H
#define C_NOTE_VALIDATOR_H

#include <Windows.h>
#include <string>
#include <iostream>
#include "SSPInclude.h"
#include "Commands.h"
#include "CLogging.h"

using namespace std;

class CValidator
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
    CLogging* m_Log; // Logging class
    int m_NumStackedNotes; // Keep a track of the number of notes accepted by this unit
	SChannelData* m_UnitData; // The dataset data (channel num, value, currency)
	int m_NumberOfChannels; // The number of channels in this dataset
	unsigned char m_ProtocolVersion; // The protocol version the validator is using
	ostream* m_Output; // The output stream
    unsigned char m_Type; // The type of validator
	unsigned char* m_Response; // A backup of the poll response
	unsigned char m_ResponseLength; // The poll response length

public:
    // constructor
    CValidator()
    {
        cmd = new SSP_COMMAND();
        keys = new SSP_KEYS();
        sspKey = new SSP_FULL_KEY();
        info = new SSP_COMMAND_INFO();

		// Create ssp library functions
		InitialiseLibrary();
        m_NumberOfChannels = 0;    
        m_Log = new CLogging();
		m_Type = 0;
		m_NumStackedNotes = 0;
		m_UnitData = 0;
		m_Response = new unsigned char[256];
		m_ResponseLength = 0;
    }

	// destructor
	~CValidator()
	{
		if (m_UnitData)
			delete[] m_UnitData;
		delete[] m_Response;
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

    // access to number of notes stacked
	inline int GetNumNotesStacked(){ return m_NumStackedNotes; }

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

	// get the type of the unit
	inline unsigned char GetUnitType(){ return m_Type; }

private:
	// Internal SSP lib functions
	bool InitialiseLibrary();
	// Internal Command Function definitions
	bool NegotiateKeys();
	bool SetupRequest();
	void QueryRejection();
	bool SetProtocolVersion(char pVersion);
	bool SendCommand();
	char* GetCommandName(char commandByte);
	bool CheckGenericResponses();

	// External functions to be called by the main program
public:
	inline void SetOutputStream(ostream* o){ m_Output = o; m_Output->precision(2); *m_Output << fixed; }
	bool OpenComPort(char portNum);
	inline void CloseComPort(){ ClosePort(); }
	bool ConnectToValidator(const SSP_COMMAND& command, int protocolVersion, int attempts);
	bool EnableValidator();
	bool DisableValidator();
	bool SetInhibits();
	bool GetLastNoteRejectionInfo();
	bool ResetValidator();
	bool DoPoll();
};

#endif

