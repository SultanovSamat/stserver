#include "CSMARTPayout.h"

// Loads the library and links function pointers to the methods in the library
// Critical Section: YES
bool CPayout::InitialiseLibrary()
{
	THREAD_LOCK
	// Load dll
	HINSTANCE hInst = LoadLibrary("ITLSSPProc.dll");
	if (hInst != NULL)
	{
		// Link function names

		// Opens the COM port
		OpenPort = (LPFNDLLFUNC1)GetProcAddress(hInst, "OpenSSPComPortUSB");
		if (OpenPort == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Closes the COM port
		ClosePort = (LPFNDLLFUNC2)GetProcAddress(hInst, "CloseSSPComPortUSB");
		if (ClosePort == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Creates a packet and transmits it to the validator
		SSPSendCommand = (LPFNDLLFUNC3)GetProcAddress(hInst, "SSPSendCommand");
		if (SSPSendCommand == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Generates the prime numbers needed for creating an encryption key
		InitiateSSPHostKeys = (LPFNDLLFUNC4)GetProcAddress(hInst, "InitiateSSPHostKeys");
		if (InitiateSSPHostKeys == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Creates the final encryption key
		CreateSSPHostEncryptionKey = (LPFNDLLFUNC5)GetProcAddress(hInst, "CreateSSPHostEncryptionKey");
		if (CreateSSPHostEncryptionKey == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}
	}
	else
	{
		THREAD_UNLOCK
		return false;
	}
	THREAD_UNLOCK
	return true;
}

// The enable command allows the validator to act on commands sent to it.
// Critical Section: YES
bool CPayout::EnableValidator()
{
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_ENABLE;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    // check response
	if (CheckGenericResponses())
		*m_Output << "Enabled validator" << endl;
	THREAD_UNLOCK
	return true;
}

// Disable command stops the validator acting on commands sent to it.
// Critical Section: YES
bool CPayout::DisableValidator() 
{
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_DISABLE;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    // check response
    if (CheckGenericResponses())
		*m_Output << "Disabled validator" << endl;
	THREAD_UNLOCK
	return true;
}

// The reset command instructs the validator to restart
// Critical Section: YES
bool CPayout::ResetValidator()
{
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_RESET;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    // check response
    if (CheckGenericResponses())
		*m_Output << "Resetting validator" << endl;
	THREAD_UNLOCK
	return true;
}

// This function sets the protocol version in the validator to the version passed across. Whoever calls
// this needs to check the response to make sure the version is supported.
// Critical Section: YES
bool CPayout::SetProtocolVersion(char pVersion)
{
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_HOST_PROTOCOL_VERSION;
    cmd->CommandData[1] = pVersion;
    cmd->CommandDataLength = 2;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	THREAD_UNLOCK
	return true;
}

// This function performs a number of commands in order to setup the encryption between the host and the validator.
// Critical Section: YES
bool CPayout::NegotiateKeys()
{
	THREAD_LOCK
    int i;
    string s = "";

    // make sure encryption is off
    cmd->EncryptionStatus = false;

    // send sync
    cmd->CommandData[0] = SSP_CMD_SYNC;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    InitiateSSPHostKeys(keys, cmd);

    // send generator
    cmd->CommandData[0] = SSP_CMD_SET_GENERATOR;
    cmd->CommandDataLength = 9;
    for (i = 0; i < 8; ++i)
    {
        cmd->CommandData[i + 1] = (char)(keys->Generator >> (8 * i));
    }

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    // send modulus
    cmd->CommandData[0] = SSP_CMD_SET_MODULUS;
    cmd->CommandDataLength = 9;
    for (i = 0; i < 8; ++i)
    {
        cmd->CommandData[i + 1] = (char)(keys->Modulus >> (8 * i));
    }

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    // send key exchange
    cmd->CommandData[0] = SSP_CMD_KEY_EXCHANGE;
    cmd->CommandDataLength = 9;
    for (i = 0; i < 8; ++i)
    {
        cmd->CommandData[i + 1] = (char)(keys->HostInter >> (8 * i));
    }

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

    keys->SlaveInterKey = 0;
    for (i = 0; i < 8; ++i)
    {
        keys->SlaveInterKey += (ULONG)cmd->ResponseData[1 + i] << (8 * i);
    }

    CreateSSPHostEncryptionKey(keys);

    // get full encryption key
    cmd->Key.FixedKey = 0x0123456701234567;
    cmd->Key.EncryptKey = keys->KeyHost;

	cmd->EncryptionStatus = true; // turn on encrypting

	*m_Output << "Negotiated keys" << endl;
	THREAD_UNLOCK
    return true;
}

// This function uses the setup request command to get information about the validator.
// The response packet from the validator is a variable length depending on the number
// of channels in the dataset.
// Critical Section: YES
bool CPayout::SetupRequest()
{         
	THREAD_LOCK
    // send setup request
    cmd->CommandData[0] = SSP_CMD_SETUP_REQUEST;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}
	
	// check response
    if (CheckGenericResponses())
	{
		// Output setup request data

		// Unit type
		*m_Output << "Unit type: ";
		m_UnitType = cmd->ResponseData[1];
		switch (m_UnitType)
		{
		case 0x00: *m_Output << "Note Validator" << endl; break;
		case 0x03: *m_Output << "SMART Hopper" << endl; break;
		case 0x06: *m_Output << "SMART Payout" << endl; break;
		case 0x07: *m_Output << "NV11" << endl; break;
		}

		// Firmware
		*m_Output << "Firmware: ";
		*m_Output << cmd->ResponseData[2] << cmd->ResponseData[3] << "." <<
			cmd->ResponseData[4] << cmd->ResponseData[5] << endl;

		// Channel setup
		m_NumberOfChannels = cmd->ResponseData[12];
		int index = 13; // End of fixed data

		m_UnitData = new SUnitData[m_NumberOfChannels];
		
		index += m_NumberOfChannels * 2; // Skip channel security and old channel values

		// Value multiplier
		m_ValueMultiplier = cmd->ResponseData[index+2];
		m_ValueMultiplier += cmd->ResponseData[index+1] << 8;
		m_ValueMultiplier += cmd->ResponseData[index] << 16;
		index += 3;

		*m_Output << "Multiplier: " << m_ValueMultiplier << endl;

		// Protocol version
		m_ProtocolVersion = cmd->ResponseData[index++];

		*m_Output << "Protocol Version: " << m_ProtocolVersion << endl;

		// Country codes
		int count = 0;
		for (int i = 0; i < m_NumberOfChannels * 3; i+=3, ++count)
		{
			m_UnitData[count].Currency[0] = cmd->ResponseData[index++];
			m_UnitData[count].Currency[1] = cmd->ResponseData[index++];
			m_UnitData[count].Currency[2] = cmd->ResponseData[index++];
			*m_Output << "Channel " << count+1 << " currency: " << m_UnitData[count].Currency[0] <<
				m_UnitData[count].Currency[1] << m_UnitData[count].Currency[2] << endl;
		}

		// Channel values
		count = 0;
		for (int i = index; i < index + (m_NumberOfChannels * 4); i+=4, ++count)
		{
			m_UnitData[count].Value = 0;
			for (int j = 0; j < 4; ++j)
				m_UnitData[count].Value += cmd->ResponseData[i+j] << (j*8);
			*m_Output << "Channel " << count + 1 << " value: " << m_UnitData[count].Value << endl;
			m_UnitData[count].Value *= m_ValueMultiplier;

			// using this loop to save processing
			m_UnitData[count].Channel = count + 1;

			string s(m_UnitData[count].Currency);
			if (s != "") // Check a currency is associated with the channel
			{
				THREAD_UNLOCK
				GetNoteRecycling(m_UnitData[count].Value, s, &m_UnitData[count].Recycling);
				THREAD_LOCK
			}
		}
	}
	THREAD_UNLOCK
	return true;
}

// This function sends the set inhibits command to set the inhibits on the validator.
// The two bytes after the command byte represent two bit registers with each bit being
// a channel. 1-8 and 9-16 respectively. 0xFF = 11111111 in binary indicating all channels
// in this register are able to accept notes.
// Critical Section: YES
bool CPayout::SetInhibits()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_INHIBITS;
	cmd->CommandData[1] = 0xFF;
	cmd->CommandData[2] = 0xFF;
	cmd->CommandDataLength = 3;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Inhibits set" << endl;
	THREAD_UNLOCK
	return true;
}

// Enables the payout facility of the validator which lets it store and
// payout notes.
// Critical Section: YES
bool CPayout::EnablePayout()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_ENABLE_PAYOUT;
	cmd->CommandDataLength = 1;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Payout enabled" << endl;
	THREAD_UNLOCK
	return true;
}

// Stops the validator from being able to store or payout notes.
// Critical Section: YES
bool CPayout::DisablePayout()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_DISABLE_PAYOUT;
	cmd->CommandDataLength = 1;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Payout disabled" << endl;
	THREAD_UNLOCK
	return true;
}

