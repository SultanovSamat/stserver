#include "CNV11.h"

// This method is used to load the library and link the function pointers
// defined in SSPInclude.h
bool CNV11::InitialiseLibrary()
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

		// Sends a command to the unit
		SSPSendCommand = (LPFNDLLFUNC3)GetProcAddress(hInst, "SSPSendCommand");
		if (SSPSendCommand == NULL)
		{
			FreeLibrary(hInst);
			THREAD_UNLOCK
			return false;
		}

		// Creates the generator and modulus prime numbers
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

// The enable command allows the validator to begin accepting and acting on commands.
bool CNV11::EnableValidator()
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

// Disable command stops the validator acting on commands.
bool CNV11::DisableValidator() 
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

// The enable payout command allows the validator to store and payout notes.
bool CNV11::EnablePayout()
{
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_ENABLE_PAYOUT;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}
    // check response
	if (CheckGenericResponses())
		*m_Output << "Enabled payout" << endl;
	THREAD_UNLOCK
	return true;
}

// Disable command stops the validator storing and paying out notes.
bool CNV11::DisablePayout() 
{
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_DISABLE_PAYOUT;
    cmd->CommandDataLength = 1;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}
    // check response
    if (CheckGenericResponses())
		*m_Output << "Disabled payout" << endl;
	THREAD_UNLOCK
	return true;
}

// The reset command instructs the validator to restart (same effect as switching on and off)
bool CNV11::ResetValidator()
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
bool CNV11::SetProtocolVersion(char pVersion)
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

// This function sends the command LAST REJECT CODE which gives info about why a note has been rejected. It then
// outputs the info.
bool CNV11::QueryRejection()
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

// This function performs a number of commands in order to setup the encryption between the host and the validator.
bool CNV11::NegotiateKeys()
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

// This function uses the setup request command to get all the information about the validator.
bool CNV11::SetupRequest()
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
			case 0x00: *m_Output << "Note Validator" << endl; THREAD_UNLOCK return true;
			case 0x03: *m_Output << "SMART Hopper" << endl; THREAD_UNLOCK return true;
			case 0x06: *m_Output << "SMART Payout" << endl; THREAD_UNLOCK return true;
			case 0x07: *m_Output << "NV11" << endl; break; // No return if correct type for this SDK (NV11)
			default: *m_Output << "Unrecognised unit" << endl; THREAD_UNLOCK return true;
		}

		// Firmware
		*m_Output << "Firmware: ";
		*m_Output << cmd->ResponseData[2] << cmd->ResponseData[3] << "." <<
			cmd->ResponseData[4] << cmd->ResponseData[5] << endl;

		// Number of channels
		m_NumberOfChannels = cmd->ResponseData[12]; // end of fixed size
		int currentIndex = 13;

		// Skip old channel values and security
		currentIndex += m_NumberOfChannels * 2;

		// Create struct to hold data for each channel
		m_UnitData = new SUnitData[m_NumberOfChannels];

		// Value multiplier
		m_ValueMultiplier = 0;
		int count = 2;
		for (int i = 0; i < 3; i++, count--)
			m_ValueMultiplier += cmd->ResponseData[currentIndex + count] << (i*8);
		currentIndex += 3;

		// Protocol version
		*m_Output << "Protocol Version: " << (int)cmd->ResponseData[currentIndex++] << endl;

		// Channel currencies
		*m_Output << "Channel Currencies: ";
		for (int i = 0; i < m_NumberOfChannels; ++i)
		{
			for (int j = 0; j < 3; ++j)
			{
				m_UnitData[i].Currency[j] = (char)cmd->ResponseData[currentIndex+j];
				*m_Output << m_UnitData[i].Currency[j];
			}
			*m_Output << " ";
			currentIndex += 3;
		}
		*m_Output << endl;

		// Channel values
		*m_Output << "Channel Values: ";
		for (int i = 0; i < m_NumberOfChannels; ++i)
		{
			for (int j = 0; j < 4; ++j)
				m_UnitData[i].Value += cmd->ResponseData[currentIndex+j] << (j*8);
			*m_Output << m_UnitData[i].Value << " ";
			currentIndex += 4;

			// Add channel numbers here to make use of this loop
			m_UnitData[i].Channel = i+1;
		}
		*m_Output << endl;

		// Update data
		THREAD_UNLOCK
		UpdateData();
		THREAD_LOCK
	}
	THREAD_UNLOCK
	return true;
}

