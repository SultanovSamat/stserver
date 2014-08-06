#ifndef INPUT_H
#define INPUT_H

#include <iostream>
#include <string>
#include <conio.h>

using namespace std;

// Returns a char based on a request
char GetInputChar(const string& request = "")
{
	cout << request;
	return _getch();
}

// Returns a string based on a request
string GetInputString(const string& request = "")
{
	cout << request;
	string s;
	getline(cin, s);
	return s;
}

// Writes a string out to the console window
void WriteString(const string& msg)
{
	cout << msg << endl;
}

#endif