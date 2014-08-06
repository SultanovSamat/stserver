       
namespace eSSP_example
{
    // This class contains a list of definitions of bytes that can be sent or
    // receieved from the validator. 
    class CCommands
    {
        public const byte SSP_CMD_RESET = 0x01;
        public const byte SSP_CMD_HOST_PROTOCOL_VERSION = 0x06;
        public const byte SSP_CMD_POLL = 0x07;
        public const byte SSP_CMD_SYNC = 0x11;
        public const byte SSP_CMD_DISABLE = 0x09;
        public const byte SSP_CMD_ENABLE = 0x0A;
        public const byte SSP_CMD_SET_GENERATOR = 0x4A;
        public const byte SSP_CMD_SET_MODULUS = 0x4B;
        public const byte SSP_CMD_REQUEST_KEY_EXCHANGE = 0x4C;

        public const byte SSP_CMD_SET_INHIBITS = 0x02;
        public const byte SSP_CMD_SETUP_REQUEST = 0x05;
        public const byte SSP_CMD_SET_ROUTING = 0x3B;
        public const byte SSP_CMD_GET_ROUTING = 0x3C;
        public const byte SSP_CMD_PAYOUT_AMOUNT = 0x33;
        public const byte SSP_CMD_GET_COIN_AMOUNT = 0x35;
        public const byte SSP_CMD_SET_COIN_AMOUNT = 0x34;
        public const byte SSP_CMD_HALT_PAYOUT = 0x38;
        public const byte SSP_CMD_FLOAT_AMOUNT = 0x3D;
        public const byte SSP_CMD_GET_MINIMUM_PAYOUT = 0x3E;
        public const byte SSP_CMD_SET_COIN_MECH_INHIBITS = 0x40;
        public const byte SSP_CMD_PAYOUT_BY_DENOMINATION = 0x46;
        public const byte SSP_CMD_FLOAT_BY_DENOMINATION = 0x44;
        public const byte SSP_CMD_SET_COMMAND_CALIBRATION = 0x47;
        public const byte SSP_CMD_RUN_COMMAND_CALIBRATION = 0x48;
        public const byte SSP_CMD_EMPTY_ALL = 0x3F;
        public const byte SSP_CMD_SET_OPTIONS = 0x50;
        public const byte SSP_CMD_GET_OPTIONS = 0x51;
        public const byte SSP_CMD_COIN_MECH_GLOBAL_INHIBIT = 0x49;
        public const byte SSP_CMD_SMART_EMPTY = 0x52;
        public const byte SSP_CMD_CASHBOX_PAYOUT_OP_DATA = 0x53;
        public const byte SSP_CMD_POLL_WITH_ACK = 0x56;
        public const byte SSP_CMD_EVENT_ACK = 0x57;

        public const byte SSP_POLL_RESET = 0xF1;
        public const byte SSP_POLL_DISABLED = 0xE8;
        public const byte SSP_POLL_DISPENSING = 0xDA;
        public const byte SSP_POLL_DISPENSED = 0xD2;
        public const byte SSP_POLL_COINS_LOW = 0xD3;
        public const byte SSP_POLL_EMPTY = 0xD4;
        public const byte SSP_POLL_JAMMED = 0xD5;
        public const byte SSP_POLL_HALTED = 0xD6;
        public const byte SSP_POLL_FLOATING = 0xD7;
        public const byte SSP_POLL_FLOATED = 0xD8;
        public const byte SSP_POLL_TIME_OUT = 0xD9;
        public const byte SSP_POLL_INCOMPLETE_PAYOUT = 0xDC;
        public const byte SSP_POLL_INCOMPLETE_FLOAT = 0xDD;
        public const byte SSP_POLL_CASHBOX_PAID = 0xDE;
        public const byte SSP_POLL_COIN_CREDIT = 0xDF;
        public const byte SSP_POLL_COIN_MECH_JAMMED = 0xC4;
        public const byte SSP_POLL_COIN_MECH_RETURN_PRESSED = 0xC5;
        public const byte SSP_POLL_EMPTYING = 0xC2;
        public const byte SSP_POLL_EMPTIED = 0xC3;
        public const byte SSP_POLL_FRAUD_ATTEMPT = 0xE6;
        public const byte SSP_POLL_SMART_EMPTYING = 0xB3;
        public const byte SSP_POLL_SMART_EMPTIED = 0xB4;
        public const byte SSP_POLL_COIN_ROUTED = 0xE5;

        public const byte SSP_RESPONSE_OK = 0xF0;
        public const byte SSP_RESPONSE_COMMAND_NOT_KNOWN = 0xF2;
        public const byte SSP_RESPONSE_WRONG_NUMBER_OF_PARAMS = 0xF3;
        public const byte SSP_RESPONSE_PARAMS_OUT_OF_RANGE = 0xF4;
        public const byte SSP_RESPONSE_CMD_CANNOT_BE_PROCESSED = 0xF5;
        public const byte SSP_RESPONSE_SOFTWARE_ERROR = 0xF6;
        public const byte SSP_RESPONSE_FAIL = 0xF8;
        public const byte SSP_RESPONSE_KEY_NOT_SET = 0xFA;

    }
}