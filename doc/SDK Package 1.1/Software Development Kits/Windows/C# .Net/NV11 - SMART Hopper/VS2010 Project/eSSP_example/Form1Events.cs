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
            NV11 = new CNV11();
            if (Hopper == null || NV11 == null)
            {
                MessageBox.Show("Error with memory allocation, exiting", "ERROR");
                Application.Exit();
            }

            // Load settings
            logTickBox.Checked = Properties.Settings.Default.Comms;

            // Position comms windows
            Point p = Location;
            p.Y += this.Height;
            Hopper.Comms.Location = p;
            p.X += Hopper.Comms.Width;
            NV11.CommsLog.Location = p;
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
            CHelpers.Shutdown = true;
            Application.Exit();
        }

        private void settingsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Form formSettings = new frmSettings();
            formSettings.ShowDialog();
            hopperRunning = false;
            NV11Running = false;
        }

        private void timer1_Tick(object sender, EventArgs e)
        {
            timer1.Enabled = false;
        }

        private void btnHalt_Click(object sender, EventArgs e)
        {
            textBox1.AppendText("Poll loop stopped\r\n");
            hopperRunning = false;
            NV11Running = false;
            btnRun.Enabled = true;
            btnHalt.Enabled = false;
        }

        private void resetValidatorBtn_Click(object sender, EventArgs e)
        {
            if (Hopper != null)
                Hopper.Reset(textBox1);
        }

        private void logTickBox_CheckedChanged(object sender, EventArgs e)
        {
            if (logTickBox.Checked)
            {
                Hopper.Comms.Show();
                NV11.CommsLog.Show();
            }
            else
            {
                Hopper.Comms.Hide();
                NV11.CommsLog.Hide();
            }
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            NV11Running = false;
            hopperRunning = false;
            CHelpers.Shutdown = true;
            Properties.Settings.Default.Comms = logTickBox.Checked;
            Properties.Settings.Default.ComPort = Global.ComPort;
            Properties.Settings.Default.SSP1 = Global.Validator1SSPAddress;
            Properties.Settings.Default.SSP2 = Global.Validator2SSPAddress;
            Properties.Settings.Default.Save();
        }

        private void btnPayout_Click(object sender, EventArgs e)
        {
            if (tbPayout.Text != "" && tbPayoutCurrency.TextLength == 3)
                CalculatePayout(tbPayout.Text, tbPayoutCurrency.Text);
        }

        private void cbRecycleChannelNV11_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (cbRecycleChannelNV11.Text == "No recycling")
            {
                // switch all notes to stacking by disabling the payout
                NV11.DisablePayout();
            }
            else
            {
                // route all to stack to begin with
                NV11.RouteAllToStack();

                // switch selected note to recycle after enabling payout
                NV11.EnablePayout();
                string name = cbRecycleChannelNV11.Items[cbRecycleChannelNV11.SelectedIndex].ToString();
                string[] sArr = name.Split(' ');
                try
                {
                    NV11.ChangeNoteRoute(Int32.Parse(sArr[0]) * 100, sArr[1].ToCharArray(), false, textBox1);
                }
                catch(Exception ex)
                {
                    MessageBox.Show(ex.ToString());
                    return;
                }
            }
        }

        private void btnStackNextNote_Click(object sender, EventArgs e)
        {
            NV11.StackNextNote(textBox1);
        }

        private void btnPayoutNextNote_Click(object sender, EventArgs e)
        {
            NV11.PayoutNextNote(textBox1);
        }

        private void btnNoteFloatStackAll_Click(object sender, EventArgs e)
        {
            NV11.EmptyPayoutDevice(textBox1);
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
            CheckBox c;
            if (sender is CheckBox)
            {
                c = sender as CheckBox;
                try
                {
                    int n = Int32.Parse(c.Name);
                    if (c.Checked)
                        Hopper.RouteChannelToStorage(n, textBox1);
                    else
                        Hopper.RouteChannelToCashbox(n, textBox1);
                }
                catch (Exception ex)
                {
                    return;
                }
            }
        }

        private void btnResetHopper_Click(object sender, EventArgs e)
        {
            Hopper.Reset(textBox1);
        }

        private void btnResetNoteFloat_Click(object sender, EventArgs e)
        {
            NV11.Reset(textBox1);
        }

        private void setLevelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Form f = new frmSetLevel (Hopper);
            f.Show ();
        }

        private void setAllToZeroToolStripMenuItem_Click(object sender, EventArgs e)
        {
            for (int i = 0; i < Hopper.NumberOfChannels; i++)
                Hopper.SetCoinLevelsByChannel (i, 0, textBox1);
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
