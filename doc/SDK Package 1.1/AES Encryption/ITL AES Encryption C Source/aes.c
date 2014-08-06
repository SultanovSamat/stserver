#pragma ghs section text=".aes"

#include "include\aes.h"

// concatenates 4 × 8-bit words (= 1 byte) to one 32-bit word
#define CONCAT_4_BYTES( w32, w8, w8_i)           \
{                                                \
  (w32) = ( (UINT32) (w8)[(w8_i)    ] << 24 ) |  \
          ( (UINT32) (w8)[(w8_i) + 1] << 16 ) |  \
          ( (UINT32) (w8)[(w8_i) + 2] <<  8 ) |  \
          ( (UINT32) (w8)[(w8_i) + 3]       );   \
}


// splits a 32-bit word into 4 × 8-bit words (= 1 byte)
#define SPLIT_INTO_4_BYTES( w32, w8, w8_i)       \
{                                                \
  (w8)[(w8_i)    ] = (UINT8) ( (w32) >> 24 );    \
  (w8)[(w8_i) + 1] = (UINT8) ( (w32) >> 16 );    \
  (w8)[(w8_i) + 2] = (UINT8) ( (w32) >>  8 );    \
  (w8)[(w8_i) + 3] = (UINT8) ( (w32)       );    \
}


// get x-th byte of 32 bit word
#define BYTE_0(w32) ( (UINT8) (w32 >> 24) )   
#define BYTE_1(w32) ( (UINT8) (w32 >> 16) )
#define BYTE_2(w32) ( (UINT8) (w32 >>  8) )
#define BYTE_3(w32) ( (UINT8) (w32      ) )  


#define FORWARD_SUB_BYTE(input) forward_S_box[(input)] 
#define INVERSE_SUB_BYTE(input) inverse_S_box[(input)]

// define the GF2^8 irreducible field polynomial 0x11B = x^8 + x^4 + x^3 + x + 1
#define GF2_8_FIELD_POLYNOMIAL 0x1B

// forward S-box = SubBytes() transformation
static const UINT8 forward_S_box[256] =
{
  0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5,
  0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
  0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0,
  0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
  0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC,
  0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
  0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A,
  0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
  0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0,
  0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
  0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B,
  0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
  0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85,
  0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
  0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5,
  0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
  0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17,
  0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
  0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88,
  0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
  0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C,
  0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
  0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9,
  0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
  0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6,
  0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
  0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E,
  0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
  0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94,
  0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
  0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68,
  0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16
};

// inverse S-box = InvSubBytes() transformation
static const UINT8 inverse_S_box[256] =
{
  0x52, 0x09, 0x6A, 0xD5, 0x30, 0x36, 0xA5, 0x38,
  0xBF, 0x40, 0xA3, 0x9E, 0x81, 0xF3, 0xD7, 0xFB,
  0x7C, 0xE3, 0x39, 0x82, 0x9B, 0x2F, 0xFF, 0x87,
  0x34, 0x8E, 0x43, 0x44, 0xC4, 0xDE, 0xE9, 0xCB,
  0x54, 0x7B, 0x94, 0x32, 0xA6, 0xC2, 0x23, 0x3D,
  0xEE, 0x4C, 0x95, 0x0B, 0x42, 0xFA, 0xC3, 0x4E,
  0x08, 0x2E, 0xA1, 0x66, 0x28, 0xD9, 0x24, 0xB2,
  0x76, 0x5B, 0xA2, 0x49, 0x6D, 0x8B, 0xD1, 0x25,
  0x72, 0xF8, 0xF6, 0x64, 0x86, 0x68, 0x98, 0x16,
  0xD4, 0xA4, 0x5C, 0xCC, 0x5D, 0x65, 0xB6, 0x92,
  0x6C, 0x70, 0x48, 0x50, 0xFD, 0xED, 0xB9, 0xDA,
  0x5E, 0x15, 0x46, 0x57, 0xA7, 0x8D, 0x9D, 0x84,
  0x90, 0xD8, 0xAB, 0x00, 0x8C, 0xBC, 0xD3, 0x0A,
  0xF7, 0xE4, 0x58, 0x05, 0xB8, 0xB3, 0x45, 0x06,
  0xD0, 0x2C, 0x1E, 0x8F, 0xCA, 0x3F, 0x0F, 0x02,
  0xC1, 0xAF, 0xBD, 0x03, 0x01, 0x13, 0x8A, 0x6B,
  0x3A, 0x91, 0x11, 0x41, 0x4F, 0x67, 0xDC, 0xEA,
  0x97, 0xF2, 0xCF, 0xCE, 0xF0, 0xB4, 0xE6, 0x73,
  0x96, 0xAC, 0x74, 0x22, 0xE7, 0xAD, 0x35, 0x85,
  0xE2, 0xF9, 0x37, 0xE8, 0x1C, 0x75, 0xDF, 0x6E,
  0x47, 0xF1, 0x1A, 0x71, 0x1D, 0x29, 0xC5, 0x89,
  0x6F, 0xB7, 0x62, 0x0E, 0xAA, 0x18, 0xBE, 0x1B,
  0xFC, 0x56, 0x3E, 0x4B, 0xC6, 0xD2, 0x79, 0x20,
  0x9A, 0xDB, 0xC0, 0xFE, 0x78, 0xCD, 0x5A, 0xF4,
  0x1F, 0xDD, 0xA8, 0x33, 0x88, 0x07, 0xC7, 0x31,
  0xB1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xEC, 0x5F,
  0x60, 0x51, 0x7F, 0xA9, 0x19, 0xB5, 0x4A, 0x0D,
  0x2D, 0xE5, 0x7A, 0x9F, 0x93, 0xC9, 0x9C, 0xEF,
  0xA0, 0xE0, 0x3B, 0x4D, 0xAE, 0x2A, 0xF5, 0xB0,
  0xC8, 0xEB, 0xBB, 0x3C, 0x83, 0x53, 0x99, 0x61,
  0x17, 0x2B, 0x04, 0x7E, 0xBA, 0x77, 0xD6, 0x26,
  0xE1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0C, 0x7D
};

