#include <Windows.h>
#include <vector>
#include "IO.h"
#include "CNV11.h"

std::vector<string> GetComPorts(); // Function to return a vector of port names as strings
bool ParseKeys(CNV11* nv11); // Function which parses keyboard hits and deals with them
void DisplayCommands(); // Displays the main menu of commands
unsigned char FormatComPort(const string& portstring); // Removes the X. COM of a COM port string to leave the number
HANDLE StartPollThread(CNV11* nv11); // Starts the poll thread, returns the handle to the thread
DWORD WINAPI PollThread(LPVOID params); // The method run by the poll thread, polls the unit at a specified interval

volatile int pollInterval = 250; // The interval in milliseconds between polls
volatile bool IsPolling = false; // When this is true, the poll thread sends poll commands to the unit
volatile bool PollFailed = false; // When this is true, the last poll command sent failed
const int MAX_PROTOCOL_VERSION = 7; // Max protocol for NV11 (27/10/11).

int main(int argc, char *argv[])
{
	CNV11* NV11 = new CNV11(); // Create instance of validator class
	// Start the polling thread, this will not send any polls until
	// the IsPolling variable is true
	HANDLE nv11Thread = StartPollThread(NV11); 
	// Set output to std::cout
	NV11->SetOutputStream(&cout);
	WriteString("Example C++ NV11 SDK\n\nSearching for ports...\n");

	// Find out about the validator and create a connection struct to pass
	// to the validator class
	SSP_COMMAND commandStructure;
	commandStructure.BaudRate = 9600;
	commandStructure.Timeout = 1000;
	commandStructure.RetryLevel = 3;
	commandStructure.IgnoreError = 1;

	char* end;
	// Get port number
	std::vector<string> ports = GetComPorts();
	for (unsigned int i = 0; i < ports.size(); ++i)
		cout << ports[i] << endl;
	string in = "";
	unsigned int portIndex = 0;
	do
	{
		in = GetInputString("Select a port: ");
		portIndex = strtol(in.c_str(), &end, 0);
	} while (portIndex <= 0 || portIndex > ports.size());
	commandStructure.PortNumber = FormatComPort(ports[portIndex-1]); 
		
	// Get ssp address
	int ssp = 0;
	do
	{
		in = GetInputString("SSP Address (Default = 0): ");
		ssp = strtol(in.c_str(), &end, 0);
	} while (ssp < 0 || ssp > 32 || end == in.c_str());
	commandStructure.SSPAddress = (unsigned char)ssp;

	// Get protocol version to use
	int p = 0;
	do
	{
		in = GetInputString("Protocol version: ");
		p = strtol(in.c_str(), &end, 0);
	} while (p <= 0 || p > MAX_PROTOCOL_VERSION);

	// Now connect to validator
	if (NV11->ConnectToValidator(commandStructure, p, 5))
	{
		WriteString("Connected to validator successfully");
		DisplayCommands();
		while  (true)
		{
			// Parse the keys, if this returns false then the user has selected
			// to exit the program
			if (!ParseKeys(NV11))
			{
				IsPolling = false; // Stop polling before exit
				break;
			}

			// If PollFailed is set by the polling thread, attempt to reconnect to the unit
			if (PollFailed)
			{
				// Make 5 attempts to reconnect, if none successful break out
				if (NV11->ConnectToValidator(commandStructure, p, 5)) 
				{
					// If successful reconnect, restart the polling thread and continue
					PollFailed = false;
					TerminateThread(nv11Thread, 0);
					CloseHandle(nv11Thread);
					nv11Thread = StartPollThread(NV11);
				}
				else
					break;
			}
		}
		WriteString("Poll loop stopped");
	}
	TerminateThread(nv11Thread, 0);
	CloseHandle(nv11Thread);
	delete NV11;
	WriteString("Press any key to exit...");
	_getch();
}