// This function is used to instruct the validator to make a payout of the specified
// amount, the validator decides what denominations are paid out to meet the requested
// total.
// Critical Section: YES
bool CPayout::Payout(int amount, const string& currency)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_PAYOUT;
	// Using memcpy to copy a single integer into 4 bytes
	memcpy(cmd->CommandData + 1, &amount, 4); 
	cmd->CommandData[5] = currency[0];
	cmd->CommandData[6] = currency[1];
	cmd->CommandData[7] = currency[2];
	cmd->CommandData[8] = 0x58;
	cmd->CommandDataLength = 9;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Paying out " << amount/100.0f << " " << currency << endl;
	THREAD_UNLOCK
	return true;
}

// Payout by denomination - this method is used to payout multiple denominations
// in specified amounts. Due to the data of this method being so variable, the
// parameters contain most of the array already initialised. The developer needs
// to pass the number of denominations they are paying out as the first param and
// then an array pointer containing the 2 byte number to payout, 4 byte denomination
// value and 3 byte country code for each denomination required.
// Critical Section: YES
bool CPayout::PayoutByDenomination(int numDenoms, unsigned char* denomData)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_PAYOUT_BY_DENOMINATION;
	cmd->CommandData[1] = numDenoms;
	int count = 0, i = 0;
	for (i = 2; i < numDenoms*9+2; ++i)
		cmd->CommandData[i] = denomData[count++];

	cmd->CommandData[i] = 0x58; // real payout

	cmd->CommandDataLength = numDenoms * 9 + 3;
	
	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Paying out by denomination..." << endl;
	THREAD_UNLOCK
	return true;
}

