#include "CSMARTHopper.h"

// This function is used to load the library and link the methods to function pointers
// defined in SSPInclude.h
bool CHopper::InitialiseLibrary()
{
	THREAD_LOCK
	// Load dll
	HINSTANCE hInst = LoadLibrary("ITLSSPProc.dll");
	if (hInst != NULL)
	{
		// Link function names

		// Open the COM port
		OpenPort = (LPFNDLLFUNC1)GetProcAddress(hInst, "OpenSSPComPortUSB");
		if (OpenPort == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Close the COM port
		ClosePort = (LPFNDLLFUNC2)GetProcAddress(hInst, "CloseSSPComPortUSB");
		if (ClosePort == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Send a command to the unit
		SSPSendCommand = (LPFNDLLFUNC3)GetProcAddress(hInst, "SSPSendCommand");
		if (SSPSendCommand == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Initiate the host keys, create modulus and generator
		InitiateSSPHostKeys = (LPFNDLLFUNC4)GetProcAddress(hInst, "InitiateSSPHostKeys");
		if (InitiateSSPHostKeys == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Create the final encryption key
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

// The enable command allows the Hopper to receive and act on commands sent to it.
bool CHopper::EnableValidator()
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

// Disable command stops the Hopper acting on commands.
bool CHopper::DisableValidator() 
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

// The reset command instructs the validator to restart (same effect as switching on and off).
bool CHopper::ResetValidator()
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
bool CHopper::SetProtocolVersion(char pVersion)
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
bool CHopper::NegotiateKeys()
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
bool CHopper::SetupRequest()
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
		default: *m_Output << "Unknown Type" << endl; break;
		}

		// Firmware
		*m_Output << "Firmware: ";
		*m_Output << cmd->ResponseData[2] << cmd->ResponseData[3] << "." <<
			cmd->ResponseData[4] << cmd->ResponseData[5] << endl;

		// Protocol version
		*m_Output << "Protocol Version: " << (int)cmd->ResponseData[9] << endl;

		// Channel setup
		m_NumberOfChannels = cmd->ResponseData[10];
		m_UnitData = new SUnitData[m_NumberOfChannels];

		// Channel values
		*m_Output << "Channel Values: ";
		int index = 11;
		for (int i = 0; i < m_NumberOfChannels; ++i)
		{
			m_UnitData[i].Value = cmd->ResponseData[index++];
			m_UnitData[i].Value += cmd->ResponseData[index++] << 8;
			*m_Output << m_UnitData[i].Value << " ";
		}
		*m_Output << endl;

		// Channel currencies
		*m_Output << "Channel Currencies: ";
		for (int i = 0; i < m_NumberOfChannels; i++)
		{
			m_UnitData[i].Currency[0] = cmd->ResponseData[index++];
			*m_Output << m_UnitData[i].Currency[0];
			m_UnitData[i].Currency[1] = cmd->ResponseData[index++];
			*m_Output << m_UnitData[i].Currency[1];
			m_UnitData[i].Currency[2] = cmd->ResponseData[index++];
			*m_Output << m_UnitData[i].Currency[2] << " ";
		}
		*m_Output << endl;
	}
	THREAD_UNLOCK
	return true;
}

// This function is used to instruct the SMART Hopper to make a payout of the specified
// amount. The way the hopper decides what coins to pay out is determined by its split
// mode, either free pay or highest value split. This split mode can be altered using the
// set options command (0x50).
bool CHopper::Payout(int amount, const string& currency)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_PAYOUT;
	memcpy(&cmd->CommandData[1], &amount, 4);
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
		*m_Output << "Paying out " << amount * 0.01f << " " << currency << endl;
	THREAD_UNLOCK
	return true;
}

// This method allows a user to payout a specified amount of each coin.
// The first parameter is the number of denominations that will be paid out, so if the
// user wanted to only payout 1 and 2 euro coins, the value would be 2.
// The other parameter contains the data about each denomination that needs to be paid out
// in the format |amount to payout (2 bytes)|value of coin to payout (4 bytes)|currency of
// coin to payout (3 bytes).
// If the user wanted to payout 10 x 1 EUR and 10 x 2 EUR; the first parameter would be 2
// and the second would be |0a 00|e8 03 00 00|45 55 52|0a 00|c8 00 00 00|45 55 52.
// The command has an additional byte on the end indicating whether this is a test payout
// or a real one. 0x58 is real, 0x19 is test.
bool CHopper::PayoutByDenomination(int numDenoms, unsigned char* dataArray)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_PAYOUT_BY_DENOMINATION;
	cmd->CommandData[1] = numDenoms;

	int counter = 2;
	for (int i = 0; i < numDenoms*9; ++i, ++counter)
		cmd->CommandData[counter] = dataArray[i];

	cmd->CommandData[counter++] = 0x58; // real payout (0x19 for test)

	cmd->CommandDataLength = counter;

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

// This method allows a user to float a specified amount of each coin in the Hopper
// The first parameter is the number of denominations that will be floated, so if the
// user wanted to only leave 1 and 2 euro coins in the hopper, the value would be 2.
// The other parameter contains the data about each denomination that needs to be floated
// in the format |amount to float (2 bytes)|value of coin to float (4 bytes)|currency of
// coin to float (3 bytes).
// If the user wanted to float 10 x 1 EUR and 10 x 2 EUR; the first parameter would be 2
// and the second would be |0a 00|e8 03 00 00|45 55 52|0a 00|c8 00 00 00|45 55 52
// The command has an additional byte on the end indicating whether this is a test float
// or a real one. 0x58 is real, 0x19 is test.
bool CHopper::FloatByDenomination(int numDenoms, unsigned char* dataArray)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_FLOAT_BY_DENOMINATION;
	cmd->CommandData[1] = numDenoms;

	int counter = 2;
	for (int i = 0; i < numDenoms*9; ++i, ++counter)
		cmd->CommandData[counter] = dataArray[i];

	cmd->CommandData[counter++] = 0x58; // real float (0x19 for test)

	cmd->CommandDataLength = counter;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Floating by denomination..." << endl;
	THREAD_UNLOCK
	return true;
}

// This uses the EMPTY command to instruct the SMART Hopper to dump all stored
// coins into the cashbox. The SMART Hopper will not keep a track of what coins
// have been emptied.
bool CHopper::Empty()
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
bool CHopper::SMARTEmpty()
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

// This function uses the SET COIN MECH INHIBITS command to set the mech to accept
// specified coins. 0x01 is accept coin, 0x00 is reject. If the coin mech does not
// support accepting a coin and the developer tries to alter inhibiting on this coin
// the unit will return a 0xF5 error (command cannot be processed). This response will
// also be returned when sending this command with no mech attached.
bool CHopper::SetCoinMechInhibits()
{
	THREAD_LOCK
	// Enable all coins to be accepted
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		cmd->CommandData[0] = SSP_CMD_SET_COIN_MECH_INHIBITS;
		cmd->CommandData[1] = 0x01; // 0x01 = enable acceptance, 0x00 = disable acceptance
		memcpy(cmd->CommandData+2, &m_UnitData[i].Value, 2); // 2 byte coin value
		cmd->CommandData[4] = m_UnitData[i].Currency[0];
		cmd->CommandData[5] = m_UnitData[i].Currency[1];
		cmd->CommandData[6] = m_UnitData[i].Currency[2];
		cmd->CommandDataLength = 7;

		if (!SendCommand())
		{
			THREAD_UNLOCK
			return false;
		}

		if (CheckGenericResponses())
			*m_Output << "Mech inhibits on channel " << i+1 << " set" << endl;
	}

	// If firmware version is >= 6.05 the global inhibits command can be used instead
	// of changing inhibits on each channel seperately.
	/*
	cmd->CommandData[0] = SSP_CMD_COIN_MECH_GLOBAL_INHIBIT;
	cmd->CommandData[1] = 0x01; // enabled
	if (!SendCommand()) return false;
	if (CheckGenericResponses())
		*m_Output << "Set global inhibit on coin mech" << endl;
	*/
	THREAD_UNLOCK
	return true;
}