// This function sends the set inhibits command to set the inhibits on the validator.
// The two bytes after the command byte represent two bit registers with each bit being
// a channel. 1-8 and 9-16 respectively. 0xFF = 11111111 in binary indicating all channels
// in this register are able to accept notes.
bool CNV11::SetInhibits()
{
	THREAD_LOCK
    // set inhibits
    cmd->CommandData[0] = SSP_CMD_SET_INHIBITS;
    cmd->CommandData[1] = 0xFF;
    cmd->CommandData[2] = 0xFF;
    cmd->CommandDataLength = 3;

    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}
    // check response
    if (CheckGenericResponses())
		*m_Output << "Inhibits set" << endl;
	THREAD_UNLOCK
	return true;
}

// The poll function is called repeatedly to poll the validator for information, it returns as
// a response in the command structure what events are currently happening.
bool CNV11::DoPoll()
{
    // send poll
	THREAD_LOCK
    cmd->CommandData[0] = SSP_CMD_POLL;
    cmd->CommandDataLength = 1;

    if (!SendCommand()) 
	{
		THREAD_UNLOCK
		return false;
	}

	// if 'key not set' response received the unit may have been power cycled
	// so return false to reconnect
	if (cmd->ResponseData[0] == 0xFA)
	{
		THREAD_UNLOCK
		return false;
	}

	CheckGenericResponses();

	// isolate poll response to prevent the cmd structure being overwritten
	m_PollResponseLength = cmd->ResponseDataLength;
	memcpy(m_PollResponse, cmd->ResponseData, m_PollResponseLength);

    //parse poll response
    int noteVal = 0;
	char* noteCurr = null;
    for (int i = 1; i < m_PollResponseLength; ++i)
    {
        switch (m_PollResponse[i])
        {
			// The unit is disabled and will not act on certain commands.
			case SSP_POLL_DISABLED:
				*m_Output << "Unit disabled" << endl;
				break;
			// The unit has been reset since the last time a poll was sent
            case SSP_POLL_RESET:
				*m_Output << "Unit reset" << endl;
                break;
			// A note is being read, if the byte immediately following the response
			// is 0 then the note has not finished reading, if it is greater than 0
			// then this is the channel of that note.
            case SSP_POLL_NOTE_READ:
                if (m_PollResponse[i + 1] > 0)
                {
					noteVal = GetChannelValue(m_PollResponse[i + 1]);
					noteCurr = GetChannelCurrency(m_PollResponse[i + 1]);
                    *m_Output << "Note in escrow, amount: ";
					*m_Output << (float)noteVal << " " << noteCurr[0] << noteCurr[1] << noteCurr[2] << endl;
                }
                else
                    *m_Output << "Reading note" << endl;
                ++i;
                break;
			// The unit has accepted a note as valid currency
            case SSP_POLL_CREDIT:
				noteVal = GetChannelValue(m_PollResponse[i + 1]);
				noteCurr = GetChannelCurrency(m_PollResponse[i + 1]);
				*m_Output << "Credit, amount: ";
				*m_Output << (float)noteVal << " " << noteCurr[0] << noteCurr[1] << noteCurr[2] << endl;
				++m_NumStackedNotes;
                ++i;
                break;
			// The unit is in the process of rejecting a note
            case SSP_POLL_REJECTING:
                break;
			// The unit has rejected a note
            case SSP_POLL_REJECTED:
                *m_Output << "Note rejected" << endl;
				THREAD_UNLOCK
                QueryRejection(); // Get reason for reject
				THREAD_LOCK
                break;
			// The unit is stacking a note to either the recycler or the cashbox
            case SSP_POLL_STACKING:
                *m_Output << "Stacking note" << endl;
                break;
			// The unit has finished stacking the note
            case SSP_POLL_STACKED:
                *m_Output << "Note stacked" << endl;
                break;
			// A note is jammed somewhere the user cannot retrieve it
            case SSP_POLL_SAFE_JAM:
                *m_Output << "Safe jam" << endl;
                break;
			// A note is jammed in a place where it may be recoverable by the user
            case SSP_POLL_UNSAFE_JAM:
                *m_Output << "Unsafe jam" << endl;
                break;
			// The unit is jammed
			case SSP_POLL_JAMMED:
				*m_Output << "Unit jammed" << endl;
				break;
			// An operation has been halted
			case SSP_POLL_HALTED:
				*m_Output << "Unit halted" << endl;
				break;
			// A fraud attempt has been detected
            case SSP_POLL_FRAUD_ATTEMPT:
				{
					float f = (float)GetChannelValue(m_PollResponse[i + 1]);
					*m_Output << "Fraud attempt, note type: " << f << endl;
					++i;
					break;
				}
			case SSP_POLL_PAYOUT_OUT_OF_SERVICE:
				*m_Output << "The recycler is out of service" << endl;
				break;
			// The cashbox is full
            case SSP_POLL_STACKER_FULL:
                *m_Output << "Stacker full" << endl;
                break;
			// A note was in the unit at startup and was rejected from the front of the unit
            case SSP_POLL_NOTE_CLEARED_FROM_FRONT:
                *m_Output << "Note cleared from front at reset." << endl;
                ++i;
                break;
			// A note was in the unit at startup and was stacked to the cashbox
            case SSP_POLL_NOTE_CLEARED_TO_CASHBOX:
                *m_Output << "Note cleared to stacker at reset." << endl;
                ++i;
                break;
			// A note was in the recycler payout/storage process at startup and was sent to the
			// cashbox.
            case SSP_POLL_NOTE_PAID_TO_CASHBOX_ON_START:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j);
						j += 4;
						*m_Output << "Cleared " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << 
							" to cashbox from recycler on reset." << endl;
					}
					i += l - 2;
					break;
				}
			// A note was in the recycler payout/storage process at startup and was sent to the
			// recycler.
            case SSP_POLL_NOTE_PAID_TO_RECYCLER_ON_START:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j);
						j += 4;
						*m_Output << "Cleared " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << 
							" to recycler on reset." << endl;
					}
					i += l - 2;
					break;
				}
			// A note was in the recycler payout/storage process at startup and was dispensed.
			case SSP_POLL_NOTE_DISPENSED_ON_START:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j);
						j += 4;
						*m_Output << "Dispensed " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << 
							" on reset." << endl;
					}
					i += l - 2;
					break;
				}
			// The cashbox is not attached to the unit
            case SSP_POLL_CASHBOX_REMOVED:
                *m_Output << "Cashbox removed" << endl;
                break;
			// The cashbox has been refitted to the unit
            case SSP_POLL_CASHBOX_REPLACED:
                *m_Output << "Cashbox replaced" << endl;
                break;
			// The recycler unit is not attached to the NV9
            case SSP_POLL_NOTEFLOAT_REMOVED:
                *m_Output << "Recycler unit removed" << endl;
                break;
			// The recycler unit has been refitted to the NV9
            case SSP_POLL_NOTEFLOAT_ATTACHED:
                *m_Output << "Recycler unit replaced" << endl;
                break;
			// The recycler unit of the NV11 has reached maximum capacity
            case SSP_POLL_DEVICE_FULL:
                *m_Output << "Recycler unit full" << endl;
                break;
			// The unit has been opened exposing the note path.
			case SSP_POLL_NOTE_PATH_OPEN:
				*m_Output << "Note path open" << endl;
				break;
			// The unit has been disabled by inhibiting all of its channels (Protocol version >= 7)
			case SSP_POLL_CHANNEL_DISABLE:
                *m_Output << "Unit disabled (all channels inhibited)" << endl;
                break;
			// The unit is in the process of dispensing a note
			case SSP_POLL_DISPENSING:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j);
						j += 4;
						*m_Output << "Dispensing " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << "..." << endl;
					}
					i += l - 2;
					break;
				}
			// The unit has finished dispensing a note
			case SSP_POLL_DISPENSED:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j);
						j += 4;
						*m_Output << "Dispensed " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << endl;
					}
					i += l - 2;
					THREAD_UNLOCK
					EnableValidator();
					THREAD_LOCK
					break;
				}
			// The unit was interrupted while making a payout, some notes may have been dispensed
			case SSP_POLL_INCOMPLETE_PAYOUT:
				{
					int j = i + 2;
					int l = m_PollResponse[i+1] * 11 + j;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j); // Dispensed value
						int n1 = ConvertBytesToInt(m_PollResponse, j + 4); // Requested value
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
			// The unit is emptying all its stored notes to the cashbox
			case SSP_POLL_EMPTYING:
				*m_Output << "Emptying payout device" << endl;
				break;
			// The unit has finished emptying all its stored notes to the cashbox
			case SSP_POLL_EMPTIED:
				*m_Output << "Device empty" << endl;
				THREAD_UNLOCK
				EnableValidator();
				THREAD_LOCK
				break;
			// A note has been stored in the recycler for payout
			case SSP_POLL_NOTE_STORED:
				*m_Output << "Note stored for payout" << endl;
				break;
			// A note has been moved from storage to the cashbox
			case SSP_POLL_NOTE_TRANSFERRED_TO_STACKER:
				{
					int j = i + 2, l = m_PollResponse[i+1] * 7 + i + 2;
					while (j < l)
					{
						int n = ConvertBytesToInt(m_PollResponse, j);
						j += 4;
						*m_Output << "Moved " << n * 0.01f << " ";
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++];
						*m_Output << m_PollResponse[j++] << " to cashbox" << endl;
					}
					i += l - 1;
					THREAD_UNLOCK
					EnableValidator();
					THREAD_LOCK
					++m_NumStackedNotes;
					break;
				}
			// The unit has a note resting in the bezel waiting to be removed
			case SSP_POLL_NOTE_IN_BEZEL:
				{
					*m_Output << "Note held in bezel: ";
					*m_Output << ConvertBytesToInt(m_PollResponse, i + 2)/100 << " ";
					*m_Output << m_PollResponse[i + 6];
					*m_Output << m_PollResponse[i + 7];
					*m_Output << m_PollResponse[i + 8] << endl;

					i += m_PollResponse[i + 1] * 7 + 1;
					break;
				}
			// If any other poll response is caught, output it with a warning
            default:
				cout << "WARNING: Unknown poll response detected - " << hex << (int)m_PollResponse[i] << dec << endl;
				break;
        }
    }
	THREAD_UNLOCK
    return true;
}

