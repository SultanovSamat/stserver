#include <vector>
#include "IO.h"
#include "CValidator.h"

// Function headers
std::vector<string> GetComPorts(); // Returns a vector of available ports on the host
bool ParseKeys(CValidator* nv); // The method used to parse and deal with key presses
void DisplayCommands(); // Displays the available commands for this unit
unsigned char FormatComPort(const string& portString); // Removes the ?. COM from the COM port string

// Checks whether this unit is the right type for this SDK, this should only be called
// after setup request.
bool IsUnitValid(CValidator* nv);

// Global vars
bool IsPolling = false;
const int MIN_SUPPORTED_PROTOCOL = 6; // This SDK does not support a protocol < 6
const int MAX_SUPPORTED_PROTOCOL = 7; // The maximum supported protocol by a unit of this type (07/09/12).

int main(int argc, char *argv[])
{
	CValidator* Validator = new CValidator(); // The main class used to interface with the validator
	Validator->SetOutputStream(&cout); // Set output to std::cout
	int pollInterval = 250; // Set interval between polls
	WriteString("C++ Note Validator SDK\n\nSearching for ports...\n");

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
	unsigned int choice = 0;
	string in;
	do
	{
		in = GetInputString("Select a port: ");
		choice = strtol(in.c_str(), &end, 10);	
	}
	while(choice <= 0 || choice > ports.size());
	
	commandStructure.PortNumber = FormatComPort(ports[choice-1]);
		
	// Get ssp address
	unsigned int ssp = 0;
	do
	{
		in = GetInputString("SSP Address (Default = 0): ");
		ssp = strtol(in.c_str(), &end, 0);
	}
	while (ssp < 0 || ssp > 32 || end == in.c_str());
	commandStructure.SSPAddress = (unsigned char)ssp;

	// Get protocol version to use
	int p;
	do
	{
		in = GetInputString("Protocol version: ");
		p = strtol(in.c_str(), &end, 0);
	}
	while (p < MIN_SUPPORTED_PROTOCOL || p > MAX_SUPPORTED_PROTOCOL);

	// Now connect to validator
	if (Validator->ConnectToValidator(commandStructure, p, 5))
	{
		WriteString("Connected to validator successfully");
		// After connection check to make sure the unit is supported
		if (!IsUnitValid(Validator))
		{
			WriteString("This program supports note validators only\nPlease connect a note validator and restart...");
			PAUSE
			delete Validator;
			exit(0);
		}
		// Display the available commands
		DisplayCommands();
		// Begin the poll loop
		while  (true)
		{
			// If the validator is supposed to be polling but fails
			// a poll, attempt to reconnect
			if (IsPolling && !Validator->DoPoll())
			{
				if (!Validator->ConnectToValidator(commandStructure, p, 5)) break;
			}
			if (!ParseKeys(Validator)) break; // If false break the loop
			Sleep(pollInterval); // Wait for the specified interval
		}

		WriteString("Poll loop stopped\n");
	}
	WriteString("Press any key to exit...");
	PAUSE
	delete Validator;
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
		delete[] lpCC;	

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

// This function detects a keyboard hit and then parses the keystroke.
bool ParseKeys(CValidator* nv)
{
	// If a keystroke is detected
	if (_kbhit())
	{
		char c = _getch(); // get the key

		// Parse the key
		switch (c)
		{
		case 'x': return false;
		case 'e': nv->EnableValidator(); break;
		case 'd': nv->DisableValidator(); break;
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
				cout << "Number of notes accepted: " << nv->GetNumNotesStacked() << endl;
				break;
			}
		case 'h': DisplayCommands(); break;
		case 't': 
			nv->ResetValidator();
			nv->CloseComPort();
			break;
		default: break;
		}
	}
	return true;
}

// Simply displays the list of user commands
void DisplayCommands()
{
	string s = "Command List\n";
	s += "e = enable\nd = disable\n";
	s += "p = start/stop polling\n";
	s += "t = reset validator\n\n";
	s += "r = report\n";
	s += "h = display this list again\n";
	s += "x = exit\n";
	WriteString(s);
}

bool IsUnitValid(CValidator* nv)
{
	// This program only works with note validators, type 0
	if (nv->GetUnitType() != 0x00)
		return false;
	return true;
}

// Remove the ?. COM from the start of the COM port string and return an
// unsigned char which can be directly set in the command structure.
unsigned char FormatComPort(const string& portString)
{
	string s = portString;
	s.erase(0, 6);
	return (unsigned char)atoi(s.c_str());
}