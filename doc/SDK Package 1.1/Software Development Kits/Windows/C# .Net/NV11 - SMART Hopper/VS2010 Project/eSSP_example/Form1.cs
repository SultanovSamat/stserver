using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using ITLlib;

namespace eSSP_example
{
    public partial class Form1 : Form
    {
        // Variables
        SSPComms sspLib = new SSPComms();
        public bool hopperRunning = false, NV11Running = false;
        volatile bool hopperConnecting = false, NV11Connecting = false;
        int pollTimer = 250; // timer in ms
        CHopper Hopper; // Class to interface with the Hopper
        CNV11 NV11; // Class to interface with the NV11
        bool FormSetup = false;
        frmPayoutByDenom payoutByDenomFrm;
        delegate void OutputMessage(string s);

        // Constructor
        public Form1()
        {
            InitializeComponent();
            timer1.Interval = pollTimer;
            this.Location = new Point(Screen.PrimaryScreen.Bounds.X, Screen.PrimaryScreen.Bounds.Y);
            btnHalt.Enabled = false;
        }

        // This updates UI variables such as textboxes etc.
        void UpdateUI()
        {
            // Get stored notes info from NV11
            tbNotesStored.Text = NV11.GetStorageInfo();

            // Get channel info from hopper
            tbCoinLevels.Text = Hopper.GetChannelLevelInfo ();
        }

        bool OpenComPort()
        {
            SSP_COMMAND cmd = new SSP_COMMAND();
            cmd.ComPort = Global.ComPort;
            cmd.BaudRate = 9600;
            cmd.Timeout = 500;
            return sspLib.OpenSSPComPort(cmd);
        }

        // The main program loop, this is to control the validator, it polls at
        // a value set in this class (pollTimer).
        public void MainLoop()
        {
            btnRun.Enabled = false;
            btnHalt.Enabled = true;
            Thread tNV11Rec = null, tHopRec = null;

            // Connect to the validators
            ConnectToNV11(textBox1);
            ConnectToHopper(textBox1);

            NV11.EnableValidator();
            Hopper.EnableValidator();

            // While application is still active
            while (!CHelpers.Shutdown)
            {
                // Setup form layout on first run
                if (!FormSetup)
                {
                    SetupFormLayout();
                    FormSetup = true;
                }

                // If the hopper is supposed to be running but the poll fails
                if (hopperRunning && !Hopper.DoPoll(textBox1))
                {
                    textBox1.AppendText("Lost connection to SMART Hopper\r\n");
                    // If the other unit isn't running, refresh the port by closing it
                    if (!NV11Running) LibraryHandler.ClosePort();
                    hopperRunning = false;
                    tHopRec = new Thread(() => ReconnectHopper());
                    tHopRec.Start();
                }

                // If the NV11 is supposed to be running but the poll fails
                if (NV11Running && !NV11.DoPoll(textBox1))
                {
                    textBox1.AppendText("Lost connection to NV11\r\n");
                    // If the other unit isn't running, refresh the port by closing it
                    if (!hopperRunning) LibraryHandler.ClosePort();
                    NV11Running = false;
                    tNV11Rec = new Thread(() => ReconnectNV11());
                    tNV11Rec.Start();
                }

                UpdateUI();
                timer1.Enabled = true;

                while (timer1.Enabled)
                {
                    Application.DoEvents();
                    Thread.Sleep(1); // Yield so windows can schedule other threads to run
                }
            }

            //close com port
            LibraryHandler.ClosePort();

            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        // This is a one off function that is called the first time the MainLoop()
        // function runs, it just sets up a few of the UI elements that only need
        // updating once.
        private void SetupFormLayout()
        {
            // Note float UI setup

            // Find number and value of channels in NV11 and update combo box
            cbRecycleChannelNV11.Items.Add("No recycling");
            foreach (ChannelData d in NV11.UnitDataList)
            {
                cbRecycleChannelNV11.Items.Add(d.Value / 100 + " " + new String(d.Currency));
            }
            cbRecycleChannelNV11.SelectedIndex = 0;

            // Get channel levels in hopper
            tbCoinLevels.Text = Hopper.GetChannelLevelInfo();

            // setup list of recyclable channel tick boxes
            int x = 0, y = 0;
            x = 465;
            y = 30;
           
            Label lbl = new Label();
            lbl.Location = new Point(x, y);
            lbl.Size = new Size(70, 35);
            lbl.Name = "lbl";
            lbl.Text = "Recycle\nChannels:";
            Controls.Add(lbl);

            y += 20;
            for (int i = 1; i <= Hopper.NumberOfChannels; i++)
            {
                CheckBox c = new CheckBox();
                c.Location = new Point(x, y + (i * 20));
                c.Name = i.ToString();
                c.Text = CHelpers.FormatToCurrency(Hopper.GetChannelValue(i)) + " " + new String(Hopper.GetChannelCurrency(i));
                c.Checked = Hopper.IsChannelRecycling(i);
                c.CheckedChanged += new EventHandler(recycleBox_CheckedChange);
                Controls.Add(c);
            }
        }

        public void ConnectToHopper(TextBox log = null)
        {
            // setup timer
            System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Interval = 1000; // ms
            int attempts = 10;

            // Setup connection info
            Hopper.CommandStructure.ComPort = Global.ComPort;
            Hopper.CommandStructure.SSPAddress = Global.Validator2SSPAddress;
            Hopper.CommandStructure.BaudRate = 9600;
            Hopper.CommandStructure.Timeout = 1000;
            Hopper.CommandStructure.RetryLevel = 3;

            // Run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                if (log != null) log.AppendText("Trying connection to SMART Hopper\r\n");

                // turn encryption off for first stage
                Hopper.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (Hopper.OpenPort() && Hopper.NegotiateKeys(log))
                {
                    Hopper.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxHopperProtocolVersion();
                    if (maxPVersion >= 6)
                        Hopper.SetProtocolVersion(maxPVersion, log);
                    else
                    {
                        MessageBox.Show("This program does not support slaves under protocol 6!", "ERROR");
                        return;
                    }
                    // get info from the validator and store useful vars
                    Hopper.SetupRequest(log);
                    // inhibits, this sets which channels can receive notes
                    Hopper.SetInhibits(log);
                    // set running to true so the hopper begins getting polled
                    hopperRunning = true;
                    return;
                }
                // reset timer
                reconnectionTimer.Enabled = true;
                while (reconnectionTimer.Enabled)
                {
                    if (CHelpers.Shutdown)
                        return;
                    Application.DoEvents();
                }
            }
        }

