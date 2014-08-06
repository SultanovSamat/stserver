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
        // Events handling section 

        private void Form1_Load(object sender, EventArgs e)
        {
            // create an instance of the validator info class
            Payout = new CPayout();
            btnHalt.Enabled = false;

            Global.ComPort = Properties.Settings.Default.ComPort;
            Global.SSPAddress = Properties.Settings.Default.SSPAddress;

            if (Properties.Settings.Default.CommWindow)
            {
                Payout.CommsLog.Show ();
                logTickBox.Checked = true;
            }
            else
                logTickBox.Checked = false;
        }

        private void Form1_Shown(object sender, EventArgs e)
        {
            // position the comms window
            Payout.CommsLog.Location = new Point (this.Location.X + this.Width, this.Location.Y);

            // hide this and show opening menu
            Hide();
            frmOpenMenu menu = new frmOpenMenu(this);
            menu.Show();
        }

        private void btnRun_Click(object sender, EventArgs e)
        {
            MainLoop();
        }

        private void settingsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Form formSettings = new frmSettings();
            formSettings.ShowDialog();
			Running = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            timer1.Enabled = false;
        }

        private void btnHalt_Click(object sender, EventArgs e)
        {
            textBox1.AppendText("Poll loop stopped\r\n");
            Running = false;
        }

        private void logTickBox_CheckedChanged(object sender, EventArgs e)
        {
            if (logTickBox.Checked)
                Payout.CommsLog.Show ();
            else
                Payout.CommsLog.Hide ();
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            Running = false;
            Thread.Sleep(100); // allows poll thread to stop running
            Properties.Settings.Default.CommWindow = logTickBox.Checked;
            Properties.Settings.Default.ComPort = Global.ComPort;
            Properties.Settings.Default.SSPAddress = Global.SSPAddress;
            Properties.Settings.Default.Save();
        }

        private void emptyStoredNotesToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (Payout != null)
                Payout.EmptyPayoutDevice (textBox1);
        }

        private void optionsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            frmSettings f = new frmSettings();
            f.Show();
        }

        private void btnPayout_Click(object sender, EventArgs e)
        {
            if (tbPayoutAmount.Text != "" && tbPayoutCurrency.Text != "")
                CalculatePayout (tbPayoutAmount.Text, tbPayoutCurrency.Text.ToCharArray());
        }

        private void recycleBox_CheckedChange(object sender, EventArgs e)
        {
            try
            {
                // Get the sending object as a checkbox
                CheckBox c = sender as CheckBox;

                // Get the data from the payout
                ChannelData d = new ChannelData();
                Payout.GetDataByChannel(Int32.Parse(c.Name), ref d);

                if (c.Checked)
                    Payout.ChangeNoteRoute(d.Value, d.Currency, false, textBox1);
                else
                    Payout.ChangeNoteRoute(d.Value, d.Currency, true, textBox1);
            }
            catch (Exception ex)
            {
                MessageBox.Show (ex.ToString (), "EXCEPTION");
                return;
            }
        }

        private void btnSetFloat_Click(object sender, EventArgs e)
        {
            try
            {
                double mp = double.Parse (tbMinPayout.Text) * 100;
                double fa = double.Parse (tbFloatAmount.Text) * 100;
                Payout.SetFloat ((Int32)mp, (Int32)fa, tbFloatCurrency.Text.ToCharArray(), textBox1);
            }
            catch (Exception ex)
            {
                MessageBox.Show (ex.ToString (), "EXCEPTION");
                return;
            }
        }

        private void btnSMARTEmpty_Click(object sender, EventArgs e)
        {
            Payout.SmartEmpty (textBox1);
        }

        private void btnEmpty_Click(object sender, EventArgs e)
        {
            Payout.EmptyPayoutDevice (textBox1);
        }

        private void btnReset_Click(object sender, EventArgs e)
        {
            Payout.Reset(textBox1);
            // Shut port to force reconnect
            Payout.SSPComms.CloseComPort();
            ClearCheckBoxes();
        }

        private void exitToolStripMenuItem1_Click(object sender, EventArgs e)
        {
            Application.Exit ();
        }

        private void reconnectionTimer_Tick(object sender, EventArgs e)
        {
            reconnectionTimer.Enabled = false;
        }

        private void btnPayoutByDenom_Click(object sender, EventArgs e)
        {
            if (Running && ((payoutByDenomFrm == null) || (payoutByDenomFrm != null && !payoutByDenomFrm.Visible)))
            {
                payoutByDenomFrm = new frmPayoutByDenom(Payout, textBox1);
                payoutByDenomFrm.Show();
            }
        }
    }
}