// This function uses the GET ROUTING command to determine whether a particular
// note is recycling. The function then sets a passed across boolean to 
// indicate the recycling state of the note. If the response is 0x01 then the
// note is being sent to cashbox, 0x00 is storage.
// Critical Section: YES
bool CPayout::GetNoteRecycling(unsigned int note, const string& currency, bool* isRecycling)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_GET_ROUTING;
	// Using memcpy to copy a single integer into 4 bytes
	memcpy(cmd->CommandData + 1, &note, 4);
	cmd->CommandData[5] = currency[0];
	cmd->CommandData[6] = currency[1];
	cmd->CommandData[7] = currency[2];
	cmd->CommandDataLength = 8;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		// Set the bool based on the result
		if (cmd->ResponseData[1] == (char)0x00)
			*isRecycling = true;
		else
			*isRecycling = false;
	}
	THREAD_UNLOCK
	return true;
}

// The following two functions alter the routing of a note using the 
// SET ROUTING command. Passing 0x01 as the second byte indicates that the
// selected note should be routed to the cashbox, 0x00 indicates routing
// for storage. 
// Critical Section: YES
bool CPayout::SendNoteToCashbox(int value, const string& currency)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_ROUTING;
	cmd->CommandData[1] = 0x01;
	// using memcpy to copy one integer to 4 bytes
	memcpy(cmd->CommandData + 2, &value, 4);
	cmd->CommandData[6] = currency[0];
	cmd->CommandData[7] = currency[1];
	cmd->CommandData[8] = currency[2];
	cmd->CommandDataLength = 9;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		*m_Output << "Routed " << value*0.01f << " " << currency[0] << currency[1] <<
			currency[2] << " to cashbox" << endl;
		THREAD_UNLOCK
		UpdateData();
	}
	THREAD_UNLOCK
	return true;
}

// See description of above function.
bool CPayout::SendNoteToStorage(int value, const string& currency)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_ROUTING;
	cmd->CommandData[1] = 0x00;
	// using memcpy to copy one integer to 4 bytes
	memcpy(cmd->CommandData + 2, &value, 4);
	cmd->CommandData[6] = currency[0];
	cmd->CommandData[7] = currency[1];
	cmd->CommandData[8] = currency[2];
	cmd->CommandDataLength = 9;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		*m_Output << "Routed " << value*0.01f<< " " << currency[0] << currency[1] <<
			currency[2] << " to storage" << endl;
		THREAD_UNLOCK
		UpdateData();
	}
	THREAD_UNLOCK
	return true;
}

// This uses the EMPTY command to instruct the SMART Hopper to dump all stored
// coins into the cashbox. The SMART Hopper will not keep a track of what coins
// have been emptied.
// Critical Section: YES
bool CPayout::Empty()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_EMPTY;
	cmd->CommandDataLength = 1;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Emptying unit..." << endl;
	THREAD_UNLOCK
	return true;
}

// This uses the SMART EMPTY command to instruct the SMART Hopper to dump all stored
// coins into the cashbox. The SMART Hopper will keep a track of what coins have been
// emptied, this data can be retrieved using the CASHBOX PAYOUT OPERATION DATA command.
// Critical Section: YES
bool CPayout::SMARTEmpty()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SMART_EMPTY;
	cmd->CommandDataLength = 1;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "SMART emptying unit..." << endl;
	THREAD_UNLOCK
	return true;
}