// round constants = [2^i in GF(2^8)]
static const UINT8 rcon[10] =
{
  0x01, 0x02, 0x04, 0x08, 0x10,
  0x20, 0x40, 0x80, 0x1B, 0x36
};

void mem_copy(UINT8* dest, const UINT8* source) 
{
  // declarations
  int i;
  
  // copy 16 bytes
  for ( i = 0; i < 16; i++ ) 
    dest[i] = source[i];
}

static UINT8 GF2_8_field_mult_by_2( UINT8 a )
{  
  // declarations 
  UINT8 r[2];

  // left shift = mult by 0x02 
  r[0] = ( a << 1 );                     
  
  // if MSB  was 1 then reduce by field polynomial
  r[1] = r[0] ^ GF2_8_FIELD_POLYNOMIAL; 
  
  // this is for SPA resistance
  r[0] = r[(a & 0x80) != 0x0];
  
  // return result
  return r[0];
}

static UINT32 forward_mix_col(UINT32 state_in)
{                                                                      
  // declarations    
  UINT32 state_out;
  UINT8 t, v;
   
  // t = a[0] + a[1] + a[2] + a[3]
  t = BYTE_0( state_in ) ^ BYTE_1( state_in ) ^ BYTE_2( state_in ) ^ BYTE_3( state_in );
  
  // v = a[0] + a[1]
  // v = x*v
  // r[0] = a[0] + v + t
  v = BYTE_0 ( state_in ) ^ BYTE_1( state_in );
  v = GF2_8_field_mult_by_2( v );
  state_out = ( BYTE_0( state_in ) ^ v ^ t) << 24;
  
  // v = a[1] + a[2]
  // v = x*v
  // r[1] = a[1] + v + t
  v = BYTE_1( state_in ) ^ BYTE_2( state_in );
  v = GF2_8_field_mult_by_2( v ); 
  state_out ^= ( BYTE_1( state_in ) ^ v ^ t) << 16;
 
  // v = a[2] + a[3]
  // v = x*v
  // r[2] = a[2] + v + t 
  v = BYTE_2( state_in ) ^ BYTE_3( state_in );
  v = GF2_8_field_mult_by_2( v ); 
  state_out ^= ( BYTE_2( state_in ) ^ v ^ t) << 8;
 
  // v = a[3] + a[0]
  // v = x*v
  // r[3] = a[3] + v + t
  v = BYTE_3( state_in ) ^ BYTE_0( state_in );
  v = GF2_8_field_mult_by_2( v );  
  state_out ^= ( BYTE_3( state_in ) ^ v ^ t);       
  
  // return result
  return state_out;
}