// This function uses the SET ROUTING command to change the routing of a channel.
// The route is set based on the param "stack" which indicates whether the channel
// is stacked or stored.
bool CNV11::ChangeRouting(int noteValue, char* currency, bool stack)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_ROUTING;
	// 0x01 is cashbox, 0x00 is recycle for second byte of array
	(stack)?cmd->CommandData[1]=0x01:cmd->CommandData[1]=0x00;
	
	// Set the note
	memcpy(cmd->CommandData + 2, &noteValue, 4); 

	// Set the currency
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
		*m_Output << "Routed " << noteValue/100 << " " << 
			currency[0] << currency[1] << currency[2] <<
			" to ";
		(stack) ? *m_Output << "cashbox" : *m_Output << "storage";
		*m_Output << endl;
	}
	THREAD_UNLOCK
	return true;
}

// The command SET VALUE REPORTING TYPE sets the validator to report
// either the 4 byte note value or the single byte channel number
bool CNV11::SetValueReportingType(bool byChannel)
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_SET_VALUE_REPORTING_TYPE;
	(byChannel)?cmd->CommandData[1]=0x01:cmd->CommandData[1]=0x00;
	cmd->CommandDataLength = 2;

	if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
		*m_Output << "changed value reporting type" << endl;
	THREAD_UNLOCK
	return true;
}

