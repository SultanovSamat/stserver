namespace eSSP_example
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.optionsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.settingsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.btnRun = new System.Windows.Forms.Button();
            this.btnHalt = new System.Windows.Forms.Button();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.logTickBox = new System.Windows.Forms.CheckBox();
            this.msLevel = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.setLevelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.setAllToZeroToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.tbPayout = new System.Windows.Forms.TextBox();
            this.btnPayout = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.btnResetBasic = new System.Windows.Forms.Button();
            this.btnEmptyHopper = new System.Windows.Forms.Button();
            this.btnSmartEmptyHopper = new System.Windows.Forms.Button();
            this.tbCoinLevels = new System.Windows.Forms.TextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.btnResetHopper = new System.Windows.Forms.Button();
            this.label2 = new System.Windows.Forms.Label();
            this.tbPayoutCurrency = new System.Windows.Forms.TextBox();
            this.btnPayoutByDenom = new System.Windows.Forms.Button();
            this.menuStrip1.SuspendLayout();
            this.msLevel.SuspendLayout();
            this.SuspendLayout();
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.optionsToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(0, 0);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size(679, 24);
            this.menuStrip1.TabIndex = 0;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(37, 20);
            this.fileToolStripMenuItem.Text = "&File";
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new System.Drawing.Size(92, 22);
            this.exitToolStripMenuItem.Text = "E&xit";
            this.exitToolStripMenuItem.Click += new System.EventHandler(this.exitToolStripMenuItem_Click);
            // 
            // optionsToolStripMenuItem
            // 
            this.optionsToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.settingsToolStripMenuItem});
            this.optionsToolStripMenuItem.Name = "optionsToolStripMenuItem";
            this.optionsToolStripMenuItem.Size = new System.Drawing.Size(61, 20);
            this.optionsToolStripMenuItem.Text = "&Options";
            // 
            // settingsToolStripMenuItem
            // 
            this.settingsToolStripMenuItem.Name = "settingsToolStripMenuItem";
            this.settingsToolStripMenuItem.Size = new System.Drawing.Size(116, 22);
            this.settingsToolStripMenuItem.Text = "&Settings";
            this.settingsToolStripMenuItem.Click += new System.EventHandler(this.settingsToolStripMenuItem_Click);
            // 
            // btnRun
            // 
            this.btnRun.Location = new System.Drawing.Point(12, 400);
            this.btnRun.Name = "btnRun";
            this.btnRun.Size = new System.Drawing.Size(75, 23);
            this.btnRun.TabIndex = 1;
            this.btnRun.Text = "&Run";
            this.btnRun.UseVisualStyleBackColor = true;
            this.btnRun.Click += new System.EventHandler(this.btnRun_Click);
            // 
            // btnHalt
            // 
            this.btnHalt.Location = new System.Drawing.Point(105, 400);
            this.btnHalt.Name = "btnHalt";
            this.btnHalt.Size = new System.Drawing.Size(75, 23);
            this.btnHalt.TabIndex = 2;
            this.btnHalt.Text = "&Halt";
            this.btnHalt.UseVisualStyleBackColor = true;
            this.btnHalt.Click += new System.EventHandler(this.btnHalt_Click);
            // 
            // timer1
            // 
            this.timer1.Interval = 250;
            this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
            // 
            // textBox1
            // 
            this.textBox1.BackColor = System.Drawing.SystemColors.ButtonHighlight;
            this.textBox1.Location = new System.Drawing.Point(12, 27);
            this.textBox1.Multiline = true;
            this.textBox1.Name = "textBox1";
            this.textBox1.ReadOnly = true;
            this.textBox1.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBox1.Size = new System.Drawing.Size(280, 367);
            this.textBox1.TabIndex = 4;
            // 
            // logTickBox
            // 
            this.logTickBox.AutoSize = true;
            this.logTickBox.Location = new System.Drawing.Point(212, 404);
            this.logTickBox.Name = "logTickBox";
            this.logTickBox.Size = new System.Drawing.Size(81, 17);
            this.logTickBox.TabIndex = 16;
            this.logTickBox.Text = "Comms Log";
            this.logTickBox.UseVisualStyleBackColor = true;
            this.logTickBox.CheckedChanged += new System.EventHandler(this.logTickBox_CheckedChanged);
            // 
            // msLevel
            // 
            this.msLevel.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.setLevelToolStripMenuItem,
            this.setAllToZeroToolStripMenuItem});
            this.msLevel.Name = "msLevel";
            this.msLevel.Size = new System.Drawing.Size(154, 70);
            // 
            // setLevelToolStripMenuItem
            // 
            this.setLevelToolStripMenuItem.Name = "setLevelToolStripMenuItem";
            this.setLevelToolStripMenuItem.Size = new System.Drawing.Size(153, 22);
            this.setLevelToolStripMenuItem.Text = "&Set Level";
            this.setLevelToolStripMenuItem.Click += new System.EventHandler(this.setLevelToolStripMenuItem_Click);
            // 
            // setAllToZeroToolStripMenuItem
            // 
            this.setAllToZeroToolStripMenuItem.Name = "setAllToZeroToolStripMenuItem";
            this.setAllToZeroToolStripMenuItem.Size = new System.Drawing.Size(153, 22);
            this.setAllToZeroToolStripMenuItem.Text = "Set All to Zero";
            this.setAllToZeroToolStripMenuItem.Click += new System.EventHandler(this.setAllToZeroToolStripMenuItem_Click);
            // 
            // tbPayout
            // 
            this.tbPayout.Location = new System.Drawing.Point(340, 283);
            this.tbPayout.Name = "tbPayout";
            this.tbPayout.Size = new System.Drawing.Size(100, 20);
            this.tbPayout.TabIndex = 17;
            // 
            // btnPayout
            // 
            this.btnPayout.Location = new System.Drawing.Point(340, 311);
            this.btnPayout.Name = "btnPayout";
            this.btnPayout.Size = new System.Drawing.Size(204, 23);
            this.btnPayout.TabIndex = 18;
            this.btnPayout.Text = "Payout";
            this.btnPayout.UseVisualStyleBackColor = true;
            this.btnPayout.Click += new System.EventHandler(this.btnPayout_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(340, 267);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(94, 13);
            this.label1.TabIndex = 19;
            this.label1.Text = "Amount to Payout:";
            // 
            // btnResetBasic
            // 
            this.btnResetBasic.Location = new System.Drawing.Point(338, 398);
            this.btnResetBasic.Name = "btnResetBasic";
            this.btnResetBasic.Size = new System.Drawing.Size(206, 23);
            this.btnResetBasic.TabIndex = 5;
            this.btnResetBasic.Text = "Reset Validator";
            this.btnResetBasic.UseVisualStyleBackColor = true;
            this.btnResetBasic.Click += new System.EventHandler(this.btnResetBasic_Click);
            // 
            // btnEmptyHopper
            // 
            this.btnEmptyHopper.Location = new System.Drawing.Point(340, 241);
            this.btnEmptyHopper.Name = "btnEmptyHopper";
            this.btnEmptyHopper.Size = new System.Drawing.Size(206, 23);
            this.btnEmptyHopper.TabIndex = 0;
            this.btnEmptyHopper.Text = "Empty Hopper";
            this.btnEmptyHopper.UseVisualStyleBackColor = true;
            // 
            // btnSmartEmptyHopper
            // 
            this.btnSmartEmptyHopper.Location = new System.Drawing.Point(340, 212);
            this.btnSmartEmptyHopper.Name = "btnSmartEmptyHopper";
            this.btnSmartEmptyHopper.Size = new System.Drawing.Size(206, 23);
            this.btnSmartEmptyHopper.TabIndex = 1;
            this.btnSmartEmptyHopper.Text = "SMART Empty Hopper";
            this.btnSmartEmptyHopper.UseVisualStyleBackColor = true;
            this.btnSmartEmptyHopper.Click += new System.EventHandler(this.btnSmartEmptyHopper_Click);
            // 
            // tbCoinLevels
            // 
            this.tbCoinLevels.BackColor = System.Drawing.SystemColors.ButtonHighlight;
            this.tbCoinLevels.ContextMenuStrip = this.msLevel;
            this.tbCoinLevels.Location = new System.Drawing.Point(344, 77);
            this.tbCoinLevels.Multiline = true;
            this.tbCoinLevels.Name = "tbCoinLevels";
            this.tbCoinLevels.ReadOnly = true;
            this.tbCoinLevels.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.tbCoinLevels.Size = new System.Drawing.Size(202, 116);
            this.tbCoinLevels.TabIndex = 2;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(340, 47);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(46, 26);
            this.label6.TabIndex = 3;
            this.label6.Text = "Channel\r\nLevels:";
            // 
            // btnResetHopper
            // 
            this.btnResetHopper.Location = new System.Drawing.Point(338, 368);
            this.btnResetHopper.Name = "btnResetHopper";
            this.btnResetHopper.Size = new System.Drawing.Size(206, 23);
            this.btnResetHopper.TabIndex = 4;
            this.btnResetHopper.Text = "Reset Hopper";
            this.btnResetHopper.UseVisualStyleBackColor = true;
            this.btnResetHopper.Click += new System.EventHandler(this.btnResetHopper_Click);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(443, 267);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(88, 13);
            this.label2.TabIndex = 20;
            this.label2.Text = "Payout Currency:";
            // 
            // tbPayoutCurrency
            // 
            this.tbPayoutCurrency.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.tbPayoutCurrency.Location = new System.Drawing.Point(446, 283);
            this.tbPayoutCurrency.Name = "tbPayoutCurrency";
            this.tbPayoutCurrency.Size = new System.Drawing.Size(100, 20);
            this.tbPayoutCurrency.TabIndex = 21;
            // 
            // btnPayoutByDenom
            // 
            this.btnPayoutByDenom.Location = new System.Drawing.Point(338, 340);
            this.btnPayoutByDenom.Margin = new System.Windows.Forms.Padding(2, 2, 2, 2);
            this.btnPayoutByDenom.Name = "btnPayoutByDenom";
            this.btnPayoutByDenom.Size = new System.Drawing.Size(206, 23);
            this.btnPayoutByDenom.TabIndex = 22;
            this.btnPayoutByDenom.Text = "Payout by Denomination";
            this.btnPayoutByDenom.UseVisualStyleBackColor = true;
            this.btnPayoutByDenom.Click += new System.EventHandler(this.btnPayoutByDenom_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(679, 435);
            this.Controls.Add(this.btnPayoutByDenom);
            this.Controls.Add(this.logTickBox);
            this.Controls.Add(this.tbPayoutCurrency);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.btnResetBasic);
            this.Controls.Add(this.tbCoinLevels);
            this.Controls.Add(this.btnResetHopper);
            this.Controls.Add(this.btnHalt);
            this.Controls.Add(this.btnSmartEmptyHopper);
            this.Controls.Add(this.btnEmptyHopper);
            this.Controls.Add(this.btnPayout);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.btnRun);
            this.Controls.Add(this.tbPayout);
            this.Controls.Add(this.menuStrip1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Form1";
            this.StartPosition = System.Windows.Forms.FormStartPosition.Manual;
            this.Text = "SMART Hopper and Validator Example Software";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.Shown += new System.EventHandler(this.Form1_Shown);
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.msLevel.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem optionsToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem settingsToolStripMenuItem;
        private System.Windows.Forms.Button btnRun;
        private System.Windows.Forms.Button btnHalt;
        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.CheckBox logTickBox;
        private System.Windows.Forms.ContextMenuStrip msLevel;
        private System.Windows.Forms.ToolStripMenuItem setLevelToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem setAllToZeroToolStripMenuItem;
        private System.Windows.Forms.TextBox tbPayout;
        private System.Windows.Forms.Button btnPayout;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button btnResetBasic;
        private System.Windows.Forms.Button btnResetHopper;
        private System.Windows.Forms.Button btnEmptyHopper;
        private System.Windows.Forms.Button btnSmartEmptyHopper;
        private System.Windows.Forms.TextBox tbCoinLevels;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox tbPayoutCurrency;
        private System.Windows.Forms.Button btnPayoutByDenom;

    }
}