        public void ConnectToNV11(TextBox log = null)
        {
            // setup timer
            System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Interval = 1000; // ms
            int attempts = 10;

            // Setup connection info
            NV11.CommandStructure.ComPort = Global.ComPort;
            NV11.CommandStructure.SSPAddress = Global.Validator1SSPAddress;
            NV11.CommandStructure.BaudRate = 9600;
            NV11.CommandStructure.Timeout = 1000;
            NV11.CommandStructure.RetryLevel = 3;

            // Run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                if (log != null) log.AppendText("Trying connection to NV11\r\n");

                // turn encryption off for first stage
                NV11.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (NV11.OpenPort() && NV11.NegotiateKeys(log))
                {
                    NV11.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxNV11ProtocolVersion();
                    if (maxPVersion >= 6)
                        NV11.SetProtocolVersion(maxPVersion, log);
                    else
                    {
                        MessageBox.Show("This program does not support slaves under protocol 6!", "ERROR");
                        return;
                    }
                    // get info from the validator and store useful vars
                    NV11.SetupRequest(log);
                    // inhibits, this sets which channels can receive notes
                    NV11.SetInhibits(log);
                    // enable payout to begin with
                    NV11.EnablePayout(log);
                    // check for any stored notes
                    NV11.CheckForStoredNotes(log);
                    // report by the 4 byte value of the note, not the channel
                    NV11.SetValueReportingType(false, log);
                    // set running to true so the NV11 begins getting polled
                    NV11Running = true;
                    return;
                }
                // reset timer
                reconnectionTimer.Enabled = true;
                while (reconnectionTimer.Enabled)
                {
                    if (CHelpers.Shutdown)
                        return;
                    Application.DoEvents();
                }
            }
        }

