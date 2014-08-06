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
            this.logTickBox = new System.Windows.Forms.CheckBox();
            this.msLevel = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.setLevelToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.setAllToZeroToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.tbPayout = new System.Windows.Forms.TextBox();
            this.btnPayout = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.cbRecycleChannelNV11 = new System.Windows.Forms.ComboBox();
            this.btnResetNoteFloat = new System.Windows.Forms.Button();
            this.btnNoteFloatStackAll = new System.Windows.Forms.Button();
            this.btnStackNextNote = new System.Windows.Forms.Button();
            this.btnPayoutNextNote = new System.Windows.Forms.Button();
            this.label5 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.tbNotesStored = new System.Windows.Forms.TextBox();
            this.btnEmptyHopper = new System.Windows.Forms.Button();
            this.btnSmartEmptyHopper = new System.Windows.Forms.Button();
            this.tbCoinLevels = new System.Windows.Forms.TextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.btnResetHopper = new System.Windows.Forms.Button();
            this.tbPayoutCurrency = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
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
            this.menuStrip1.Size = new System.Drawing.Size(732, 24);
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
            this.tbPayout.Location = new System.Drawing.Point(357, 260);
            this.tbPayout.Name = "tbPayout";
            this.tbPayout.Size = new System.Drawing.Size(100, 20);
            this.tbPayout.TabIndex = 17;
            // 
            // btnPayout
            // 
            this.btnPayout.Location = new System.Drawing.Point(294, 323);
            this.btnPayout.Name = "btnPayout";
            this.btnPayout.Size = new System.Drawing.Size(160, 23);
            this.btnPayout.TabIndex = 18;
            this.btnPayout.Text = "Payout";
            this.btnPayout.UseVisualStyleBackColor = true;
            this.btnPayout.Click += new System.EventHandler(this.btnPayout_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(297, 253);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(55, 26);
            this.label1.TabIndex = 19;
            this.label1.Text = "Amount\r\nto Payout:";
            // 
            // cbRecycleChannelNV11
            // 
            this.cbRecycleChannelNV11.FormattingEnabled = true;
            this.cbRecycleChannelNV11.Location = new System.Drawing.Point(536, 192);
            this.cbRecycleChannelNV11.Name = "cbRecycleChannelNV11";
            this.cbRecycleChannelNV11.Size = new System.Drawing.Size(121, 21);
            this.cbRecycleChannelNV11.TabIndex = 0;
            this.cbRecycleChannelNV11.SelectedIndexChanged += new System.EventHandler(this.cbRecycleChannelNV11_SelectedIndexChanged);
            // 
            // btnResetNoteFloat
            // 
            this.btnResetNoteFloat.Location = new System.Drawing.Point(567, 404);
            this.btnResetNoteFloat.Name = "btnResetNoteFloat";
            this.btnResetNoteFloat.Size = new System.Drawing.Size(144, 23);
            this.btnResetNoteFloat.TabIndex = 5;
            this.btnResetNoteFloat.Text = "Reset NV11";
            this.btnResetNoteFloat.UseVisualStyleBackColor = true;
            this.btnResetNoteFloat.Click += new System.EventHandler(this.btnResetNoteFloat_Click);
            // 
            // btnNoteFloatStackAll
            // 
            this.btnNoteFloatStackAll.Location = new System.Drawing.Point(536, 277);
            this.btnNoteFloatStackAll.Name = "btnNoteFloatStackAll";
            this.btnNoteFloatStackAll.Size = new System.Drawing.Size(121, 23);
            this.btnNoteFloatStackAll.TabIndex = 5;
            this.btnNoteFloatStackAll.Text = "Stack All";
            this.btnNoteFloatStackAll.UseVisualStyleBackColor = true;
            this.btnNoteFloatStackAll.Click += new System.EventHandler(this.btnNoteFloatStackAll_Click);
            // 
            // btnStackNextNote
            // 
            this.btnStackNextNote.Location = new System.Drawing.Point(536, 219);
            this.btnStackNextNote.Name = "btnStackNextNote";
            this.btnStackNextNote.Size = new System.Drawing.Size(121, 23);
            this.btnStackNextNote.TabIndex = 4;
            this.btnStackNextNote.Text = "Stack Next Note";
            this.btnStackNextNote.UseVisualStyleBackColor = true;
            this.btnStackNextNote.Click += new System.EventHandler(this.btnStackNextNote_Click);
            // 
            // btnPayoutNextNote
            // 
            this.btnPayoutNextNote.Location = new System.Drawing.Point(536, 248);
            this.btnPayoutNextNote.Name = "btnPayoutNextNote";
            this.btnPayoutNextNote.Size = new System.Drawing.Size(121, 23);
            this.btnPayoutNextNote.TabIndex = 3;
            this.btnPayoutNextNote.Text = "Payout Next Note";
            this.btnPayoutNextNote.UseVisualStyleBackColor = true;
            this.btnPayoutNextNote.Click += new System.EventHandler(this.btnPayoutNextNote_Click);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(552, 176);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(87, 13);
            this.label5.TabIndex = 2;
            this.label5.Text = "Note to Recycle:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(533, 41);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(72, 13);
            this.label4.TabIndex = 1;
            this.label4.Text = "Notes Stored:";
            // 
            // tbNotesStored
            // 
            this.tbNotesStored.BackColor = System.Drawing.SystemColors.ButtonHighlight;
            this.tbNotesStored.Location = new System.Drawing.Point(536, 57);
            this.tbNotesStored.Multiline = true;
            this.tbNotesStored.Name = "tbNotesStored";
            this.tbNotesStored.ReadOnly = true;
            this.tbNotesStored.Size = new System.Drawing.Size(121, 116);
            this.tbNotesStored.TabIndex = 0;
            // 
            // btnEmptyHopper
            // 
            this.btnEmptyHopper.Location = new System.Drawing.Point(298, 208);
            this.btnEmptyHopper.Name = "btnEmptyHopper";
            this.btnEmptyHopper.Size = new System.Drawing.Size(161, 23);
            this.btnEmptyHopper.TabIndex = 0;
            this.btnEmptyHopper.Text = "Empty All to Cashbox";
            this.btnEmptyHopper.UseVisualStyleBackColor = true;
            // 
            // btnSmartEmptyHopper
            // 
            this.btnSmartEmptyHopper.Location = new System.Drawing.Point(298, 179);
            this.btnSmartEmptyHopper.Name = "btnSmartEmptyHopper";
            this.btnSmartEmptyHopper.Size = new System.Drawing.Size(161, 23);
            this.btnSmartEmptyHopper.TabIndex = 1;
            this.btnSmartEmptyHopper.Text = "SMART Empty";
            this.btnSmartEmptyHopper.UseVisualStyleBackColor = true;
            this.btnSmartEmptyHopper.Click += new System.EventHandler(this.btnSmartEmptyHopper_Click);
            // 
            // tbCoinLevels
            // 
            this.tbCoinLevels.BackColor = System.Drawing.SystemColors.ButtonHighlight;
            this.tbCoinLevels.ContextMenuStrip = this.msLevel;
            this.tbCoinLevels.Location = new System.Drawing.Point(298, 57);
            this.tbCoinLevels.Multiline = true;
            this.tbCoinLevels.Name = "tbCoinLevels";
            this.tbCoinLevels.ReadOnly = true;
            this.tbCoinLevels.Size = new System.Drawing.Size(161, 116);
            this.tbCoinLevels.TabIndex = 2;
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(295, 28);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(46, 26);
            this.label6.TabIndex = 3;
            this.label6.Text = "Channel\r\nLevels:";
            // 
            // btnResetHopper
            // 
            this.btnResetHopper.Location = new System.Drawing.Point(417, 404);
            this.btnResetHopper.Name = "btnResetHopper";
            this.btnResetHopper.Size = new System.Drawing.Size(144, 23);
            this.btnResetHopper.TabIndex = 4;
            this.btnResetHopper.Text = "Reset Hopper";
            this.btnResetHopper.UseVisualStyleBackColor = true;
            this.btnResetHopper.Click += new System.EventHandler(this.btnResetHopper_Click);
            // 
            // tbPayoutCurrency
            // 
            this.tbPayoutCurrency.CharacterCasing = System.Windows.Forms.CharacterCasing.Upper;
            this.tbPayoutCurrency.Location = new System.Drawing.Point(357, 292);
            this.tbPayoutCurrency.Name = "tbPayoutCurrency";
            this.tbPayoutCurrency.Size = new System.Drawing.Size(51, 20);
            this.tbPayoutCurrency.TabIndex = 20;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(297, 287);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(55, 26);
            this.label2.TabIndex = 21;
            this.label2.Text = "Currency\r\nof Payout:";
            // 
            // btnPayoutByDenom
            // 
            this.btnPayoutByDenom.Location = new System.Drawing.Point(294, 351);
            this.btnPayoutByDenom.Margin = new System.Windows.Forms.Padding(2);
            this.btnPayoutByDenom.Name = "btnPayoutByDenom";
            this.btnPayoutByDenom.Size = new System.Drawing.Size(160, 23);
            this.btnPayoutByDenom.TabIndex = 22;
            this.btnPayoutByDenom.Text = "Payout by Denomination";
            this.btnPayoutByDenom.UseVisualStyleBackColor = true;
            this.btnPayoutByDenom.Click += new System.EventHandler(this.btnPayoutByDenom_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(732, 435);
            this.Controls.Add(this.btnPayoutByDenom);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.tbPayoutCurrency);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.tbCoinLevels);
            this.Controls.Add(this.btnNoteFloatStackAll);
            this.Controls.Add(this.btnResetNoteFloat);
            this.Controls.Add(this.btnResetHopper);
            this.Controls.Add(this.btnSmartEmptyHopper);
            this.Controls.Add(this.btnStackNextNote);
            this.Controls.Add(this.btnPayoutNextNote);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.logTickBox);
            this.Controls.Add(this.btnPayout);
            this.Controls.Add(this.btnEmptyHopper);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.tbPayout);
            this.Controls.Add(this.btnHalt);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.cbRecycleChannelNV11);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.btnRun);
            this.Controls.Add(this.tbNotesStored);
            this.Controls.Add(this.menuStrip1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MainMenuStrip = this.menuStrip1;
            this.Name = "Form1";
            this.Text = "Hopper and NV11 Example Software";
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
        private System.Windows.Forms.CheckBox logTickBox;
        private System.Windows.Forms.ContextMenuStrip msLevel;
        private System.Windows.Forms.ToolStripMenuItem setLevelToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem setAllToZeroToolStripMenuItem;
        private System.Windows.Forms.TextBox tbPayout;
        private System.Windows.Forms.Button btnPayout;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox tbNotesStored;
        private System.Windows.Forms.ComboBox cbRecycleChannelNV11;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.Button btnStackNextNote;
        private System.Windows.Forms.Button btnPayoutNextNote;
        private System.Windows.Forms.Button btnNoteFloatStackAll;
        private System.Windows.Forms.Button btnResetNoteFloat;
        private System.Windows.Forms.Button btnResetHopper;
        private System.Windows.Forms.Button btnEmptyHopper;
        private System.Windows.Forms.Button btnSmartEmptyHopper;
        private System.Windows.Forms.TextBox tbCoinLevels;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.TextBox tbPayoutCurrency;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button btnPayoutByDenom;

    }
}