// This function uses the LAST REJECT CODE command to query the validator
// on what the last recorded reason was for rejecting a note. The reason is
// returned as a single byte.
// Critical Section: YES
bool CPayout::QueryRejection()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_LAST_REJECT_CODE;
	cmd->CommandDataLength = 1;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		switch (cmd->ResponseData[1])
		{
			case 0x00: *m_Output << "Note accepted\r\n"; break;
			case 0x01: *m_Output << "Note length incorrect\r\n"; break;
			case 0x02: *m_Output << "Invalid note\r\n"; break;
			case 0x03: *m_Output << "Invalid note\r\n"; break;
			case 0x04: *m_Output << "Invalid note\r\n"; break;
			case 0x05: *m_Output << "Invalid note\r\n"; break;
			case 0x06: *m_Output << "Channel inhibited\r\n"; break;
			case 0x07: *m_Output << "Second note inserted during read\r\n"; break;
			case 0x08: *m_Output << "Host rejected note\r\n"; break;
			case 0x09: *m_Output << "Invalid note\r\n"; break;
			case 0x0A: *m_Output << "Invalid note read\r\n"; break;
			case 0x0B: *m_Output << "Note too long\r\n"; break;
			case 0x0C: *m_Output << "Validator disabled\r\n"; break;
			case 0x0D: *m_Output << "Mechanism slow/stalled\r\n"; break;
			case 0x0E: *m_Output << "Strim attempt\r\n"; break;
			case 0x0F: *m_Output << "Fraud channel reject\r\n"; break;
			case 0x10: *m_Output << "No notes inserted\r\n"; break;
			case 0x11: *m_Output << "Invalid note read\r\n"; break;
			case 0x12: *m_Output << "Twisted note detected\r\n"; break;
			case 0x13: *m_Output << "Escrow time-out\r\n"; break;
			case 0x14: *m_Output << "Bar code scan fail\r\n"; break;
			case 0x15: *m_Output << "Invalid note read\r\n"; break;
			case 0x16: *m_Output << "Invalid note read\r\n"; break;
			case 0x17: *m_Output << "Invalid note read\r\n"; break;
			case 0x18: *m_Output << "Invalid note read\r\n"; break;
			case 0x19: *m_Output << "Incorrect note width\r\n"; break;
			case 0x1A: *m_Output << "Note too short\r\n"; break;
		}
	}
	THREAD_UNLOCK
	return true;
}

// This function uses the GET CASHBOX PAYOUT OPERATION DATA command which
// instructs the SMART Hopper to report the number of coins moved and their
// denominations in the last cashbox operation. This could be a dispense, float
// or SMART empty.
// Critical Section: YES
bool CPayout::GetCashboxPayoutOpData()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_GET_CASHBOX_PAYOUT_OP_DATA;
	cmd->CommandDataLength = 1;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		int n = cmd->ResponseData[1]; // number of denominations of coin moved
		for (int i = 1; i < n * 9; i+=9)
		{
			// Number of this denomination moved
			int numMoved = cmd->ResponseData[i+1];
			numMoved += cmd->ResponseData[i+2] << 8;
			
			// Value of this denomination
			int value = cmd->ResponseData[i+3];
			value += cmd->ResponseData[i+4] << 8;
			value += cmd->ResponseData[i+5] << 16;
			value += cmd->ResponseData[i+6] << 24;
		
			*m_Output << "Moved " << numMoved << " x " << value*0.01f << 
			" " << cmd->ResponseData[i+7] << cmd->ResponseData[i+8] << cmd->ResponseData[i+9] << 
			endl;
		}
	}
	THREAD_UNLOCK
	return true;
}

// Gets the number of notes stored and reports them in a string which is passed as 
// a parameter.
// Critical Section: YES
bool CPayout::OutputLevelInfo()
{
	THREAD_LOCK
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		// Get unit data
		cmd->CommandData[0] = SSP_CMD_GET_NOTE_AMOUNT;
		memcpy(cmd->CommandData + 1, &m_UnitData[i].Value, 4);

		cmd->CommandData[5] = m_UnitData[i].Currency[0];
		cmd->CommandData[6] = m_UnitData[i].Currency[1];
		cmd->CommandData[7] = m_UnitData[i].Currency[2];

		cmd->CommandDataLength = 8;

		if (!SendCommand())
		{
			THREAD_UNLOCK
			return false;
		}

		if (CheckGenericResponses())
		{
			short NumNotes = cmd->ResponseData[1];
			NumNotes += cmd->ResponseData[2] << 8; 

			*m_Output << m_UnitData[i].Value * 0.01f << " ";
			*m_Output << m_UnitData[i].Currency[0];
			*m_Output << m_UnitData[i].Currency[1];
			*m_Output << m_UnitData[i].Currency[2];
			*m_Output << "[" << NumNotes << "] = ";
			*m_Output << NumNotes * m_UnitData[i].Value * 0.01f << endl;
		}
	}
	THREAD_UNLOCK
	return true;
}