static UINT32 inverse_mix_col(UINT32 state_in)
{                                          
  // declarations    
  UINT32 state_out;  
  UINT8 u, v;
 
  // u = x*(x*(a[0] + a[2]))
  u = GF2_8_field_mult_by_2( BYTE_0(state_in) ^ BYTE_2(state_in) );
  u = GF2_8_field_mult_by_2( u );
 
  // v = v*(v*(a[1] + a[3]))
  v = GF2_8_field_mult_by_2( BYTE_1(state_in) ^ BYTE_3(state_in) );
  v = GF2_8_field_mult_by_2( v );
 
  // a[0] = a[0] + u
  // a[1] = a[1] + v
  // a[2] = a[2] + u
  // a[3] = a[3] + v
  state_out = ( ( (BYTE_0(state_in) ^ u) << 24) ^
                ( (BYTE_1(state_in) ^ v) << 16) ^
                ( (BYTE_2(state_in) ^ u) <<  8) ^
                ( (BYTE_3(state_in) ^ v)      ) );
 
  // MixCol( a )
  state_out = forward_mix_col( state_out );
  
  // return result            
  return state_out;
}

static int aes_set_key(aes_context *ctx,
                        const UINT8       *key)
{
  // declarations
  int i;
  UINT32 *enc_round_key;
  
  // get pointer to encryption round keys
  enc_round_key = ctx->enc_round_keys;

  // enc_round_key[0..3] is the original key 
  for ( i = 0; i < 4; i++ )
  {
    CONCAT_4_BYTES( enc_round_key[i], key, i * 4 );
  }
  
  // derive enc_round_key[4..43] from enc_round_key[0..3]
  for ( i = 0; i < 10; i++ )
  {
    // enc_round_key[i mod 4 == 0] = .. where i = this i * 4 (means i from FIPS-197 Fig.11)
    enc_round_key[4]  = (  enc_round_key[0]                                                         ) ^ /* 32-bit w[i-4]                                                        */
                        ( (FORWARD_SUB_BYTE( (int)( BYTE_1( enc_round_key[3] ) ) ) ^ rcon[i]) << 24 ) ^ /* 32-bit { S-box( enc_round_key[i-1]-a1 ),                             */
                        (  FORWARD_SUB_BYTE( (int)( BYTE_2( enc_round_key[3] ) ) )            << 16 ) ^ /*          S-box( enc_round_key[i-1]-a2 ),                             */
                        (  FORWARD_SUB_BYTE( (int)( BYTE_3( enc_round_key[3] ) ) )            <<  8 ) ^ /*          S-box( enc_round_key[i-1]-a3 ),                             */
                        (  FORWARD_SUB_BYTE( (int)( BYTE_0( enc_round_key[3] ) ) )                  );  /*          S-box( enc_round_key[i-1]-a0 ) } = SubWord(RotWord(w[i-1])) */
                            
    // enc_round_key[i mod 4 != 0] = w[i-4] XOR w[i-1], where i = this i * 4 (means i from FIPS-197 Fig.11)
    enc_round_key[5]  = enc_round_key[1] ^ enc_round_key[4];
    enc_round_key[6]  = enc_round_key[2] ^ enc_round_key[5];
    enc_round_key[7]  = enc_round_key[3] ^ enc_round_key[6];

    // next 4 32-bit words
    enc_round_key += 4;
  }
  // break;
 
  // successful
  return  0;
}

