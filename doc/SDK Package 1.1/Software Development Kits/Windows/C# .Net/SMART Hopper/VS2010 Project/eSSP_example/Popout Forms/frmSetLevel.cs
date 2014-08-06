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
    public partial class frmSetLevel : Form
    {
        CHopper m_Hopper;
        public frmSetLevel(CHopper hop)
        {
            InitializeComponent();
            m_Hopper = hop;
        }

        private void frmSetLevel_Load(object sender, EventArgs e)
        {
            for (int i = 0; i < m_Hopper.NumberOfChannels; i++)
            {
                string s = (m_Hopper.UnitDataList[i].Value*0.01).ToString();
                s += " ";
                s += m_Hopper.UnitDataList[i].Currency[0];
                s += m_Hopper.UnitDataList[i].Currency[1];
                s += m_Hopper.UnitDataList[i].Currency[2];
                cbChannels.Items.Add(s);
            }
        }

        private void btnDone_Click(object sender, EventArgs e)
        {
            try
            {
                // Ignore blank box
                if (tbValue.Text != "")
                {
                    // Split item into value and currency
                    string s = cbChannels.Items[cbChannels.SelectedIndex].ToString();
                    string[] ss = s.Split(' ');
                    int n = (int)(float.Parse(ss[0]) * 100f);
                    // first set the level to 0, otherwise it just adds to level 
                    m_Hopper.SetCoinLevelsByCoin(n, ss[1].ToCharArray(), 0);
                    // now set to value
                    m_Hopper.SetCoinLevelsByCoin(n, ss[1].ToCharArray(), Int16.Parse(tbValue.Text));
                }
                else
                    MessageBox.Show("No input, channel values unaltered", "WARNING");
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString(), "EXCEPTION");
                return;
            }
            m_Hopper.UpdateData();
            this.Close();
        }
    }
}