// This method sends the GET COIN AMOUNT command which uses the value and currency of
// a denomination of coin. The response will contain a 2 byte value indicating the
// number of that type of coin stored in the Hopper.
bool CHopper::GetCoinLevel(int value, const string& currency, int* returnAmount)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_GET_COIN_AMOUNT;
	memcpy(&cmd->CommandData[1], &value, 4);
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
		*returnAmount = cmd->ResponseData[1];
		*returnAmount += cmd->ResponseData[2] << 8;
	}
	THREAD_UNLOCK
	return true;
}

// This method is used to increment the level of a specified coin in the Hopper.
// The user passes the value and the currency of the coin and the amount they wish
// to increment it by. If the user attempts to set a zero level to a coin then the
// level will be set to zero rather than incremented by zero.
bool CHopper::SetCoinLevel(int value, const string& currency, int level)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_COIN_AMOUNT;
	memcpy(cmd->CommandData + 1, &level, 2);
	memcpy(cmd->CommandData + 3, &value, 4);
	cmd->CommandData[7] = currency[0];
	cmd->CommandData[8] = currency[1];
	cmd->CommandData[9] = currency[2];
	cmd->CommandDataLength = 10;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "Changed " << value*0.01 << " " << currency << " level\n";
	THREAD_UNLOCK
	return true;
}

