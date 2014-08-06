using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;
using System.Diagnostics;
using ITLlib;

namespace eSSP_example
{
    public partial class Form1 : Form
    {
        // Variables
        public bool hopperRunning = false, payoutRunning = false;
        volatile public bool hopperConnecting = false, payoutConnecting = false;
        int pollTimer = 250; // timer in ms
        CHopper Hopper; // The class that interfaces with the Hopper
        CPayout Payout; // The class that interfaces with the Payout
        bool FormSetup = false; // To ensure the form is only set up on first run
        frmPayoutByDenom payoutByDenomFrm; // Payout by denomination form
        delegate void OutputMessage(string msg); // Delegate for invoking on cross thread calls
        Thread tHopRec, tSPRec; // Handles to each of the reconnection threads for the 2 units

        // Constructor
        public Form1()
        {
            InitializeComponent();
            timer1.Interval = pollTimer;
            timer2.Interval = 500; // update UI every 500ms
            this.Location = new Point(Screen.PrimaryScreen.Bounds.X + 50, Screen.PrimaryScreen.Bounds.Y + 30);
            this.Enabled = false;
        }

        // This updates UI variables such as textboxes etc.
        void UpdateUI()
        {
            // Get stored notes info from SMART Payout and SMART Hopper at intervals
            if (!timer2.Enabled)
            {
                tbChannelLevels.Text = Payout.GetChannelLevelInfo();
                tbCoinLevels.Text = Hopper.GetChannelLevelInfo();
                timer2.Enabled = true;
            }
        }

