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
        // Variables used by this program.
        bool Running = false;
        int pollTimer = 250; // timer in ms
        int reconnectionAttempts = 5;
        System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
        CPayout Payout;
        bool bFormSetup = false;
        frmPayoutByDenom payoutByDenomFrm;
        List<CheckBox> recycleBoxes = new List<CheckBox>();

        // Constructor
        public Form1()
        {
            InitializeComponent();
            timer1.Interval = pollTimer;
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
        }

        // This updates UI variables such as textboxes etc.
        void UpdateUI()
        {
            // update text boxes
            tbLevelInfo.Text = Payout.GetChannelLevelInfo ();

            // disable buttons if input would be invalid
            if (tbMinPayout.Text == "" || tbFloatAmount.Text == "" || tbFloatCurrency.Text == "")
                btnSetFloat.Enabled = false;
            else
                btnSetFloat.Enabled = true;

            if (tbPayoutAmount.Text == "" || tbPayoutCurrency.Text == "")
                btnPayout.Enabled = false;
            else
                btnPayout.Enabled = true;
        }

        // The main program loop, this is to control the validator, it polls at
        // a value set in this class (pollTimer).
        void MainLoop()
        {
            btnRun.Enabled = false;
            Payout.CommandStructure.ComPort = Global.ComPort;
            Payout.CommandStructure.SSPAddress = Global.SSPAddress;
            Payout.CommandStructure.Timeout = 3000;

            // connect to validator
            if (ConnectToValidator(reconnectionAttempts, 2))
            {
                Running = true; 
                textBox1.AppendText("\r\nPoll Loop\r\n*********************************\r\n");
                btnHalt.Enabled = true;
            }

            while (Running)
            {
                // if the poll fails, try to reconnect
                if (Payout.DoPoll (textBox1) == false)
                {
                    textBox1.AppendText("Poll failed, attempting to reconnect...\r\n");
                    while (true)
                    {
                        Payout.SSPComms.CloseComPort (); // close com port

                        // attempt reconnect, pass over number of reconnection attempts
                        if (ConnectToValidator(reconnectionAttempts, 2) == true)
                            break; // if connection successful, break out and carry on
                        // if not successful, stop the execution of the poll loop
                        btnRun.Enabled = true;
                        btnHalt.Enabled = false;
                        Payout.SSPComms.CloseComPort (); // close com port before return
                        return;
                    }
                    textBox1.AppendText("Reconnected\r\n");
                }

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
            Payout.SSPComms.CloseComPort ();

            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        // This is a one off function that is called the first time the MainLoop()
        // function runs, it just sets up a few of the UI elements that only need
        // updating once.
        private void SetupFormLayout()
        {
            // need validator class instance
            if (Payout == null)
            {
                MessageBox.Show("Validator class is null.", "ERROR");
                return;
            }

            // Positioning
            int x = 600, y = 40;

            // Add label
            Label lbl = new Label();
            lbl.Location = new Point(x, y-10);
            lbl.Size = new Size(80, 30);
            lbl.Text = "Recycle\nChannels:";
            Controls.Add(lbl);

            for (int i = 1; i <= Payout.NumberOfChannels; i++)
            {
                CheckBox c = new CheckBox();
                c.Location = new Point(x, y + (i * 20));
                c.Name = i.ToString();
                ChannelData d = new ChannelData();
                Payout.GetDataByChannel(i, ref d);
                c.Text = CHelpers.FormatToCurrency(d.Value) + " " + new String(d.Currency);
                c.Checked = d.Recycling;
                c.CheckedChanged += new EventHandler(recycleBox_CheckedChange);
                Controls.Add(c);
            }
        }

        // Clears all the checkboxes to unticked
        private void ClearCheckBoxes()
        {
            foreach (CheckBox c in recycleBoxes)
                c.Checked = false;
        }

        // This function opens the com port and attempts to connect with the validator. It then negotiates
        // the keys for encryption and performs some other setup commands.
        private bool ConnectToValidator(int attempts, int interval)
        {
            // setup timer
            reconnectionTimer.Interval = interval * 1000; // ms

            // run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                // close com port in case it was open
                Payout.SSPComms.CloseComPort ();

                // turn encryption off for first stage
                Payout.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (Payout.OpenComPort(textBox1) && Payout.NegotiateKeys(textBox1))
                {
                    Payout.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxProtocolVersion();
                    if (maxPVersion >= 6)
                    {
                        Payout.SetProtocolVersion(maxPVersion, textBox1);
                    }
                    else
                    {
                        MessageBox.Show("This program does not support slaves under protocol 6!", "ERROR");
                        return false;
                    }
                    // get info from the validator and store useful vars
                    Payout.SetupRequest(textBox1);
                    // check this unit is supported
                    if (!IsUnitValid(Payout.UnitType))
                    {
                        MessageBox.Show("Unsupported type shown by SMART Payout, this SDK supports the SMART Payout only");
                        Application.Exit();
                        return false;
                    }
                    // inhibits, this sets which channels can receive notes
                    Payout.SetInhibits(textBox1);
                    // enable, this allows the validator to operate
                    Payout.EnableValidator(textBox1);
                    // enable the payout system on the validator
                    Payout.EnablePayout(textBox1);
                    return true;
                }
                // Set timer
                reconnectionTimer.Enabled = true;
                while (reconnectionTimer.Enabled) Application.DoEvents();
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
                Payout.SetProtocolVersion (b);
                if (Payout.CommandStructure.ResponseData[0] == CCommands.SSP_RESPONSE_CMD_FAIL)
                    return --b;
                b++;

                // catch runaway
                if (b > 12)
                    return 0x06; // return default
            }
        }

        private void CalculatePayout(string amount, char[] currency)
        {
            string[] s = amount.Split ('.');
            // only need to deal with whole numbers so we can always just deal
            // with s[0]
            int n = 0;
            try
            {
                n = Int32.Parse (s[0]) * 100; // Multiply by 100 for penny value
            }
            catch (Exception ex)
            {
                MessageBox.Show (ex.ToString (), "EXCEPTION");
                return;
            }
            // Make payout
            Payout.PayoutAmount (n, currency, textBox1);
        }

        private bool IsUnitValid(char unitType)
        {
            if (unitType == (char)0x06) // 0x06 is Payout, no other types supported by this program
                return true;
            return false;
        }
    }
}
