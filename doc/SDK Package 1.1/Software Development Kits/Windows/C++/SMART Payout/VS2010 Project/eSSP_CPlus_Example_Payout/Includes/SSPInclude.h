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
	unsigned char* Port;
	unsigned char SSPAddress, ProtocolVersion, RetryLevel;
	unsigned long BaudRate, TimeOut;
} SSP_CONNECTION_INFO;

#define NO_ENCRYPTION 0
#define ENCRYPTION_SET 1

#define SSP_CMD_SET_ROUTING 0x3B
#define SSP_CMD_SYNC 0x11
#define SSP_CMD_HOST_PROTOCOL 0x06
#define SSP_CMD_SETUP_REQUEST 0x05
#define SSP_CMD_ENABLE 0x0A
#define SSP_CMD_ENABLE_PAYOUT_DEVICE 0x5C
#define SSP_CMD_SET_INHIBITS 0x02
#define SSP_CMD_POLL 0x07
#define SSP_CMD_RESET 0x01
#define SSP_CMD_DISABLE_PAYOUT_DEVICE 0x5B
#define SSP_CMD_DISABLE 0x09
#define SSP_CMD_PAYOUT_BY_DENOMINATION 0x46

#define SSP_POLL_RESET 0xF1
#define SSP_POLL_READ 0xEF //next byte is channel (0 for unknown)
#define SSP_POLL_CREDIT 0xEE //next byte is channel
#define SSP_POLL_INCOMPLETE_PAYOUT 0xDC
#define SSP_POLL_INCOMPLETE_FLOAT 0xDD
#define SSP_POLL_REJECTING 0xED
#define SSP_POLL_REJECTED 0xEC
#define SSP_POLL_STACKING 0xCC
#define SSP_POLL_STACKED 0xEB
#define SSP_POLL_SAFE_JAM 0xEA
#define SSP_POLL_UNSAFE_JAM 0xE9
#define SSP_POLL_DISABLED 0xE8
#define SSP_POLL_FRAUD_ATTEMPT 0xE6 //next byte is channel
#define SSP_POLL_STACKER_FULL 0xE7
#define SSP_POLL_CASH_BOX_REMOVED   0xE3
#define SSP_POLL_CASH_BOX_REPLACED  0xE4
#define SSP_POLL_CLEARED_FROM_FRONT 0xE1
#define SSP_POLL_CLEARED_INTO_CASHBOX 0xE2

#define SSP_CMD_SET_GENERATOR 0x4A
#define SSP_CMD_SET_MODULUS 0x4B
#define SSP_CMD_KEY_EXCHANGE 0x4C

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

#ifndef S_UNIT_DATA
#define S_UNIT_DATA

struct SUnitData
{
	int Channel, Value;
	char* Currency;
	bool Recycling;
	SUnitData() : Channel(0), Value(0), Recycling(false)
	{
		Currency = new char[3];
	}
	~SUnitData(){ delete[] Currency; }
	SUnitData& operator=(const SUnitData& rhs)
	{
		this->Channel = rhs.Channel;
		this->Value = rhs.Value;
		this->Recycling = rhs.Recycling;
		Currency = new char[3];
		Currency[0] = rhs.Currency[0];
		Currency[1] = rhs.Currency[1];
		Currency[2] = rhs.Currency[2];
		return *this;
	}
};

#endif

