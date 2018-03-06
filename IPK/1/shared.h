//
// Created by zeusko on 06/03/18.
//

#ifndef INC_1_SHARED_H
#define INC_1_SHARED_H

#include <stdio.h>

// macros

#define BUFFER_SIZE 1024

#define SERVER_HI "Zdarec, co chces delat?"
#define SERVER_OK "Cajk"
#define SERVER_ERR "Sorry, nejde to."
#define SERVER_BYE "Tot vse, uz se nevracej."
#define SERVER_SEND_FILENAME "Posli mi prosimte nazev souboru, dik."

#define CLIENT_HI_WRITE "Chcu ti neco poslat."
#define CLIENT_HI_READ "Chcu si neco stahnout."

// macro-functions

#define DEBUG 1
#define debug_print(...) \
            do { if (DEBUG) fprintf(stderr,__VA_ARGS__); } while (0)


int receive_file(int target_socket, char *buffer, FILE *fw);
int send_file(int target_socket, char *buffer, FILE *fr);


#endif //INC_1_SHARED_H
