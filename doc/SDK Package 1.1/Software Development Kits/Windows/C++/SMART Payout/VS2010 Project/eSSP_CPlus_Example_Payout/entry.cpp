#include <vector>
#include <time.h>
#include "IO.h"
#include "CSMARTPayout.h"

std::vector<string> GetComPorts(); // Returns a vector containing a list of all the available ports on the host
bool ParseKeys(CPayout* payout); // The method responsible for parsing and responding to key presses
void DisplayCommands(); // Displays a list of commands available for this SDK
unsigned char FormatComPort(const string& comPort); // Remove the x. COM from the COM port string
DWORD WINAPI PollThread(LPVOID params); // The polling function, run as a seperate thread
HANDLE StartPollThread(CPayout* payout); // The method to start the polling thread, returns a handle to it

const int PollInterval = 250; // Interval between polls (ms)
volatile bool IsPolling = false; // Indicates whether the poll thread should actively poll the unit
volatile bool PollFailed = false; // Indicates whether a poll has failed, can be called by multiple threads

#define MAX_PROTOCOL_VERSION 8 // Max protocol version supported by SMART Payout (3/11/11).

int main(int argc, char *argv[])
{
	CPayout* Payout = new CPayout(); // The main class used to interface with the validator

	// Create a new thread here, this thread will not actively poll the unit until the
	// bool IsPolling is set to true.
	HANDLE payoutThread = StartPollThread(Payout);

	// Set output to std::cout
	Payout->SetOutputStream(&std::cout);
	WriteString("C++ SDK\n");

	// Find out about the validator and create a connection struct to pass
	// to the validator class
	SSP_COMMAND command;
	command.BaudRate = 9600;
	command.Timeout = 1000;
	command.RetryLevel = 3;
	command.IgnoreError = 1;

	char* end;

	// Get port number
	std::vector<string> ports = GetComPorts();
	for (unsigned int i = 0; i < ports.size(); ++i)
		WriteString(ports[i]);

	string in = "";
	unsigned int portNum = 0;
	do
	{
		in = GetInputString("Select a port: ");
		portNum = strtol(in.c_str(), &end, 0);
	} while (portNum < 1 || portNum > ports.size());

	command.PortNumber = FormatComPort(ports[portNum-1]);
		
	// Get ssp address
	unsigned int ssp = 0;
	do
	{
		in = GetInputString("SSP Address (Default 0): ");
		ssp = strtol(in.c_str(), &end, 0);
	} while (ssp < 0 || ssp > 32 || end == in.c_str());
	command.SSPAddress = (unsigned char)ssp;

	// Get protocol version to use
	unsigned int p = 0;
	do
	{
		in = GetInputString("Protocol version: ");
		p = strtol(in.c_str(), &end, 0);
	} while (p < 1 || p > MAX_PROTOCOL_VERSION);

	// Now connect to validator
	if (Payout->ConnectToPayout(command, p, 5) && Payout->GetUnitType() == 0x06)
	{
		WriteString("Connected to SMART Payout successfully");
		// Display the available commands
		DisplayCommands();
		// Begin checking input and poll fails
		while  (true)
		{
			if (!ParseKeys(Payout))
			{
				IsPolling = false;
				break; // If false break the loop
			}
			// This bool is set by the poll loop thread
			if (PollFailed)
			{
				cout << "Poll failed" << endl;
				if (Payout->ConnectToPayout(command, p, 5))
				{
					PollFailed = false;
					StartPollThread(Payout);
				}
				else
					break;
			}
		}
	}
	else if (Payout->GetUnitType() != 0x06)
		WriteString("Incorrect unit detected, this SDK supports the SMART Payout only");
	else
		WriteString("Failed to connect to the unit");

	cout << "Program shutting down... ";
	// Close poll thread
	TerminateThread(payoutThread, 1);
	CloseHandle(payoutThread);
	// Delete payout
	delete Payout;
	cout << "Complete\n\nPress any key to exit..." << endl;
	_getch();
}