// The poll function is called repeatedly to poll the validator for information, it returns as
// a response in the command structure what events are currently happening.	
// Critical Section: YES
bool CPayout::DoPoll()
{
	THREAD_LOCK
	// send poll
    cmd->CommandData[0] = SSP_CMD_POLL;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	// If 'key not set' response is received then it is possible
	// the unit lost power so return false to reconnect
	if (cmd->ResponseData[0] == 0xFA)
	{
		THREAD_UNLOCK
		return false;
	}

	CheckGenericResponses();

	// isolate poll response so it can't get overwritten while we parse the poll response
	m_PollResponseLength = cmd->ResponseDataLength;
	memcpy(m_PollResponse, cmd->ResponseData, m_PollResponseLength);

    // parse poll response
	char* noteCurrency;
    for (int i = 1; i < m_PollResponseLength; ++i)
    {
        switch (m_PollResponse[i])
        {
			// The unit is disabled and unable to accept notes or make payouts
			case SSP_POLL_DISABLED:
				*m_Output << "Unit disabled" << endl;
				break;
			// The unit has accepted a note as valid currency
            case SSP_POLL_CREDIT:
				{
					float f = GetChannelValue(m_PollResponse[i+1])*0.01f;
					noteCurrency = GetChannelCurrency(m_PollResponse[i+1]);
					*m_Output << "Credit " << f << " " << noteCurrency[0] << 
						noteCurrency[1] << noteCurrency[2] << endl;
					++i;
					break;
				}
			// A note is being read, if the byte immediately following the response
			// is 0 then the note has not finished reading, if it is greater than 0
			// then this is the channel of that note.
			case SSP_POLL_NOTE_READ:
				{
					if (m_PollResponse[i + 1] > 0)
					{
						float f = GetChannelValue(m_PollResponse[i+1])*0.01f;
						noteCurrency = GetChannelCurrency(m_PollResponse[i+1]);
						*m_Output << "Note in escrow, amount: ";
						*m_Output << f << " " << noteCurrency[0] << 
						noteCurrency[1] << noteCurrency[2] << endl;
					}
					else
						*m_Output << "Reading note..." << endl;
					++i;
					break;
				}
			// The unit is either stacking the note to the cashbox or to storage
			case SSP_POLL_STACKING:
				*m_Output << "Stacking note..." << endl;
				break;
			// The unit has stacked the note in the cashbox
			case SSP_POLL_STACKED:
				*m_Output << "Note stacked" << endl;
				break;
			// The unit is in the process of rejecting a note
			case SSP_POLL_REJECTING:
				*m_Output << "Note rejecting..." << endl;
				break;
			// The unit has rejected a note
			case SSP_POLL_REJECTED:
				*m_Output << "Note rejected" << endl;
				THREAD_UNLOCK
				QueryRejection();
				THREAD_LOCK
				break;
			// The cashbox has reached maximum capacity
			case SSP_POLL_STACKER_FULL:
				*m_Output << "Stacker full" << endl;
				break;
			// The cashbox is not attached to the unit
			case SSP_POLL_CASHBOX_REMOVED:
				*m_Output << "Cashbox removed..." << endl;
				break;
			// The cashbox has been refitted to the unit
			case SSP_POLL_CASHBOX_REPLACED:
				*m_Output << "Cashbox replaced" << endl;
				break;
			// A fraud attempt has been detected
            case SSP_POLL_FRAUD_ATTEMPT:
                *m_Output << "Fraud attempt!" << endl;
                ++i;
                break;
			// A note was detected inside the unit at startup and dispensed from the
			// front of the unit
			case SSP_POLL_NOTE_CLEARED_FROM_FRONT:
				{
					float f = GetChannelValue(m_PollResponse[i+1])*0.01f;
					noteCurrency = GetChannelCurrency(m_PollResponse[i+1]);
					*m_Output << f << " " << noteCurrency[0] << 
						noteCurrency[1] << noteCurrency[2] << " cleared from front" << endl;
					++i;
					break;
				}
			// A note was detected inside the unit at startup and stacked to the
			// cashbox
			case SSP_POLL_NOTE_CLEARED_TO_CASHBOX:
				{
					float f = GetChannelValue(m_PollResponse[i+1])*0.01f;
					noteCurrency = GetChannelCurrency(m_PollResponse[i+1]);
					*m_Output << f << " " << noteCurrency[0] << 
						noteCurrency[1] << noteCurrency[2] << " cleared to cashbox" << endl;
					++i;
					break;
				}
			// A note has been sent to storage for payout
			case SSP_POLL_NOTE_STORED_IN_PAYOUT:
				*m_Output << "Note stored for payout" << endl;
				break;
			// The unit has been reset since the last time a poll was sent
			case SSP_POLL_RESET:
				*m_Output << "Unit reset" << endl;
				break;
			// The unit is in the process of dispensing a note
			case SSP_POLL_DISPENSING:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = 0;
						memcpy(&n, m_PollResponse + (i + 2), 4);
						j += 4;
						*m_Output << "Dispensing " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << "..." << endl;
					}
					i += l - 1;
					break;
				}
			// The unit has finished dispensing a note
			case SSP_POLL_DISPENSED:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = 0;
						memcpy(&n, m_PollResponse + (i + 2), 4);
						j += 4;
						*m_Output << "Dispensed " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << endl;
					}
					i += l - 1;

					THREAD_UNLOCK
					EnableValidator();
					THREAD_LOCK
					break;
				}
			// The payout device is empty
			case SSP_POLL_EMPTY:
				*m_Output << "Unit empty" << endl;
				break;
			// The unit has become jammed
			case SSP_POLL_JAMMED:
				*m_Output << "Unit jammed..." << endl;
				break;
			// A note has jammed in a place where the user may be able to retrieve it
			case SSP_POLL_UNSAFE_JAM:
				*m_Output << "Unsafe jam detected..." << endl;
				break;
			// A note has jammed in a place where the user cannot retrieve it
			case SSP_POLL_SAFE_JAM:
				*m_Output << "Safe jam detected..." << endl;
				break;
			// The unit was interrupted while making a payout, some notes may have been dispensed
			case SSP_POLL_INCOMPLETE_PAYOUT:
				{
					int j = i + 2;
					int l = m_PollResponse[i+1] * 11 + j;
					while (j < l)
					{
						int n = 0, n1 = 0;
						memcpy(&n, m_PollResponse + (i + 2), 4); // copy dispensed value
						memcpy(&n1, m_PollResponse + (i + 6), 4); // copy requested value
						// Get currency 
						j += 8;
						string curr = " ";
						curr += m_PollResponse[j++];
						curr += m_PollResponse[j++];
						curr += m_PollResponse[j++];

						*m_Output << "Incomplete payout\nDispensed: ";
						*m_Output << n * 0.01f << curr << endl;
						*m_Output << "Requested: " << n1 * 0.01f << curr << endl;
					}
					i += l - 1;
					break;
				}
			// The unit is in the process of emptying its stored notes to the cashbox
			case SSP_POLL_EMPTYING:
				*m_Output << "Emptying..." << endl;
				break;
			// The unit has finished emptying its stored notes to the cashbox
			case SSP_POLL_EMPTIED:
				*m_Output << "Unit emptied" << endl;
				break;
			// The unit is in the process of emptying its stored notes to the cashbox and keeping
			// a track of what notes were emptied
			case SSP_POLL_SMART_EMPTYING:
				*m_Output << "SMART emptying..." << endl;
				break;
			// The unit has finished emptying its stored notes to the cashbox and has kept track of
			// what was emptied. This data can be retrieved using the GetCashboxPayoutOpData method.
			case SSP_POLL_SMART_EMPTIED:
				*m_Output << "SMART emptied" << endl;
				THREAD_UNLOCK
				GetCashboxPayoutOpData(); // Report what was moved in the empty
				THREAD_LOCK
				break;
			// A note is resting in the bezel waiting to be removed by the user
			case SSP_POLL_NOTE_IN_BEZEL:
				{
					int n = 0;
					memcpy(&n, m_PollResponse + (i + 1), 4);
					*m_Output << n * 0.01f << " ";
					*m_Output << m_PollResponse[i + 5];
					*m_Output << m_PollResponse[i + 6];
					*m_Output << m_PollResponse[i + 7];
					*m_Output << " held in bezel" << endl;
					i += 7;
					break;
				}
			// The unit has been opened exposing the note path
			case SSP_POLL_NOTE_PATH_OPEN:
				*m_Output << "The note path is open" << endl;
				break;
			// The unit is disabled as all the channels are inhibited
			case SSP_POLL_CHANNEL_DISABLE:
				*m_Output << "Unit disabled (all channels inhibited)" << endl;
				break;
			// Any poll response that is not detected is output with a warning
            default:
				*m_Output << "WARNING: Unknown poll response detected: " << (int)m_PollResponse[i] << endl;
                break;
        }
    }
	THREAD_UNLOCK
    return true;
}

