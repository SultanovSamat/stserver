using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Timers;
using ITLlib;

namespace eSSP_example
{
    public class CValidator
    {
        // ssp library variables
        SSPComms eSSP;
        SSP_COMMAND cmd, storedCmd;
        SSP_KEYS keys;
        SSP_FULL_KEY sspKey;
        SSP_COMMAND_INFO info;

        // variable declarations

        // The comms window class, used to log everything sent to the validator visually and to file
        CCommsWindow m_Comms; 

        // A variable to hold the type of validator, this variable is initialised using the setup request command
        char m_UnitType;

        // Two variables to hold the number of notes accepted by the validator and the value of those
        // notes when added up
        int m_NumStackedNotes;
        
        // Variable to hold the number of channels in the validator dataset
        int m_NumberOfChannels;

        // The multiplier by which the channel values are multiplied to give their
        // real penny value. E.g. £5.00 on channel 1, the value would be 5 and the multiplier
        // 100.
        int m_ValueMultiplier;

        // A list of dataset data, sorted by value. Holds the info on channel number, value, currency,
        // level and whether it is being recycled.
        List<ChannelData> m_UnitDataList;

        // constructor
        public CValidator()
        {
            eSSP = new SSPComms();
            cmd = new SSP_COMMAND();
            storedCmd = new SSP_COMMAND();
            keys = new SSP_KEYS();
            sspKey = new SSP_FULL_KEY();
            info = new SSP_COMMAND_INFO();
            
            m_Comms = new CCommsWindow("NoteValidator");
            m_NumberOfChannels = 0;
            m_ValueMultiplier = 1;
            m_UnitType = (char)0xFF;
            m_UnitDataList = new List<ChannelData>();
        }

        /* Variable Access */

        // access to ssp variables
        // the pointer which gives access to library functions such as open com port, send command etc
        public SSPComms SSPComms
        {
            get { return eSSP; }
            set { eSSP = value; }
        }

        // a pointer to the command structure, this struct is filled with info and then compiled into
        // a packet by the library and sent to the validator
        public SSP_COMMAND CommandStructure
        {
            get { return cmd; }
            set { cmd = value; }
        }

        // pointer to an information structure which accompanies the command structure
        public SSP_COMMAND_INFO InfoStructure
        {
            get { return info; }
            set { info = value; }
        }

        // access to the comms log for recording new log messages
        public CCommsWindow CommsLog
        {
            get { return m_Comms; }
            set { m_Comms = value; }
        }

        // access to the type of unit, this will only be valid after the setup request
        public char UnitType
        {
            get { return m_UnitType; }
        }

        // access to number of channels being used by the validator
        public int NumberOfChannels
        {
            get { return m_NumberOfChannels; }
            set { m_NumberOfChannels = value; }
        }

        // access to number of notes stacked
        public int NumberOfNotesStacked
        {
            get { return m_NumStackedNotes; }
            set { m_NumStackedNotes = value; }
        }

        // access to value multiplier
        public int Multiplier
        {
            get { return m_ValueMultiplier; }
            set { m_ValueMultiplier = value; }
        }

