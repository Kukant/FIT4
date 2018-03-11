/**
 * Created by Tomas Kukan, xkukan00, 11. 3. 2018
 */

#ifndef INC_1_SHARED_H
#define INC_1_SHARED_H

#include <stdio.h>
#include <stdbool.h>

// macros

#define BUFFER_SIZE 1024
#define SH_TABLE_ROWS 64

#define SERVER_HI "Hello there."
#define SERVER_OK "OK"
#define SERVER_ERR "Error on server side."

#define CLIENT_WRITE "SEND"
#define CLIENT_READ "READ"
#define CLIENT_ERR "Error on client side."
#define CLIENT_OK "COK"

#define FILE_RECEIVED "File received."
#define FILE_ERR "Err"


// macro-functions

#define DEBUG 0
#define debug_print(...) \
            do { if (DEBUG) fprintf(stderr,__VA_ARGS__); } while (0)

struct packet_info {
    long packet_size;
    bool eof;
};

/**
 * Receive file.
 * @param target_socket
 * @param fw
 * @return
 */
int receive_file(int target_socket, FILE *fw);

/**
 * Send file
 * @param target_socket
 * @param fr
 * @return
 */
int send_file(int target_socket, FILE *fr);

/**
 * Send one packet.
 * @param target_socket
 * @param buffer
 * @param bytes
 * @param eof
 * @return
 */
int send_packet(int target_socket,char *buffer, long bytes, bool eof);

/**
 * Receive one packet.
 * @param in_socket
 * @param buffer
 * @param eof
 * @return
 */
long recv_packet(int in_socket, char *buffer, bool *eof);


#endif //INC_1_SHARED_H