/* Non-Command functions */

// This function calls the open com port function of the SSP library.
// Critical Section: YES
bool CPayout::OpenComPort(char portNum)
{
	THREAD_LOCK
    if (OpenPort(cmd) == 0)
	{
		THREAD_UNLOCK
        return false;
	}
	THREAD_UNLOCK
    return true;
}

/* Exception and Error Handling */

// This is used for generic response error catching, it outputs the info in a
// meaningful way.
// Critical Section: NO (but must always be called from within a critical section)
bool CPayout::CheckGenericResponses()
{
    if (cmd->ResponseData[0] == SSP_RESPONSE_CMD_OK)
        return true;
    else
    {	
        switch (cmd->ResponseData[0])
        {
            case SSP_RESPONSE_CMD_CANNOT_PROCESS:
				{
					*m_Output << "Command response is CANNOT PROCESS COMMAND";
					if (cmd->ResponseDataLength > 1)
					{
						*m_Output << ", error code - 0x" << (int)cmd->ResponseData[1];
					}
					*m_Output << endl;
					return false;
				}
            case SSP_RESPONSE_CMD_FAIL:
				{
					*m_Output << "Command response is FAIL\n";
					return false;
				}
            case SSP_RESPONSE_CMD_KEY_NOT_SET:
				{
					*m_Output << "Command response is KEY NOT SET, renegotiate keys\n";
					return false;
				}
            case SSP_RESPONSE_CMD_PARAM_OUT_OF_RANGE:
				{
					*m_Output << "Command response is PARAM OUT OF RANGE\n";
					return false;
				}
            case SSP_RESPONSE_CMD_SOFTWARE_ERROR:
				{
					*m_Output << "Command response is SOFTWARE ERROR\n";
					return false;
				}
            case SSP_RESPONSE_CMD_UNKNOWN:
				{
					*m_Output << "Command response is UNKNOWN\n";
					return false;
				}
            case SSP_RESPONSE_CMD_WRONG_PARAMS:
				{
					*m_Output << "Command response is WRONG PARAMETERS\n";
					return false;
				}
            default:
                return false;	
        }
	}
}

