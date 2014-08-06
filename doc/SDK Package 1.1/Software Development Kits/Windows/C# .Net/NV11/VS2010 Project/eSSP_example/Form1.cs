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
        CNV11 NV11; // the class used to interface with the validator
        bool FormSetup = false;

        // Constructor
        public Form1()
        {
            InitializeComponent();
            timer1.Interval = pollTimer;
        }

        // This updates UI variables such as textboxes etc.
        void UpdateUI()
        {
            // update text boxes
            totalAcceptedNumText.Text = NV11.NotesAccepted.ToString();
            totalNumNotesDispensedText.Text = NV11.NotesDispensed.ToString();
            notesInStorageText.Text = NV11.GetStorageInfo();
        }

        // The main program loop, this is to control the validator, it polls at
        // a value set in this class (pollTimer).
        void MainLoop()
        {
            btnRun.Enabled = false;
            NV11.CommandStructure.ComPort = Global.ComPort;
            NV11.CommandStructure.SSPAddress = Global.SSPAddress;
            NV11.CommandStructure.Timeout = 1500;

            // connect to validator
            if (ConnectToValidator(reconnectionAttempts))
            {
                Running = true; 
                textBox1.AppendText("\r\nPoll Loop\r\n*********************************\r\n");
                btnHalt.Enabled = true;
            }

            while (Running)
            {
                // if the poll fails, try to reconnect
                if (NV11.DoPoll(textBox1) == false)
                {
                    textBox1.AppendText("Poll failed, attempting to reconnect...\r\n");
                    while (true)
                    {
                        // attempt reconnect, pass over number of reconnection attempts
                        if (ConnectToValidator(reconnectionAttempts) == true)
                            break; // if connection successful, break out and carry on
                        // if not successful, stop the execution of the poll loop
                        btnRun.Enabled = true;
                        btnHalt.Enabled = false;
                        NV11.SSPComms.CloseComPort(); // close com port before return
                        return;
                    }
                    textBox1.AppendText("Reconnected\r\n");
                }

                timer1.Enabled = true;
                // update form
                UpdateUI();
                // setup dynamic elements of win form once
                if (!FormSetup)
                {
                    SetupFormLayout();
                    FormSetup = true;
                }
                while (timer1.Enabled)
                {
                    Application.DoEvents();
                    Thread.Sleep(1); // Yield to free up CPU
                }
            }

            //close com port
            NV11.SSPComms.CloseComPort();

            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        // This is a one off function that is called the first time the MainLoop()
        // function runs, it just sets up a few of the UI elements that only need
        // updating once.
        private void SetupFormLayout()
        {
            // need validator class instance
            if (NV11 == null)
            {
                MessageBox.Show("NV11 class is null.", "ERROR");
                return;
            }

            // find number and value of channels and update combo box
            noteToRecycleComboBox.Items.Add("No Recycling");

            foreach (ChannelData d in NV11.UnitDataList)
            {
                string s = d.Value / 100 + " " + d.Currency[0] + d.Currency[1] + d.Currency[2];
                noteToRecycleComboBox.Items.Add(s);
            }
            
            // start on first choice which will always be no recycling
            noteToRecycleComboBox.Text = noteToRecycleComboBox.Items[0].ToString();
        }

        // This function opens the com port and attempts to connect with the validator. It then negotiates
        // the keys for encryption and performs some other setup commands.
        private bool ConnectToValidator(int attempts)
        {
            // setup timer
            System.Windows.Forms.Timer reconnectionTimer = new System.Windows.Forms.Timer();
            reconnectionTimer.Tick += new EventHandler(reconnectionTimer_Tick);
            reconnectionTimer.Interval = 3000; // ms

            // run for number of attempts specified
            for (int i = 0; i < attempts; i++)
            {
                // close com port in case it was open
                NV11.SSPComms.CloseComPort();

                // turn encryption off for first stage
                NV11.CommandStructure.EncryptionStatus = false;

                // if the key negotiation is successful then set the rest up
                if (NV11.NegotiateKeys(textBox1) == true)
                {
                    NV11.CommandStructure.EncryptionStatus = true; // now encrypting
                    // find the max protocol version this validator supports
                    byte maxPVersion = FindMaxProtocolVersion();
                    if (maxPVersion >= 6)
                    {
                        NV11.SetProtocolVersion(maxPVersion, textBox1);
                    }
                    else
                    {
                        MessageBox.Show("This program does not support validators under protocol 6!", "ERROR");
                        return false;
                    }
                    // get info from the validator and store useful vars
                    NV11.SetupRequest(textBox1);
                    // inhibits, this sets which channels can receive notes
                    NV11.SetInhibits(textBox1);
                    // enable, this allows the validator to operate
                    NV11.EnableValidator(textBox1);
                    // value reporting, set whether the validator reports channel or coin value in 
                    // subsequent requests
                    NV11.SetValueReportingType(false, textBox1);
                    // check for notes already in the float on startup
                    NV11.CheckForStoredNotes(textBox1);
                    // 
                    return true;
                }
                // reset timer
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
                NV11.SetProtocolVersion(b);
                if (NV11.CommandStructure.ResponseData[0] == CCommands.SSP_RESPONSE_CMD_FAIL)
                    return --b;
                b++;
                if (b > 20) return 0x06; // return lowest if p version runs too high
            }
        }
    }
}