// This function searches through com ports 1 to 128 and determines if they
// exist or not. The list of existing ports is sent as a response.
std::vector<string> GetComPorts()
{
	vector<string> ports;
	int index = 1;
	// Go through each possible port
	for (int i = 1; i <= 64; ++i)    
    {
		char* buffer = new char[8];
		string port = "COM";
		port += itoa(i, buffer, 10);
		
		// First get the size
		DWORD dwSize = 0;
		LPCOMMCONFIG lpCC = (LPCOMMCONFIG) new BYTE[1];
		BOOL ret = GetDefaultCommConfig(port.c_str(), lpCC, &dwSize);
		delete [] lpCC;	

		// Now test the port
		lpCC = (LPCOMMCONFIG) new BYTE[dwSize];
		ret = GetDefaultCommConfig(port.c_str(), lpCC, &dwSize);
		
		// If the port exists, add to the vector
		if (ret != null)
		{
			string pusher = itoa(index++, buffer, 10);
			pusher += ". ";
			pusher += port;
			ports.push_back(pusher);
		}
		delete[] buffer;
		delete [] lpCC;
    }
	return ports;
}

bool ParseKeys(CPayout* payout)
{
	// If a keystroke is detected
	if (_kbhit())
	{
		// Parse the key
		switch (_getch())
		{
		case 'x': return false; 
		case 'e': 
			payout->EnableValidator(); 
			break;
		case 'd':
			payout->DisableValidator(); 
			break;
		case 'p':
			{
				// Toggle the polling
				if (IsPolling)
				{
					IsPolling = false; 
					WriteString("Stopped polling");
				}
				else
				{
					IsPolling = true;
					WriteString("Started polling");
				}
				break;
			}
		case 'r':
			{
				// Stored notes
				WriteString("Notes stored:\n");
				payout->OutputLevelInfo();

				// Recycling status of notes
				WriteString("Recycling status of notes:\n");
				for (int i = 0; i < payout->GetNumChannels(); ++i) 
				{
					cout << payout->GetUnitData()[i].Value*0.01f << " " << payout->GetUnitData()[i].Currency[0] <<
						payout->GetUnitData()[i].Currency[1] << payout->GetUnitData()[i].Currency[2] << " is ";
					(payout->GetUnitData()[i].Recycling)?cout << "being sent to storage" << endl:
						cout << "being sent to cashbox" << endl;
				}

				// Cashbox payout data
				WriteString("\nCashbox payout data:\n");
				payout->GetCashboxPayoutOpData();
				break;
			}
		case 'h': DisplayCommands(); break;
		case 't':
			{
				payout->ResetValidator();
				payout->CloseComPort(); // Force reconnection by closing com port
				break;
			}
		case 'a': 
			{
				char choice = GetInputChar("1. Standard payout\n2. Payout by Denomination\r\n");
				if (choice == '1') // Standard payout
				{
					string s = GetInputString("Enter amount and currency to payout\nFormat is Amount Currency e.g. 10.00 EUR: ");

					// Break into 2 strings, first contains value, second currency
					string s1 = "";	
					size_t pos = s.find_first_of(' ');
					if (pos == s.npos)
					{
						WriteString("Input was not in the correct format");
						break;
					}
					s1 = s.substr(pos+1, s1.npos);
					s.erase(pos, s.npos);

					// Convert value to integer
					float value = (float)atof(s.c_str());

					// Make sure each char in the currency is uppercase and
					// only 3 chars were entered
					if (s1.length() != 3)
					{
						WriteString("Invalid currency");
						break;
					}
					for (int i = 0; i < 3; ++i)
						s1[i] = toupper(s1[i]);

					// If the value entered isn't 0 or negative, make the payout
					if (value > 0)
					{
						payout->Payout((int)(value * 100), s1);
					}
				}
				else if (choice == '2') // Payout by Denomination
				{
					// Get the dataset info from the validator
					SUnitData* UnitData = payout->GetUnitData();
					// Create a temporary array to hold denomination info
					unsigned char* dataArray = new unsigned char[255];
					int counter = 0, temp = 0, numDenoms = 0;
					// Go through each channel and query the user on how many of each denomination
					// they want to payout.
					for (int i = 0; i < payout->GetNumChannels(); ++i)
					{
						cout << "Number of " << UnitData[i].Value*0.01f << " " << UnitData[i].Currency[0] <<
							UnitData[i].Currency[1] << UnitData[i].Currency[2] << " to payout: ";
						temp = atoi(GetInputString("").c_str());
						if (temp != 0) // if they have entered a number
						{
							numDenoms++; // increase number of denominations to payout
							memcpy(dataArray + counter, &temp, 2); // copy number of denominations to the data array
							counter += 2;
							memcpy(dataArray + counter, &UnitData[i].Value, 4); // copy value of denomination to data array
							counter += 4;
							// copy currency of denom to data array
							dataArray[counter++] = UnitData[i].Currency[0];
							dataArray[counter++] = UnitData[i].Currency[1];
							dataArray[counter++] = UnitData[i].Currency[2];
						}
					}

					if (numDenoms > 0) // if the user didn't enter 0 for all denominations
					{
						payout->PayoutByDenomination(numDenoms, dataArray);
					}
					delete[] dataArray; // delete temp array once command sent
				}
				else
					WriteString("Invalid input\r\n");
				break;
			}
		case 'm':
			{
				// Empty has two choices, SMART or just empty. SMART keeps track of
				// what notes were emptied, empty does not.
				char choice = GetInputChar("1. Empty\n2. SMART Empty\n");
				if (choice == '1')
					payout->Empty();
				else if (choice == '2')
					payout->SMARTEmpty();
				else
					WriteString("Invalid input");
				break;
			}
		case 'c':
			{
				// Note recycling
				string s = GetInputString("Enter amount and currency to alter recycling on\nFormat is Amount Currency e.g. 10.00 EUR: ");

				// Break into 2 strings, first contains value, second currency
				string s1 = "";	
				size_t pos = s.find_first_of(' ');
				if (pos == s.npos)
				{
					WriteString("Input was not in the correct format");
					break;
				}
				s1 = s.substr(pos+1, s1.npos);
				s.erase(pos, s.npos);

				// Convert value to integer
				float value = (float)atof(s.c_str());

				// Make sure each char in the currency is uppercase and
				// only 3 chars were entered
				if (s1.length() != 3)
				{
					WriteString("Invalid currency");
					break;
				}
				for (int i = 0; i < 3; ++i)
					s1[i] = toupper(s1[i]);

				char c = GetInputChar("1. Store note\n2. Stack note\r\n");

				// Validation
				if ((c != '1' && c != '2'))
				{
					cout << "Invalid input" << endl;
					break;
				}

				// Convert to bool
				bool stack = atoi(&c)-1;

				// Convert value to penny value
				value *= 100;

				// Send to either cashbox or storage depending on choice
				(stack)?payout->SendNoteToCashbox((int)value, s1):payout->SendNoteToStorage((int)value, s1);

				break;
			}
		default: break;
		}
	}
	return true;
}

