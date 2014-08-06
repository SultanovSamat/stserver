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
            this.emptyNoteFloatToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.optionsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.settingsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.btnRun = new System.Windows.Forms.Button();
            this.btnHalt = new System.Windows.Forms.Button();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.textBox1 = new System.Windows.Forms.TextBox();
            this.resetValidatorBtn = new System.Windows.Forms.Button();
            this.logTickBox = new System.Windows.Forms.CheckBox();
            this.emptyAllBtn = new System.Windows.Forms.Button();
            this.tbAmountToPayout = new System.Windows.Forms.TextBox();
            this.btnPayout = new System.Windows.Forms.Button();
            this.btnToggleCoinMech = new System.Windows.Forms.Button();
            this.btnSmartEmpty = new System.Windows.Forms.Button();
            this.textBox2 = new System.Windows.Forms.TextBox();
            this.msLevel = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.setLevelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.setAllToZeroToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.label2 = new System.Windows.Forms.Label();
            this.tbFloatAmount = new System.Windows.Forms.TextBox();
            this.btnSetFloat = new System.Windows.Forms.Button();
            this.tbMinPayout = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.tbFloatCurrency = new System.Windows.Forms.TextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.tbPayoutCurrency = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
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
            this.menuStrip1.Size = new System.Drawing.Size(656, 24);
            this.menuStrip1.TabIndex = 0;
            this.menuStrip1.Text = "menuStrip1";
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.emptyNoteFloatToolStripMenuItem1,
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(37, 20);
            this.fileToolStripMenuItem.Text = "&File";
            // 
            // emptyNoteFloatToolStripMenuItem1
            // 
            this.emptyNoteFloatToolStripMenuItem1.Name = "emptyNoteFloatToolStripMenuItem1";
            this.emptyNoteFloatToolStripMenuItem1.Size = new System.Drawing.Size(92, 22);
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
            // resetValidatorBtn
            // 
            this.resetValidatorBtn.Location = new System.Drawing.Point(306, 417);
            this.resetValidatorBtn.Name = "resetValidatorBtn";
            this.resetValidatorBtn.Size = new System.Drawing.Size(114, 23);
            this.resetValidatorBtn.TabIndex = 15;
            this.resetValidatorBtn.Text = "Reset Hopper";
            this.resetValidatorBtn.UseVisualStyleBackColor = true;
            this.resetValidatorBtn.Click += new System.EventHandler(this.resetValidatorBtn_Click);
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
            // emptyAllBtn
            // 
            this.emptyAllBtn.Location = new System.Drawing.Point(306, 341);
            this.emptyAllBtn.Name = "emptyAllBtn";
            this.emptyAllBtn.Size = new System.Drawing.Size(114, 23);
            this.emptyAllBtn.TabIndex = 17;
            this.emptyAllBtn.Text = "E&mpty to Cashbox";
            this.emptyAllBtn.UseVisualStyleBackColor = true;
            this.emptyAllBtn.Click += new System.EventHandler(this.emptyAllBtn_Click);
            // 
            // tbAmountToPayout
            // 
            this.tbAmountToPayout.Location = new System.Drawing.Point(438, 358);
            this.tbAmountToPayout.Name = "tbAmountToPayout";
            this.tbAmountToPayout.Size = new System.Drawing.Size(105, 20);
            this.tbAmountToPayout.TabIndex = 19;
            // 
            // btnPayout
            // 
            this.btnPayout.Location = new System.Drawing.Point(438, 419);
            this.btnPayout.Name = "btnPayout";
            this.btnPayout.Size = new System.Drawing.Size(105, 23);
            this.btnPayout.TabIndex = 20;
            this.btnPayout.Text = "&Payout";
            this.btnPayout.UseVisualStyleBackColor = true;
            this.btnPayout.Click += new System.EventHandler(this.btnPayout_Click);
            // 
            // btnToggleCoinMech
            // 
            this.btnToggleCoinMech.Location = new System.Drawing.Point(306, 312);
            this.btnToggleCoinMech.Name = "btnToggleCoinMech";
            this.btnToggleCoinMech.Size = new System.Drawing.Size(114, 23);
            this.btnToggleCoinMech.TabIndex = 21;
            this.btnToggleCoinMech.Text = "&Disable Coin Mech";
            this.btnToggleCoinMech.UseVisualStyleBackColor = true;
            this.btnToggleCoinMech.Click += new System.EventHandler(this.btnToggleCoinMech_Click);
            // 
            // btnSmartEmpty
            // 
            this.btnSmartEmpty.Location = new System.Drawing.Point(306, 371);
            this.btnSmartEmpty.Name = "btnSmartEmpty";
            this.btnSmartEmpty.Size = new System.Drawing.Size(114, 40);
            this.btnSmartEmpty.TabIndex = 22;
            this.btnSmartEmpty.Text = "&SMART Empty to Cashbox";
            this.btnSmartEmpty.UseVisualStyleBackColor = true;
            this.btnSmartEmpty.Click += new System.EventHandler(this.btnSmartEmpty_Click);
            // 
            // textBox2
            // 
            this.textBox2.BackColor = System.Drawing.SystemColors.ButtonHighlight;
            this.textBox2.ContextMenuStrip = this.msLevel;
            this.textBox2.Location = new System.Drawing.Point(304, 47);
            this.textBox2.Multiline = true;
            this.textBox2.Name = "textBox2";
            this.textBox2.ReadOnly = true;
            this.textBox2.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBox2.Size = new System.Drawing.Size(239, 144);
            this.textBox2.TabIndex = 23;
            // 
            // msLevel
            // 
            this.msLevel.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.setLevelToolStripMenuItem,
            this.setAllToZeroToolStripMenuItem});
            this.msLevel.Name = "msLevel";
            this.msLevel.Size = new System.Drawing.Size(175, 52);
            // 
            // setLevelToolStripMenuItem
            // 
            this.setLevelToolStripMenuItem.Name = "setLevelToolStripMenuItem";
            this.setLevelToolStripMenuItem.Size = new System.Drawing.Size(174, 24);
            this.setLevelToolStripMenuItem.Text = "&Set Level";
            this.setLevelToolStripMenuItem.Click += new System.EventHandler(this.setLevelToolStripMenuItem_Click);
            // 
            // setAllToZeroToolStripMenuItem
            // 
            this.setAllToZeroToolStripMenuItem.Name = "setAllToZeroToolStripMenuItem";
            this.setAllToZeroToolStripMenuItem.Size = new System.Drawing.Size(174, 24);
            this.setAllToZeroToolStripMenuItem.Text = "Set All to Zero";
            this.setAllToZeroToolStripMenuItem.Click += new System.EventHandler(this.setAllToZeroToolStripMenuItem_Click);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(302, 29);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(65, 13);
            this.label2.TabIndex = 24;
            this.label2.Text = "Coin Levels:";
            // 
            // tbFloatAmount
            // 
            this.tbFloatAmount.Location = new System.Drawing.Point(438, 250);
            this.tbFloatAmount.Name = "tbFloatAmount";
            this.tbFloatAmount.Size = new System.Drawing.Size(105, 20);
            this.tbFloatAmount.TabIndex = 25;
            // 
            // btnSetFloat
            // 
            this.btnSetFloat.Location = new System.Drawing.Point(440, 312);
            this.btnSetFloat.Name = "btnSetFloat";
            this.btnSetFloat.Size = new System.Drawing.Size(104, 23);
            this.btnSetFloat.TabIndex = 26;
            this.btnSetFloat.Text = "Set F&loat";
            this.btnSetFloat.UseVisualStyleBackColor = true;
            this.btnSetFloat.Click += new System.EventHandler(this.btnSetFloat_Click);
            // 
            // tbMinPayout
            // 
            this.tbMinPayout.Location = new System.Drawing.Point(438, 211);
            this.tbMinPayout.Name = "tbMinPayout";
            this.tbMinPayout.Size = new System.Drawing.Size(105, 20);
            this.tbMinPayout.TabIndex = 27;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(438, 341);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(82, 13);
            this.label3.TabIndex = 28;
            this.label3.Text = "Payout Amount:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(436, 194);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(63, 13);
            this.label4.TabIndex = 29;
            this.label4.Text = "Min Payout:";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(436, 232);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(72, 13);
            this.label5.TabIndex = 30;
            this.label5.Text = "Float Amount:";
            // 
            // tbFloatCurrency
            // 
            this.tbFloatCurrency.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.tbFloatCurrency.Location = new System.Drawing.Point(438, 288);
            this.tbFloatCurrency.Name = "tbFloatCurrency";
            this.tbFloatCurrency.Size = new System.Drawing.Size(105, 20);
            this.tbFloatCurrency.TabIndex = 31;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(436, 271);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(78, 13);
            this.label6.TabIndex = 32;
            this.label6.Text = "Float Currency:";
            // 
            // tbPayoutCurrency
            // 
            this.tbPayoutCurrency.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.tbPayoutCurrency.Location = new System.Drawing.Point(438, 394);
            this.tbPayoutCurrency.Name = "tbPayoutCurrency";
            this.tbPayoutCurrency.Size = new System.Drawing.Size(105, 20);
            this.tbPayoutCurrency.TabIndex = 33;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(438, 379);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(88, 13);
            this.label1.TabIndex = 34;
            this.label1.Text = "Payout Currency:";
            // 
            // btnPayoutByDenom
            // 
            this.btnPayoutByDenom.Location = new System.Drawing.Point(306, 256);
            this.btnPayoutByDenom.Name = "btnPayoutByDenom";
            this.btnPayoutByDenom.Size = new System.Drawing.Size(114, 50);
            this.btnPayoutByDenom.TabIndex = 35;
            this.btnPayoutByDenom.Text = "Payout by Denomination";
            this.btnPayoutByDenom.UseVisualStyleBackColor = true;
            this.btnPayoutByDenom.Click += new System.EventHandler(this.btnPayoutByDenom_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(656, 454);
            this.Controls.Add(this.logTickBox);
            this.Controls.Add(this.btnPayoutByDenom);
            this.Controls.Add(this.textBox2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.tbPayoutCurrency);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.btnPayout);
            this.Controls.Add(this.tbFloatCurrency);
            this.Controls.Add(this.btnSmartEmpty);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.btnHalt);
            this.Controls.Add(this.btnToggleCoinMech);
            this.Controls.Add(this.btnSetFloat);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.tbAmountToPayout);
            this.Controls.Add(this.tbMinPayout);
            this.Controls.Add(this.tbFloatAmount);
            this.Controls.Add(this.btnRun);
            this.Controls.Add(this.menuStrip1);
            this.Controls.Add(this.emptyAllBtn);
            this.Controls.Add(this.resetValidatorBtn);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Form1";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "SMART Hopper eSSP C# example";
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
        private System.Windows.Forms.ToolStripMenuItem emptyNoteFloatToolStripMenuItem1;
        private System.Windows.Forms.Button resetValidatorBtn;
        private System.Windows.Forms.CheckBox logTickBox;
        private System.Windows.Forms.Button emptyAllBtn;
        private System.Windows.Forms.TextBox tbAmountToPayout;
        private System.Windows.Forms.Button btnPayout;
        private System.Windows.Forms.Button btnToggleCoinMech;
        private System.Windows.Forms.Button btnSmartEmpty;
        private System.Windows.Forms.TextBox textBox2;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.ContextMenuStrip msLevel;
        private System.Windows.Forms.ToolStripMenuItem setLevelToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem setAllToZeroToolStripMenuItem;
        private System.Windows.Forms.TextBox tbFloatAmount;
        private System.Windows.Forms.Button btnSetFloat;
        private System.Windows.Forms.TextBox tbMinPayout;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.TextBox tbFloatCurrency;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.TextBox tbPayoutCurrency;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button btnPayoutByDenom;

    }
}

