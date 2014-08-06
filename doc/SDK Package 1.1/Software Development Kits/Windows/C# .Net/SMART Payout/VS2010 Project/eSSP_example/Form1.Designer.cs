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
            this.btnRun = new System.Windows.Forms.Button();
            this.btnHalt = new System.Windows.Forms.Button();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.StorageListBoxMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.emptyStoredNotesToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.logTickBox = new System.Windows.Forms.CheckBox();
            this.btnPayout = new System.Windows.Forms.Button();
            this.tbPayoutAmount = new System.Windows.Forms.TextBox();
            this.tbLevelInfo = new System.Windows.Forms.TextBox();
            this.tbMinPayout = new System.Windows.Forms.TextBox();
            this.tbFloatAmount = new System.Windows.Forms.TextBox();
            this.btnSetFloat = new System.Windows.Forms.Button();
            this.btnReset = new System.Windows.Forms.Button();
            this.btnEmpty = new System.Windows.Forms.Button();
            this.btnSMARTEmpty = new System.Windows.Forms.Button();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.menuStrip1 = new System.Windows.Forms.MenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.exitToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.settingsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.tbPayoutCurrency = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.tbFloatCurrency = new System.Windows.Forms.TextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.btnPayoutByDenom = new System.Windows.Forms.Button();
            this.StorageListBoxMenu.SuspendLayout();
            this.menuStrip1.SuspendLayout();
            this.SuspendLayout();
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
            this.textBox1.ContextMenuStrip = this.StorageListBoxMenu;
            this.textBox1.Location = new System.Drawing.Point(12, 27);
            this.textBox1.Multiline = true;
            this.textBox1.Name = "textBox1";
            this.textBox1.ReadOnly = true;
            this.textBox1.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBox1.Size = new System.Drawing.Size(280, 367);
            this.textBox1.TabIndex = 4;
            // 
            // StorageListBoxMenu
            // 
            this.StorageListBoxMenu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.emptyStoredNotesToolStripMenuItem});
            this.StorageListBoxMenu.Name = "contextMenuStrip1";
            this.StorageListBoxMenu.Size = new System.Drawing.Size(199, 28);
            // 
            // emptyStoredNotesToolStripMenuItem
            // 
            this.emptyStoredNotesToolStripMenuItem.Name = "emptyStoredNotesToolStripMenuItem";
            this.emptyStoredNotesToolStripMenuItem.Size = new System.Drawing.Size(198, 24);
            this.emptyStoredNotesToolStripMenuItem.Text = "Empty to Cashbox";
            this.emptyStoredNotesToolStripMenuItem.Click += new System.EventHandler(this.emptyStoredNotesToolStripMenuItem_Click);
            // 
            // logTickBox
            // 
            this.logTickBox.AutoSize = true;
            this.logTickBox.Checked = true;
            this.logTickBox.CheckState = System.Windows.Forms.CheckState.Checked;
            this.logTickBox.Location = new System.Drawing.Point(212, 404);
            this.logTickBox.Name = "logTickBox";
            this.logTickBox.Size = new System.Drawing.Size(81, 17);
            this.logTickBox.TabIndex = 16;
            this.logTickBox.Text = "Comms Log";
            this.logTickBox.UseVisualStyleBackColor = true;
            this.logTickBox.CheckedChanged += new System.EventHandler(this.logTickBox_CheckedChanged);
            // 
            // btnPayout
            // 
            this.btnPayout.Location = new System.Drawing.Point(496, 284);
            this.btnPayout.Name = "btnPayout";
            this.btnPayout.Size = new System.Drawing.Size(78, 23);
            this.btnPayout.TabIndex = 17;
            this.btnPayout.Text = "Payout";
            this.btnPayout.UseVisualStyleBackColor = true;
            this.btnPayout.Click += new System.EventHandler(this.btnPayout_Click);
            // 
            // tbPayoutAmount
            // 
            this.tbPayoutAmount.Location = new System.Drawing.Point(319, 287);
            this.tbPayoutAmount.Name = "tbPayoutAmount";
            this.tbPayoutAmount.Size = new System.Drawing.Size(80, 20);
            this.tbPayoutAmount.TabIndex = 18;
            this.tbPayoutAmount.TextAlign = System.Windows.Forms.HorizontalAlignment.Center;
            // 
            // tbLevelInfo
            // 
            this.tbLevelInfo.BackColor = System.Drawing.SystemColors.ButtonHighlight;
            this.tbLevelInfo.Location = new System.Drawing.Point(317, 151);
            this.tbLevelInfo.Multiline = true;
            this.tbLevelInfo.Name = "tbLevelInfo";
            this.tbLevelInfo.ReadOnly = true;
            this.tbLevelInfo.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.tbLevelInfo.Size = new System.Drawing.Size(185, 100);
            this.tbLevelInfo.TabIndex = 19;
            // 
            // tbMinPayout
            // 
            this.tbMinPayout.Location = new System.Drawing.Point(372, 34);
            this.tbMinPayout.Name = "tbMinPayout";
            this.tbMinPayout.Size = new System.Drawing.Size(130, 20);
            this.tbMinPayout.TabIndex = 26;
            // 
            // tbFloatAmount
            // 
            this.tbFloatAmount.Location = new System.Drawing.Point(372, 60);
            this.tbFloatAmount.Name = "tbFloatAmount";
            this.tbFloatAmount.Size = new System.Drawing.Size(130, 20);
            this.tbFloatAmount.TabIndex = 27;
            // 
            // btnSetFloat
            // 
            this.btnSetFloat.Location = new System.Drawing.Point(319, 108);
            this.btnSetFloat.Name = "btnSetFloat";
            this.btnSetFloat.Size = new System.Drawing.Size(182, 23);
            this.btnSetFloat.TabIndex = 28;
            this.btnSetFloat.Text = "Set Float";
            this.btnSetFloat.UseVisualStyleBackColor = true;
            this.btnSetFloat.Click += new System.EventHandler(this.btnSetFloat_Click);
            // 
            // btnReset
            // 
            this.btnReset.Location = new System.Drawing.Point(319, 400);
            this.btnReset.Name = "btnReset";
            this.btnReset.Size = new System.Drawing.Size(172, 23);
            this.btnReset.TabIndex = 29;
            this.btnReset.Text = "Reset";
            this.btnReset.UseVisualStyleBackColor = true;
            this.btnReset.Click += new System.EventHandler(this.btnReset_Click);
            // 
            // btnEmpty
            // 
            this.btnEmpty.Location = new System.Drawing.Point(319, 342);
            this.btnEmpty.Name = "btnEmpty";
            this.btnEmpty.Size = new System.Drawing.Size(172, 23);
            this.btnEmpty.TabIndex = 30;
            this.btnEmpty.Text = "Empty to Cashbox";
            this.btnEmpty.UseVisualStyleBackColor = true;
            this.btnEmpty.Click += new System.EventHandler(this.btnEmpty_Click);
            // 
            // btnSMARTEmpty
            // 
            this.btnSMARTEmpty.Location = new System.Drawing.Point(319, 370);
            this.btnSMARTEmpty.Name = "btnSMARTEmpty";
            this.btnSMARTEmpty.Size = new System.Drawing.Size(172, 23);
            this.btnSMARTEmpty.TabIndex = 31;
            this.btnSMARTEmpty.Text = "SMART Empty";
            this.btnSMARTEmpty.UseVisualStyleBackColor = true;
            this.btnSMARTEmpty.Click += new System.EventHandler(this.btnSMARTEmpty_Click);
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(303, 37);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(63, 13);
            this.label4.TabIndex = 32;
            this.label4.Text = "Min Payout:";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(298, 63);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(72, 13);
            this.label5.TabIndex = 33;
            this.label5.Text = "Float Amount:";
            // 
            // menuStrip1
            // 
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.settingsToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(0, 0);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Size = new System.Drawing.Size(750, 24);
            this.menuStrip1.TabIndex = 34;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.exitToolStripMenuItem1});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(37, 20);
            this.fileToolStripMenuItem.Text = "&File";
            // 
            // exitToolStripMenuItem1
            // 
            this.exitToolStripMenuItem1.Name = "exitToolStripMenuItem1";
            this.exitToolStripMenuItem1.Size = new System.Drawing.Size(92, 22);
            this.exitToolStripMenuItem1.Text = "Exit";
            this.exitToolStripMenuItem1.Click += new System.EventHandler(this.exitToolStripMenuItem1_Click);
            // 
            // settingsToolStripMenuItem
            // 
            this.settingsToolStripMenuItem.Name = "settingsToolStripMenuItem";
            this.settingsToolStripMenuItem.Size = new System.Drawing.Size(61, 20);
            this.settingsToolStripMenuItem.Text = "&Settings";
            this.settingsToolStripMenuItem.Click += new System.EventHandler(this.settingsToolStripMenuItem_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(316, 134);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(57, 13);
            this.label1.TabIndex = 35;
            this.label1.Text = "Level Info:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(316, 270);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(82, 13);
            this.label2.TabIndex = 36;
            this.label2.Text = "Payout Amount:";
            // 
            // tbPayoutCurrency
            // 
            this.tbPayoutCurrency.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.tbPayoutCurrency.Location = new System.Drawing.Point(406, 287);
            this.tbPayoutCurrency.Name = "tbPayoutCurrency";
            this.tbPayoutCurrency.Size = new System.Drawing.Size(86, 20);
            this.tbPayoutCurrency.TabIndex = 37;
            this.tbPayoutCurrency.Text = "EUR";
            this.tbPayoutCurrency.TextAlign = System.Windows.Forms.HorizontalAlignment.Center;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(404, 270);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(88, 13);
            this.label3.TabIndex = 38;
            this.label3.Text = "Payout Currency:";
            // 
            // tbFloatCurrency
            // 
            this.tbFloatCurrency.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.tbFloatCurrency.Location = new System.Drawing.Point(372, 84);
            this.tbFloatCurrency.Name = "tbFloatCurrency";
            this.tbFloatCurrency.Size = new System.Drawing.Size(130, 20);
            this.tbFloatCurrency.TabIndex = 39;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(295, 87);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(78, 13);
            this.label6.TabIndex = 40;
            this.label6.Text = "Float Currency:";
            // 
            // btnPayoutByDenom
            // 
            this.btnPayoutByDenom.Location = new System.Drawing.Point(319, 313);
            this.btnPayoutByDenom.Name = "btnPayoutByDenom";
            this.btnPayoutByDenom.Size = new System.Drawing.Size(172, 23);
            this.btnPayoutByDenom.TabIndex = 41;
            this.btnPayoutByDenom.Text = "Payout by Denomination";
            this.btnPayoutByDenom.UseVisualStyleBackColor = true;
            this.btnPayoutByDenom.Click += new System.EventHandler(this.btnPayoutByDenom_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(750, 435);
            this.ContextMenuStrip = this.StorageListBoxMenu;
            this.Controls.Add(this.btnPayoutByDenom);
            this.Controls.Add(this.tbFloatCurrency);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.tbPayoutCurrency);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.menuStrip1);
            this.Controls.Add(this.btnReset);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.logTickBox);
            this.Controls.Add(this.btnSMARTEmpty);
            this.Controls.Add(this.btnEmpty);
            this.Controls.Add(this.btnSetFloat);
            this.Controls.Add(this.tbFloatAmount);
            this.Controls.Add(this.tbPayoutAmount);
            this.Controls.Add(this.tbMinPayout);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.btnPayout);
            this.Controls.Add(this.btnHalt);
            this.Controls.Add(this.btnRun);
            this.Controls.Add(this.tbLevelInfo);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Form1";
            this.Text = "SMART Payout eSSP C# example";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.Shown += new System.EventHandler(this.Form1_Shown);
            this.StorageListBoxMenu.ResumeLayout(false);
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnRun;
        private System.Windows.Forms.Button btnHalt;
        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.CheckBox logTickBox;
        private System.Windows.Forms.ContextMenuStrip StorageListBoxMenu;
        private System.Windows.Forms.ToolStripMenuItem emptyStoredNotesToolStripMenuItem;
        private System.Windows.Forms.Button btnPayout;
        private System.Windows.Forms.TextBox tbPayoutAmount;
        private System.Windows.Forms.TextBox tbLevelInfo;
        private System.Windows.Forms.TextBox tbMinPayout;
        private System.Windows.Forms.TextBox tbFloatAmount;
        private System.Windows.Forms.Button btnSetFloat;
        private System.Windows.Forms.Button btnReset;
        private System.Windows.Forms.Button btnEmpty;
        private System.Windows.Forms.Button btnSMARTEmpty;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem settingsToolStripMenuItem;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox tbPayoutCurrency;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox tbFloatCurrency;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Button btnPayoutByDenom;

    }
}

