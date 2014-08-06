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
            // Create an instance of the validator info class
            Hopper = new CHopper();
            btnHalt.Enabled = false;

            // Load settings
            logTickBox.Checked = Properties.Settings.Default.Comms;

            // Position comms window
            Point p = Location;
            p.X += this.Width;
            Hopper.Comms.Location = p;
        }

        private void Form1_Shown(object sender, EventArgs e)
        {
            frmOpenMenu f = new frmOpenMenu(this);
            f.Show();
            this.Hide();
        }

        private void btnRun_Click(object sender, EventArgs e)
        {
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

        private void resetValidatorBtn_Click(object sender, EventArgs e)
        {
            if (Hopper != null)
                Hopper.Reset(textBox1);
            // Deliberately closing the port to force a reconnect
            Hopper.SSPComms.CloseComPort();
        }

        private void logTickBox_CheckedChanged(object sender, EventArgs e)
        {
            if (Hopper != null)
            {
                if (logTickBox.Checked)
                    Hopper.Comms.Show();
                else
                    Hopper.Comms.Hide();
            }
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            Running = false;
            Properties.Settings.Default.Comms = logTickBox.Checked;
            Properties.Settings.Default.ComPort = Global.ComPort;
            Properties.Settings.Default.Save();
        }

        private void emptyAllBtn_Click(object sender, EventArgs e)
        {
            if (Hopper != null)
                Hopper.EmptyDevice(textBox1);
        }

        private void recycleBox_CheckedChange(object sender, EventArgs e)
        {
            if (sender is CheckBox)
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
                    MessageBox.Show(ex.ToString(), "EXCEPTION");
                    return;
                }
            }
        }

        private void btnPayout_Click(object sender, EventArgs e)
        {
            CalculatePayout (tbAmountToPayout.Text, tbPayoutCurrency.Text.ToCharArray());
        }

        private void btnToggleCoinMech_Click(object sender, EventArgs e)
        {
            if (Hopper != null)
            {
                if (Hopper.CoinMechEnabled)
                {
                    if (Hopper.DisableCoinMech(textBox1))
                        btnToggleCoinMech.Text = "En&able Coin Mech";
                }
                else
                    if (Hopper.EnableCoinMech(textBox1))
                        btnToggleCoinMech.Text = "&Disable Coin Mech";
            }
        }

        private void btnSmartEmpty_Click(object sender, EventArgs e)
        {
            if (Hopper != null)
                Hopper.SmartEmpty(textBox1);
        }

        private void setLevelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            frmSetLevel levelForm = new frmSetLevel(Hopper);
            levelForm.Show();
        }

        private void setAllToZeroToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (Hopper != null)
            {
                for (int i = 1; i < Hopper.NumberOfChannels+1; i++)
                    Hopper.SetCoinLevelsByChannel(i, 0);
                Hopper.UpdateData();
            }
        }

        private void btnSetFloat_Click(object sender, EventArgs e)
        {
            try
            {
                double minPayoutTxt = double.Parse(tbMinPayout.Text) * 100;
                short minPayout = (Int16)minPayoutTxt;

                double floatAmountTxt = double.Parse(tbFloatAmount.Text) * 100;
                int floatAmount = (Int32)floatAmountTxt;

                if (Hopper != null)
                    Hopper.SetFloat(minPayout, floatAmount, tbFloatCurrency.Text.ToCharArray(), textBox1);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString(), "EXCEPTION");
                return;
            }
        }

        private void reconnectionTimer_Tick(object sender, EventArgs e)
        {
            reconnectionTimer.Enabled = false;
        }

        private void btnPayoutByDenom_Click(object sender, EventArgs e)
        {
            if (Running && ((payoutByDenomFrm == null) || (payoutByDenomFrm != null && !payoutByDenomFrm.Visible)))
            {
                payoutByDenomFrm = new frmPayoutByDenom(Hopper, textBox1);
                payoutByDenomFrm.Show();
            }
        }
    }
}
