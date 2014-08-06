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
        // Events handling section 
        private void Form1_Load(object sender, EventArgs e)
        {
            // create an instance of the validator info class
            NV11 = new CNV11();
            btnHalt.Enabled = false;

            if (Properties.Settings.Default.CommWindow)
            {
                NV11.CommsLog.Show();
                logTickBox.Checked = true;
            }
            else
                logTickBox.Checked = false;
        }

        private void Form1_Shown(object sender, EventArgs e)
        {
            // Position the comms window of the validator
            Point p = this.Location;
            p.X += this.Width;
            NV11.CommsLog.Location = p;
            // hide this and show opening menu
            Hide();
            frmOpenMenu menu = new frmOpenMenu(this);
            menu.Show();
        }

        private void btnRun_Click(object sender, EventArgs e)
        {
            MainLoop();
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            base.Dispose();
        }

        private void settingsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Running = false;
            Form formSettings = new frmSettings();
            formSettings.ShowDialog();
            textBox1.AppendText("Poll loop stopped\r\n");
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

        private void payoutBtn_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                // make sure payout is switched on
                NV11.EnablePayout();
                NV11.PayoutNextNote(textBox1);
            }
        }

        private void noteToRecycleComboBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                textBox1.AppendText("Changing note routing...\r\n");
                if (noteToRecycleComboBox.Text == "No Recycling")
                {
                    // switch all notes to stacking
                    NV11.RouteAllToStack(textBox1);
                }
                else
                {
                    // switch all notes to stacking first
                    NV11.RouteAllToStack();
                    // make sure payout is switched on
                    NV11.EnablePayout();
                    // switch selected note to payout
                    string s = noteToRecycleComboBox.Items[noteToRecycleComboBox.SelectedIndex].ToString();
                    string[] sArr = s.Split(' ');
                    try
                    {
                        NV11.ChangeNoteRoute(Int32.Parse(sArr[0]) * 100, sArr[1].ToCharArray(), false, textBox1);
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show(ex.ToString());
                        return;
                    }
                }
            }
        }

        private void emptyNoteFloatToolStripMenuItem1_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                // make sure payout is switched on
                NV11.EnablePayout();
                NV11.EmptyPayoutDevice(textBox1);
            }
        }

        private void cashboxBtn_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
                NV11.StackNextNote(textBox1);
        }

        private void resetValidatorBtn_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                NV11.Reset(textBox1);
                NV11.SSPComms.CloseComPort(); // close com port to force reconnect
            }
        }

        private void logTickBox_CheckedChanged(object sender, EventArgs e)
        {
            if (logTickBox.Checked)
                NV11.CommsLog.Show();
            else
                NV11.CommsLog.Hide();
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            Running = false;
            Properties.Settings.Default.CommWindow = logTickBox.Checked;
            Properties.Settings.Default.ComPort = Global.ComPort;
            Properties.Settings.Default.Save();
        }

        private void ResetTotalsText_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                NV11.NotesAccepted = 0;
                NV11.NotesDispensed = 0;
            }
        }

        private void testToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                // make sure payout is switched on
                NV11.EnablePayout();
                NV11.PayoutNextNote(textBox1);
            }
        }

        private void stackNextNoteToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                // make sure payout is switched on
                NV11.EnablePayout();
                NV11.StackNextNote(textBox1);
            }
        }

        private void emptyStoredNotesToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (NV11 != null)
            {
                // make sure payout is switched on
                NV11.EnablePayout();
                NV11.EmptyPayoutDevice();
            }
        }

        private void optionsToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Running = false;
            frmSettings f = new frmSettings();
            f.Show();
        }

        private void reconnectionTimer_Tick(object sender, EventArgs e)
        {
            if (sender is Timer)
            {
                Timer t = sender as Timer;
                t.Enabled = false;
            }
        }
    }
}
