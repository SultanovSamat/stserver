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
        public bool hopperRunning = false, validatorRunning = false;
        int pollTimer = 250; // timer in ms
        CHopper Hopper; // The class used to interface with the Hopper
        CValidator Validator; // The class used to interface with the Validator
        bool FormSetup = false; // Used so the form is only setup once
        frmPayoutByDenom payoutByDenomFrm; // Pointer to the payout by denomination form
        
        delegate void OutputMessage(string msg); // delegate for cross thread invoking

        // Constructor
        public Form1()
        {
            InitializeComponent();
            timer1.Interval = pollTimer;
            this.Location = new Point(Screen.PrimaryScreen.Bounds.X, Screen.PrimaryScreen.Bounds.Y);
            this.Enabled = false;
        }

        // This updates UI variables such as textboxes etc.
        void UpdateUI()
        {
            tbCoinLevels.Text = Hopper.GetChannelLevelInfo ();
        }

        // The main program loop, this is to control the validator, it polls at
        // a value set in this class (pollTimer).
        public void MainLoop()
        {
            Enabled = true;

            // Connect to the validators
            ConnectToNoteValidator(textBox1);
            ConnectToHopper(textBox1);

            // Enable the validators
            Validator.EnableValidator(textBox1);
            Hopper.EnableValidator(textBox1);

            // While either one is still running
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
                    hopperRunning = false;
                    // If the other device has also stopped, close the port
                    if (!validatorRunning)
                        LibraryHandler.ClosePort();
                    // Create a reconnection thread, this allows this loop to continue executing
                    // and polling the other validator
                    Thread t = new Thread(ReconnectHopper);
                    t.Start();
                }
                // If the validator is supposed to be running but the poll fails
                if (validatorRunning && !Validator.DoPoll(textBox1))
                {
                    textBox1.AppendText("Lost connection to note validator\r\n");
                    validatorRunning = false;
                    // If the other device has also stopped, close the port
                    if (!hopperRunning)
                        LibraryHandler.ClosePort();
                    // Create a reconnection thread, this allows this loop to continue executing
                    // and polling the other validator
                    Thread t = new Thread(ReconnectValidator);
                    t.Start();
                }
                UpdateUI();
                timer1.Enabled = true;
                while (timer1.Enabled) Application.DoEvents();
            }

            LibraryHandler.ClosePort();

            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        // This is a one off function that is called the first time the MainLoop()
        // function runs, it just sets up a few of the UI elements that only need
        // updating once.
        private void SetupFormLayout()
        {
            // Basic Validator UI setup

            // Get channel levels in hopper
            tbCoinLevels.Text = Hopper.GetChannelLevelInfo();

            // setup list of recyclable channel tick boxes based on OS type
            System.OperatingSystem osInfo = System.Environment.OSVersion;
            int x = 560, y = 47;

            Label l = new Label();
            l.Location = new Point(x, y);
            l.Size = new Size(65, 38);
            l.Name = "lblRecycleCheckBoxes";
            l.Text = "Recycle\nChannels:";
            Controls.Add(l);

            for (int i = 1; i <= Hopper.NumberOfChannels; i++)
            {
                CheckBox c = new CheckBox();
                c.Location = new Point(x, y + 15 + (i * 20));
                c.Name = i.ToString();
                c.Text = CHelpers.FormatToCurrency(Hopper.GetChannelValue(i)) +
                    " " + new String(Hopper.GetChannelCurrency(i));
                c.Checked = Hopper.IsChannelRecycling (i);
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
            Hopper.CommandStructure.Timeout = 1000;
            Hopper.CommandStructure.RetryLevel = 3;

            // Run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                // reset timer
                reconnectionTimer.Enabled = true;

                // turn encryption off for first stage
                Hopper.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (Hopper.OpenComPort(log) && Hopper.NegotiateKeys(log) == true)
                {
                    Hopper.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxHopperProtocolVersion();
                    if (maxPVersion >= 6)
                        Hopper.SetProtocolVersion(maxPVersion, log);
                    else
                    {
                        MessageBox.Show("This program does not support units under protocol 6!", "ERROR");
                        return;
                    }
                    // get info from the validator and store useful vars
                    Hopper.SetupRequest(log);
                    // check unit is valid
                    if (!IsHopperValid(Hopper.UnitType))
                    {
                        string s = "Unsupported unit type detected on the Hopper port, this SDK requires a SMART Hopper\n";
                        s += "on the Hopper port.";
                        MessageBox.Show(s);
                        return;
                    }
                    // inhibits, this sets which channels can receive notes
                    Hopper.SetInhibits(log);
                    // set running to true so the hopper begins getting polled
                    hopperRunning = true;
                    return;
                }
                while (reconnectionTimer.Enabled)
                {
                    if (CHelpers.Shutdown)
                        return;
                    Application.DoEvents();
                    Thread.Sleep(1); // Yield to free up CPU
                }
            }
            return;
        }

        public void ConnectToNoteValidator(TextBox log = null)
        {
            // setup timer and attempts
            System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Interval = 1000;
            int attempts = 10;

            // Setup connection info
            Validator.CommandStructure.ComPort = Global.ComPort;
            Validator.CommandStructure.SSPAddress = Global.Validator1SSPAddress;
            Validator.CommandStructure.Timeout = 1000;
            Validator.CommandStructure.RetryLevel = 3;

            // Run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                // reset timer
                reconnectionTimer.Enabled = true;

                // turn encryption off for first stage
                Validator.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (Validator.OpenComPort(log) && Validator.NegotiateKeys(log) == true)
                {
                    Validator.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxValidatorProtocolVersion();
                    if (maxPVersion > 6)
                    {
                        Validator.SetProtocolVersion(maxPVersion, log);
                    }
                    else
                    {
                        MessageBox.Show("This program does not support units under protocol 6!", "ERROR");
                        return;
                    }
                    // get info from the validator and store useful vars
                    Validator.SetupRequest(log);
                    // check unit is valid
                    if (!IsValidatorValid(Validator.UnitType))
                    {
                        string s = "Unsupported unit type detected on the Validator port, this SDK requires a BV or NV series ";
                        s += "validator on the Validator port (excluding NV11).";
                        MessageBox.Show(s);
                        Application.Exit();
                        return;
                    }
                    // inhibits, this sets which channels can receive notes
                    Validator.SetInhibits(log);
                    // set running to true so the validator begins getting polled
                    validatorRunning = true;
                    return;
                }
                while (reconnectionTimer.Enabled)
                {
                    if (CHelpers.Shutdown)
                        return;
                    Application.DoEvents();
                    Thread.Sleep(1);
                }
            };
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
                if (!Hopper.SetProtocolVersion(b)) return 0x06; // return default
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (Hopper.CommandStructure.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_FAIL)
                    return --b;
                b++;
            }
        }

        private byte FindMaxValidatorProtocolVersion()
        {
            // not dealing with protocol under level 6
            // attempt to set in validator
            byte b = 0x06;
            while (true)
            {
                Validator.SetProtocolVersion(b);
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (Validator.CommandStructure.ResponseData[0] == CCommands.Generic.SSP_RESPONSE_FAIL)
                    return --b;
                b++;

                if (b > 20)
                    return 0x06; // return default
            }
        }

        // This is for use with the delegate function to deal with cross-thread access to the text box
        void AppendToTextBox(string s)
        {
            textBox1.AppendText(s);
        }

        // This function is run in a seperate thread, it allows a reconnection to a validator to happen in the
        // background.
        private void ReconnectValidator()
        {
            OutputMessage m = new OutputMessage(AppendToTextBox); // setup delegate
            while (!validatorRunning)
            {
                if (CHelpers.Shutdown)
                    return;
                // Check for cross-thread access
                if (textBox1.InvokeRequired)
                    textBox1.Invoke(m, new object[] { "Attempting to reconnect to note validator...\r\n" });
                else
                    textBox1.AppendText("Attempting to reconnect to note validator...\r\n");

                // Attempt reconnect (can't pass text box across for output as the class does not
                // deal with threading issues).
                ConnectToNoteValidator();
                CHelpers.Pause(1000);
            }
            if (textBox1.InvokeRequired)
                textBox1.Invoke(m, new object[] { "Reconnected to note validator\r\n" });
            else
                textBox1.AppendText("Reconnected to note validator\r\n");
            Validator.EnableValidator();
        }

        // See above commenting
        private void ReconnectHopper()
        {
            OutputMessage m = new OutputMessage(AppendToTextBox);
            while (!hopperRunning)
            {
                if (CHelpers.Shutdown)
                    return;
                if (textBox1.InvokeRequired)
                    textBox1.Invoke(m, new object[] { "Attempting to reconnect to SMART Hopper...\r\n" });
                else
                    textBox1.AppendText("Attempting to reconnect to SMART Hopper...\r\n");

                ConnectToHopper();
                Hopper.EnableValidator();
            }
            if (textBox1.InvokeRequired)
                textBox1.Invoke(m, new object[] { "Reconnected to SMART Hopper\r\n" });
            else
                textBox1.AppendText("Reconnected to SMART Hopper\r\n");
        }

        // This function takes a string input from the text box and then converts it to a usable number
        // that the validator will understand. E.g. user enters "3.40", this gets translated to 340 to
        // pass to the hopper.
        private void CalculatePayout(string amount, char[] currency)
        {
            float payoutAmount = 0;
            try
            {
                // Parse it to a number
                payoutAmount = float.Parse(amount);
            }
            catch (Exception ex)
            {
                return;
            }

            // Pay it out
            Hopper.PayoutAmount ((int)payoutAmount, currency, textBox1);
        }

        // Methods to check whether the program supports the connected units
        private bool IsHopperValid(char unitType)
        {
            if (unitType == (char)0x03) // 0x03 is hopper
                return true;
            return false;
        }

        private bool IsValidatorValid(char unitType)
        {
            if (unitType == (char)0x00) // 0x00 is note validator
                return true;
            return false;
        }
    }
}
