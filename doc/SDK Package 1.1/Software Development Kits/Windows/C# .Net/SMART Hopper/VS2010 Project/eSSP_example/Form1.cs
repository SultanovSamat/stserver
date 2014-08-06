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
        bool Running = false;
        int pollTimer = 250; // timer in ms
        CHopper Hopper;
        bool bFormSetup = false;
        frmPayoutByDenom payoutByDenomFrm;
        System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();

        // Constructor
        public Form1()
        {
            InitializeComponent();
            timer1.Interval = pollTimer;
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Start();
        }

        // This updates UI variables such as textboxes etc.
        void UpdateUI()
        {
            textBox2.Text = "";
            textBox2.AppendText (Hopper.GetChannelLevelInfo ());

            // can't payout if payout amount or currency box is empty
            if (tbAmountToPayout.Text == "" || tbPayoutCurrency.Text == "")
                btnPayout.Enabled = false;
            else
                btnPayout.Enabled = true;

            // can't set float if either min payout, float amount or float currency is empty
            if (tbMinPayout.Text == "" || tbFloatAmount.Text == "" || tbFloatCurrency.Text == "")
                btnSetFloat.Enabled = false;
            else
                btnSetFloat.Enabled = true;
        }

        // The main program loop, this is to control the validator, it polls at
        // a value set in this class (pollTimer).
        void MainLoop()
        {
            btnRun.Enabled = false;
            textBox1.Clear();
            Hopper.CommandStructure.ComPort = Global.ComPort;
            Hopper.CommandStructure.SSPAddress = Global.SSPAddress;
            Hopper.CommandStructure.Timeout = 2000;
            Hopper.CommandStructure.RetryLevel = 3;

            // First connect to the validator
            if (ConnectToValidator(10, 3))
            {
                btnHalt.Enabled = true;
                Running = true;

                textBox1.AppendText("\r\nPoll Loop\r\n"
                + "*********************************\r\n");
            }

            // This loop won't run until the validator is connected
            while (Running)
            {
                // poll the validator
                if (!Hopper.DoPoll(textBox1))
                {
                    // If the poll fails, try to reconnect
                    textBox1.AppendText("Attempting to reconnect...\r\n");
                    if (!ConnectToValidator(10, 3))
                    {
                        // If it fails after 5 attempts, exit the loop
                        Running = false;
                    }
                }
                // tick the timer
                timer1.Enabled = true;
                // update form
                UpdateUI();
                // setup dynamic elements of win form once
                if (!bFormSetup)
                {
                    SetupFormLayout();
                    bFormSetup = true;
                }
                while (timer1.Enabled)
                {
                    Application.DoEvents();
                    Thread.Sleep(1); // Yield to free up CPU
                }
            }

            //close com port
            Hopper.SSPComms.CloseComPort();

            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        // This is a one off function that is called the first time the MainLoop()
        // function runs, it just sets up a few of the UI elements that only need
        // updating once.
        private void SetupFormLayout()
        {
            // need validator class instance
            if (Hopper == null)
            {
                MessageBox.Show("Validator class is null.", "ERROR");
                return;
            }

           // Positioning
            int x = 555, y = 30;

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
                c.Checked = Hopper.IsChannelRecycling (i);
                c.CheckedChanged += new EventHandler(recycleBox_CheckedChange);
                Controls.Add(c);
            }
        }

        private bool ConnectToValidator(int attempts, int interval)
        {
            // setup the timer
            reconnectionTimer.Interval = interval * 1000; // for ms

            // run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                // reset timer
                reconnectionTimer.Enabled = true;

                // close com port in case it was open
                Hopper.SSPComms.CloseComPort ();

                // turn encryption off for first stage
                Hopper.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (Hopper.OpenComPort(textBox1) && Hopper.NegotiateKeys(textBox1) == true)
                {
                    Hopper.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxProtocolVersion();
                    if (maxPVersion >= 6)
                        Hopper.SetProtocolVersion(maxPVersion, textBox1);
                    else
                    {
                        MessageBox.Show("This program does not support validators under protocol 6!", "ERROR");
                        return false;
                    }
                    // get info from the validator and store useful vars
                    Hopper.SetupRequest(textBox1);
                    // check unit is valid type
                    if (!IsUnitValid(Hopper.UnitType))
                    {
                        MessageBox.Show("Unsupported type shown by SMART Hopper, this SDK supports the SMART Hopper only");
                        Application.Exit();
                        return false;
                    }
                    // inhibits, this sets which channels can receive coins
                    Hopper.SetInhibits(textBox1);
                    // enable, this allows the validator to operate
                    Hopper.EnableValidator(textBox1);
                    return true;
                }
                while (reconnectionTimer.Enabled)
                {
                    Application.DoEvents();
                    Thread.Sleep(1); // Yield to free up CPU
                }
            }
            return false;
        }

        // This function finds the maximum protocol version that a validator supports. To do this
        // it attempts to set a protocol version starting at 6 in this case, and then increments the
        // version until error 0xF8 is returned from the validator which indicates that it has failed
        // to set it. The function then returns the version number one less than the failed version.
        private byte FindMaxProtocolVersion()
        {
            // not dealing with protocol under level 6
            // attempt to set in validator
            byte b = 0x06;
            while (true)
            {
                if (!Hopper.SetProtocolVersion(b) || b > 20)
                    return 0x06; // return default
                // If it fails then it can't be set so fall back to previous iteration and return it
                if (Hopper.CommandStructure.ResponseData[0] == CCommands.SSP_RESPONSE_FAIL)
                    return --b;
                b++;
            }
        }

        private void CalculatePayout(string amount, char[] currency)
        {
            // Split string by decimal point
            string[] s = amount.Split ('.');
            string final = "";
            int payoutAmount = 0;

            // If there was a decimal point
            if (s.Length > 1)
            {
                // Add a trailing zero if necessary
                if (s[1].Length == 1)
                    s[1] += "0";
                // If more than 2 decimal places, cull end
                else if (s[1].Length > 2)
                    s[1] = s[1].Substring (0, 2);

                final += s[0] + s[1]; // Add to final result string
            }
            else
                final += s[0] + "00"; // Add two zeros if there is no decimal point entered

            try
            {
                // Parse it to a number
                payoutAmount = Int32.Parse (final);
            }
            catch (Exception ex)
            {
                return;
            }

            // Pay it out
            Hopper.PayoutAmount (payoutAmount, currency, textBox1);
        }

        private bool IsUnitValid(char unitType)
        {
            if (unitType == (char)0x03) // 0x03 is Hopper, only Hopper supported here
                return true;
            return false;
        }
    }
}
