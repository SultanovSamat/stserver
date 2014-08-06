#include <vector>
#include <time.h>
#include "IO.h"
#include "CSMARTHopper.h"

std::vector<string> GetComPorts(); // Method to return the com ports on the host
bool ParseKeys(CHopper* hop); // Method which parses and responds to key presses
void DisplayCommands(); // Method to display what commands are available
unsigned char FormatComPort(const string& portString);
DWORD WINAPI PollThread(LPVOID params); // Method that is run as a thread to poll the unit
HANDLE StartPollThread(CHopper* Hop); // Method that starts the above thread and returns the handle to it

bool IsPolling = false; // Should the unit be currently polled?
volatile bool PollFailed = false; // Has the poll failed to be sent to the unit?
int PollInterval = 250; // Wait 250ms between polls
const int MAX_PROTOCOL_VERSION = 7; // Max protocol version for SMART Hopper (18/10/11).

int main(int argc, char *argv[])
{
	CHopper* Hop = new CHopper(); // The main class to interface with the unit

	// Start the poll thread, this will not actively poll the unit until the 
	// IsPolling boolean is set to true
	HANDLE hopThread = StartPollThread(Hop);

	// Set output to std::cout
	Hop->SetOutputStream(&std::cout);
	WriteString("C++ SDK\n");

	// Create a command struct to pass to the validator class
	SSP_COMMAND commandStruct;
	commandStruct.BaudRate = 9600;
	commandStruct.Timeout = 1000;
	commandStruct.RetryLevel = 3;
	commandStruct.IgnoreError = 1;

	// Get port number
	std::vector<string> ports = GetComPorts();
	for (unsigned int i = 0; i < ports.size(); ++i)
		WriteString(ports[i]);

	char* end;
	string in = "";
	unsigned int portNum = 0;
	do
	{
		in = GetInputString("Select a port: ");
		portNum = strtol(in.c_str(), &end, 0);
	} while (portNum < 1 || portNum > ports.size());	
	commandStruct.PortNumber = FormatComPort(ports[portNum-1]);

	// Get ssp address
	int ssp = 0;
	do
	{
		in = GetInputString("SSP Address (Default = 16): ");
		ssp = strtol(in.c_str(), &end, 0);
	} while (ssp < 0 || ssp > 32 || end == in.c_str());
	commandStruct.SSPAddress = (unsigned char)ssp;

	// Get protocol version to use
	int p = 0;
	do
	{
		in = GetInputString("Protocol version: ");
		p = atoi(in.c_str());
	} while (p <= 0 || p > MAX_PROTOCOL_VERSION);

	// Now connect to validator passing the connection struct
	if (Hop->ConnectToHopper(commandStruct, p, 5) && Hop->GetUnitType() == 0x03)
	{
		WriteString("Connected to SMART Hopper successfully");
		// Display the available commands
		DisplayCommands();
		// Begin checking input and poll fails
		while  (true)
		{
			if (!ParseKeys(Hop))
			{
				IsPolling = false;
				break; // If false break the loop
			}
			// This bool is set by the poll loop thread
			if (PollFailed)
			{
				cout << "Poll failed" << endl;
				if (Hop->ConnectToHopper(commandStruct, p, 5))
				{
					PollFailed = false;
					StartPollThread(Hop);
				}
				else
					break;
			}
		}
	}
	else if (Hop->GetUnitType() != 0x03)
		WriteString("Incorrect unit detected, this SDK supports the SMART Hopper only");
	else
		WriteString("Failed to connect to the unit");

	cout << "Shutting program down... ";
	TerminateThread(hopThread, 1);
	CloseHandle(hopThread);
	delete Hop;
	cout << "Complete\n\nPress any key to exit..." << endl;
	_getch();
}

// This function searches through com ports 1 to 64 and determines if they
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
		delete[] lpCC;
    }
	return ports;
}