        // These functions are run in a seperate thread to allow the main loop to continue executing.
        private void ReconnectHopper()
        {
            OutputMessage m = new OutputMessage(AppendToTextBox);
            hopperConnecting = true;
            while (!hopperRunning)
            {
                if (textBox1.InvokeRequired)
                    textBox1.Invoke(m, new object[] { "Attempting to reconnect to SMART Hopper...\r\n" });
                else
                    textBox1.AppendText("Attempting to reconnect to SMART Hopper...\r\n");

                ConnectToHopper();
                CHelpers.Pause(1000);
                if (CHelpers.Shutdown) return;
            }
            if (textBox1.InvokeRequired)
                textBox1.Invoke(m, new object[] { "Reconnected to SMART Hopper\r\n" });
            else
                textBox1.AppendText("Reconnected to SMART Hopper\r\n");
            Hopper.EnableValidator();
            hopperConnecting = false;
        }

        private void ReconnectNV11()
        {
            OutputMessage m = new OutputMessage(AppendToTextBox);
            NV11Connecting = true;
            while (!NV11Running)
            {
                if (textBox1.InvokeRequired)
                    textBox1.Invoke(m, new object[] { "Attempting to reconnect to NV11...\r\n" });
                else
                    textBox1.AppendText("Attempting to reconnect to NV11...\r\n");

                ConnectToNV11(null); // Have to pass null as can't update text box from a different thread without invoking

                CHelpers.Pause(1000);
                if (CHelpers.Shutdown) return;
            }
            if (textBox1.InvokeRequired)
                textBox1.Invoke(m, new object[] { "Reconnected to NV11\r\n" });
            else
                textBox1.AppendText("Reconnected to NV11\r\n");
            NV11.EnableValidator();
            NV11Connecting = false;
        }

        public void AppendToTextBox(string s)
        {
            textBox1.AppendText(s);
        }

        // This function finds the maximum protocol version that a validator supports. To do this
        // it attempts to set a protocol version starting at 6 in this case, and then increments the
        // version until error 0xF8 is returned from the validator which indicates that it has failed
        // to set it. The function then returns the version number one less than the failed version.
        private byte FindMaxHopperProtocolVersion()
        {
            // not dealing with protocol under level 6
            // attempt to set in validator
            byte b = 0x06;
            while (true)
            {
                Hopper.SetProtocolVersion(b);
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (Hopper.CommandStructure.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_FAIL)
                    return --b;
                b++;
                if (b > 20) return 0x06; // return default if p version runs too high
            }
        }

        private byte FindMaxNV11ProtocolVersion()
        {
            // not dealing with protocol under level 6
            // attempt to set in validator
            byte b = 0x06;
            while (true)
            {
                NV11.SetProtocolVersion(b);
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (NV11.CommandStructure.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_FAIL)
                    return --b;
                b++;
                if (b > 20) return 0x06; // return default if p version runs too high
            }
        }

        // This function calculates how the payout should be handled, it takes the input as a string
        // from the text box and converts it to a number to payout in the proper format. E.g. The user
        // enters "9.50", this is converted to 950 for payouts. Logic can then be performed on this number
        // to work out the best way of dispensing it E.g. 5.00 note dispensed from nv11 and 4.50 from hopper.
        private void CalculatePayout(string amount, string currency)
        {
            float payoutAmount = 0;

            try
            {
                // Parse amount to a number
                payoutAmount = float.Parse(amount) * 100;
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
                return;
            }

            // Now we have a number we have to work out which validator needs to process the payout

            // This section can be modified to payout in any way needed. Currently it pays out one
            // note and the rest from the hopper - this could be extended to payout multiple notes.

            // First work out if we need a note, if it's less than the value of the first channel (assuming channels are in value order)
            // then we know it doesn't need one
            if (payoutAmount < NV11.GetChannelValue(1))
            {
                // So we can send the command straight to the hopper
                Hopper.PayoutAmount((int)payoutAmount, currency.ToCharArray(), textBox1);
            }
            else
            {
                // Otherwise we need to work out how to payout notes
                // Check what value the last note stored in the payout was
                int noteVal = NV11.GetLastNoteValue();
                // If less or equal to payout, pay it out
                if (noteVal <= payoutAmount && noteVal > 0)
                {
                    payoutAmount -= noteVal;// take the note off the total requested
                    NV11.EnablePayout();
                    NV11.PayoutNextNote (textBox1);
                }
                // payout the remaining from the hopper if needed
                if (payoutAmount > 0)
                    Hopper.PayoutAmount((int)payoutAmount, currency.ToCharArray(), textBox1); 
            }
        }
    }
}