// This function sends the current command structure to the validator, if it returns
// false the command never reached the validator. This method also has to be threadsafe
// to avoid threads sending commands before previous commands have returned.
// Critical Section: NO (but must always be called from within a critical section)
bool CPayout::SendCommand()
{
	// setup info structure
	info->CommandName = (unsigned char*)GetCommandName(cmd->CommandData[0]);

	// attempt to send the command
	if (SSPSendCommand(cmd, info) == 0)
	{
		// If the command fails
		ClosePort(); // close the com port
		*m_Output << "Failed to send command, port status: ";
		*m_Output << DecodeResponseStatus(cmd->ResponseStatus) << endl;
		m_Log->UpdateLog(info);
		return false;
	}
	m_Log->UpdateLog(info);

    return true;
}

// This takes a response byte and decodes it into a string and returns
// Critical Section: NO
string CPayout::DecodeResponseStatus(char statusByte)
{
	switch (statusByte)
	{
		case PORT_CLOSED: return "Port Closed";
		case PORT_OPEN: return "Port Open";
		case PORT_ERROR: return "Port Error";
		case SSP_REPLY_OK: return "Reply OK";
		case SSP_PACKET_ERROR: return "Packet Error";
		case SSP_CMD_TIMEOUT: return "Command Timeout";
		default: return "Status not recognised";
	}
}