// Uses the ChangeRouting() command to instruct all notes to be sent
// to the cashbox
bool CNV11::DisableAllRecycling()
{
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		if (!ChangeRouting(m_UnitData[i].Value, m_UnitData[i].Currency, true))
			return false;
	}
	return true;
}

// Pays out the next note, which will be the last note that was paid in
// and stored.
bool CNV11::PayoutNextNote()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_PAYOUT_LAST_NOTE;
	cmd->CommandDataLength = 1;

	if (!SendCommand()) 
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		*m_Output << "Paying out next note..." << endl;
	}
	THREAD_UNLOCK
	return true;
}

// Moves the next note, which will be the last one paid in and stored,
// to the cashbox.
bool CNV11::StackNextNote()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_STACK_LAST_NOTE;
	cmd->CommandDataLength = 1;

	if (!SendCommand()) 
	{
		THREAD_UNLOCK
		return false;
	}

	if (CheckGenericResponses())
	{
		*m_Output << "Stacking next note..." << endl;
	}
	THREAD_UNLOCK
	return true;
}

// Moves all the stored notes to the cashbox.
bool CNV11::EmptyPayout()
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
	{
		*m_Output << "Emptying notes from storage to cashbox..." << endl;
	}
	THREAD_UNLOCK
	return true;
}

