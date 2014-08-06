#ifndef SSP_LIBRARY
#define SSP_LIBRARY

//generic SSP Responses
typedef enum
{
SSP_RESPONSE_OK = 0xF0,
SSP_RESPONSE_UNKNOWN_COMMAND = 0xF2,
SSP_RESPONSE_INCORRECT_PARAMETERS =  0xF3,
SSP_RESPONSE_INVALID_PARAMETER = 0xF4,
SSP_RESPONSE_COMMAND_NOT_PROCESSED = 0xF5,
SSP_RESPONSE_SOFTWARE_ERROR = 0xF6,
SSP_RESPONSE_CHECKSUM_ERROR = 0xF7,
SSP_RESPONSE_FAILURE = 0xF8,
SSP_RESPONSE_HEADER_FAILURE = 0xF9,
SSP_RESPONSE_KEY_NOT_SET = 0xFA,
SSP_RESPONSE_TIMEOUT = 0xFF,
} SSP_RESPONSE_ENUM;

/* command status enumeration */
typedef enum{
	PORT_CLOSED,
	PORT_OPEN,
	PORT_ERROR,
	SSP_REPLY_OK,
	SSP_PACKET_ERROR,
	SSP_CMD_TIMEOUT,
}PORT_STATUS;

typedef struct{
	unsigned long long FixedKey;
	unsigned long long EncryptKey;
}SSP_FULL_KEY;

typedef struct{
	SSP_FULL_KEY Key;
	unsigned long BaudRate;
	unsigned long Timeout;
	unsigned char PortNumber;
	unsigned char SSPAddress;
	unsigned char RetryLevel;
	unsigned char EncryptionStatus;
	unsigned char CommandDataLength;
	unsigned char CommandData[255];
	unsigned char ResponseStatus;
	unsigned char ResponseDataLength;
	unsigned char ResponseData[255];
	unsigned char IgnoreError;
}SSP_COMMAND;

typedef struct{
	unsigned short packetTime;
	unsigned char PacketLength;
	unsigned char PacketData[255];
}SSP_PACKET;

typedef struct{
	unsigned char* CommandName;
	unsigned char* LogFileName;
	unsigned char Encrypted;
	SSP_PACKET Transmit;
	SSP_PACKET Receive;
	SSP_PACKET PreEncryptTransmit;
	SSP_PACKET PreEncryptRecieve;
}SSP_COMMAND_INFO;

typedef struct{ 
    unsigned __int64 Generator;
    unsigned __int64 Modulus;
    unsigned __int64 HostInter;
    unsigned __int64 HostRandom;
    unsigned __int64 SlaveInterKey;
    unsigned __int64 SlaveRandom;
    unsigned __int64 KeyHost;
    unsigned __int64 KeySlave;
}SSP_KEYS;

typedef struct
{
	char* Port;
	char SSPAddress, ProtocolVersion, RetryLevel;
	long BaudRate, TimeOut;
} SSP_CONNECTION_INFO;

// others
#ifndef CALLBACK
#define CALLBACK __stdcall
#endif

#ifndef UINT
#define UINT unsigned int
#endif

#ifndef null
#define null 0
#endif

#endif

#ifndef CALLBACK_FUNCTIONS
#define CALLBACK_FUNCTIONS
typedef UINT (CALLBACK* LPFNDLLFUNC1)(SSP_COMMAND* cmd);
typedef UINT (CALLBACK* LPFNDLLFUNC2)(void);
typedef UINT (CALLBACK* LPFNDLLFUNC3)(SSP_COMMAND* cmd, SSP_COMMAND_INFO* sspInfo);
typedef UINT (CALLBACK* LPFNDLLFUNC4)(SSP_KEYS* key, SSP_COMMAND* cmd);
typedef UINT (CALLBACK* LPFNDLLFUNC5)(SSP_KEYS* key);
#endif

#ifndef PAUSE
#define PAUSE _getch();
#endif

#ifndef S_CHANNEL_DATA
#define S_CHANNEL_DATA
struct SChannelData
{
	unsigned char Channel;
	unsigned int Value;
	char* Currency;
	SChannelData() 
	: Channel(0), Value(0)
	{
		Currency = new char[3];
	}
	~SChannelData()
	{ 
		delete[] Currency; 
	}
};
#endif