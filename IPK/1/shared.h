//
// Created by zeusko on 06/03/18.
//

#ifndef INC_1_SHARED_H
#define INC_1_SHARED_H

#include <stdio.h>

// macros

#define BUFFER_SIZE 1024
#define SH_TABLE_ROWS 64

#define SERVER_HI "Zdarec, co chces delat?"
#define SERVER_OK "Cajk"
#define SERVER_ERR "Sorry, nejde to."
#define SERVER_BYE "Tot vse, uz se nevracej."
#define SERVER_SEND_FILENAME "Posli mi prosimte nazev souboru, dik."

#define CLIENT_HI_WRITE "Chcu ti neco poslat."
#define CLIENT_HI_READ "Chcu si neco stahnout."
#define CLIENT_ERR "Hele nejde mi to"
#define CLIENT_OK "Vse ok, pac a pusu"
#define CLIENT_SAY_SHUTDOWN "Hele kamo vypni se uz jo."

#define FILE_RECEIVED "Hele uz mi to vsechno doslo, dik."
#define FILE_SENT_OK "Vsechno jsem poslal, cajk."
#define FILE_NOT_EMPTY "Aspon neco ti teda poslu."
#define FILE_EMPTY "On je prazdnej, proradne testy."


// macro-functions

#define DEBUG 1
#define debug_print(...) \
            do { if (DEBUG) fprintf(stderr,__VA_ARGS__); } while (0)

struct packet_info {
    long packet_size;
};

int receive_file(int target_socket, FILE *fw);
int send_file(int target_socket, FILE *fr);

int send_packet(int target_socket,char *buffer, long bytes);

int recv_packet(int in_socket, char *buffer);


#endif //INC_1_SHARED_H