// Method that outputs a formatted string of the level of each denomination along with
// their currency and their total value in the Hopper.
bool CHopper::OutputLevelInfo()
{
	// Go through each channel
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		// Get the level of each coin
		int temp = 0;
		if (!GetCoinLevel(m_UnitData[i].Value, m_UnitData[i].Currency, &temp)) return false;
		// Output
		*m_Output << m_UnitData[i].Value * 0.01f << " " << m_UnitData[i].Currency[0] <<
			m_UnitData[i].Currency[1] << m_UnitData[i].Currency[2] << "[" << temp << "] = " <<
			(temp * m_UnitData[i].Value) * 0.01f << endl;
	}
	return true;
}

// This method send the GET ROUTING command to find out whether a specified denomination
// of coin is being sent to the cashbox or being recycled for payout.
bool CHopper::GetCoinRecycling(unsigned int value, const string& currency, bool* isRecycling)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_GET_ROUTING;
	memcpy(&cmd->CommandData[1], &value, 4);
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
		if (cmd->ResponseData[1] == (char)0x00)
			*isRecycling = true;
		else
			*isRecycling = false;
	}
	THREAD_UNLOCK
	return true;
}

// This method is used to send the SET ROUTING command in order to change
// where a specified denomination of coin is being routed to. If the second
// byte is 0x00 the coin is recycled, 0x01 and it is sent to cashbox.
bool CHopper::SendCoinToCashbox(int value, const string& currency)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_ROUTING;
	cmd->CommandData[1] = 0x01;
	memcpy(&cmd->CommandData[2], &value, 4);
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
		*m_Output << "Routed " << value*0.01f << " " << currency << " to cashbox" << endl;
	THREAD_UNLOCK
	return true;
}

// See above commenting
bool CHopper::SendCoinToStorage(int value, const string& currency)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_ROUTING;
	cmd->CommandData[1] = 0x00;
	memcpy(&cmd->CommandData[2], &value, 4);
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
		*m_Output << "Routed " << value*0.01f << " " << currency << " to storage" << endl;
	THREAD_UNLOCK
	return true;
}

// This function uses the GET CASHBOX PAYOUT OPERATION DATA command which
// instructs the SMART Hopper to report the number of coins moved and their
// denominations in the last cashbox operation. This could be a dispense, float
// or SMART empty.
bool CHopper::GetCashboxPayoutOpData()
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
		
			*m_Output << "Moved " << numMoved << " x " << value * 0.01f << 
			" " << cmd->ResponseData[i+7] << cmd->ResponseData[i+8] << cmd->ResponseData[i+9] << 
			endl;
		}
	}
	THREAD_UNLOCK
	return true;
}

// This method updates the unit data internal structure to match the validators stored info
bool CHopper::UpdateData()
{
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		if (!GetCoinRecycling(m_UnitData[i].Value, m_UnitData[i].Currency, &m_UnitData[i].Recycling))
			return false;
	}
	return true;
}

