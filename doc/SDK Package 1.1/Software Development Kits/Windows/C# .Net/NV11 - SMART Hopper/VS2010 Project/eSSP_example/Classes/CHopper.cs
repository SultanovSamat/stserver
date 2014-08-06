using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using ITLlib;

namespace eSSP_example
{
    public class CHopper
    {
        // handles to SSP classes
        SSP_COMMAND m_cmd;
        SSP_KEYS m_keys;
        SSP_FULL_KEY m_sspKey;
        SSP_COMMAND_INFO m_info;

        // variable declarations

        // The protocol version this validator is using, set in setup request
        int m_ProtocolVersion;

        // The number of channels being used in this dataset.
        int m_NumberOfChannels;

        // A boolean indicating whether the coin mech is enabled or disabled (globally inhibited).
        bool m_CoinMechEnabled;

        // The class representing the comms log. Deals with logging info both visually and to file.
        CCommsWindow m_Comms;

        // A variable to hold the type of the unit, obtained in setup request
        char m_UnitType;

        // A list of dataset data, sorted by value. Holds the info on channel number, value, currency,
        // level and whether it is being recycled.
        List<ChannelData> m_UnitDataList;

        // An array to hold the current poll response, this allows the command structure to be used
        // while polling without losing the remainder of the poll response
        byte[] m_CurrentPollResponse;
        byte m_CurrentPollResponseLength;

        // constructor
        public CHopper()
        {
            // init SSP handles
            m_cmd = new SSP_COMMAND();
            m_keys = new SSP_KEYS();
            m_sspKey = new SSP_FULL_KEY();
            m_info = new SSP_COMMAND_INFO();

            m_NumberOfChannels = 0;
            m_ProtocolVersion = 0;
            m_CoinMechEnabled = true;
            m_Comms = new CCommsWindow("SMART Hopper");
            m_UnitDataList = new List<ChannelData>();
            m_CurrentPollResponse = new byte[255];

            if (Properties.Settings.Default.Comms)
                Comms.Show();
        }

        /* Variable Access */

        public CCommsWindow Comms
        {
            get { return m_Comms; }
        }

        // access to number of channels
        public int NumberOfChannels
        {
            get { return m_NumberOfChannels; }
            set { m_NumberOfChannels = value; }
        }

        // access to coin mech bool
        public bool CoinMechEnabled
        {
            get { return m_CoinMechEnabled; }
            set { m_CoinMechEnabled = value; }
        }

        // access to the command structure
        public SSP_COMMAND CommandStructure
        {
            get { return m_cmd; }
            set { m_cmd = value; }
        }

        // access to the info structure
        public SSP_COMMAND_INFO InfoStructure
        {
            get { return m_info; }
            set { m_info = value; }
        }

        // access to the unit's type
        public char UnitType
        {
            get { return m_UnitType; }
        }

        // access to the dataset data
        public List<ChannelData> UnitDataList
        {
            get { return m_UnitDataList; }
        }