// This function gets and outputs information on the notes stored in the
// recycler.
bool CNV11::OutputNoteLevelInfo()
{
	THREAD_LOCK
	cmd->CommandData[0] = SSP_CMD_GET_NOTE_POSITIONS;
    cmd->CommandDataLength = 1;
    if (!SendCommand())
	{
		THREAD_UNLOCK
		return false;
	}
    if (CheckGenericResponses())
    {
		if (cmd->ResponseData[1] == 0x00)
			*m_Output << "No notes stored" << endl;
		else
		{
			for (int i = 0; i < cmd->ResponseData[1]*4; i+=4)
			{
				THREAD_UNLOCK
				int n = ConvertBytesToInt(cmd->ResponseData, i+2);
				THREAD_LOCK
				*m_Output << "Position " << (i/4)+1 << ": " << n * 0.01f <<
					endl;
			}
		}
    }
	THREAD_UNLOCK
	return true;
}

// Returns whether a channel is recycling
bool CNV11::GetChannelRecycling (int channelNum, bool* recycling)
{
	THREAD_LOCK
	if (channelNum >= 0 && channelNum <= m_NumberOfChannels)
	{
		*recycling = m_UnitData[channelNum].Recycling;
		THREAD_UNLOCK
		return true;
	}
	THREAD_UNLOCK
	return false;
}

/* Non-Command functions */

// This function calls the open com port function of the SSP library and sets up
// the command structure.
bool CNV11::OpenComPort(char portNum)
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

// Takes 4 bytes as an array and converts them to a single 32 bit integer
unsigned int CNV11::ConvertBytesToInt(unsigned char* c, unsigned int index)
{
	unsigned int ret = 0, count = 0;
	for (unsigned int i = index; i < index + 4; ++i, ++count)
		ret += c[i] << (8 * count);
	return ret;
}

// Outputs the recycling status of each channel using the output stream
bool CNV11::OutputCurrentRecycling()
{
	UpdateData(); // Make sure the class instance data is synchronised with the validator
	THREAD_LOCK
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		if (m_UnitData[i].Recycling == true)
		{
			*m_Output << (float)m_UnitData[i].Value << " " <<
				m_UnitData[i].Currency[0] << m_UnitData[i].Currency[1] << m_UnitData[i].Currency[2];
			THREAD_UNLOCK
			return true;
		}
	}
	*m_Output << "No note";
	THREAD_UNLOCK
	return true;
}

/* Exception and Error Handling */