        // get a channel value
        public int GetChannelValue(int channelNum)
        {
            if (channelNum >= 1 && channelNum <= m_NumberOfChannels)
            {
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channelNum)
                        return d.Value;
                }
            }
            return -1;
        }

        // get a channel currency
        public string GetChannelCurrency(int channelNum)
        {
            if (channelNum >= 1 && channelNum <= m_NumberOfChannels)
            {
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channelNum)
                        return new string(d.Currency);
                }
            }
            return "";
        }

        /* Command functions */

        // The enable command allows the validator to receive and act on commands sent to it.
        public void EnableValidator(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.SSP_CMD_ENABLE;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            // check response
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Unit enabled\r\n");
        }

        // Disable command stops the validator from acting on commands.
        public void DisableValidator(TextBox log = null) 
        {
            cmd.CommandData[0] = CCommands.SSP_CMD_DISABLE;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            // check response
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Unit disabled\r\n");
        }

        // The reset command instructs the validator to restart (same effect as switching on and off)
        public void Reset(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.SSP_CMD_RESET;
            cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return;

            if(CheckGenericResponses(log))
                log.AppendText("Resetting unit\r\n");
        }

        // This command just sends a sync command to the validator
        public bool SendSync(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.SSP_CMD_SYNC;
            cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return false;

            if (CheckGenericResponses(log))
                log.AppendText("Successfully sent sync\r\n");
            return true;
        }

        // This function sets the protocol version in the validator to the version passed across. Whoever calls
        // this needs to check the response to make sure the version is supported.
        public void SetProtocolVersion(byte pVersion, TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.SSP_CMD_HOST_PROTOCOL_VERSION;
            cmd.CommandData[1] = pVersion;
            cmd.CommandDataLength = 2;
            if (!SendCommand(log)) return;
        }

        // This function sends the command LAST REJECT CODE which gives info about why a note has been rejected. It then
        // outputs the info to a passed across textbox.
        public void QueryRejection(TextBox log)
        {
            cmd.CommandData[0] = CCommands.SSP_CMD_LAST_REJECT_CODE;
            cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return;

            if (CheckGenericResponses(log))
            {
                if (log == null) return;
                switch (cmd.ResponseData[1])
                {
                    case 0x00: log.AppendText("Note accepted\r\n"); break;
                    case 0x01: log.AppendText("Note length incorrect\r\n"); break;
                    case 0x02: log.AppendText("Invalid note\r\n"); break;
                    case 0x03: log.AppendText("Invalid note\r\n"); break;
                    case 0x04: log.AppendText("Invalid note\r\n"); break;
                    case 0x05: log.AppendText("Invalid note\r\n"); break;
                    case 0x06: log.AppendText("Channel inhibited\r\n"); break;
                    case 0x07: log.AppendText("Second note inserted during read\r\n"); break;
                    case 0x08: log.AppendText("Host rejected note\r\n"); break;
                    case 0x09: log.AppendText("Invalid note\r\n"); break;
                    case 0x0A: log.AppendText("Invalid note read\r\n"); break;
                    case 0x0B: log.AppendText("Note too long\r\n"); break;
                    case 0x0C: log.AppendText("Validator disabled\r\n"); break;
                    case 0x0D: log.AppendText("Mechanism slow/stalled\r\n"); break;
                    case 0x0E: log.AppendText("Strim attempt\r\n"); break;
                    case 0x0F: log.AppendText("Fraud channel reject\r\n"); break;
                    case 0x10: log.AppendText("No notes inserted\r\n"); break;
                    case 0x11: log.AppendText("Invalid note read\r\n"); break;
                    case 0x12: log.AppendText("Twisted note detected\r\n"); break;
                    case 0x13: log.AppendText("Escrow time-out\r\n"); break;
                    case 0x14: log.AppendText("Bar code scan fail\r\n"); break;
                    case 0x15: log.AppendText("Invalid note read\r\n"); break;
                    case 0x16: log.AppendText("Invalid note read\r\n"); break;
                    case 0x17: log.AppendText("Invalid note read\r\n"); break;
                    case 0x18: log.AppendText("Invalid note read\r\n"); break;
                    case 0x19: log.AppendText("Incorrect note width\r\n"); break;
                    case 0x1A: log.AppendText("Note too short\r\n"); break;
                }
            }
        }

        // This function performs a number of commands in order to setup the encryption between the host and the validator.
        public bool NegotiateKeys(TextBox log = null)
        {
            byte i;

            // make sure encryption is off
            cmd.EncryptionStatus = false;

            // send sync
            if (log != null) log.AppendText("Syncing... ");
            cmd.CommandData[0] = CCommands.SSP_CMD_SYNC;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success");

            eSSP.InitiateSSPHostKeys(keys, cmd);

            // send generator
            cmd.CommandData[0] = CCommands.SSP_CMD_SET_GENERATOR;
            cmd.CommandDataLength = 9;
            if (log != null) log.AppendText("Setting generator... ");
            for (i = 0; i < 8; i++)
            {
                cmd.CommandData[i + 1] = (byte)(keys.Generator >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success\r\n");

            // send modulus
            cmd.CommandData[0] = CCommands.SSP_CMD_SET_MODULUS;
            cmd.CommandDataLength = 9;
            if (log != null) log.AppendText("Sending modulus... ");
            for (i = 0; i < 8; i++)
            {
                cmd.CommandData[i + 1] = (byte)(keys.Modulus >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success\r\n");

            // send key exchange
            cmd.CommandData[0] = CCommands.SSP_CMD_KEY_EXCHANGE;
            cmd.CommandDataLength = 9;
            if (log != null) log.AppendText("Exchanging keys... ");
            for (i = 0; i < 8; i++)
            {
                cmd.CommandData[i + 1] = (byte)(keys.HostInter >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success\r\n");

            keys.SlaveInterKey = 0;
            for (i = 0; i < 8; i++)
            {
                keys.SlaveInterKey += (UInt64)cmd.ResponseData[1 + i] << (8 * i);
            }

            eSSP.CreateSSPHostEncryptionKey(keys);

            // get full encryption key
            cmd.Key.FixedKey = 0x0123456701234567;
            cmd.Key.VariableKey = keys.KeyHost;

            if (log != null) log.AppendText("Keys successfully negotiated\r\n");

            return true;
        }

        // This function uses the setup request command to get all the information about the validator.
        public void SetupRequest(TextBox log = null)
        {
            // send setup request
            cmd.CommandData[0] = CCommands.SSP_CMD_SETUP_REQUEST;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;

            // display setup request
            string displayString = "Unit Type: ";
            int index = 1;

            // unit type (table 0-1)
            m_UnitType = (char)cmd.ResponseData[index++];
            switch (m_UnitType)
            {
                case (char)0x00: displayString += "Validator"; break;
                case (char)0x03: displayString += "SMART Hopper"; break;
                case (char)0x06: displayString += "SMART Payout"; break;
                case (char)0x07: displayString += "NV11"; break;
                default: displayString += "Unknown Type"; break;
            }

            displayString += "\r\nFirmware: ";

            // firmware (table 2-5)
            while (index <= 5)
            {
                displayString += (char)cmd.ResponseData[index++];
                if (index == 4)
                    displayString += ".";
            }

            // country code (table 6-8)
            // this is legacy code, in protocol version 6+ each channel has a seperate currency
            index = 9; // to skip country code

            // value multiplier (table 9-11) 
            // also legacy code, a real value multiplier appears later in the response
            index = 12; // to skip value multiplier

            displayString += "\r\nNumber of Channels: ";
            int numChannels = cmd.ResponseData[index++];
            m_NumberOfChannels = numChannels;

            displayString += numChannels + "\r\n";
            // channel values (table 13 to 13+n)
            // the channel values located here in the table are legacy, protocol 6+ provides a set of expanded
            // channel values.
            index = 13 + m_NumberOfChannels; // Skip channel values

            // channel security (table 13+n to 13+(n*2))
            // channel security values are also legacy code
            index = 13 + (m_NumberOfChannels * 2); // Skip channel security

            displayString += "Real Value Multiplier: ";

            // real value multiplier (table 13+(n*2) to 15+(n*2))
            // (big endian)
            m_ValueMultiplier = cmd.ResponseData[index + 2];
            m_ValueMultiplier += cmd.ResponseData[index + 1] << 8;
            m_ValueMultiplier += cmd.ResponseData[index] << 16;
            displayString += m_ValueMultiplier + "\r\nProtocol Version: ";
            index += 3;

            // protocol version (table 16+(n*2))
            index = 16 + (m_NumberOfChannels * 2);
            int protocol = cmd.ResponseData[index++];
            displayString += protocol + "\r\n";

            // protocol 6+ only

            // channel currency country code (table 17+(n*2) to 17+(n*5))
            index = 17 + (m_NumberOfChannels * 2);
            int sectionEnd = 17 + (m_NumberOfChannels * 5);
            int count = 0;
            byte[] channelCurrencyTemp = new byte[3 * m_NumberOfChannels];
            while (index < sectionEnd)
            {
                displayString += "Channel " + ((count / 3) + 1) + ", currency: ";
                channelCurrencyTemp[count] = cmd.ResponseData[index++];
                displayString += (char)channelCurrencyTemp[count++];
                channelCurrencyTemp[count] = cmd.ResponseData[index++];
                displayString += (char)channelCurrencyTemp[count++];
                channelCurrencyTemp[count] = cmd.ResponseData[index++];
                displayString += (char)channelCurrencyTemp[count++];
                displayString += "\r\n";
            }

            // expanded channel values (table 17+(n*5) to 17+(n*9))
            index = sectionEnd;
            displayString += "Expanded channel values:\r\n";
            sectionEnd = 17 + (m_NumberOfChannels * 9);
            int n = 0;
            count = 0;
            int[] channelValuesTemp = new int[m_NumberOfChannels];
            while (index < sectionEnd)
            {
                n = CHelpers.ConvertBytesToInt32(cmd.ResponseData, index);
                channelValuesTemp[count] = n;
                index += 4;
                displayString += "Channel " + ++count + ", value = " + n + "\r\n";
            }

            // Create list entry for each channel
            m_UnitDataList.Clear(); // clear old table
            for (byte i = 0; i < m_NumberOfChannels; i++)
            {
                ChannelData d = new ChannelData();
                d.Channel = i;
                d.Channel++; // Offset from array index by 1
                d.Value = channelValuesTemp[i] * Multiplier;
                d.Currency[0] = (char)channelCurrencyTemp[0 + (i * 3)];
                d.Currency[1] = (char)channelCurrencyTemp[1 + (i * 3)];
                d.Currency[2] = (char)channelCurrencyTemp[2 + (i * 3)];
                d.Level = 0; // Can't store notes 
                d.Recycling = false; // Can't recycle notes

                m_UnitDataList.Add(d);
            }

            // Sort the list of data by the value.
            m_UnitDataList.Sort(delegate(ChannelData d1, ChannelData d2) { return d1.Value.CompareTo(d2.Value); });

            if (log != null)
                log.AppendText(displayString);
        }

        // This function sends the set inhibits command to set the inhibits on the validator. An additional two
        // bytes are sent along with the command byte to indicate the status of the inhibits on the channels.
        // For example 0xFF and 0xFF in binary is 11111111 11111111. This indicates all 16 channels supported by
        // the validator are uninhibited. If a user wants to inhibit channels 8-16, they would send 0x00 and 0xFF.
        public void SetInhibits(TextBox log = null)
        {
            // set inhibits
            cmd.CommandData[0] = CCommands.SSP_CMD_SET_INHIBITS;
            cmd.CommandData[1] = 0xFF;
            cmd.CommandData[2] = 0xFF;
            cmd.CommandDataLength = 3;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
            {
                log.AppendText("Inhibits set\r\n");
            }
        }

        // The poll function is called repeatedly to poll to validator for information, it returns as
        // a response in the command structure what events are currently happening.
        public bool DoPoll(TextBox log)
        {
            byte i;

            //send poll
            cmd.CommandData[0] = CCommands.SSP_CMD_POLL;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return false;

            //parse poll response
            int noteVal = 0;
            for (i = 1; i < cmd.ResponseDataLength; i++)
            {
                switch (cmd.ResponseData[i])
                {
                    // This response indicates that the unit was reset and this is the first time a poll
                    // has been called since the reset.
                    case CCommands.SSP_POLL_RESET:
                        log.AppendText("Unit reset\r\n"); 
                        break;
                    // A note is currently being read by the validator sensors. The second byte of this response
                    // is zero until the note's type has been determined, it then changes to the channel of the 
                    // scanned note.
                    case CCommands.SSP_POLL_NOTE_READ:
                        if (cmd.ResponseData[i + 1] > 0)
                        {
                            noteVal = GetChannelValue(cmd.ResponseData[i + 1]);
                            log.AppendText("Note in escrow, amount: " + CHelpers.FormatToCurrency(noteVal) + "\r\n");
                        }
                        else
                            log.AppendText("Reading note...\r\n");
                        i++;
                        break;
                    // A credit event has been detected, this is when the validator has accepted a note as legal currency.
                    case CCommands.SSP_POLL_CREDIT:
                        noteVal = GetChannelValue(cmd.ResponseData[i + 1]);
                        log.AppendText("Credit " + CHelpers.FormatToCurrency(noteVal) + "\r\n");
                        m_NumStackedNotes++;
                        i++;
                        break;
                    // A note is being rejected from the validator. This will carry on polling while the note is in transit.
                    case CCommands.SSP_POLL_REJECTING:
                        log.AppendText("Rejecting note...\r\n");
                        break;
                    // A note has been rejected from the validator, the note will be resting in the bezel. This response only
                    // appears once.
                    case CCommands.SSP_POLL_REJECTED:
                        log.AppendText("Note rejected\r\n");
                        QueryRejection(log);
                        break;
                    // A note is in transit to the cashbox.
                    case CCommands.SSP_POLL_STACKING:
                        log.AppendText("Stacking note...\r\n");
                        break;
                    // A note has reached the cashbox.
                    case CCommands.SSP_POLL_STACKED:
                        log.AppendText("Note stacked\r\n");
                        break;
                    // A safe jam has been detected. This is where the user has inserted a note and the note
                    // is jammed somewhere that the user cannot reach.
                    case CCommands.SSP_POLL_SAFE_JAM:
                        log.AppendText("Safe jam\r\n");
                        break;
                    // An unsafe jam has been detected. This is where a user has inserted a note and the note
                    // is jammed somewhere that the user can potentially recover the note from.
                    case CCommands.SSP_POLL_UNSAFE_JAM:
                        log.AppendText("Unsafe jam\r\n");
                        break;
                    // The validator is disabled, it will not execute any commands or do any actions until enabled.
                    case CCommands.SSP_POLL_DISABLED:
                        break;
                    // A fraud attempt has been detected. The second byte indicates the channel of the note that a fraud
                    // has been attempted on.
                    case CCommands.SSP_POLL_FRAUD_ATTEMPT:
                        log.AppendText("Fraud attempt, note type: " + GetChannelValue(cmd.ResponseData[i + 1]) + "\r\n");
                        i++;
                        break;
                    // The stacker (cashbox) is full. 
                    case CCommands.SSP_POLL_STACKER_FULL:
                        log.AppendText("Stacker full\r\n");
                        break;
                    // A note was detected somewhere inside the validator on startup and was rejected from the front of the
                    // unit.
                    case CCommands.SSP_POLL_NOTE_CLEARED_FROM_FRONT:
                        log.AppendText(GetChannelValue(cmd.ResponseData[i + 1]) + " note cleared from front at reset." + "\r\n");
                        i++;
                        break;
                    // A note was detected somewhere inside the validator on startup and was cleared into the cashbox.
                    case CCommands.SSP_POLL_NOTE_CLEARED_TO_CASHBOX:
                        log.AppendText(GetChannelValue(cmd.ResponseData[i + 1]) + " note cleared to stacker at reset." + "\r\n");
                        i++;
                        break;
                    // The cashbox has been removed from the unit. This will continue to poll until the cashbox is replaced.
                    case CCommands.SSP_POLL_CASHBOX_REMOVED:
                        log.AppendText("Cashbox removed...\r\n");
                        break;
                    // The cashbox has been replaced, this will only display on a poll once.
                    case CCommands.SSP_POLL_CASHBOX_REPLACED:
                        log.AppendText("Cashbox replaced\r\n");
                        break;
                    // A bar code ticket has been detected and validated. The ticket is in escrow, continuing to poll will accept
                    // the ticket, sending a reject command will reject the ticket.
                    case CCommands.SSP_POLL_BAR_CODE_VALIDATED:
                        log.AppendText("Bar code ticket validated\r\n");
                        break;
                    // A bar code ticket has been accepted (equivalent to note credit).
                    case CCommands.SSP_POLL_BAR_CODE_ACK:
                        log.AppendText("Bar code ticket accepted\r\n");
                        break;
                    // The validator has detected its note path is open. The unit is disabled while the note path is open.
                    // Only available in protocol versions 6 and above.
                    case CCommands.SSP_POLL_NOTE_PATH_OPEN:
                        log.AppendText("Note path open\r\n");
                        break;
                    // All channels on the validator have been inhibited so the validator is disabled. Only available on protocol
                    // versions 7 and above.
                    case CCommands.SSP_POLL_CHANNEL_DISABLE:
                        log.AppendText("All channels inhibited, unit disabled\r\n");
                        break;
                    default:
                        log.AppendText("Unrecognised poll response detected " + (int)cmd.ResponseData[i] + "\r\n");
                        break;
                }
            }
            return true;
        }

        /* Non-Command functions */

        // This function calls the open com port function of the SSP library.
        public bool OpenComPort(TextBox log = null)
        {
            if (log != null)
                log.AppendText("Opening com port\r\n");
            if (!eSSP.OpenSSPComPort(cmd))
            {
                return false;
            }
            return true;
        }

        /* Exception and Error Handling */

        // This is used for generic response error catching, it outputs the info in a
        // meaningful way.
        private bool CheckGenericResponses(TextBox log)
        {
            if (cmd.ResponseData[0] == CCommands.SSP_RESPONSE_CMD_OK)
                return true;
            else
            {
                if (log != null)
                {
                    switch (cmd.ResponseData[0])
                    {
                        case CCommands.SSP_RESPONSE_CMD_CANNOT_PROCESS:
                            if (cmd.ResponseData[1] == 0x03)
                            {
                                log.AppendText("Validator has responded with \"Busy\", command cannot be processed at this time\r\n");
                            }
                            else
                            {
                                log.AppendText("Command response is CANNOT PROCESS COMMAND, error code - 0x"
                                + BitConverter.ToString(cmd.ResponseData, 1, 1) + "\r\n");
                            }
                            return false;
                        case CCommands.SSP_RESPONSE_CMD_FAIL:
                            log.AppendText("Command response is FAIL\r\n");
                            return false;
                        case CCommands.SSP_RESPONSE_CMD_KEY_NOT_SET:
                            log.AppendText("Command response is KEY NOT SET, Validator requires encryption on this command or there is"
                                + "a problem with the encryption on this request\r\n");
                            return false;
                        case CCommands.SSP_RESPONSE_CMD_PARAM_OUT_OF_RANGE:
                            log.AppendText("Command response is PARAM OUT OF RANGE\r\n");
                            return false;
                        case CCommands.SSP_RESPONSE_CMD_SOFTWARE_ERROR:
                            log.AppendText("Command response is SOFTWARE ERROR\r\n");
                            return false;
                        case CCommands.SSP_RESPONSE_CMD_UNKNOWN:
                            log.AppendText("Command response is UNKNOWN\r\n");
                            return false;
                        case CCommands.SSP_RESPONSE_CMD_WRONG_PARAMS:
                            log.AppendText("Command response is WRONG PARAMETERS\r\n");
                            return false;
                        default:
                            return false;
                    }
                }
                else
                {
                    return false;
                }
            }
        }

        public bool SendCommand(TextBox log)
        {
            // Backup data and length in case we need to retry
            byte[] backup = new byte[255];
            cmd.CommandData.CopyTo(backup, 0);
            byte length = cmd.CommandDataLength;

            // attempt to send the command
            if (eSSP.SSPSendCommand(cmd, info) == false)
            {
                eSSP.CloseComPort();
                m_Comms.UpdateLog(info, true); // update the log on fail as well
                if (log != null) log.AppendText("Sending command failed\r\nPort status: " + cmd.ResponseStatus.ToString() + "\r\n");
                return false;
            }

            // update the log after every command
            m_Comms.UpdateLog(info);

            return true;
        }
    };
}