        // get a channel value
        public int GetChannelValue(int channelNum)
        {
            if (channelNum > 0 && channelNum <= m_NumberOfChannels)
            {
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channelNum)
                        return d.Value;
                }
            }
            return -1;
        }

        // Command functions

        // This function send the SYNC command to the validator. It returns true if it receives an OK response.
        public bool SendSync(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SYNC;
            m_cmd.CommandDataLength = 1;
            if (!SendCommand(log)) return false;
            if (CheckGenericResponses(log))
                return true;
            return false;
        }

        // The enable command allows the validator to receive and act on commands.
        public void EnableValidator(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_ENABLE;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            // check response
            if (CheckGenericResponses(log))
            {
                if (log != null)
                    log.AppendText("SMART Hopper enabled\r\n");
            }
        }

        // Empty device moves all the coins in the device to the cashbox using command EMPTY ALL. It then
        // sets the channel levels to 0.
        public void EmptyDevice(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_EMPTY_ALL;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log) && log != null)
            {
                log.AppendText("Emptying all stored coins to cashbox...\r\n");
                foreach (ChannelData d in m_UnitDataList)
                    d.Level = 0;
            }
        }

        // Set a channel to route to cashbox, this sends the SET ROUTING command.
        public void RouteChannelToCashbox(int channelNumber, TextBox log = null)
        {
            // setup command
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_SET_ROUTING;
            m_cmd.CommandData[1] = 0x01; // cashbox

            // coin to route

            // Get value of coin (4 byte protocol 6)
            byte[] b = CHelpers.ConvertInt32ToBytes(GetChannelValue(channelNumber));
            m_cmd.CommandData[2] = b[0];
            m_cmd.CommandData[3] = b[1];
            m_cmd.CommandData[4] = b[2];
            m_cmd.CommandData[5] = b[3];

            // Add country code, locate from dataset
            foreach (ChannelData d in m_UnitDataList)
            {
                if (d.Channel == channelNumber)
                {
                    m_cmd.CommandData[6] = (byte)d.Currency[0];
                    m_cmd.CommandData[7] = (byte)d.Currency[1];
                    m_cmd.CommandData[8] = (byte)d.Currency[2];
                    break;
                }
            }
            
            m_cmd.CommandDataLength = 9;

            // send command
            if (!SendCommand(log)) return;

            if (CheckGenericResponses(log))
            {
                // update list
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channelNumber)
                    {
                        d.Recycling = false;
                        break;
                    }
                }

                if (log != null)
                    log.AppendText("Successfully routed coin on channel " + channelNumber.ToString() + " to cashbox\r\n");
            }
        }
        // Set a channel to route to storage, this sends the SET ROUTING command.
        public void RouteChannelToStorage(int channelNumber, TextBox log = null)
        {
            // setup command
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_SET_ROUTING;
            m_cmd.CommandData[1] = 0x00; // storage

            // coin to route

            // Get value of coin (4 byte protocol 6)
            byte[] b = CHelpers.ConvertInt32ToBytes(GetChannelValue(channelNumber));
            m_cmd.CommandData[2] = b[0];
            m_cmd.CommandData[3] = b[1];
            m_cmd.CommandData[4] = b[2];
            m_cmd.CommandData[5] = b[3];

            // Add country code, locate from dataset
            foreach (ChannelData d in m_UnitDataList)
            {
                if (d.Channel == channelNumber)
                {
                    m_cmd.CommandData[6] = (byte)d.Currency[0];
                    m_cmd.CommandData[7] = (byte)d.Currency[1];
                    m_cmd.CommandData[8] = (byte)d.Currency[2];
                    break;
                }
            }

            m_cmd.CommandDataLength = 9;

            // send command
            if (!SendCommand(log)) return;

            if (CheckGenericResponses(log))
            {
                // update list
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channelNumber)
                    {
                        d.Recycling = true;
                        break;
                    }
                }

                if (log != null)
                    log.AppendText("Successfully routed coin on channel " + channelNumber.ToString() + " to storage\r\n");
            }
        }

        // Disable command stops the unit accepting commands and acting on them.
        public void DisableValidator(TextBox log = null) 
        {
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_DISABLE;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            // check response
            if (CheckGenericResponses(log) && log != null)
                log.AppendText("SMART Hopper disabled\r\n");
        }

        // The reset command instructs the validator to restart (same effect as switching on and off)
        public void Reset(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_RESET;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            CheckGenericResponses(log);
        }

        // This uses the PAYOUT AMOUNT command to payout a value specified by the param amountToPayout.
        // Protocol 6+ - We can use an option byte to test whether the payout is possible (0x19), and if
        // it is then we can resend with the option byte 0x58 to do the payout.
        public bool PayoutAmount(int amountToPayout, char[] currency, TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_PAYOUT_AMOUNT;

            // Value to payout
            byte[] b = CHelpers.ConvertInt32ToBytes(amountToPayout);
            m_cmd.CommandData[1] = b[0];
            m_cmd.CommandData[2] = b[1];
            m_cmd.CommandData[3] = b[2];
            m_cmd.CommandData[4] = b[3];
            
            // Country code
            m_cmd.CommandData[5] = (byte)currency[0];
            m_cmd.CommandData[6] = (byte)currency[1];
            m_cmd.CommandData[7] = (byte)currency[2];

            m_cmd.CommandData[8] = 0x58; // payout option (0x19 for test, 0x58 for real)

            m_cmd.CommandDataLength = 9;

            if (!SendCommand(log)) return false;

            if (CheckGenericResponses(log))
                return true;
            return false;
        }

        // Payout by denomination. This function allows a developer to payout specified amounts of certain
        // coins. Due to the variable length of the data that could be passed to the function, the user 
        // passes an array containing the data to payout and the length of that array along with the number
        // of denominations they are paying out.
        public void PayoutByDenomination(byte numDenoms, byte[] data, byte dataLength, TextBox log = null)
        {
            // First is the command byte
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_PAYOUT_BY_DENOMINATION;

            // Next is the number of denominations to be paid out
            m_cmd.CommandData[1] = numDenoms;

            // Copy over data byte array parameter into command structure
            int currentIndex = 2;
            for (int i = 0; i < dataLength; i++)
                m_cmd.CommandData[currentIndex++] = data[i];

            // Perform a real payout (0x19 for test)
            m_cmd.CommandData[currentIndex++] = 0x58;

            // Length of command data (add 3 to cover the command byte, num of denoms and real/test byte)
            dataLength += 3;
            m_cmd.CommandDataLength = dataLength;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                if (log != null)
                    log.AppendText("Paying out by denomination...\r\n");
            }
        }

        // This function uses the COIN MECH GLOBAL INHIBIT command to disable the coin mech.
        public bool DisableCoinMech(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_COIN_MECH_GLOBAL_INHIBIT;
            m_cmd.CommandData[1] = 0x00; // 0 for disable
            m_cmd.CommandDataLength = 2;

            if (!SendCommand(log)) return false;
            if (CheckGenericResponses(log) && log != null)
            {
                log.AppendText("Disabled coin mech\r\n");
                m_CoinMechEnabled = false;
                return true;
            }
            return false;
        }

        // This function uses the COIN MECH GLOBAL INHIBIT command to enable the coin mech.
        public bool EnableCoinMech(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_COIN_MECH_GLOBAL_INHIBIT;
            m_cmd.CommandData[1] = 0x01; // 1 for enable
            m_cmd.CommandDataLength = 2;

            if (!SendCommand(log)) return false;
            if (CheckGenericResponses(log) && log != null)
            {
                m_CoinMechEnabled = true;
                log.AppendText("Enabled coin mech\r\n");
                return true;
            }
            return false;
        }

        // This function uses the command SMARTY EMPTY which empties all the coins to the cashbox but keeps a 
        // count of what was put in, the data of what coins were emptied can be accessed with the command
        // CASHBOX PAYOUT OPERATION DATA
        public void SmartEmpty(TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_SMART_EMPTY;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                if (log != null) log.AppendText("SMART empyting...\r\n");
            }
        }

        // This uses the SET COIN AMOUNT command to increase a channel level by passing over the channel and the amount to increment by
        public void SetCoinLevelsByChannel(int channel, short amount, TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_SET_COIN_AMOUNT;
            // Level to set
            byte[] b = CHelpers.ConvertInt16ToBytes(amount);
            m_cmd.CommandData[1] = b[0];
            m_cmd.CommandData[2] = b[1];

            // Coin(channel) to set
            b = CHelpers.ConvertInt32ToBytes(GetChannelValue(channel));
            m_cmd.CommandData[3] = b[0];
            m_cmd.CommandData[4] = b[1];
            m_cmd.CommandData[5] = b[2];
            m_cmd.CommandData[6] = b[3];

            // Add country code, locate from dataset
            foreach (ChannelData d in m_UnitDataList)
            {
                if (d.Channel == channel)
                {
                    m_cmd.CommandData[7] = (byte)d.Currency[0];
                    m_cmd.CommandData[8] = (byte)d.Currency[1];
                    m_cmd.CommandData[9] = (byte)d.Currency[2];
                    break;
                }
            }

            m_cmd.CommandDataLength = 10;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                // Update the level
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channel)
                    {
                        d.Level += amount;
                        break;
                    }
                }

                if (log != null)
                {
                    log.AppendText ("Changed coin value " + CHelpers.FormatToCurrency (GetChannelValue (channel)).ToString () +
                        "'s level to " + amount.ToString() + "\r\n");
                }
            }
    
        }

        // This uses the SET COIN AMOUNT command to increase a channel level by passing over the coin value and the amount to increment by
        public void SetCoinLevelsByCoin(int coin, char[] currency, short amount, TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_SET_COIN_AMOUNT;
            byte[] b = CHelpers.ConvertInt16ToBytes(amount);
            m_cmd.CommandData[1] = b[0];
            m_cmd.CommandData[2] = b[1];
            b = CHelpers.ConvertInt32ToBytes(coin);
            m_cmd.CommandData[3] = b[0];
            m_cmd.CommandData[4] = b[1];
            m_cmd.CommandData[5] = b[2];
            m_cmd.CommandData[6] = b[3];
            m_cmd.CommandData[7] = (byte)currency[0];
            m_cmd.CommandData[8] = (byte)currency[1];
            m_cmd.CommandData[9] = (byte)currency[2];
            m_cmd.CommandDataLength = 10;

            if (!SendCommand(log)) return;
            if (CheckGenericResponses(log))
            {
                // Update the level
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Value == coin)
                    {
                        d.Level += amount;
                        break;
                    }
                }

                if (log != null)
                    log.AppendText ("Increased coin value " + CHelpers.FormatToCurrency (coin).ToString () + "'s level by " + amount.ToString () + "\r\n");

            }
        }

        // This uses the GET COIN AMOUNT command to query the validator on a specified coin it has stored, it returns
        // the level as an int.
        public short CheckCoinLevel(int coinValue, char[] currency, TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_GET_COIN_AMOUNT;
            byte[] b = CHelpers.ConvertInt32ToBytes(coinValue);
            m_cmd.CommandData[1] = b[0];
            m_cmd.CommandData[2] = b[1];
            m_cmd.CommandData[3] = b[2];
            m_cmd.CommandData[4] = b[3];
            m_cmd.CommandData[5] = (byte)currency[0];
            m_cmd.CommandData[6] = (byte)currency[1];
            m_cmd.CommandData[7] = (byte)currency[2];
            m_cmd.CommandDataLength = 8;

            if (!SendCommand(log)) return -1;
            if (CheckGenericResponses(log))
            {
                return CHelpers.ConvertBytesToInt16(m_cmd.ResponseData, 1);
            }
            return -1;
        }

        // This function just updates all the coin levels in the list
        public void UpdateData(TextBox log = null)
        {
            foreach (ChannelData d in m_UnitDataList)
            {
                d.Level = CheckCoinLevel(d.Value, d.Currency, log);
                IsCoinRecycling(d.Value, d.Currency, ref d.Recycling);
            }
        }

        // This function uses the GET ROUTING command to see if a specified coin is recycling. The
        // caller passes a bool across which is set by the function.
        public void IsCoinRecycling(int coinValue, char[] currency, ref bool response, TextBox log = null)
        {
            // First determine if the coin is currently being recycled
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_GET_ROUTING;
            byte[] b = CHelpers.ConvertInt32ToBytes(coinValue);
            m_cmd.CommandData[1] = b[0];
            m_cmd.CommandData[2] = b[1];
            m_cmd.CommandData[3] = b[2];
            m_cmd.CommandData[4] = b[3];

            // Add currency
            m_cmd.CommandData[5] = (byte)currency[0];
            m_cmd.CommandData[6] = (byte)currency[1];
            m_cmd.CommandData[7] = (byte)currency[2];
            m_cmd.CommandDataLength = 8;

            if (!SendCommand (log)) return;
            if (CheckGenericResponses (log))
            {
                // True if it is currently being recycled
                if (m_cmd.ResponseData[1] == 0x00)
                {
                    response = true;
                    if (log != null)
                        log.AppendText (CHelpers.FormatToCurrency(coinValue) + " is recycling\r\n");
                }
                // False if not
                else if (m_cmd.ResponseData[1] == 0x01)
                {
                    response = false;
                    if (log != null)
                        log.AppendText (CHelpers.FormatToCurrency (coinValue) + " is not recycling\r\n");
                }
            }
        }

        // This function returns a member of the internal array to check whether a channel is
        // recycling, this can't be called before setup request or it will be inaccurate.
        public bool IsChannelRecycling(int channel, TextBox log = null)
        {
            if (channel > 0 && channel <= m_NumberOfChannels)
            {
                foreach (ChannelData d in m_UnitDataList)
                {
                    if (d.Channel == channel)
                    {
                        if (log != null) log.AppendText("Channel " + channel + " recycling status: " + d.Recycling.ToString() + "\r\n");
                        return d.Recycling;
                    }
                }
            }
            return false;
        }

        // This function gets the CASHBOX PAYOUT OPERATION DATA from the validator and returns it as a string
        public void GetCashboxPayoutOpData(TextBox log = null)
        {
            // first send the command
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_CASHBOX_PAYOUT_OP_DATA;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return;

            // now deal with the response data
            if (CheckGenericResponses(log))
            {
                // number of different coins
                int n = m_cmd.ResponseData[1];
                string displayString = "Number of Total Coins: " + n.ToString() + "\r\n\r\n";
                int i = 0;
                for (i = 2; i < (9 * n); i += 9)
                {
                    displayString += "Moved " + CHelpers.ConvertBytesToInt16(m_cmd.ResponseData, i) +
                        " x " + CHelpers.FormatToCurrency (CHelpers.ConvertBytesToInt32 (m_cmd.ResponseData, i + 2)) +
                        " " + (char)m_cmd.ResponseData[i + 6] + (char)m_cmd.ResponseData[i + 7] + (char)m_cmd.ResponseData[i + 8] 
                        + " to cashbox\r\n";
                }
                displayString += CHelpers.ConvertBytesToInt32(m_cmd.ResponseData, i) + " coins not recognised\r\n";
                
                if (log != null) log.AppendText(displayString);
            }
        }

        // This function uses the FLOAT AMOUNT command to set the float amount. The Hopper will empty
        // coins into the cashbox leaving the requested floating amount in the payout. The minimum payout
        // is also setup so the validator will leave itself the ability to payout the minimum value requested.
        public bool SetFloat(short minPayout, int floatAmount, char[] currency, TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_FLOAT_AMOUNT;

            // Min payout
            byte[] b = CHelpers.ConvertInt16ToBytes(minPayout);
            m_cmd.CommandData[1] = b[0];
            m_cmd.CommandData[2] = b[1];

            // Amount to payout
            b = CHelpers.ConvertInt32ToBytes(floatAmount);
            m_cmd.CommandData[3] = b[0];
            m_cmd.CommandData[4] = b[1];
            m_cmd.CommandData[5] = b[2];
            m_cmd.CommandData[6] = b[3];

            // Country code
            m_cmd.CommandData[7] = (byte)currency[0];
            m_cmd.CommandData[8] = (byte)currency[1];
            m_cmd.CommandData[9] = (byte)currency[2];

            m_cmd.CommandData[10] = 0x58; // real float

            m_cmd.CommandDataLength = 11;

            if (!SendCommand(log)) return false;

            if (CheckGenericResponses(log))
            {
                if (log != null)
                    log.AppendText("Set float successfully\r\n");
                return true;
            }
            return false;
        }

        // This function sets the protocol version using the command HOST PROTOCOL VERSION.
        public bool SetProtocolVersion(byte b, TextBox log = null)
        {
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_HOST_PROTOCOL_VERSION;
            m_cmd.CommandData[1] = b;
            m_cmd.CommandDataLength = 2;

            if (!SendCommand(log)) return false;
            if (CheckGenericResponses(log))
            {
                if (log != null) log.AppendText("Setting protocol version " + b.ToString() + "\r\n");
            }
            return true;
        }

        // This function opens the com port identified in the command structure, using the SSP library.
        public bool OpenPort(TextBox log = null)
        {
            // open com port
            if (log != null) log.AppendText("Opening com port\r\n");
            return LibraryHandler.OpenPort(ref m_cmd);
        }

        // This function performs a number of commands in order to setup the encryption between the host and the validator.
        public bool NegotiateKeys(TextBox log = null)
        {
            byte i;

            // make sure encryption is off
            m_cmd.EncryptionStatus = false;

            // send sync
            if (log != null) log.AppendText("Syncing... ");
            if (!SendSync()) return false;
            if (log != null) log.AppendText("Success\r\n");

            LibraryHandler.InitiateKeys(ref m_keys, ref m_cmd);

            // send generator
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SET_GENERATOR;
            m_cmd.CommandDataLength = 9;
            if (log != null) log.AppendText("Setting generator... ");
            for (i = 0; i < 8; i++)
            {
                m_cmd.CommandData[i + 1] = (byte)(m_keys.Generator >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success\r\n");

            // send modulus
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SET_MODULUS;
            m_cmd.CommandDataLength = 9;
            if (log != null) log.AppendText("Sending modulus... ");
            for (i = 0; i < 8; i++)
            {
                m_cmd.CommandData[i + 1] = (byte)(m_keys.Modulus >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success\r\n");

            // send key exchange
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_REQUEST_KEY_EXCHANGE;
            m_cmd.CommandDataLength = 9;
            if (log != null) log.AppendText("Exchanging keys... ");
            for (i = 0; i < 8; i++)
            {
                m_cmd.CommandData[i + 1] = (byte)(m_keys.HostInter >> (8 * i));
            }

            if (!SendCommand(log)) return false;
            if (log != null) log.AppendText("Success\r\n");

            m_keys.SlaveInterKey = 0;
            for (i = 0; i < 8; i++)
            {
                m_keys.SlaveInterKey += (UInt64)m_cmd.ResponseData[1 + i] << (8 * i);
            }

            LibraryHandler.CreateFullKey(ref m_keys);

            // get full encryption key
            m_cmd.Key.FixedKey = 0x0123456701234567;
            m_cmd.Key.VariableKey = m_keys.KeyHost;

            if (log != null) log.AppendText("Keys successfully negotiated\r\n");

            return true;
        }
        
        // This function uses the setup request command to get all the information about the validator. It can optionally
        // output to a specified textbox.
        public void SetupRequest(TextBox outputBox = null)
        {
            // send setup request
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_SETUP_REQUEST;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(outputBox)) return;

            // display setup request

            // unit type
            string displayString = "Unit Type: ";
            int index = 1;
            m_UnitType = (char)m_cmd.ResponseData[index++];
            switch (m_UnitType)
            {
                case (char)0x00: displayString += "Validator"; break;
                case (char)0x03: displayString += "SMART Hopper"; break;
                case (char)0x06: displayString += "SMART Payout"; break;
                case (char)0x07: displayString += "NV11"; break;
                default: displayString += "Unknown Type"; break;
            }

            // firmware
            displayString += "\r\nFirmware: ";
            for (int i = index; i < index + 4; i++)
            {
                displayString += (char)m_cmd.ResponseData[i];
                if (i == 3) displayString += ".";
            }
            index += 4;

            // country code
            // legacy code for old protocol versions, protocol 6+ allows each channel to have a currency
            // for multi currency datasets
            index += 3; // +3 to skip country code

            // protocol version
            m_ProtocolVersion = m_cmd.ResponseData[index++];
            displayString += "\r\nProtocol Version: " + m_ProtocolVersion;

            // number of coin values
            m_NumberOfChannels = m_cmd.ResponseData[index++];
            displayString += "\r\nNumber of Coin Values: " + m_NumberOfChannels;

            // coin values
            int[] channelValuesTemp = new int[m_NumberOfChannels + 1];
            int counter = 1;
            displayString += "\r\nCoin Values: ";
            for (int i = index; i < index + (m_NumberOfChannels * 2) - 1; i += 2, counter++)
            {
                short x = BitConverter.ToInt16(m_cmd.ResponseData, i);
                channelValuesTemp[counter] = x; // update array
                displayString += " (" + x + ") ";
            }
            index += (m_NumberOfChannels * 2);

            // country codes for values (protocol 6+ only)
            char[] channelCurrencyTemp = new char[(m_NumberOfChannels + 1) * 3];
            counter = 3;
            displayString += "\r\nCoin Currencies: ";
            for (int i = index; i < index + (3 * m_NumberOfChannels); i+=3)
            {
                channelCurrencyTemp[counter] = (char)m_cmd.ResponseData[i];
                displayString += channelCurrencyTemp[counter++];
                channelCurrencyTemp[counter] = (char)m_cmd.ResponseData[i+1];
                displayString += channelCurrencyTemp[counter++];
                channelCurrencyTemp[counter] = (char)m_cmd.ResponseData[i+2];
                displayString += channelCurrencyTemp[counter++] + " ";
            }
            index += (3 * m_NumberOfChannels);

            // create list entry for each channel
            m_UnitDataList.Clear(); // clear old table
            for (byte i = 1; i <= m_NumberOfChannels; i++)
            {
                ChannelData d = new ChannelData();
                d.Channel = i;
                d.Value = channelValuesTemp[i];
                d.Currency[0] = channelCurrencyTemp[0 + (i * 3)];
                d.Currency[1] = channelCurrencyTemp[1 + (i * 3)];
                d.Currency[2] = channelCurrencyTemp[2 + (i * 3)];
                d.Level = CheckCoinLevel(d.Value, d.Currency);
                IsCoinRecycling(d.Value, d.Currency, ref d.Recycling);

                m_UnitDataList.Add(d);
            }

            // Sort the list of data by the value.
            m_UnitDataList.Sort(delegate(ChannelData d1, ChannelData d2) { return d1.Value.CompareTo(d2.Value); });

            displayString += "\r\n\r\n";

            if (outputBox != null) outputBox.AppendText(displayString);
        }
        
        // This function sends the set coin mech inhibits command to set which coins are accepted on the Hopper.
        // Please note: The response data of this command if it is sent with no coin mech attached will be
        // WRONG PARAMETERS.
        public void SetInhibits(TextBox log = null)
        {
            // set inhibits on each coin
            for (int i = 0; i < m_NumberOfChannels; i++)
            {
                m_cmd.CommandData[0] = CCommands.Hopper.SSP_CMD_SET_COIN_MECH_INHIBITS;
                m_cmd.CommandData[1] = 0x01; // coin accepted

                // convert values to byte array and set command data
                byte[] b = BitConverter.GetBytes(GetChannelValue(i));
                m_cmd.CommandData[2] = b[0];
                m_cmd.CommandData[3] = b[1];

                // currency
                ChannelData d = m_UnitDataList[i];
                m_cmd.CommandData[4] = (byte)d.Currency[0];
                m_cmd.CommandData[5] = (byte)d.Currency[1];
                m_cmd.CommandData[6] = (byte)d.Currency[2];

                m_cmd.CommandDataLength = 7;

                if (!SendCommand(log)) return;
                if (CheckGenericResponses(log) && log != null)
                    log.AppendText("Inhibits set on channel " + (i+1).ToString() + "\r\n");
            }
        }

        // This function is called repeatedly to poll the validator about what events are happening. It
        // can optionally output these events to a textbox.
        public bool DoPoll(TextBox log)
        {
            byte i;

            //send poll
            m_cmd.CommandData[0] = CCommands.Generic.SSP_CMD_POLL;
            m_cmd.CommandDataLength = 1;

            if (!SendCommand(log)) return false;

            // Check unit hasn't lost key (could be due to power loss or reset)
            if (m_cmd.ResponseData[0] == 0xFA) return false;

            // isolate poll response to allow reuse of the SSP_COMMAND structure
            m_CurrentPollResponseLength = m_cmd.ResponseDataLength;
            m_cmd.ResponseData.CopyTo(m_CurrentPollResponse, 0);

            //parse poll response
            int coin = 0;
            string currency = "";
            for (i = 1; i < m_CurrentPollResponseLength; i++)
            {
                switch (m_CurrentPollResponse[i])
                {
                    // This response indicates that the unit was reset and this is the first time a poll
                    // has been called since the reset.
                    case CCommands.Hopper.SSP_POLL_RESET:
                        UpdateData();
                        break;
                    // This response is given when the unit is disabled.
                    case CCommands.Hopper.SSP_POLL_DISABLED:
                        log.AppendText("Unit disabled...\r\n");
                        break;
                    // The unit is in the process of paying out a coin or series of coins, this will continue to poll
                    // until the coins have been fully dispensed
                    case CCommands.Hopper.SSP_POLL_DISPENSING:
                        log.AppendText("Dispensing coin(s)...\r\n");
                        // Now the index needs to be moved on to skip over the data provided by this response so it
                        // is not parsed as a normal poll response.
                        // In this response, the data includes the number of countries being dispensed (1 byte), then a 4 byte value
                        // and 3 byte currency code for each country. 
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // This is polled when a unit has finished a dispense operation. The following 4 bytes give the 
                    // value of the coin(s) dispensed.
                    case CCommands.Hopper.SSP_POLL_DISPENSED:
                        for (int j = 0; j < m_CurrentPollResponse[i + 1] * 7; j += 7)
                        {
                            coin = CHelpers.ConvertBytesToInt32(m_cmd.ResponseData, i + j + 2); // get coin data from response
                            // get currency from response
                            currency = "";
                            currency += (char)m_CurrentPollResponse[i + j + 6];
                            currency += (char)m_CurrentPollResponse[i + j + 7];
                            currency += (char)m_CurrentPollResponse[i + j + 8];
                            log.AppendText(CHelpers.FormatToCurrency(coin) + " " + currency + " coin(s) dispensed\r\n");
                        }

                        UpdateData();
                        EnableValidator();
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // The coins being recycled inside the unit are running low.
                    case CCommands.Hopper.SSP_POLL_COINS_LOW:
                        log.AppendText("Coins low\r\n");
                        break;
                    // There are no coins left in the unit.
                    case CCommands.Hopper.SSP_POLL_EMPTY:
                        log.AppendText("Unit empty\r\n");
                        break;
                    // The mechanism inside the unit is jammed.
                    case CCommands.Hopper.SSP_POLL_JAMMED:
                        log.AppendText("Jammed\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // A dispense, SMART empty or float operation has been halted.
                    case CCommands.Hopper.SSP_POLL_HALTED:
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // The device is 'floating' a specified amount of coins. It will transfer some to the cashbox and
                    // leave the specified amount in the device.
                    case CCommands.Hopper.SSP_POLL_FLOATING:
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // The float operation has completed.
                    case CCommands.Hopper.SSP_POLL_FLOATED:
                        UpdateData();
                        EnableValidator();
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // This poll appears when the SMART Hopper has been searching for a coin but cannot find it within
                    // the timeout period.
                    case CCommands.Hopper.SSP_POLL_TIME_OUT:
                        log.AppendText("Search for suitable coins failed\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // A payout was interrupted in some way. The amount paid out does not match what was requested. The value
                    // of the dispensed and requested amount is contained in the response.
                    case CCommands.Hopper.SSP_POLL_INCOMPLETE_PAYOUT:
                        log.AppendText("Incomplete payout detected...\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 11) + 1);
                        break;
                    // A float was interrupted in some way. The amount floated does not match what was requested. The value
                    // of the dispensed and requested amount is contained in the response.
                    case CCommands.Hopper.SSP_POLL_INCOMPLETE_FLOAT:
                        log.AppendText("Incomplete float detected...\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 11) + 1);
                        break;
                    // This poll appears when coins have been dropped to the cashbox whilst making a payout. The value of
                    // coins and the currency is reported in the response.
                    case CCommands.Hopper.SSP_POLL_CASHBOX_PAID:
                        log.AppendText("Cashbox paid\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // A credit event has been detected, this is when the coin mech has accepted a coin as legal currency.
                    case CCommands.Hopper.SSP_POLL_COIN_CREDIT:
                        coin = CHelpers.ConvertBytesToInt16(m_cmd.ResponseData, i + 1);
                        currency = "";
                        currency += (char)m_CurrentPollResponse[i + 5];
                        currency += (char)m_CurrentPollResponse[i + 6];
                        currency += (char)m_CurrentPollResponse[i + 7];
                        log.AppendText(CHelpers.FormatToCurrency(coin) + " " + currency + " credited\r\n");
                        UpdateData();
                        i += 7;
                        break;
                    // The coin mech has become jammed.
                    case CCommands.Hopper.SSP_POLL_COIN_MECH_JAMMED:
                        log.AppendText("Coin mech jammed\r\n");
                        break;
                    // The return button on the coin mech has been pressed.
                    case CCommands.Hopper.SSP_POLL_COIN_MECH_RETURN_PRESSED:
                        log.AppendText("Return button pressed\r\n");
                        break;
                    // The unit is in the process of dumping all the coins stored inside it into the cashbox.
                    case CCommands.Hopper.SSP_POLL_EMPTYING:
                        log.AppendText("Emptying...\r\n");
                        break;
                    // The unit has finished dumping coins to the cashbox.
                    case CCommands.Hopper.SSP_POLL_EMPTIED:
                        log.AppendText("Emptied\r\n");
                        UpdateData();
                        EnableValidator();
                        break;
                    // A fraud attempt has been detected.
                    case CCommands.Hopper.SSP_POLL_FRAUD_ATTEMPT:
                        log.AppendText("Fraud attempted\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // The unit is in the process of dumping all the coins stored inside it into the cashbox.
                    // This poll means that the unit is keeping track of what it empties.
                    case CCommands.Hopper.SSP_POLL_SMART_EMPTYING:
                        log.AppendText("SMART emptying...\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // The unit has finished SMART emptying. The info on what has been dumped can be obtained
                    // by sending the CASHBOX PAYOUT OPERATION DATA command.
                    case CCommands.Hopper.SSP_POLL_SMART_EMPTIED:
                        GetCashboxPayoutOpData(log);
                        UpdateData();
                        EnableValidator();
                        log.AppendText("SMART emptied\r\n");
                        i += (byte)((m_CurrentPollResponse[i + 1] * 7) + 1);
                        break;
                    // A coin has had its routing changed to either cashbox or recycling.
                    case CCommands.Hopper.SSP_POLL_COIN_ROUTED:
                        log.AppendText("Routed coin\r\n");
                        UpdateData();
                        break;
                    default:
                        log.AppendText("Unsupported poll response received: " + (int)m_CurrentPollResponse[i] + "\r\n");
                        break;
                }
            }
            return true;
        }
        
        // Non-Command functions 

        // This is used to send a command via SSP to the validator
        public bool SendCommand(TextBox log)
        {
            // attempt to send the command
            if (!LibraryHandler.SendCommand(ref m_cmd, ref m_info))
            {
                m_Comms.UpdateLog(m_info, true);
                if (log != null) log.AppendText("Sending command failed\r\nPort status: " + m_cmd.ResponseStatus.ToString() + "\r\n");
                return false;
            }
            // update the log after every command
            m_Comms.UpdateLog(m_info);
            return true;
        }

        // This returns the currency of a specified channel.
        public char[] GetChannelCurrency(int channel)
        {
            if (channel > 0 && channel <= m_NumberOfChannels)
            {
                ChannelData d = m_UnitDataList[channel - 1];
                return d.Currency;
            }
            return null;
        }

        // This returns the level of a specified channel.
        public int GetChannelLevel(int channel)
        {
            if (channel > 0 && channel <= m_NumberOfChannels)
            {
                ChannelData d = m_UnitDataList[channel - 1];
                return d.Level;
            }
            return -1;
        }

        // This is similar to the above function but instead returns all the channels as a
        // nicely formatted string
        public string GetChannelLevelInfo()
        {
            string s = "";
            foreach (ChannelData d in m_UnitDataList)
            {
                s += (d.Value * 0.01f).ToString("0.00") + " " + d.Currency[0] + d.Currency[1] + d.Currency[2];
                s += " [" + d.Level + "] = " + ((d.Level * d.Value) * 0.01f).ToString("0.00");
                s += " " + d.Currency[0] + d.Currency[1] + d.Currency[2] + "\r\n";
            }
            return s;
        }

        // This takes a coin value and returns the channel number
        public int GetChannelofCoin(int coin)
        {
            foreach (ChannelData d in m_UnitDataList)
            {
                if (d.Value == coin)
                    return d.Channel;
            }
            return -1;
        }

        // Exception and Error Handling

        // This is used for generic response error catching, it outputs the info in a
        // meaningful way.
        public bool CheckGenericResponses(TextBox log = null)
        {
            if (m_cmd.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_OK)
                return true;
            else
            {
                if (log != null)
                {
                    switch (m_cmd.ResponseData[0])
                    {
                        case CCommands.Generic.SSP_RESPONSE_CMD_CANNOT_BE_PROCESSED:
                            if (m_cmd.ResponseData[1] == 0x03)
                            {
                                log.AppendText("Unit responded with a \"Busy\" response, command cannot be " +
                                    "processed at this time\r\n");
                            }
                            else
                            {
                                log.AppendText("Command response is CANNOT PROCESS COMMAND, error code - 0x"
                                + BitConverter.ToString(m_cmd.ResponseData, 1, 1) + "\r\n");
                            }
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_FAIL:
                            log.AppendText("Command response is FAIL\r\n");
                            return false;
                        case CCommands.Generic.SSP_RESPONSE_KEY_NOT_SET:
                            log.AppendText("Command response is KEY NOT SET\r\n");
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
                    return false;
            }
        }
    };
}