// Takes a byte and converts it to a command name, used for logging
// Critical Section: NO
char* CPayout::GetCommandName(char commandByte)
{
	switch (commandByte)
	{
		case 0x4A: return "SET GENERATOR";
		case 0x4B: return "SET MODULUS";
		case 0x4C: return "REQUEST KEY EXCHANGE";
		case 0x01: return "RESET";
		case 0x02: return "SET INHIBITS";
		case 0x03: return "DISPLAY ON";
		case 0x04: return "DISPLAY OFF";
		case 0x05: return "SETUP REQUEST";
		case 0x06: return "HOST PROTOCOL VERSION";
		case 0x07: return "POLL";
		case 0x08: return "REJECT";
		case 0x09: return "DISABLE";
		case 0x0A: return "ENABLE";
		case 0x0B: return "PROGRAM FIRMWARE";
		case 0x0C: return "GET SERIAL NUMBER";
		case 0x0D: return "UNIT DATA";
		case 0x0E: return "CHANNEL VALUE DATA";
		case 0x0F: return "CHANNEL SECURITY DATA";
		case 0x10: return "CHANNEL RETEACH DATA";
		case 0x11: return "SYNC";
		case 0x12: return "UPDATE COIN ROUTE";
		case 0x13: return "DISPENSE";
		case 0x14: return "HOST SERIAL NUMBER REQUEST";
		case 0x15: return "SETUP REQUEST";
		case 0x17: return "LAST REJECT CODE";
		case 0x18: return "HOLD";
		case 0x19: return "ENABLE PROTOCOL VERSION EVENTS";
		case 0x23: return "GET BAR CODE READER CONFIGURATION";
		case 0x24: return "SET BAR CODE READER CONFIGURATION";
		case 0x25: return "GET BAR CODE INHIBIT";
		case 0x26: return "SET BAR CODE INHIBIT";
		case 0x27: return "GET BAR CODE DATA";	
		case 0x54: return "CONFIGURE BEZEL";
		case 0x56: return "POLL WITH ACK";
		case 0x57: return "EVENT ACK";
		case 0x3B: return "SET ROUTING";
		case 0x3C: return "GET ROUTING";
		case 0x33: return "PAYOUT AMOUNT";
		case 0x35: return "GET NOTE/COIN AMOUNT";
		case 0x34: return "SET NOTE/COIN AMOUNT";
		case 0x38: return "HALT PAYOUT";
		case 0x3D: return "FLOAT AMOUNT";
		case 0x3E: return "GET MINIMUM PAYOUT";
		case 0x40: return "SET COIN MECH INHIBITS";
		case 0x46: return "PAYOUT BY DENOMINATION";
		case 0x44: return "FLOAT BY DENOMINATION";
		case 0x47: return "SET COMMAND CALIBRATION";
		case 0x48: return "RUN COMMAND CALIBRATION";
		case 0x3F: return "EMPTY ALL";
		case 0x50: return "SET OPTIONS";
		case 0x51: return "GET OPTIONS";
		case 0x49: return "COIN MECH GLOBAL INHIBIT";
		case 0x52: return "SMART EMPTY";
		case 0x53: return "CASHBOX PAYOUT OPERATION DATA";
		case 0x5C: return "ENABLE PAYOUT DEVICE";
		case 0x5B: return "DISABLE PAYOUT DEVICE";
		case 0x58: return "GET NOTE COUNTERS";
		case 0x59: return "RESET NOTE COUNTERS";
		case 0x30: return "SET REFILL MODE";
		case 0x41: return "GET NOTE POSITIONS";
		case 0x42: return "PAYOUT NOTE";
		case 0x43: return "STACK NOTE";
		case 0x45: return "SET VALUE REPORTING TYPE";
		default: return "COMMAND NOT FOUND";
	}
}

// This function sends a series of commands to the validator to initialise it for use.
// It opens the com port, negotiates keys for encryption, sets the protocol version,
// sets the inhibits, calls the setup request and enables the payout section of the 
// validator. After this function the validator is ready to be enabled and used for eSSP.
// Critical Section: YES
bool CPayout::ConnectToPayout(const SSP_COMMAND& command, int protocolVersion, int attempts)
{
	THREAD_LOCK
	// Setup command structure (cmd is the command structure of this class instance)
	memcpy(cmd, &command, sizeof(SSP_COMMAND));
	THREAD_UNLOCK
	
	for (int i = 0; i < attempts; ++i)
	{
		// Close port in case it was left open
		ClosePort();

		// Open the com port
		if (!OpenComPort(cmd->PortNumber))
		{
			*m_Output << "Failed to open port " << (int)cmd->PortNumber << endl;
			Sleep(1000); // Sleep for a second on failure to open ports
			continue;
		}

		// Negotiate keys for encryption
		if (!NegotiateKeys())
		{
			*m_Output << "Failed on key negotiation..." << endl;
			continue;
		}

		// Set the protocol version
		if (!SetProtocolVersion(protocolVersion))
		{
			*m_Output << "Failed on setting protocol version..." << endl;
			continue;
		}

		// Call setup request
		if (!SetupRequest())
		{
			*m_Output << "Failed on setup request..." << endl;
			continue;
		}

		// Set inhibits
		if (!SetInhibits())
		{
			*m_Output << "Failed on setting inhibits..." << endl;
			continue;
		}

		// Enable payout
		if (!EnablePayout())
		{
			*m_Output << "Failed on enabling payout..." << endl;
			continue;
		}
		return true;
	}
	return false;
}

// This method updates the recycling status of each note so it is correctly
// represented in the unit data table.
// Critical Section: NO
void CPayout::UpdateData()
{
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		string s(m_UnitData[i].Currency);
		if (s != "")
			GetNoteRecycling(m_UnitData[i].Value, s, &m_UnitData[i].Recycling);
	}
}

// Threading critical section functions
bool CPayout::WaitForRelease()
{
	clock_t start, final = 0;
	while (m_ThreadLock)
	{
		start = clock();
		Sleep(0);
		final += clock() - start;
		if (final > 3500)
		{
			if (m_Output) *m_Output << "A thread timed out" << endl;
			return false;
		}
	}
	return m_ThreadLock = true;
}

void CPayout::Release()
{
	m_ThreadLock = false;
}