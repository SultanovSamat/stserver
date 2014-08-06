using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace eSSP_example
{
    public partial class frmPayoutByDenom : Form
    {
        CPayout Payout;
        TextBox Output;

        TextBox[] AmountToPayout;
        public frmPayoutByDenom(CPayout payout, TextBox output)
        {
            InitializeComponent();
            Payout = payout;
            Output = output;
        }

        private void frmPayoutByDenom_Shown(object sender, EventArgs e)
        {
            // Create textbox array based on number of channels
            AmountToPayout = new TextBox[Payout.NumberOfChannels];

            // Add description labels
            Label l = new Label();
            l.Size = new Size(80, 45);
            l.Location = new Point(100, 10);
            l.Text = "Number to\nPayout";
            Controls.Add(l);

            // Iterate through the channels
            for (int i = 0; i < Payout.NumberOfChannels; i++)
            {
                // Create an entry box and label for each denomination (channel)
                l = new Label();
                l.Size = new Size(80, 25);
                l.Location = new Point(10, 55 + i * 25);
                ChannelData d = Payout.UnitDataList[i];
                l.Text = d.Value/100 + " " + new String(d.Currency);
                Controls.Add(l);

                AmountToPayout[i] = new TextBox();
                AmountToPayout[i].Size = new Size(60, 25);
                AmountToPayout[i].Location = new Point(100, 55 + i * 25);
                Controls.Add(AmountToPayout[i]);
            }

            // Add button to payout
            Button b = new Button();
            b.Size = new Size(140, 25);
            Point p = AmountToPayout[Payout.NumberOfChannels - 1].Location;
            p.Y += 25;
            p.X -= 90;
            b.Location = p;
            b.Text = "Payout";
            b.Click += new EventHandler(PayoutBtn_Click);
            Controls.Add(b);
        }

        private void PayoutBtn_Click(object sender, EventArgs e)
        {
            bool payoutRequired = false;
            byte[] data = new byte[9 * Payout.NumberOfChannels]; // create to size of maximum possible
            byte length = 0;
            int dataIndex = 0;
            byte denomsToPayout = 0;
            // For each denomination
            for (int i = 0; i < Payout.NumberOfChannels; i++)
            {
                try
                {
                    // Check if there is input in the box
                    if (AmountToPayout[i].Text != "" && AmountToPayout[i].Text != "0")
                    {
                        // If textbox isn't blank then this denom is being paid out
                        denomsToPayout++;
                        length += 9; // 9 bytes per denom to payout (2 amount, 4 value, 3 currency)
                        payoutRequired = true; // need to do a payout as there is now > 0 denoms

                        // Number of this denomination to payout
                        UInt16 numToPayout = UInt16.Parse(AmountToPayout[i].Text);
                        byte[] b = CHelpers.ConvertIntToBytes(numToPayout);
                        data[dataIndex++] = b[0];
                        data[dataIndex++] = b[1];

                        // Value of this denomination
                        ChannelData d = Payout.UnitDataList[i];
                        b = CHelpers.ConvertIntToBytes(d.Value);
                        data[dataIndex++] = b[0];
                        data[dataIndex++] = b[1];
                        data[dataIndex++] = b[2];
                        data[dataIndex++] = b[3];

                        // Currency of this denomination
                        data[dataIndex++] = (Byte)d.Currency[0];
                        data[dataIndex++] = (Byte)d.Currency[1];
                        data[dataIndex++] = (Byte)d.Currency[2];
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show(ex.ToString());
                    payoutRequired = false; // don't payout on exception
                }
            }

            if (payoutRequired)
            {
                // Send payout command and shut this form
                Payout.PayoutByDenomination(denomsToPayout, data, length, Output);
                base.Dispose();
            }
        }
    }
}