static int aes_encrypt_16_byte_block( const aes_context *ctx,
                                      const UINT8       *plain,
                                  /*@out@*/ UINT8       *cipher )
{
  // declarations
  UINT32 *round_key;          // pointer to round key
  UINT32 cx0, cx1, cx2, cx3;  // state array columns, cx for input
  UINT32 cy0, cy1, cy2, cy3;  // state array columns, cy for output
  UINT8 i;                    // counter for encryption loop
      
  // get encryption round keys
  round_key = (UINT32 *) ctx->enc_round_keys;

  // read 16 plain text bytes into four 32-bit words (FIPS-197: state = in )
  CONCAT_4_BYTES( cx0, plain,  0 );
  CONCAT_4_BYTES( cx1, plain,  4 );
  CONCAT_4_BYTES( cx2, plain,  8 );
  CONCAT_4_BYTES( cx3, plain,  12 ); 
   
  // XOR it with encryption round_key[0..3] (FIPS-197: AddRoundKey(state))
  cx0 ^= round_key[0];
  cx1 ^= round_key[1];
  cx2 ^= round_key[2];
  cx3 ^= round_key[3];
     
   // do encryption rounds 1..9
   // forward S-box, ShiftRows() via input structure, MixCol, XOR round_key
  round_key+=4;
  i = 0;
  while ( i < 9 )
  {
    cy0 = round_key[0] ^ forward_mix_col( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx0 ) ) ) ) << 24 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx1 ) ) ) ) << 16 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx2 ) ) ) ) <<  8 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx3 ) ) ) )       ) );
     
    cy1 = round_key[1] ^ forward_mix_col( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx1 ) ) ) ) << 24 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx2 ) ) ) ) << 16 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx3 ) ) ) ) <<  8 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx0 ) ) ) )       ) );
 
    cy2 = round_key[2] ^ forward_mix_col( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx2 ) ) ) ) << 24 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx3 ) ) ) ) << 16 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx0 ) ) ) ) <<  8 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx1 ) ) ) )       ) );

    cy3 = round_key[3] ^ forward_mix_col( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx3 ) ) ) ) << 24 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx0 ) ) ) ) << 16 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx1 ) ) ) ) <<  8 ) ^
                                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx2 ) ) ) )       ) );
                       
    round_key += 4;
    i++;
  
    // copy cy --> cx for input for next round
    cx0 = cy0;
    cx1 = cy1;
    cx2 = cy2;
    cx3 = cy3;  
  } 
    
  // final forward S-box round inluding ShiftRows() via input structure
  cy0 = round_key[0] ^ ( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx0 ) ) ) ) << 24 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx1 ) ) ) ) << 16 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx2 ) ) ) ) <<  8 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx3 ) ) ) )       ) );

  cy1 = round_key[1] ^ ( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx1 ) ) ) ) << 24 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx2 ) ) ) ) << 16 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx3 ) ) ) ) <<  8 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx0 ) ) ) )       ) );

  cy2 = round_key[2] ^ ( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx2 ) ) ) ) << 24 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx3 ) ) ) ) << 16 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx0 ) ) ) ) <<  8 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx1 ) ) ) )       ) );

  cy3 = round_key[3] ^ ( ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_0( cx3 ) ) ) ) << 24 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_1( cx0 ) ) ) ) << 16 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_2( cx1 ) ) ) ) <<  8 ) ^
                          ( ( (UINT32) (FORWARD_SUB_BYTE( (int) BYTE_3( cx2 ) ) ) )       ) );
      
  // write result into cipher text byte array (FIPS 197: out = state )
  SPLIT_INTO_4_BYTES( cy0, cipher,  0 );
  SPLIT_INTO_4_BYTES( cy1, cipher,  4 );
  SPLIT_INTO_4_BYTES( cy2, cipher,  8 );
  SPLIT_INTO_4_BYTES( cy3, cipher, 12 );
  
  // successful 
  return 0;
}

static int aes_decrypt_16_byte_block( const aes_context *ctx,
                                      const UINT8       *cipher,
                                  /*@out@*/ UINT8       *plain )
{
  // declarations
  UINT32 *round_key;           // pointer to round key
  UINT32 cx0, cx1, cx2, cx3;   // state array columns, cx for input
  UINT32 cy0, cy1, cy2, cy3;   // state array columns, cy for output
  int i;                       // counter for decryption loop
  
  // get decryption round keys --> changed: get encryption round keys
  round_key = (UINT32 *) ctx->enc_round_keys;
  round_key += 40;

  // read 16 cipher text bytes into four 32-bit words (FIPS-197: state = in )
  CONCAT_4_BYTES( cx0, cipher,  0 ); 
  CONCAT_4_BYTES( cx1, cipher,  4 );
  CONCAT_4_BYTES( cx2, cipher,  8 );
  CONCAT_4_BYTES( cx3, cipher, 12 );

  // XOR it with enc_round_key[43..40]
  cx0 ^= round_key[0];
  cx1 ^= round_key[1];  
  cx2 ^= round_key[2];
  cx3 ^= round_key[3];  
 
  // do decryption rounds 9..1
  // inverse S-box, inverse ShiftRows() via input structure, inverseMixCol, XOR round_key
  i = 0; 
  round_key -= 4;  
  while ( i < 9 )
  {     	
    cy0 = inverse_mix_col( round_key[0] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0( cx0 ) ) ) << 24 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1( cx3 ) ) ) << 16 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2( cx2 ) ) ) <<  8 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3( cx1 ) ) )       ) ) );
    
    cy1 = inverse_mix_col( round_key[1] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0( cx1 ) ) ) << 24 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1( cx0 ) ) ) << 16 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2( cx3 ) ) ) <<  8 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3( cx2 ) ) )       ) ) );
 
    cy2 = inverse_mix_col( round_key[2] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0 ( cx2 ) ) ) << 24 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1 ( cx1 ) ) ) << 16 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2 ( cx0 ) ) ) <<  8 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3 ( cx3 ) ) )       ) ) );

    cy3 = inverse_mix_col( round_key[3] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0 ( cx3 ) ) ) << 24 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1 ( cx2 ) ) ) << 16 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2 ( cx1 ) ) ) <<  8 ) ^
                                            ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3 ( cx0 ) ) )       ) ) );
                        
     i++;
     round_key -= 4;
     
     // copy cy --> cx for input for next round 
     cx0 = cy0;
     cx1 = cy1;
     cx2 = cy2;
     cx3 = cy3;  
  }       
 
  // final round: 
  cy0 = round_key[0] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0( cx0 ) ) ) << 24 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1( cx3 ) ) ) << 16 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2( cx2 ) ) ) <<  8 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3( cx1 ) ) )       ) );

  cy1 = round_key[1] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0( cx1 ) ) ) << 24 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1( cx0 ) ) ) << 16 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2( cx3 ) ) ) <<  8 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3( cx2 ) ) )       ) );
                           
  cy2 = round_key[2] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0( cx2 ) ) ) << 24 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1( cx1 ) ) ) << 16 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2( cx0 ) ) ) <<  8 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3( cx3 ) ) )       ) );
                           
  cy3 = round_key[3] ^ ( ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_0( cx3 ) ) ) << 24 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_1( cx2 ) ) ) << 16 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_2( cx1 ) ) ) <<  8 ) ^
                         ( (UINT32) INVERSE_SUB_BYTE( (int)( BYTE_3( cx0 ) ) )       ) );  
  
  // write result into plain text byte array
  SPLIT_INTO_4_BYTES( cy0, plain,  0 );
  SPLIT_INTO_4_BYTES( cy1, plain,  4 );
  SPLIT_INTO_4_BYTES( cy2, plain,  8 );
  SPLIT_INTO_4_BYTES( cy3, plain, 12 );

  // successful
  return 0;
  
}

