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
            Payout = new CPayout();
            if (Hopper == null || Payout == null)
            {
                MessageBox.Show("Error with memory allocation, exiting", "ERROR");
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
            Payout.CommsLog.Location = p;
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
            hopperRunning = false;
            payoutRunning = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            timer1.Enabled = false;
        }

        private void timer2_Tick(object sender, EventArgs e)
        {
            timer2.Enabled = false;
        }

        private void btnHalt_Click(object sender, EventArgs e)
        {
            textBox1.AppendText("Poll loop stopped\r\n");
            hopperRunning = false;
            payoutRunning = false;
            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        private void logTickBox_CheckedChanged(object sender, EventArgs e)
        {
            if (logTickBox.Checked)
            {
                Hopper.Comms.Show();
                Payout.CommsLog.Show();
            }
            else
            {
                Hopper.Comms.Hide();
                Payout.CommsLog.Hide();
            }
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            hopperRunning = false;
            payoutRunning = false;
            CHelpers.Shutdown = true;
            LibraryHandler.ClosePort();
            Properties.Settings.Default.Comms = logTickBox.Checked;
            Properties.Settings.Default.ComPortVal1 = Global.ValidatorComPort;
            Properties.Settings.Default.SSP1 = Global.Validator1SSPAddress;
            Properties.Settings.Default.SSP2 = Global.Validator2SSPAddress;
            Properties.Settings.Default.Save();
        }

        private void btnPayout_Click(object sender, EventArgs e)
        {
            if (tbPayout.Text != "" && tbPayoutCurrency.Text != "")
                CalculatePayout(tbPayout.Text, tbPayoutCurrency.Text.ToCharArray());
        }

        private void btnEmptyHopper_Click(object sender, EventArgs e)
        {
            Hopper.EmptyDevice(textBox1);
        }

        private void btnSmartEmptyHopper_Click(object sender, EventArgs e)
        {
            Hopper.SmartEmpty(textBox1);
        }

        private void btnEmptySMARTPayout_Click(object sender, EventArgs e)
        {
            Payout.EmptyPayoutDevice(textBox1);
        }

        private void btnSMARTEmpty_Click(object sender, EventArgs e)
        {
            Payout.SmartEmpty(textBox1);
        }

        private void recycleBoxHopper_CheckedChange(object sender, EventArgs e)
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
                MessageBox.Show(ex.ToString());
                return;
            }
        }

        private void recycleBoxPayout_CheckedChange(object sender, EventArgs e)
        {
            CheckBox chkbox = sender as CheckBox;
            try
            {
                ChannelData d = new ChannelData();
                Payout.GetDataByChannel(Int32.Parse(chkbox.Name), ref d);

                if (chkbox.Checked)
                    Payout.ChangeNoteRoute(d.Value, d.Currency, false, textBox1);
                else
                    Payout.ChangeNoteRoute(d.Value, d.Currency, true, textBox1);

                // Ensure payout ability is enabled in the validator
                Payout.EnablePayout();
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
                return;
            }
        }

        private void btnResetHopper_Click(object sender, EventArgs e)
        {
            Hopper.Reset(textBox1);
            // Force reconnect by closing com port
            //CommsLibrary.CloseComPort();
        }

        private void btnResetPayout_Click(object sender, EventArgs e)
        {
            Payout.Reset(textBox1);
            // Force reconnect by closing com port
            //CommsLibrary.CloseComPort();
        }

        private void setLevelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Form f = new frmSetLevel (Hopper);
            f.Show ();
        }

        private void setAllToZeroToolStripMenuItem_Click(object sender, EventArgs e)
        {
            for (int i = 1; i < Hopper.NumberOfChannels+1; i++)
                Hopper.SetCoinLevelsByChannel(i, 0, textBox1);
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

        private void btnFloat_Click(object sender, EventArgs e)
        {
            try
            {
                // Validate
                if (tbFloatAmount.Text == "" || tbMinPayout.Text == "" || tbFloatCurrency.Text == "")
                    return;

                // Parse to a float
                float fFa = float.Parse(tbFloatAmount.Text);
                float fMp = float.Parse(tbMinPayout.Text);

                int fa = (Int32)(fFa * 100); // multiply by 100 for penny value
                // If payout selected
                if (cbFloatSelect.Text == "SMART Payout")
                {
                    int mp = (int)(fMp * 100); // multiply by 100 for penny value
                    Payout.SetFloat(mp, fa, tbFloatCurrency.Text.ToCharArray(), textBox1);
                }
                // Or if Hopper
                else if (cbFloatSelect.Text == "SMART Hopper")
                {
                    short mp = (short)(fMp * 100); // multiply by 100 for penny value
                    Hopper.SetFloat(mp, fa, tbFloatCurrency.Text.ToCharArray(), textBox1);
                }
                else
                    MessageBox.Show("Choose a device to float from!");
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString());
                return;
            }
        }

        private void btnPayoutByDenom_Click(object sender, EventArgs e)
        {
            if (hopperRunning && payoutRunning && ((payoutByDenomFrm == null) || (payoutByDenomFrm != null && !payoutByDenomFrm.Visible)))
            {
                payoutByDenomFrm = new frmPayoutByDenom(Payout, Hopper, textBox1);
                payoutByDenomFrm.Show();
            }
        }
    }
}