void DisplayCommands()
{
	string s = "Command List\n";
	s += "e = enable\nd = disable\n";
	s += "p = start/stop polling\n";	
	s += "a = make a payout\n";
	s += "m = empty or SMART empty the unit\n";
	s += "c = change note recycling options\n";
	s += "t = reset validator\n";
	s += "r = report\n\n";
	s += "h = display this list again\n";
	s += "x = exit\n";
	WriteString(s);
}

// Remove the "x. COM" from the com port string
unsigned inline char FormatComPort(const string& portstring)
{
	string s = portstring;
	s.erase(0, 6);
	return (unsigned char)atoi(s.c_str());
}

// The following section contains threading functions. Threading is used in this program
// as it is vital that the validator is polled at regular intervals. Obviously the execution
// of the main thread of the program needs to be halted occasionally - such as when waiting
// for a user's input, so the polling of the validator needs to occur on a seperate thread
// which will never be halted. The validator class looks after critical sections and thread
// safing.

// This function is called to create a new instance of the poll thread. The function returns a
// handle to the thread.
HANDLE StartPollThread(CPayout* Payout)
{
	unsigned int hopThreadID;
	return CreateThread(NULL,
						0,
						PollThread,
						Payout,
						NULL,
						(LPDWORD)&hopThreadID);
}

// This function is responsible for polling the validator at a regular interval.
// It should be run as a seperate thread to the main execution thread.
DWORD WINAPI PollThread(LPVOID params)
{
	// Get validator instance from param
	CPayout* payout = reinterpret_cast<CPayout*>(params);

	// Keep looping until broken out of
	while (true)
	{
		// If the validator is actively being polled
		if (IsPolling)
		{
			// Attempt to send the poll
			if (!payout->DoPoll())
			{
				// If it fails (command can't get to validator) then set the failed poll
				// flag to true and exit the function. This will terminate the thread.
				PollFailed = true;
				return 0;
			}
			Sleep(PollInterval); // Wait for the interval
		}
	}
	return 0;
}