// The poll function is called repeatedly to poll the hopper for information, it returns as
// a response in the command structure what events are currently happening.
bool CHopper::DoPoll()
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

	// If 'key not set' response is received the unit could have briefly lost power
	// Return false here to reconnect
	if (cmd->ResponseData[0] == 0xFA)
	{
		THREAD_UNLOCK
		return false;
	}

	CheckGenericResponses();

	// backup poll response
	m_PollResponseLength = cmd->ResponseDataLength;
	memcpy(m_PollResponse, cmd->ResponseData, m_PollResponseLength);

    // parse poll response
    for (int i = 1; i < m_PollResponseLength; ++i)
    {
		int coinValue = 0;
		string coinCurrency = "";
        switch (m_PollResponse[i])
        {
			// The unit is in its disabled state
			case SSP_POLL_DISABLED:
				*m_Output << "Unit disabled" << endl;
				break;
			// A coin credit has been sent from the attached coin mechanism
            case SSP_POLL_COIN_CREDIT:
				coinValue = GetChannelValue(m_PollResponse[i+1]);
				coinCurrency = GetChannelCurrency(m_PollResponse[i+1]);
				*m_Output << "Credit " << coinValue << " " << coinCurrency << endl;
                ++i;
                break;
			// The unit has detected a fraud attempt
            case SSP_POLL_FRAUD_ATTEMPT:
                *m_Output << "Fraud attempt" << endl;
                ++i;
                break;
			// The unit has been reset since the last time a poll was sent
			case SSP_POLL_RESET:
				*m_Output << "Unit reset" << endl;
				break;
			// The unit is in the process of dispensing coins
			case SSP_POLL_DISPENSING:
				{
					*m_Output << "Dispensing:\n";
					int tracker = i + 2;
					// Go through each dispensing amount and currency
					for (int j = 0; j < m_PollResponse[i+1]; ++j)
					{
						// Get value
						coinValue = m_PollResponse[tracker++];
						coinValue += m_PollResponse[tracker++] << 8;
						coinValue += m_PollResponse[tracker++] << 16;
						coinValue += m_PollResponse[tracker++] << 24;
						*m_Output << coinValue * 0.01f << " ";

						// Get currency
						coinCurrency = m_PollResponse[tracker++];
						coinCurrency += m_PollResponse[tracker++];
						coinCurrency += m_PollResponse[tracker++];
						*m_Output << coinCurrency << endl;
					}
					*m_Output << endl;
					i += m_PollResponse[i+1] * 7 + i;
					break;
				}
				break;
			// The unit has finished dispensing coins
			case SSP_POLL_DISPENSED:
				{
					*m_Output << "Dispensed:\n";
					int tracker = i + 2;
					// Go through each dispensed amount and currency
					for (int j = 0; j < m_PollResponse[i+1]; ++j)
					{
						// Get value
						coinValue = m_PollResponse[tracker++];
						coinValue += m_PollResponse[tracker++] << 8;
						coinValue += m_PollResponse[tracker++] << 16;
						coinValue += m_PollResponse[tracker++] << 24;
						*m_Output << coinValue * 0.01f << " ";

						// Get currency
						coinCurrency = m_PollResponse[tracker++];
						coinCurrency += m_PollResponse[tracker++];
						coinCurrency += m_PollResponse[tracker++];
						*m_Output << coinCurrency << endl;
					}
					*m_Output << endl;
					i += m_PollResponse[i+1] * 7 + i;
					break;
				}
			// The unit has no coins stored
			case SSP_POLL_EMPTY:
				*m_Output << "Unit empty" << endl;
				break;
			// Appears at the end of an operation that could have deposited coins in
			// the cashbox
			case SSP_POLL_CASHBOX_PAID:
				{
					*m_Output << "Cashbox paid:\n";
					int tracker = i + 2;
					// Go through each dispensed amount and currency
					for (int j = 0; j < m_PollResponse[i+1]; ++j)
					{
						// Get value
						coinValue = m_PollResponse[tracker++];
						coinValue += m_PollResponse[tracker++] << 8;
						coinValue += m_PollResponse[tracker++] << 16;
						coinValue += m_PollResponse[tracker++] << 24;
						*m_Output << coinValue * 0.01f << " ";

						// Get currency
						coinCurrency = m_PollResponse[tracker++];
						coinCurrency += m_PollResponse[tracker++];
						coinCurrency += m_PollResponse[tracker++];
						*m_Output << coinCurrency << endl;
					}
					*m_Output << endl;
					i += m_PollResponse[i+1] * 7 + i;
					break;
				}
			// The unit is jammed
			case SSP_POLL_JAMMED:
				*m_Output << "Unit jammed" << endl;
				break;
			// An operation the unit was performing has been halted by sending the halt command
			case SSP_POLL_HALTED:
				*m_Output << "Operation halted" << endl;
				break;
			// The attached coin mechanism is jammed
			case SSP_POLL_COIN_MECH_JAMMED:
				*m_Output << "Coin mech jammed" << endl;
				break;
			// The unit is in the process of emptying its coins to the cashbox
			case SSP_POLL_EMPTYING:
				*m_Output << "Emptying..." << endl;
				break;
			// The unit has emptied all its coins to the cashbox
			case SSP_POLL_EMPTIED:
				*m_Output << "Unit emptied" << endl;
				break;
			// The unit is emptying its coins to the cashbox and keeping
			// a count of what was emptied.
			case SSP_POLL_SMART_EMPTYING:
				*m_Output << "SMART emptying..." << endl;
				break;
			// The unit has emptied its coins to the cashbox and kept
			// a count of what was emptied.
			case SSP_POLL_SMART_EMPTIED:
				*m_Output << "SMART emptied" << endl;
				THREAD_UNLOCK
				GetCashboxPayoutOpData(); // Output info about what was emptied
				THREAD_LOCK
				break;
            default:
				*m_Output << "WARNING: Unrecognised poll response detected: " << (int)cmd->ResponseData[i] << endl;
                break;
        }
    }
	THREAD_UNLOCK
    return true;
}

