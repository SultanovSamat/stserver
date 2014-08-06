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
            this.resetValidatorBtn = new System.Windows.Forms.Button();
            this.logTickBox = new System.Windows.Forms.CheckBox();
            this.tbNumNotes = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.btnClear = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // btnRun
            // 
            this.btnRun.Location = new System.Drawing.Point(18, 465);
            this.btnRun.Margin = new System.Windows.Forms.Padding(4);
            this.btnRun.Name = "btnRun";
            this.btnRun.Size = new System.Drawing.Size(100, 28);
            this.btnRun.TabIndex = 1;
            this.btnRun.Text = "&Run";
            this.btnRun.UseVisualStyleBackColor = true;
            this.btnRun.Click += new System.EventHandler(this.btnRun_Click);
            // 
            // btnHalt
            // 
            this.btnHalt.Location = new System.Drawing.Point(142, 465);
            this.btnHalt.Margin = new System.Windows.Forms.Padding(4);
            this.btnHalt.Name = "btnHalt";
            this.btnHalt.Size = new System.Drawing.Size(100, 28);
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
            this.textBox1.Location = new System.Drawing.Point(16, 33);
            this.textBox1.Margin = new System.Windows.Forms.Padding(4);
            this.textBox1.Multiline = true;
            this.textBox1.Name = "textBox1";
            this.textBox1.ReadOnly = true;
            this.textBox1.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.textBox1.Size = new System.Drawing.Size(372, 334);
            this.textBox1.TabIndex = 4;
            // 
            // resetValidatorBtn
            // 
            this.resetValidatorBtn.Location = new System.Drawing.Point(18, 415);
            this.resetValidatorBtn.Margin = new System.Windows.Forms.Padding(4);
            this.resetValidatorBtn.Name = "resetValidatorBtn";
            this.resetValidatorBtn.Size = new System.Drawing.Size(188, 28);
            this.resetValidatorBtn.TabIndex = 15;
            this.resetValidatorBtn.Text = "R&eset Validator";
            this.resetValidatorBtn.UseVisualStyleBackColor = true;
            this.resetValidatorBtn.Click += new System.EventHandler(this.resetValidatorBtn_Click);
            // 
            // logTickBox
            // 
            this.logTickBox.AutoSize = true;
            this.logTickBox.Checked = true;
            this.logTickBox.CheckState = System.Windows.Forms.CheckState.Checked;
            this.logTickBox.Location = new System.Drawing.Point(285, 470);
            this.logTickBox.Margin = new System.Windows.Forms.Padding(4);
            this.logTickBox.Name = "logTickBox";
            this.logTickBox.Size = new System.Drawing.Size(104, 21);
            this.logTickBox.TabIndex = 16;
            this.logTickBox.Text = "Comms Log";
            this.logTickBox.UseVisualStyleBackColor = true;
            this.logTickBox.CheckedChanged += new System.EventHandler(this.logTickBox_CheckedChanged);
            // 
            // tbNumNotes
            // 
            this.tbNumNotes.Location = new System.Drawing.Point(215, 375);
            this.tbNumNotes.Margin = new System.Windows.Forms.Padding(4);
            this.tbNumNotes.Name = "tbNumNotes";
            this.tbNumNotes.Size = new System.Drawing.Size(132, 22);
            this.tbNumNotes.TabIndex = 17;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(25, 378);
            this.label1.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(182, 17);
            this.label1.TabIndex = 19;
            this.label1.Text = "Number of Notes Accepted:";
            // 
            // btnClear
            // 
            this.btnClear.Location = new System.Drawing.Point(214, 415);
            this.btnClear.Margin = new System.Windows.Forms.Padding(4);
            this.btnClear.Name = "btnClear";
            this.btnClear.Size = new System.Drawing.Size(133, 28);
            this.btnClear.TabIndex = 21;
            this.btnClear.Text = "Clear Totals";
            this.btnClear.UseVisualStyleBackColor = true;
            this.btnClear.Click += new System.EventHandler(this.btnClear_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(401, 504);
            this.Controls.Add(this.btnClear);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.tbNumNotes);
            this.Controls.Add(this.logTickBox);
            this.Controls.Add(this.resetValidatorBtn);
            this.Controls.Add(this.textBox1);
            this.Controls.Add(this.btnHalt);
            this.Controls.Add(this.btnRun);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Margin = new System.Windows.Forms.Padding(4);
            this.Name = "Form1";
            this.Text = "Validator eSSP C# example";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.Form1_FormClosing);
            this.Load += new System.EventHandler(this.Form1_Load);
            this.Shown += new System.EventHandler(this.Form1_Shown);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnRun;
        private System.Windows.Forms.Button btnHalt;
        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.TextBox textBox1;
        private System.Windows.Forms.Button resetValidatorBtn;
        private System.Windows.Forms.CheckBox logTickBox;
        private System.Windows.Forms.TextBox tbNumNotes;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button btnClear;

    }
}

