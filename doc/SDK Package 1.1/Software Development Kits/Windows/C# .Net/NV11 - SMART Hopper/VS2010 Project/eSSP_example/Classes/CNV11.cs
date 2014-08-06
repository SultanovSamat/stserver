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
    public class CNV11
    {
        // ssp library variables
        SSP_COMMAND cmd;
        SSP_KEYS keys;
        SSP_FULL_KEY sspKey;
        SSP_COMMAND_INFO info;

        // variable declarations
        CCommsWindow m_Comms;

        // Variables to hold the number of notes accepted and dispensed
        int m_TotalNotesAccepted, m_TotalNotesDispensed;

        // The number of channels used in this validator
        int m_NumberOfChannels;

        // The multiplier by which the channel values are multiplied to get their
        // true penny value.
        int m_ValueMultiplier;

        // The type of unit this class represents, set in the setup request
        char m_UnitType;

        // Integer array to hold the value of each note stored in the payout
        int[] m_NotePositionValues;

        // Current poll response and length
        byte[] m_CurrentPollResponse;
        byte m_CurrentPollResponseLength;

        // A list of dataset data, sorted by value. Holds the info on channel number, value, currency,
        // level and whether it is being recycled.
        List<ChannelData> m_UnitDataList;

        // constructor
        public CNV11()
        {
            cmd = new SSP_COMMAND();
            keys = new SSP_KEYS();
            sspKey = new SSP_FULL_KEY();
            info = new SSP_COMMAND_INFO();

            m_Comms = new CCommsWindow("NV11");
            m_TotalNotesAccepted = 0;
            m_TotalNotesDispensed = 0;
            m_NumberOfChannels = 0;
            m_ValueMultiplier = 1;
            m_CurrentPollResponse = new byte[256];
            m_CurrentPollResponseLength = 0;
            m_UnitDataList = new List<ChannelData>();
            m_NotePositionValues = new int[30];
        }

        /* Variable Access */

        public SSP_COMMAND CommandStructure
        {
            get { return cmd; }
            set { cmd = value; }
        }

        public SSP_COMMAND_INFO InfoStructure
        {
            get { return info; }
            set { info = value; }
        }

        // access to comms
        public CCommsWindow CommsLog
        {
            get { return m_Comms; }
            set { m_Comms = value; }
        }
        // access to notes accepted
        public int NotesAccepted
        {
            get { return m_TotalNotesAccepted; }
            set { m_TotalNotesAccepted = value; }
        }

        // access to notes dispensed
        public int NotesDispensed
        {
            get { return m_TotalNotesDispensed; }
            set { m_TotalNotesDispensed = value; }
        }

        // access to number of channels
        public int NumberOfChannels
        {
            get { return m_NumberOfChannels; }
            set { m_NumberOfChannels = value; }
        }

        // access to value multiplier
        public int Multiplier
        {
            get { return m_ValueMultiplier; }
            set { m_ValueMultiplier = value; }
        }

        // access to the list of data items
        public List<ChannelData> UnitDataList
        {
            get { return m_UnitDataList; }
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

        /* Command functions */

        // The enable command allows the validator to receive and act on commands sent to it.
        public void EnableValidator(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_ENABLE;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            // check response
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Validator enabled\r\n");
        }

        // Disable command stops the validator from acting on commands.
        public void DisableValidator(TextBox log = null) 
        {
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_DISABLE;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            // check response
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Validator disabled\r\n");
        }

        // Enable payout allows the validator to payout and store notes.
        public void EnablePayout(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_ENABLE_PAYOUT;
            cmd.CommandData[1] = 0x01; // second byte to enable note value to be sent with stored event
            cmd.CommandDataLength = 2;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Payout enabled\r\n");
        }

        // Disable payout stops the validator being able to store/payout notes.
        public void DisablePayout(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_DISABLE_PAYOUT;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Payout disabled\r\n");
        }

        // Empty payout device takes all the notes stored and moves them to the cashbox. This function also
        // updates the cashbox and stored totals.
        public void EmptyPayoutDevice(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_EMPTY;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                if (log != null) log.AppendText("Emptying payout device\r\n");
            }
        }

        // Payout last note command takes the last note paid in and dispenses it first (LIFO system). 
        public void PayoutNextNote(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_PAYOUT_LAST_NOTE;
            cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return;

            if (CheckGenericResponses(log))
            {
                if (log != null) log.AppendText("Paying out next note\r\n");
            }
        }

        // Set value reporting type changes the validator to return either the value of the note, or the channel it is stored on
        // depending on what byte is sent after the command (0x01 is channel, 0x00 is 4 bit value).
        public void SetValueReportingType(bool byChannel, TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_SET_VALUE_REPORTING_TYPE;
            if (byChannel)
                cmd.CommandData[1] = 0x01; // report by channel number
            else
                cmd.CommandData[1] = 0x00; // report by 4 bit value
            cmd.CommandDataLength = 2;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("Value reporting type changed\r\n");
        }

        // The set routing command changes the way the validator deals with a note, either it can send the note straight to the cashbox
        // or it can store the note for payout. This is specified in the second byte (0x00 to store for payout, 0x01 for cashbox). The 
        // bytes after this represent the 4 bit value of the note, or the channel (see SetValueReportingType()).
        // This function allows the note to be specified as an int in the param note, the stack bool is true for cashbox, false for storage.
        public void ChangeNoteRoute(int note, char[] currency, bool stack, TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_SET_ROUTING;

            // if this note is being changed to stack (cashbox)
            if (stack)
                cmd.CommandData[1] = 0x01;
            // note being stored (payout)
            else
                cmd.CommandData[1] = 0x00;

            // get the note as a byte array
            byte[] b = BitConverter.GetBytes(note);
            cmd.CommandData[2] = b[0];
            cmd.CommandData[3] = b[1];
            cmd.CommandData[4] = b[2];
            cmd.CommandData[5] = b[3];

            // send country code
            cmd.CommandData[6] = (byte)currency[0];
            cmd.CommandData[7] = (byte)currency[1];
            cmd.CommandData[8] = (byte)currency[2];
            
            cmd.CommandDataLength = 9;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
            {
                string s;
                if (stack) s = " to cashbox)\r\n";
                else s = " to storage)\r\n";
                log.AppendText("Note routing successful (" + CHelpers.FormatToCurrency(note) + s);
            }
        }

        // This function sends the command LAST REJECT CODE which gives info about why a note has been rejected. It then
        // outputs the info to a passed across textbox.
        public void QueryRejection(TextBox log)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_LAST_REJECT_CODE;
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

        // The get note positions command instructs the validator to return in the second byte the number of
        // notes stored and then in the following bytes, the values/channel (see SetValueReportingType()) of the stored
        // notes. The length of the response will vary based on the number of stored notes.
        public void CheckForStoredNotes(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_GET_NOTE_POSITIONS;
            cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                byte numNotes = cmd.ResponseData[1];
                Array.Clear(m_NotePositionValues, 0, m_NotePositionValues.Length);

                for (int i = 0; i < numNotes; ++i)
                {
                    m_NotePositionValues[i] = BitConverter.ToInt32(cmd.ResponseData, i * 4 + 2);
                }
            }
        }

        // The stack last note command is similar to the payout last note command (PayoutNextNote()) except it
        // moves the stored notes to the cashbox instead of dispensing.
        public void StackNextNote(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_STACK_LAST_NOTE;
            cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return;

            if (CheckGenericResponses(log) && log != null)
            {
                log.AppendText("Moved note from storage to cashbox\r\n");
            }
        }

        // The reset command instructs the validator to restart (same effect as switching on and off)
        public void Reset(TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_RESET;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            CheckGenericResponses(log);
        }

        // This function sets the protocol version in the validator to the version passed across. Whoever calls
        // this needs to check the response to make sure the version is supported.
        public void SetProtocolVersion(byte pVersion, TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_HOST_PROTOCOL_VERSION;
            cmd.CommandData[1] = pVersion;
            cmd.CommandDataLength = 2;
            if (!SendCommand(log)) return;
        }
        
        // This function performs a number of commands in order to setup the encryption between the host and the validator.
        public bool NegotiateKeys(TextBox log = null)
        {
            byte i;
            string s = "";

            // make sure encryption is off
            cmd.EncryptionStatus = false;

            // send sync
            s += "Syncing... ";
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SYNC;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return false;
            s += "Success\r\n";

            LibraryHandler.InitiateKeys(ref keys, ref cmd);

            // send generator
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SET_GENERATOR;
            cmd.CommandDataLength = 9;
            s += "Setting generator... ";
            for (i = 0; i < 8; i++)
            {
                cmd.CommandData[i + 1] = (byte)(keys.Generator >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            s += "Success\r\n";

            // send modulus
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SET_MODULUS;
            cmd.CommandDataLength = 9;
            s += "Sending modulus... ";
            for (i = 0; i < 8; i++)
            {
                cmd.CommandData[i + 1] = (byte)(keys.Modulus >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            s += "Success\r\n";

            // send key exchange
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_REQUEST_KEY_EXCHANGE;
            cmd.CommandDataLength = 9;
            s += "Exchanging keys... ";
            for (i = 0; i < 8; i++)
            {
                cmd.CommandData[i + 1] = (byte)(keys.HostInter >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            s += "Success\r\n";

            keys.SlaveInterKey = 0;
            for (i = 0; i < 8; i++)
            {
                keys.SlaveInterKey += (UInt64)cmd.ResponseData[1 + i] << (8 * i);
            }

            LibraryHandler.CreateFullKey(ref keys);

            // get full encryption key
            cmd.Key.FixedKey = 0x0123456701234567;
            cmd.Key.VariableKey = keys.KeyHost;

            s += "Keys successfully negotiated\r\n";

            if (log != null)
                log.AppendText(s);
            return true;
        }

        // This function uses the setup request command to get all the information about the validator.
        public void SetupRequest(TextBox log = null)
        {
            // send setup request
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SETUP_REQUEST;
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
                d.Level = 0; // Can only store notes in a LIFO system so level not used
                IsNoteRecycling(d.Value, d.Currency, ref d.Recycling);
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
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SET_INHIBITS;
            cmd.CommandData[1] = 0xFF;
            cmd.CommandData[2] = 0xFF;
            cmd.CommandDataLength = 3;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
            {
                log.AppendText("Inhibits set\r\n");
            }
        }

        // This function uses the GET ROUTING command to determine whether a particular note
        // is recycling.
        void IsNoteRecycling(int note, char[] currency, ref bool b, TextBox log = null)
        {
            cmd.CommandData[0] = CCommands.NV11.SSP_CMD_GET_ROUTING;
            byte[] byteArr = BitConverter.GetBytes(note);
            cmd.CommandData[1] = byteArr[0];
            cmd.CommandData[2] = byteArr[1];
            cmd.CommandData[3] = byteArr[2];
            cmd.CommandData[4] = byteArr[3];
            cmd.CommandData[5] = (byte)currency[0];
            cmd.CommandData[6] = (byte)currency[1];
            cmd.CommandData[7] = (byte)currency[2];
            cmd.CommandDataLength = 8;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                if (cmd.ResponseData[1] == 0x00)
                    b = true;
                else
                    b = false;
            }
        }

        // The poll function is called repeatedly to poll to validator for information, it returns as
        // a response in the command structure what events are currently happening.
        public bool DoPoll(TextBox log)
        {
            byte i;

            //send poll
            cmd.CommandData[0] = CCommands.Generic.SSP_CMD_POLL;
            cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return false;

            // Check unit hasn't lost key (could be due to power loss or reset)
            if (cmd.ResponseData[0] == 0xFA) return false;

            // Store poll response to avoid corruption if the cmd structure is accessed whilst polling
            cmd.ResponseData.CopyTo(m_CurrentPollResponse, 0);
            m_CurrentPollResponseLength = cmd.ResponseDataLength;

            //parse poll m_CurrentPollResponse
            int noteVal = 0;
            for (i = 1; i < m_CurrentPollResponseLength; i++)
            {
                switch (m_CurrentPollResponse[i])
                {
                    // This m_CurrentPollResponse indicates that the unit was reset and this is the first time a poll
                    // has been called since the reset.
                    case CCommands.NV11.SSP_POLL_RESET:
                        log.AppendText("Unit reset\r\n");
                        UpdateData();
                        break;
                    // A note is currently being read by the validator sensors. The second byte of this response
                    // is zero until the note's type has been determined, it then changes to the channel of the 
                    // scanned note.
                    case CCommands.NV11.SSP_POLL_NOTE_READ:
                        if (m_CurrentPollResponse[i + 1] > 0)
                        {
                            noteVal = GetChannelValue(m_CurrentPollResponse[i + 1]);
                            log.AppendText("Note in escrow, amount: " + CHelpers.FormatToCurrency(noteVal) + "\r\n");
                        }
                        else
                            log.AppendText("Reading note\r\n");
                        i++;
                        break;
                    // A credit event has been detected, this is when the validator has accepted a note as legal currency.
                    case CCommands.NV11.SSP_POLL_CREDIT:
                        noteVal = GetChannelValue(m_CurrentPollResponse[i + 1]);
                        log.AppendText("Credit " + CHelpers.FormatToCurrency(noteVal) + "\r\n");
                        NotesAccepted++;
                        UpdateData();
                        i++;
                        break;
                    // A note is being rejected from the validator. This will carry on polling while the note is in transit.
                    case CCommands.NV11.SSP_POLL_REJECTING:
                        break;
                    // A note has been rejected from the validator. This response only appears once.
                    case CCommands.NV11.SSP_POLL_REJECTED:
                        log.AppendText("Note rejected\r\n");
                        QueryRejection(log);
                        UpdateData();
                        break;
                    // A note is in transit to the cashbox.
                    case CCommands.NV11.SSP_POLL_STACKING:
                        log.AppendText("Stacking note\r\n");
                        break;
                    // A note has reached the cashbox.
                    case CCommands.NV11.SSP_POLL_STACKED:
                        log.AppendText("Note stacked\r\n");
                        break;
                    // A safe jam has been detected. This is where the user has inserted a note and the note
                    // is jammed somewhere that the user cannot reach.
                    case CCommands.NV11.SSP_POLL_SAFE_JAM:
                        log.AppendText("Safe jam\r\n");
                        break;
                    // An unsafe jam has been detected. This is where a user has inserted a note and the note
                    // is jammed somewhere that the user can potentially recover the note from.
                    case CCommands.NV11.SSP_POLL_UNSAFE_JAM:
                        log.AppendText("Unsafe jam\r\n");
                        break;
                    // The validator is disabled, it will not execute any commands or do any actions until enabled.
                    case CCommands.NV11.SSP_POLL_DISABLED:
                        log.AppendText("Unit disabled...\r\n");
                        break;
                    // A fraud attempt has been detected. The second byte indicates the channel of the note that a fraud
                    // has been attempted on.
                    case CCommands.NV11.SSP_POLL_FRAUD_ATTEMPT:
                        log.AppendText("Fraud attempt, note type: " + GetChannelValue(m_CurrentPollResponse[i + 1]) + "\r\n");
                        i++;
                        break;
                    // The stacker (cashbox) is full.
                    case CCommands.NV11.SSP_POLL_STACKER_FULL:
                        log.AppendText("Stacker full\r\n");
                        break;
                    // A note was detected somewhere inside the validator on startup and was rejected from the front of the
                    // unit.
                    case CCommands.NV11.SSP_POLL_NOTE_CLEARED_FROM_FRONT:
                        log.AppendText(GetChannelValue(m_CurrentPollResponse[i + 1]) + " note cleared from front at reset." + "\r\n");
                        i++;
                        break;
                    // A note was detected somewhere inside the validator on startup and was cleared into the cashbox.
                    case CCommands.NV11.SSP_POLL_NOTE_CLEARED_TO_CASHBOX:
                        log.AppendText(GetChannelValue(m_CurrentPollResponse[i + 1]) + " note cleared to stacker at reset." + "\r\n");
                        i++;
                        break;
                    // The cashbox has been removed from the unit. This will continue to poll until the cashbox is replaced.
                    case CCommands.NV11.SSP_POLL_CASHBOX_REMOVED:
                        log.AppendText("Cashbox removed\r\n");
                        break;
                    // The cashbox has been replaced, this will only display on a poll once.
                    case CCommands.NV11.SSP_POLL_CASHBOX_REPLACED:
                        log.AppendText("Cashbox replaced\r\n");
                        break;
                    // A note has been stored in the payout device to be paid out instead of going into the cashbox.
                    case CCommands.NV11.SSP_POLL_NOTE_STORED:
                        log.AppendText("Note stored\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        UpdateData();
                        break;
                    // The validator is in the process of paying out a note, this will continue to poll until the note has 
                    // been fully dispensed and removed from the front of the validator by the user.
                    case CCommands.NV11.SSP_POLL_NOTE_DISPENSING:
                        log.AppendText("Dispensing note\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // The note has been dispensed and removed from the bezel by the user.
                    case CCommands.NV11.SSP_POLL_NOTE_DISPENSED:
                        for (int j = 0; j < m_CurrentPollResponse[i + 1]; j += 7)
                        {
                            log.AppendText("Dispensed " + (CHelpers.ConvertBytesToInt32(m_CurrentPollResponse, j + i + 2) / 100).ToString() +
                                " " + (char)m_CurrentPollResponse[j + i + 6] + (char)m_CurrentPollResponse[j + i + 7] +
                                (char)m_CurrentPollResponse[j + i + 8] + "\r\n");
                        }
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        NotesDispensed++;
                        UpdateData();
                        EnableValidator(log);
                        break;
                    // A note has been transferred from the payout storage to the cashbox
                    case CCommands.NV11.SSP_POLL_NOTE_TRANSFERRED_TO_STACKER:
                        log.AppendText("Note stacked\r\n");
                        UpdateData();
                        EnableValidator(log);
                        break;
                    // This single poll response indicates that the payout device has finished emptying.
                    case CCommands.NV11.SSP_POLL_EMPTIED:
                        log.AppendText("Device emptied\r\n");
                        UpdateData();
                        EnableValidator(log);
                        break;
                    // This response indicates a note is being dispensed and is resting in the bezel waiting to be removed
                    // before the validator can continue
                    case CCommands.NV11.SSP_POLL_NOTE_HELD_IN_BEZEL:
                        for (int j = 0; j < m_CurrentPollResponse[i + 1]; j += 7)
                        {
                            log.AppendText((CHelpers.ConvertBytesToInt32(m_CurrentPollResponse, j + i + 2) / 100).ToString() +
                                " " + (char)m_CurrentPollResponse[j + i + 6] + (char)m_CurrentPollResponse[j + i + 7] +
                                (char)m_CurrentPollResponse[j + i + 8] + " held in bezel...\r\n");
                        }
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    default:
                        break;
                }
            }
            return true;
        }

        /* Non-Command functions */

        // Opens the COM port for the device using the Library handler
        public bool OpenPort(TextBox log = null)
        {
            // open com port
            if (log != null) log.AppendText("Opening com port\r\n");
            return LibraryHandler.OpenPort(ref cmd);
        }

        // This function uses the set routing command to send all notes to the cashbox, it just calls the
        // ChangeNoteRoute() function for each channel.
        public void RouteAllToStack(TextBox log = null)
        {
            // all notes from channels need setting
            foreach (ChannelData d in m_UnitDataList)
            {
                ChangeNoteRoute(d.Value, d.Currency, true, log);
            }
        }

        // This function returns a formatted string of what notes are available in the NV11 storage device.
        public string GetStorageInfo()
        {
            string s = "";
            for (int i = m_NotePositionValues.Length - 1; i >= 0; --i)
            {
                if (m_NotePositionValues[i] > 0)
                    s += "Position " + i + ": " + (m_NotePositionValues[i] / 100).ToString("00.00") + "\r\n";
            }
            return s;
        }

        // This function updates the internal structures to check they are in sync with the validator.
        void UpdateData()
        {
            foreach (ChannelData d in m_UnitDataList)
            {
                IsNoteRecycling(d.Value, d.Currency, ref d.Recycling);
            }
            CheckForStoredNotes();
        }

        // This function returns the last note that was stored by the payout
        public int GetLastNoteValue()
        {
            if (m_NotePositionValues.Length > 0)
                return m_NotePositionValues[m_NotePositionValues.Length - 1];
            return 0;
        }

        /* Exception and Error Handling */

        // This is used for generic response error catching, it outputs the info in a
        // meaningful way.
        private bool CheckGenericResponses(TextBox log)
        {
            if (cmd.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_OK)
                return true;
            else
            {
                if (log != null)
                {
                    switch (cmd.ResponseData[0])
                    {
                        case CCommands.Generic.SSP_RESPONSE_CMD_CANNOT_BE_PROCESSED:
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
                        case CCommands.Generic.SSP_RESPONSE_FAIL:
                            log.AppendText("Command response is FAIL\r\n");
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_KEY_NOT_SET:
                            log.AppendText("Command response is KEY NOT SET, renegotiate keys\r\n");
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_PARAMS_OUT_OF_RANGE:
                            log.AppendText("Command response is PARAM OUT OF RANGE\r\n");
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_SOFTWARE_ERROR:
                            log.AppendText("Command response is SOFTWARE ERROR\r\n");
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_COMMAND_NOT_KNOWN:
                            log.AppendText("Command response is UNKNOWN\r\n");
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_WRONG_NUMBER_OF_PARAMS:
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
            if (!LibraryHandler.SendCommand(ref cmd, ref info))
            {
                m_Comms.UpdateLog(info, true); // Update on fail
                if (log != null) log.AppendText("Sending command failed\r\nPort status: " + cmd.ResponseStatus.ToString() + "\r\n");
                return false;
            }

            // update the log after every command
            m_Comms.UpdateLog(info);
            return true;
        }
    };
}