extern void aes_encrypt(  const UINT8  *aes_key,
                                UINT8  *plain_data,
                      /*@out@*/ UINT8  *cipher_data,
                          const UINT32  data_length )
{
  // declarations
  aes_context aes_ctx; // aes context
  UINT8 aes_buf[16];   // temporary buffer of one block
  UINT32 num_blocks;   // number of blocks to encrypt
  UINT8 *p_plain;      // pointer to plain text block
  UINT8 *p_cipher;     // pointer to cipher text block
  UINT32 i;
  int ret;

  // init encryption keys
  ret = aes_set_key( &aes_ctx, aes_key);
  

  // get number of blocks and padding
  num_blocks = (UINT32) ( data_length / 16 );

  // get pointers
  p_plain  = plain_data;
  p_cipher = cipher_data;

  // --- Electronic Codebook Mode (ECB) -------------------------------------

    // encrypt all 16 byte blocks
    for ( i = 0; i < num_blocks; i++ )
    {
      // fill buffer with plain
      mem_copy( aes_buf, p_plain );

      // encrypt buffer
      ret = aes_encrypt_16_byte_block( &aes_ctx, aes_buf, aes_buf );
    
      
      // write cipher
      mem_copy( p_cipher, aes_buf );
      
      // next block
      p_plain  += 16;
      p_cipher += 16;
    }
}

extern void aes_decrypt( const UINT8  *aes_key,
                                UINT8  *plain_data,
                                UINT8  *cipher_data,
                          const UINT32  data_length )
{
  // declarations
  aes_context aes_ctx; // aes context
  UINT8 aes_buf1[16];  // buffer for one 16-byte block
  UINT8 aes_buf2[16];  // buffer for one 16-byte block
  UINT32 num_blocks;   // number of blocks to decrypt
  UINT8 *p_plain;      // pointer to plain data block
  UINT8 *p_cipher;     // pointer to cipher data block
  UINT32 i;
  int ret; 
  
  // init encryption keys
  ret = aes_set_key( &aes_ctx, aes_key);
  

  // get number of blocks and padding
  num_blocks = (UINT32) ( data_length / 16 );

  // get pointers
  p_plain  = plain_data;
  p_cipher = cipher_data;

  // --- Electronic Codebook Mode (ECB) -------------------------------------

    // decrypt 16 byte blocks
    for ( i = 0; i < num_blocks; i++ )
    {
      // fill buffer 1 with cipher
      mem_copy( aes_buf1, p_cipher );
      
      // decrypt buffer 1
      ret = aes_decrypt_16_byte_block( &aes_ctx, aes_buf1, aes_buf1 );
      
      
      // write buffer 1 to plain
      mem_copy( p_plain, aes_buf1 );
      
      // next block
      p_plain  += 16;
      p_cipher += 16;
    }
}