        // The main program loop, this is to control the validator, it polls at
        // a value set in this class (pollTimer).
        public void MainLoop()
        {
            this.Enabled = true;
            btnRun.Enabled = false;
            btnHalt.Enabled = true;

            // Connect to the validators (non-threaded for initial connect)
            ConnectToSMARTPayout(textBox1);
            ConnectToHopper(textBox1);

            // Enable validators
            Payout.EnableValidator();
            Hopper.EnableValidator();

            // While app active
            while (!CHelpers.Shutdown)
            {
                // Setup form layout on first run
                if (!FormSetup)
                {
                    SetupFormLayout();
                    FormSetup = true;
                }

                // If the Hopper is supposed to be running but the poll fails
                if (hopperRunning && !hopperConnecting && !Hopper.DoPoll(textBox1))
                {
                    textBox1.AppendText("Lost connection to SMART Hopper\r\n");
                    // If the other unit isn't running, refresh the port by closing it
                    if (!payoutRunning) LibraryHandler.ClosePort();
                    hopperRunning = false;
                    // Create and start a reconnection thread, this allows this loop to continue executing
                    // and polling the other validator
                    tHopRec = new Thread(() => ReconnectHopper());
                    tHopRec.Start();
                }
                // Same as above but for the Payout
                if (payoutRunning && !payoutConnecting && !Payout.DoPoll(textBox1))
                {
                    textBox1.AppendText("Lost connection to SMART Payout\r\n");
                    // If the other unit isn't running, refresh the port by closing it
                    if (!hopperRunning) LibraryHandler.ClosePort();
                    payoutRunning = false;
                    // Create and start a reconnection thread, this allows this loop to continue executing
                    // and polling the other validator
                    tSPRec = new Thread(() => ReconnectPayout());
                    tSPRec.Start();
                }
                UpdateUI();
                timer1.Enabled = true;
                while (timer1.Enabled) Application.DoEvents();
            }

            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        // This is a one off function that is called the first time the MainLoop()
        // function runs, it just sets up a few of the UI elements that only need
        // updating once.
        private void SetupFormLayout()
        {
            // Get channel levels in hopper
            tbCoinLevels.Text = Hopper.GetChannelLevelInfo();

            // setup list of recyclable channel tick boxes based on OS type
            System.OperatingSystem osInfo = System.Environment.OSVersion;
            int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
            // XP, 2000, Server 2003
            if (osInfo.Platform == PlatformID.Win32NT && osInfo.Version.Major == 5)
            {
                x1 = this.Location.X + 455;
                y1 = this.Location.Y + 10;
                x2 = this.Location.X + 780;
                y2 = this.Location.Y + 10;
            }
            // Vista, 7
            else if (osInfo.Platform == PlatformID.Win32NT && osInfo.Version.Major == 6)
            {
                x1 = this.Location.X + 458;
                y1 = this.Location.Y + 12;
                x2 = this.Location.X + 780;
                y2 = this.Location.Y + 12;
            }

            GroupBox g1 = new GroupBox();
            g1.Size = new Size(100, 380);
            g1.Location = new Point(x1, y1);
            g1.Text = "Hopper Recycling";

            GroupBox g2 = new GroupBox();
            g2.Size = new Size(100, 380);
            g2.Location = new Point(x2, y2);
            g2.Text = "Payout Recycling";

            // Hopper checkboxes
            for (int i = 1; i <= Hopper.NumberOfChannels; i++)
            {
                CheckBox c = new CheckBox();
                c.Location = new Point(5, 20 + (i * 20));
                c.Name = i.ToString();
                c.Text = CHelpers.FormatToCurrency(Hopper.GetChannelValue(i)) + " " + new String(Hopper.GetChannelCurrency(i));
                c.Checked = Hopper.IsChannelRecycling(i);
                c.CheckedChanged += new EventHandler(recycleBoxHopper_CheckedChange);
                g1.Controls.Add(c);
            }

            // Payout checkboxes
            for (int i = 1; i <= Payout.NumberOfChannels; i++)
            {
                CheckBox c = new CheckBox();
                c.Location = new Point(5, 20 + (i * 20));
                c.Name = i.ToString();
                ChannelData d = new ChannelData();
                Payout.GetDataByChannel(i, ref d);
                c.Text = CHelpers.FormatToCurrency(d.Value) + " " + new String(d.Currency);
                c.Checked = d.Recycling;
                c.CheckedChanged += new EventHandler(recycleBoxPayout_CheckedChange);
                g2.Controls.Add(c);
            }

            Controls.Add(g1);
            Controls.Add(g2);
        }

        public void ConnectToHopper(TextBox log = null)
        {
            hopperConnecting = true;
            // setup timer, timeout delay and number of attempts to connect
            System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Interval = 1000; // ms
            int attempts = 10;

            // Setup connection info
            Hopper.CommandStructure.ComPort = Global.ValidatorComPort;
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
                        MessageBox.Show("This program does not support units under protocol 6!", "ERROR");
                        hopperConnecting = false;
                        return;
                    }
                    // get info from the hopper and store useful vars
                    Hopper.SetupRequest(log);
                    // check the right unit is connected
                    if (!IsHopperSupported(Hopper.UnitType))
                    {
                        MessageBox.Show("Unsupported type shown by SMART Hopper, this SDK supports the SMART Payout and the SMART Hopper only");
                        hopperConnecting = false;
                        Application.Exit();
                        return;
                    }
                    // inhibits, this sets which channels can receive coins
                    Hopper.SetInhibits(log);
                    // set running to true so the hopper begins getting polled
                    hopperRunning = true;
                    hopperConnecting = false;
                    return;
                }
                // reset timer
                reconnectionTimer.Enabled = true;
                while (reconnectionTimer.Enabled)
                {
                    if (CHelpers.Shutdown)
                    {
                        hopperConnecting = false;
                        return;
                    }

                    Application.DoEvents();
                    Thread.Sleep(1);
                }
            }
            hopperConnecting = false;
            return;
        }

        public void ConnectToSMARTPayout(TextBox log = null)
        {
            payoutConnecting = true;
            // setup timer, timeout delay and number of attempts to connect
            System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Interval = 1000; // ms
            int attempts = 10;

            // Setup connection info
            Payout.CommandStructure.ComPort = Global.ValidatorComPort;
            Payout.CommandStructure.SSPAddress = Global.Validator1SSPAddress;
            Payout.CommandStructure.BaudRate = 9600;
            Payout.CommandStructure.Timeout = 1000;
            Payout.CommandStructure.RetryLevel = 3;

            // Run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                if (log != null) log.AppendText("Trying connection to SMART Payout\r\n");

                // turn encryption off for first stage
                Payout.CommandStructure.EncryptionStatus = false;

                // Open port first, if the key negotiation is successful then set the rest up
                if (Payout.OpenPort() && Payout.NegotiateKeys(log))
                {
                    Payout.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxPayoutProtocolVersion();
                    if (maxPVersion >= 6)
                        Payout.SetProtocolVersion(maxPVersion, log);
                    else
                    {
                        MessageBox.Show("This program does not support units under protocol 6!", "ERROR");
                        payoutConnecting = false;
                        return;
                    }
                    // get info from the validator and store useful vars
                    Payout.SetupRequest(log);
                    // check the right unit is connected
                    if (!IsValidatorSupported(Payout.UnitType))
                    {
                        MessageBox.Show("Unsupported type shown by SMART Payout, this SDK supports the SMART Payout and the SMART Hopper only");
                        payoutConnecting = false;
                        Application.Exit();
                        return;
                    }
                    // inhibits, this sets which channels can receive notes
                    Payout.SetInhibits(log);
                    // enable payout
                    Payout.EnablePayout(log);
                    // set running to true so the validator begins getting polled
                    payoutRunning = true;
                    payoutConnecting = false;
                    return;
                }
                // reset timer
                reconnectionTimer.Enabled = true;
                while (reconnectionTimer.Enabled)
                {
                    if (CHelpers.Shutdown)
                    {
                        payoutConnecting = false;
                        return;
                    }

                    Application.DoEvents();
                    Thread.Sleep(1);
                }
            }
            payoutConnecting = false;
            return;
        }

        // Used when invoking for cross-thread calls
        private void AppendToTextBox(string s)
        {
            textBox1.AppendText(s);
        }

        // These functions are run in a seperate thread to allow the main loop to continue executing.
        private void ReconnectHopper()
        {
            OutputMessage m = new OutputMessage(AppendToTextBox);
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
        }

        private void ReconnectPayout()
        {
            OutputMessage m = new OutputMessage(AppendToTextBox);
            while (!payoutRunning)
            {
                if (textBox1.InvokeRequired)
                    textBox1.Invoke(m, new object[] { "Attempting to reconnect to SMART Payout...\r\n" });
                else
                    textBox1.AppendText("Attempting to reconnect to SMART Payout...\r\n");

                ConnectToSMARTPayout(null); // Have to pass null as can't update text box from a different thread without invoking

                CHelpers.Pause(1000);
                if (CHelpers.Shutdown) return;
            }
            if (textBox1.InvokeRequired)
                textBox1.Invoke(m, new object[] { "Reconnected to SMART Payout\r\n" });
            else
                textBox1.AppendText("Reconnected to SMART Payout\r\n");
            Payout.EnableValidator();
        }

        // This function finds the maximum protocol version that a validator supports. To do this
        // it attempts to set a protocol version starting at 6 in this case, and then increments the
        // version until error 0xF8 is returned from the validator which indicates that it has failed
        // to set it. The function then returns the version number one less than the failed version.
        private byte FindMaxHopperProtocolVersion()
        {
            // not dealing with protocol under level 6
            // attempt to set in hopper
            byte b = 0x06;
            while (true)
            {
                // If command can't get through, break out
                if (!Hopper.SetProtocolVersion(b)) break;
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (Hopper.CommandStructure.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_FAIL)
                    return --b;
                b++;

                if (b > 20)
                    return 0x06; // return default if protocol gets too high (must be failure)
            }
            return 0x06; // default
        }

        private byte FindMaxPayoutProtocolVersion()
        {
            // not dealing with protocol under level 6
            // attempt to set in validator
            byte b = 0x06;
            while (true)
            {
                Payout.SetProtocolVersion(b);
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (Payout.CommandStructure.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_FAIL)
                    return --b;
                b++;

                // If the protocol version 'runs away' because of a drop in comms. Return the default value.
                if (b > 20)
                    return 0x06;
            }
        }

        // This function shows a simple example of calculating a payout split between the SMART Payout and the 
        // SMART Hopper. It works on a highest value split, first the notes are looked at, then any remainder
        // that can't be paid out with a note is paid from the SMART Hopper.
        private void CalculatePayout(string amount, char[] currency)
        {
            float payoutAmount;
            try
            {
                // Parse it to a number
                payoutAmount = float.Parse(amount) * 100;
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            int payoutList = 0;
            // Obtain the list of sorted channels from the SMART Payout, this is sorted by channel value
            // - lowest first
            List<ChannelData> reverseList = new List<ChannelData>(Payout.UnitDataList);
            reverseList.Reverse(); // Reverse the list so the highest value is first

            // Iterate through each
            foreach(ChannelData d in reverseList)
            {
                ChannelData temp = d; // Don't overwrite real values
                // Keep testing to see whether we need to payout this note or the next note
                while (true)
                {
                    // If the amount to payout is greater than the value of the current note and there is
                    // some of that note available and it is the correct currency
                    if (payoutAmount >= temp.Value && temp.Level > 0 && String.Equals(new String(temp.Currency), new String(currency)))
                    {
                        payoutList += temp.Value; // Add to the list of notes to payout from the SMART Payout
                        payoutAmount -= temp.Value; // Minus from the total payout amount
                        temp.Level--; // Take one from the level
                    }
                    else
                        break; // Don't need any more of this note
                }
            }

            // Test the proposed payout values
            if (payoutList > 0)
            {
                // First test SP
                Payout.PayoutAmount(payoutList, currency, true);
                if (Payout.CommandStructure.ResponseData[0] != 0xF0)
                {
                    DialogResult res =
                        MessageBox.Show("Smart Payout unable to pay requested amount, attempt to pay all from Hopper?",
                        "Error with Payout", MessageBoxButtons.YesNo);

                    if (res == System.Windows.Forms.DialogResult.No)
                        return;
                    else
                        payoutAmount += payoutList;
                }

                // SP is ok to pay
                Payout.PayoutAmount(payoutList, currency, false, textBox1); 
            }

            // Now if there is any left over, request from Hopper
            if (payoutAmount > 0)
            {
                // Test Hopper first
                Hopper.PayoutAmount((int)payoutAmount, currency, true);
                if (Hopper.CommandStructure.ResponseData[0] != 0xF0)
                {
                    MessageBox.Show("Unable to pay requested amount!");
                    return;
                }

                // Hopper is ok to pay
                Hopper.PayoutAmount((int)payoutAmount, currency, false, textBox1);
            }
        }

        // This function checks whether the type of validator is supported by this program.
        private bool IsValidatorSupported(char type)
        {
            if (type == (char)0x06)
                return true;
            return false;
        }

        private bool IsHopperSupported(char type)
        {
            if (type == (char)0x03)
                return true;
            return false;
        }
    }
}
