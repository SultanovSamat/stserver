using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.IO;
using ITLlib;

namespace eSSP_example
{
    public partial class CCommsWindow : Form
    {
        // Variables
        int m_PacketCounter;
        bool m_bLogging;
        string m_LogText;
        StreamWriter m_SW;
        string m_FileName;

        // Variable access
        public string Log
        {
            get { return m_LogText; }
            set { m_LogText = value; }
        }

        // Constructor
        public CCommsWindow()
        {
            InitializeComponent();
            m_PacketCounter = 1;
            m_bLogging = false;
            // create persistent log
            try
            {
                // if the log dir doesn't exist, create it
                string logDir = "Logs\\" + DateTime.Now.ToLongDateString();
                if (!Directory.Exists(logDir))
                    Directory.CreateDirectory(logDir);
                // name this log as current time
                m_FileName = DateTime.Now.Hour.ToString() + "h" + DateTime.Now.Minute.ToString() + "m" + DateTime.Now.Second.ToString() + "s.txt";
                // create/open the file
                m_SW = File.AppendText(logDir + "\\" + m_FileName);
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.ToString(), "EXCEPTION");
                base.Dispose();
            }
        }

        // On loading, start logging and turn off the cross
        private void CommsWindow_Load(object sender, EventArgs e)
        {
            m_bLogging = true;
            this.ControlBox = false;
            
            m_SW.Write("\r\nStarted logging - " + DateTime.Now);
        }

        // On closing, stop logging
        private void CommsWindow_FormClosing(object sender, FormClosingEventArgs e)
        {
            m_bLogging = false;
            m_SW.Close();
        }

        // This function should be called in a loop, it monitors the SSP_COMMAND_INFO parameter
        // and writes the info to a text box in a readable format. If the failedCommand bool
        // is set true then it will not write a response.
        public void UpdateLog(SSP_COMMAND_INFO info, bool failedCommand = false)
        {
            if (m_bLogging)
            {
                string byteStr;
                byte len;
                // NON-ENCRPYTED
                // transmission
                m_LogText = "\r\nNo Encryption\r\nSent Packet #" + m_PacketCounter;
                len = info.PreEncryptedTransmit.PacketData[2];
                m_LogText += "\r\nLength: " + len.ToString();
                m_LogText += "\r\nSync: " + (info.PreEncryptedTransmit.PacketData[1] >> 7);
                m_LogText += "\r\nData: ";
                byteStr = BitConverter.ToString(info.PreEncryptedTransmit.PacketData, 3, len);
                m_LogText += FormatByteString(byteStr);
                m_LogText += "\r\n";

                // received
                if (!failedCommand)
                {
                    m_LogText += "\r\nReceived Packet #" + m_PacketCounter;
                    len = info.PreEncryptedRecieve.PacketData[2];
                    m_LogText += "\r\nLength: " + len.ToString();
                    m_LogText += "\r\nSync: " + (info.PreEncryptedRecieve.PacketData[1] >> 7);
                    m_LogText += "\r\nData: ";
                    byteStr = BitConverter.ToString(info.PreEncryptedRecieve.PacketData, 3, len);
                    m_LogText += FormatByteString(byteStr);
                    m_LogText += "\r\n";
                }
                else
                {
                    m_LogText += "\r\nNo response...";
                }

                if (checkBox1.Checked == true)
                {
                    // ENCRYPTED
                    // transmission
                    m_LogText += "\r\nEncryption\r\nSent Packet #" + m_PacketCounter;
                    len = info.Transmit.PacketData[2];
                    m_LogText += "\r\nLength: " + len.ToString();
                    m_LogText += "\r\nSync: " + (info.Transmit.PacketData[1] >> 7);
                    m_LogText += "\r\nData: ";
                    byteStr = BitConverter.ToString(info.Transmit.PacketData, 3, len);
                    m_LogText += FormatByteString(byteStr);
                    m_LogText += "\r\n";

                    // received
                    if (!failedCommand)
                    {
                        m_LogText += "\r\nReceived Packet #" + m_PacketCounter;
                        len = info.Receive.PacketData[2];
                        m_LogText += "\r\nLength: " + len.ToString();
                        m_LogText += "\r\nSync: " + (info.Receive.PacketData[1] >> 7);
                        m_LogText += "\r\nData: ";
                        byteStr = BitConverter.ToString(info.Receive.PacketData, 3, len);
                        m_LogText += FormatByteString(byteStr);
                        m_LogText += "\r\n";
                    }
                    else
                    {
                        m_LogText += "\r\nNo response...";
                    }
                }

                logWindowText.AppendText(m_LogText);
                AppendToLog(m_LogText);
                logWindowText.SelectionStart = logWindowText.TextLength;
                m_PacketCounter++;
            }
        }

        private string FormatByteString(string s)
        {
            string formatted = s;
            string[] sArr;
            sArr = formatted.Split('-');
            formatted = "";
            for (int i = 0; i < sArr.Length; i++)
            {
                formatted += sArr[i];
                formatted += " ";
            }
            return formatted;
        }

        public void PauseLogging() { m_bLogging = false; }
        public void ResumeLogging() { m_bLogging = true; }

        private void AppendToLog(string stringToAppend)
        {
            m_SW.Write(stringToAppend);
        }
    }
}