// This is used for generic response error catching, it outputs the info in a
// meaningful way. This should only be called inside a function which has handled
// threading access as it does not do this itself.
bool CNV11::CheckGenericResponses()
{
    if (cmd->ResponseData[0] == SSP_RESPONSE_CMD_OK)
	{
        return true; // Early return if response is OK
	}
    else
    {	
        switch (cmd->ResponseData[0])
        {
            case SSP_RESPONSE_CMD_CANNOT_PROCESS:
				{
					*m_Output << "Command response is CANNOT PROCESS COMMAND";
					if (cmd->ResponseDataLength > 1)
						*m_Output << ", error code - 0x" << (int)cmd->ResponseData[1];
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

// Attempts to transmit the command to the validator via the SSP Library class.
// This function should always be called from within a method that has already 
// negotiated thread access and therefore does not need to wait, lock and release
// like other functions. If this returns true, the command was successfully 
// transmitted to the validator and a response received. If it returns false there
// was an issue with the transmission or receipt, further information can be retrieved
// from the ResponseStatus variable inside the SSP_COMMAND struct.
bool CNV11::SendCommand()
{
    // attempt to send the command
    if (SSPSendCommand(cmd, info) == 0)
    {
		// On failure, close the com port
        ClosePort();
		*m_Output << "Failed to send command, port status: ";
		*m_Output << (int)cmd->ResponseStatus << endl;
		m_Log->UpdateLog(info);
        return false;
    }
	m_Log->UpdateLog(info);
    return true;
}

bool CNV11::ConnectToValidator(const SSP_COMMAND& commandStructure, int protocolVersion, int attempts)
{
	THREAD_LOCK
	// Setup command structure (cmd is CNV11 instance variable)
	cmd->BaudRate = commandStructure.BaudRate;
	cmd->SSPAddress = commandStructure.SSPAddress;
	cmd->Timeout = commandStructure.Timeout;
	cmd->RetryLevel = commandStructure.RetryLevel;
	cmd->PortNumber = commandStructure.PortNumber;
	cmd->IgnoreError = commandStructure.IgnoreError;
	
	for (int i = 0; i < attempts; ++i)
	{
		// Close port in case it was left open
		ClosePort();

		// Open the com port
		THREAD_UNLOCK
		if (!OpenComPort(cmd->PortNumber))
		{
			*m_Output << "Failed to open port number " << (int)cmd->PortNumber << endl;
			THREAD_LOCK
			continue;
		}
		THREAD_LOCK

		// Negotiate keys for encryption
		THREAD_UNLOCK
		if (!NegotiateKeys())
		{
			*m_Output << "Failed on key negotiation..." << endl;
			THREAD_LOCK
			continue;
		}
		THREAD_LOCK

		// Set the protocol version to the highest the validator supports
		// or to a value set in the connection info structure
		THREAD_UNLOCK
		if (!SetProtocolVersion(protocolVersion))
		{
			*m_Output << "Failed on setting protocol version..." << endl;
			THREAD_LOCK
			continue;
		}
		THREAD_LOCK

		// Set the inhibits
		THREAD_UNLOCK
		if (!SetInhibits())
		{
			*m_Output << "Failed on setting inhibits..." << endl;
			THREAD_LOCK
			continue;
		}
		THREAD_LOCK

		// Call setup request
		THREAD_UNLOCK
		if (!SetupRequest())
		{
			*m_Output << "Failed on setup request..." << endl;
			THREAD_LOCK
			continue;
		}
		THREAD_LOCK

		// Check the unit is of the right type
		THREAD_UNLOCK
		if (!UnitCheck())
		{
			*m_Output << "Wrong unit connected, this SDK supports NV11 only" << endl;
			return false;
		}
		THREAD_LOCK

		// Enable payout
		THREAD_UNLOCK
		if (!EnablePayout())
		{
			*m_Output << "Failed to enable payout..." << endl;
			THREAD_LOCK
			continue;
		}
		return true;
	}
	cout << "Failed to connect to unit" << endl;
	THREAD_UNLOCK
	return false;
}

// Removes the "x. COM" from the com port string
char* CNV11::FormatComPort(string* comPort)
{
	THREAD_LOCK
	return (char*)comPort->erase(0, 6).c_str();
	THREAD_UNLOCK
}

// This method is responsible for updating the internal data structure
// to make sure it is synchronised with the unit
bool CNV11::UpdateData()
{
	THREAD_LOCK
	// Update recycling
	for (int i = 0; i < m_NumberOfChannels; ++i)
	{
		cmd->CommandData[0] = SSP_CMD_GET_ROUTING;
		int n = m_UnitData[i].Value * 100;
		memcpy(cmd->CommandData + 1, &n, 4);
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
			(cmd->ResponseData[1] == 0)?m_UnitData[i].Recycling=true:m_UnitData[i].Recycling=false;
	}
	THREAD_UNLOCK
	return true;
}

/* Threading Methods */

// These methods are used to lock and unlock the instance of this class. The wait method should be called
// before any data is modified to check a thread is not already inside the instance. When a thread has
// finished with a method, it should call the release method to allow other threads to access the instance.

// Releases the calling thread's timeslice when the instance locked, on success it locks the 
// instance. If this method returns false then the thread was unable to access the instance for
// 3500ms (command timeout in this SDK is 1000ms with a retry level of 3) so this is triggered after a command
// transmission timeout.
bool CNV11::WaitForRelease()
{
	clock_t start, final = 0;
	while (m_ThreadLock)
	{
		start = clock();
		Sleep(0);
		final += clock() - start;
		if (final > 3500)
		{
			cout << "A thread timed out..." << endl;
			return false;
		}
	}
	return m_ThreadLock = true;
}

// Releases the instance for other threads
void CNV11::Release()
{
	m_ThreadLock = false;
}