/* Non-Command functions */

// This function calls the open com port function of the SSP library and sets up
// the command structure.
bool CHopper::OpenComPort(char portNum)
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
bool CHopper::CheckGenericResponses()
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
					if (cmd->ResponseDataLength > 1) // If there is additional data with the response
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
// false the command never reached the validator.
bool CHopper::SendCommand()
{
	// setup info structure
	info->CommandName = (unsigned char*)GetCommandName(cmd->CommandData[0]);

    // attempt to send the command
    if (SSPSendCommand(cmd, info) == 0)
    {
		// If the command fails
        ClosePort(); // close the com port
		*m_Output << "Failed to send command, port status: ";
		*m_Output << DecodeResponseStatus(cmd->ResponseStatus)<< endl;
		m_Log->UpdateLog(info);
        return false;
    }
	m_Log->UpdateLog(info);
    return true;
}

// This takes a response byte and decodes it into a string and returns
string CHopper::DecodeResponseStatus(char statusByte)
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

// Takes a byte and converts it to a command name 
char* CHopper::GetCommandName(char commandByte)
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

// This function sets up the command structure and connects to the validator.
bool CHopper::ConnectToHopper(const SSP_COMMAND& commandStructure, int protocolVersion, int attempts)
{
	THREAD_LOCK
	// Setup command structure
	memcpy(cmd, &commandStructure, sizeof(SSP_COMMAND));
	THREAD_UNLOCK

	for (int i = 0; i < attempts; ++i)
	{
		// Close port in case it was left open
		THREAD_LOCK
		ClosePort();
		THREAD_UNLOCK

		// Open the com port
		if (!OpenComPort(cmd->PortNumber))
		{
			*m_Output << "Failed to open port " << (int)cmd->PortNumber << endl;
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

		// Set the inhibits on the coin mech
		if (!SetCoinMechInhibits())
		{
			*m_Output << "Failed on setting coin mech inhibits..." << endl;
			continue;
		}
		return true;
	}
	return false;
}

// Remove the "x. COM" from the com port string to leave only the number
char* CHopper::FormatComPort(string* comPort)
{
	if (!WaitForRelease()) return null;
	char* c = (char*)comPort->erase(0, 6).c_str();
	THREAD_UNLOCK
	return c;
}

// Threading critical section functions

// This method is called when a thread needs to modify the class variables, it
// locks the instance until the release method is called. This method will return
// false if the thread is waiting for access for too long, the timeout period is
// set by the command structure timeout (1000ms in this SDK) and the retry level
// (3 in this SDK). The thread timeout needs to be longer than this to ensure it is not
// a normal command timeout.
bool CHopper::WaitForRelease()
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

// This method should be called when the method has finished modifying class variables.
// Calling this method releases the class for other threads.
void CHopper::Release()
{
	m_ThreadLock = false;
}