// This function searches through com ports 1 to 128 and determines if they
// exist or not. The list of existing ports is sent as a response.
std::vector<string> GetComPorts()
{
	vector<string> ports;
	int index = 1;
	// Go through each possible port (assuming no more than 64 ports on a system)
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

bool ParseKeys(CNV11* nv11)
{
	// If a keyboard hit is detected
	if (_kbhit())
	{
		char c = _getch(); // Get the key
		switch (c)
		{
		case 'x': return false;
		case 'e': 
				// Enable unit
				nv11->EnableValidator();
				break;
		case 'd':
				// Disable unit
				nv11->DisableValidator();
				break;
		case 'p':
			{
				if (IsPolling)
				{
					IsPolling = false; 
					cout << "Stopped polling" << endl;
				}
				else
				{
					IsPolling = true;
					cout << "Started polling" << endl;
				}
				break;
			}
		case 'r':
			{
				// Report the positions of any notes stored in the NV11
				cout << "Note positions: " << endl;
				nv11->OutputNoteLevelInfo();

				// Report which channel is recycling (1 recycling at once in NV11)
				nv11->OutputCurrentRecycling();
				WriteString(" is currently recycling");
				break;
			}
		case 'h': DisplayCommands(); break;
		case 't': 
				// Wait for thread release then reset the unit
				nv11->ResetValidator();
				nv11->CloseComPort();
				break;
		case 'c': 
			{
				string denom = GetInputString("Enter note value and currency E.g. 10 EUR: ");

				// Break into 2 strings, first contains value, second currency
				string s1 = "";	
				size_t pos = denom.find_first_of(' ');
				if (pos == denom.npos)
				{
					WriteString("Input was not in the correct format");
					break;
				}
				s1 = denom.substr(pos+1, s1.npos);
				denom.erase(pos, denom.npos);

				// Convert value to float
				float value = (float)atof(denom.c_str()) * 100.0f;

				if (value <= 0)
				{
					WriteString("Value was less than or equal to zero");
					break;
				}

				// Make sure each char in the currency is uppercase and
				// only 3 chars were entered
				if (s1.length() != 3)
				{
					WriteString("Invalid currency");
					break;
				}
				for (int i = 0; i < 3; ++i)
					s1[i] = toupper(s1[i]);

				// Get whether to stack or store note
				char stack = GetInputChar("1 = Store note\n2 = Stack note\n");
				int b = atoi(&stack)-1;
				if (b < 0 || b > 1)
				{
					WriteString("Invalid input");
					break;
				}
				nv11->ChangeRouting((int)value, (char*)s1.c_str(), b);
				break;
			}
		case 's':
				nv11->StackNextNote(); // Move the next note in the recycler to the cashbox
				break;
		case 'n': 
				nv11->PayoutNextNote(); // Payout the next note in the recycler
				break;
		case 'm': 
				nv11->EmptyPayout(); // Move all the notes in the recycler to the cashbox
				break;
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
	s += "c = set a channel to recycle\n";
	s += "n = payout next note\n";
	s += "s = stack next note\n";
	s += "t = reset validator\n";
	s += "m = empty all stored notes to cashbox\n\n";
	s += "r = report\n";
	s += "h = display this list again\n";
	s += "x = exit\n";
	cout << s << endl;
}

unsigned char FormatComPort(const string& portstring)
{
	string s = portstring;
	s.erase(0, 6);
	return (unsigned char)atoi(s.c_str());
}

// The following section contains threading functions. Threading is used in this program
// as it is vital that the validator is polled at regular intervals. Occasionally the execution
// of the main thread of the program needs to be halted - such as when waiting
// for a user's input, so the polling of the validator needs to occur on a seperate thread
// which will never be halted.

// This function is called to create a new instance of the poll thread. The function returns a
// handle to the thread.
HANDLE StartPollThread(CNV11* nv11)
{
	unsigned int hopThreadID;
	return CreateThread(NULL,
						0,
						PollThread,
						nv11,
						NULL,
						(LPDWORD)&hopThreadID);
}

// This function is responsible for polling the validator at a regular interval.
// It should be run as a seperate thread to the main execution thread.
DWORD WINAPI PollThread(LPVOID params)
{
	// Get validator instance from param
	CNV11* nv11 = reinterpret_cast<CNV11*>(params);

	// Keep looping until broken out of
	while (true)
	{
		// If the validator is actively being polled
		if (IsPolling)
		{
			// Attempt to send the poll
			if (!nv11->DoPoll())
			{
				// If it fails (command can't get to validator) then set the failed poll
				// flag to true and exit the function. This will terminate the thread.
				PollFailed = true;
				return 0;
			}
			Sleep(pollInterval); // Wait for the interval
		}
	}
	return 0;
}
