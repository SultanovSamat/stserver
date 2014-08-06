#ifndef _AES_H_
#define _AES_H_

#include "..\include\ssd_types.h"

// maximum key length (in bytes)
#define C_MAX_KEY_LENGTH 16

// number of rounds
#define C_NUMBER_ROUNDS 10

// special words
#define C_WORD_ALL    0xffffffffUL
#define C_WORD_MSB    0x80000000UL
#define C_WORD_ZERO   0x00000000UL
#define C_WORD_ONE    0x00000001UL

// NULL pointer
#ifndef NULL
#define NULL ((void *)0)
#endif

  typedef signed long long   SINT64;
  typedef signed long        SINT32;
  typedef signed short       SINT16;
  typedef signed char        SINT8;

  typedef char*              STRING;

typedef struct
{
    UINT32 enc_round_keys[44];   /* encryption round keys         */
} aes_context;


/* F-AES/100: aes_encrypt */
void aes_encrypt( const UINT8  *aes_key,               // pointer to key
                        UINT8  *plain_data,            // pointer to data to encrypt
                        UINT8  *cipher_data,           // pointer to encrypted data (might be same as plain_data)
                  const UINT32  data_length );         // length of data to encrypt in bytes (must be a multiple of 16)

/* F-AES/120: aes_decrypt */
void aes_decrypt( const UINT8  *aes_key,               // pointer to key
                        UINT8  *plain_data,            // pointer to decrypted data
                        UINT8  *cipher_data,           // pointer to encrypted data to decrypt (might be same as plain_data)
                  const UINT32  data_length );         // length of data to decrypt in bytes (must be a multiple of 16)

#endif

