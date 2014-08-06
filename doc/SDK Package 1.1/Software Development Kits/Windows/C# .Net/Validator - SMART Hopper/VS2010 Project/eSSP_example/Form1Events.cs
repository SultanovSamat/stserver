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
    public partial class Form1 : Form
    {
        /* Events handling section */

        private void Form1_Load(object sender, EventArgs e)
        {
            // Create instances of the validator classes
            Hopper = new CHopper();
            Validator = new CValidator();
            if (Hopper == null || Validator == null)
            {
                MessageBox.Show("Error with memory allocation, exiting", "ERROR");
                CHelpers.Shutdown = true;
                Application.Exit();
            }
            btnHalt.Enabled = true;

            // Load settings
            logTickBox.Checked = Properties.Settings.Default.Comms;

            // Position comms windows
            Point p = Location;
            p.Y += this.Height;
            Hopper.Comms.Location = p;
            p.X += Hopper.Comms.Width;
            Validator.CommsLog.Location = p;
        }

        private void Form1_Shown(object sender, EventArgs e)
        {
            frmOpenMenu f = new frmOpenMenu(this);
            f.Show();
            this.Hide();
        }

        private void btnRun_Click(object sender, EventArgs e)
        {
            textBox1.AppendText("Started poll loop\r\n");
            btnHalt.Enabled = true;
            btnRun.Enabled = false;
            MainLoop();
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Application.Exit();
        }

        private void settingsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Form formSettings = new frmSettings();
            formSettings.ShowDialog();
            btnHalt_Click(this, null);
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            timer1.Enabled = false;
        }

        private void btnHalt_Click(object sender, EventArgs e)
        {
            textBox1.AppendText("Poll loop stopped\r\n");
            hopperRunning = false;
            validatorRunning = false;
            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        private void logTickBox_CheckedChanged(object sender, EventArgs e)
        {
            if (logTickBox.Checked)
            {
                Hopper.Comms.Show();
                Validator.CommsLog.Show ();
            }
            else
            {
                Hopper.Comms.Hide();
                Validator.CommsLog.Hide ();
            }
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            CHelpers.Shutdown = true;
            hopperRunning = false;
            validatorRunning = false;
            Properties.Settings.Default.Comms = logTickBox.Checked;
            Properties.Settings.Default.ComPort = Global.ComPort;
            Properties.Settings.Default.SSP1 = Global.Validator1SSPAddress;
            Properties.Settings.Default.SSP2 = Global.Validator2SSPAddress;
            Properties.Settings.Default.Save();
        }

        private void btnPayout_Click(object sender, EventArgs e)
        {
            if (tbPayout.Text != "" && tbPayoutCurrency.Text != "")
                CalculatePayout (tbPayout.Text, tbPayoutCurrency.Text.ToCharArray());
        }

        private void btnEmptyHopper_Click(object sender, EventArgs e)
        {
            Hopper.EmptyDevice(textBox1);
        }

        private void btnSmartEmptyHopper_Click(object sender, EventArgs e)
        {
            Hopper.SmartEmpty(textBox1);
        }

        private void recycleBox_CheckedChange(object sender, EventArgs e)
        {
            CheckBox c = sender as CheckBox;
            try
            {
                if (c.Checked)
                    Hopper.RouteChannelToStorage(Int32.Parse(c.Name), textBox1);
                else
                    Hopper.RouteChannelToCashbox(Int32.Parse(c.Name), textBox1);
            }
            catch (Exception ex)
            {
                return;
            }
        }

        private void btnResetHopper_Click(object sender, EventArgs e)
        {
            Hopper.Reset(textBox1);
        }

        private void btnResetBasic_Click(object sender, EventArgs e)
        {
            Validator.Reset (textBox1);
        }

        private void setLevelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            frmSetLevel f = new frmSetLevel (Hopper);
            f.Show ();
        }

        private void setAllToZeroToolStripMenuItem_Click(object sender, EventArgs e)
        {
            for (int i = 1; i <= Hopper.NumberOfChannels; i++)
                Hopper.SetCoinLevelsByChannel (i, 0, textBox1);
            Hopper.UpdateData();
        }

        private void reconnectionTimer_Tick(object sender, EventArgs e)
        {
            if (sender is Timer)
            {
                Timer t = sender as Timer;
                t.Enabled = false;
            }
        }

        private void btnPayoutByDenom_Click(object sender, EventArgs e)
        {
            if (hopperRunning && (payoutByDenomFrm == null || (payoutByDenomFrm != null && !payoutByDenomFrm.Visible)))
            {
                payoutByDenomFrm = new frmPayoutByDenom(Hopper, textBox1);
                payoutByDenomFrm.Show();
            }
        }
    }
}