bool ParseKeys(CHopper* hop)
{
	// If a keystroke is detected
	if (_kbhit())
	{
		// Parse the key
		switch (_getch())
		{
		case 'x': return false; 
		case 'e': 
			hop->EnableValidator(); 
			break;
		case 'd':
			hop->DisableValidator(); 
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
				// Report data from the SMART Hopper

				// Level info of coins
				WriteString("Coin Levels:\n");
				hop->OutputLevelInfo();
				cout << endl;
				// Recycling status of coins
				hop->UpdateData(); // update data to be accurate with Hopper
				WriteString("Recycling status of coins:\n");
				for (int i = 0; i < hop->GetNumChannels(); ++i) 
				{
					cout << hop->GetUnitData()[i].Value*0.01f << " " << hop->GetUnitData()[i].Currency[0] <<
						hop->GetUnitData()[i].Currency[1] << hop->GetUnitData()[i].Currency[2] << " is ";
					(hop->GetUnitData()[i].Recycling)?cout << "being sent to storage" << endl:
						cout << "being sent to cashbox" << endl;
				}
				cout << endl;
				break;
			}
		case 'h': DisplayCommands(); break;
		case 't':
			{
				hop->ResetValidator();
				hop->CloseComPort(); // Force reconnection by closing com port
				break;
			}
		case 'a': 
			{
				char choice = GetInputChar("1. Standard payout\n2. Payout by denomination\n");
				if (choice == '1')
				{
					string s = GetInputString("Enter amount to payout and currency E.g. 10.00 EUR: ");

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

					// Convert value to float
					float value = (float)atof(s.c_str()) * 100.0f;

					// Make sure each char in the currency is uppercase and
					// only 3 chars were entered
					if (s1.length() != 3)
					{
						WriteString("Invalid currency");
						break;
					}
					for (int i = 0; i < 3; ++i)
						s1[i] = toupper(s1[i]);

					if (value > 0) // Simple validation on value
					{
						hop->Payout((int)value, s1);
					}
					else
						WriteString("Value was less than or equal to zero");
				}
				else if (choice == '2')
				{
					// Get the dataset info from the validator
					SUnitData* UnitData = hop->GetUnitData();
					// Create a temporary array to hold denomination info
					unsigned char* dataArray = new unsigned char[255];
					int counter = 0, temp = 0, numDenoms = 0;
					// Go through each channel and query the user on how many of each denomination
					// they want to payout.
					for (int i = 0; i < hop->GetNumChannels(); ++i)
					{
						cout << "Number of " << UnitData[i].Value*0.01f << " " << UnitData[i].Currency[0] <<
							UnitData[i].Currency[1] << UnitData[i].Currency[2] << " to payout: ";
						temp = atoi(GetInputString("").c_str());
						if (temp != 0) // if they have entered a number
						{
							++numDenoms; // increase number of denominations to payout
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
						hop->PayoutByDenomination(numDenoms, dataArray);
					delete[] dataArray; // delete temp array once command sent
				}
				else
					WriteString("Invalid choice");
				break;
			}
		case 'f':
			{
				// Get the dataset info from the validator
				SUnitData* UnitData = hop->GetUnitData();
				// Create a temporary array to hold denomination info
				unsigned char* dataArray = new unsigned char[255];
				int counter = 0, temp = 0, numDenoms = 0;
				// Go through each channel and query the user on how many of each denomination
				// they want to payout.
				for (int i = 0; i < hop->GetNumChannels(); ++i)
				{
					cout << "Number of " << UnitData[i].Value*0.01f << " " << UnitData[i].Currency[0] <<
						UnitData[i].Currency[1] << UnitData[i].Currency[2] << " to leave in Hopper: ";
					temp = atoi(GetInputString("").c_str());
					if (temp != 0) // if they have entered a number
					{
						++numDenoms; // increase number of denominations to payout
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
					hop->FloatByDenomination(numDenoms, dataArray);
				delete[] dataArray; // delete temp array once command sent
				break;
			}
		case 'm':
			{
				// Empty has two choices, SMART or just empty
				char choice = GetInputChar("1. Empty\n2. SMART Empty\n");

				if (choice == '1')
				{
					hop->Empty();
				}
				else if (choice == '2')
				{
					hop->SMARTEmpty();
				}
				else
					WriteString("Invalid input");
				break;
			}
		case 'c':
			{
				// Channel recycling
				string s = GetInputString("Enter amount and currency E.g. 0.50 EUR: ");

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

				// Convert value to float
				float value = (float)atof(s.c_str()) * 100.0f;

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

				char choice = GetInputChar("1. Recycle coin\n2. Send coin to cashbox\n");

				if (choice == '1')
					hop->SendCoinToStorage((int)value, s1);
				else if (choice == '2')
					hop->SendCoinToCashbox((int)value, s1);
				else
					WriteString("Invalid choice");
				break;
			}
		case 'l':
			{
				string s = GetInputString("Enter denomination to change level E.g. 1.00 EUR: ");

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

				// Convert value to float
				float value = (float)atof(s.c_str()) * 100.0f;

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

				string s2 = GetInputString("Enter new level: ");

				// Convert to integer
				int newLevel = atoi(s2.c_str());

				if (newLevel < 0 || newLevel > 255)
				{
					WriteString("New level was less than zero or greater than 255");
					break;
				}

				// First set to zero as this is the only way to reset the level
				hop->SetCoinLevel((int)value, s1, 0);
				// Now set to correct amount
				hop->SetCoinLevel((int)value, s1, newLevel);

				break;
			}
		}
	}
	return true;
}

void DisplayCommands()
{
	string s = "Command List\n";
	s += "e = enable\nd = disable\n";
	s += "p = start/stop polling\n";
	s += "c = change channel recycling\n";
	s += "l = set coin levels\n";
	s += "a = make a payout\n";
	s += "f = perform a float by denomination\n";
	s += "m = empty or SMART empty the unit\n";
	s += "t = reset validator\n\n";
	s += "r = report\n";
	s += "h = display this list again\n";
	s += "x = exit program\n";
	WriteString(s);
}

// Function to remove the ?. COM from the string to just leave the number,
// the number is then converted to an unsigned char and returned
unsigned char FormatComPort(const string& portString)
{
	string s = portString;
	s.erase(0, 6);
	return (unsigned char)atoi(s.c_str());
}

// The following section contains threading functions. Threading is used in this program
// as it is vital that the validator is polled at regular intervals. Obviously the execution
// of the main thread of the program needs to be halted occasionally - such as when waiting
// for a user's input, so the polling of the validator needs to occur on a seperate thread
// which will never be halted.

// This function is called to create a new instance of the poll thread. The function returns a
// handle to the thread.
HANDLE StartPollThread(CHopper* Hop)
{
	unsigned int hopThreadID;
	return CreateThread(NULL,
						0,
						PollThread,
						Hop,
						NULL,
						(LPDWORD)&hopThreadID);
}

// This function is responsible for polling the validator at a regular interval.
// It should be run as a seperate thread to the main execution thread.
DWORD WINAPI PollThread(LPVOID params)
{
	// Get validator instance from param
	CHopper* hopper = reinterpret_cast<CHopper*>(params);

	// Keep looping until broken out of
	while (true)
	{
		// If the validator is actively being polled and the thread is not locked
		if (IsPolling)
		{
			// Attempt to send the poll
			if (!hopper->DoPoll())
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