//
// Created by zeusko on 06/03/18.
//
#include "shared.h"

#include <sys/socket.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>


int receive_file(int target_socket, FILE *fw) {
    char buffer[BUFFER_SIZE] = {0};
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    unsigned long file_size = 0;
    recv(target_socket, &buffer, BUFFER_SIZE, 0);
    if (strcmp(buffer, FILE_NOT_EMPTY) == 0) {
        while ((block_size = recv(target_socket, &buffer, BUFFER_SIZE, 0)) > 0) {
            file_size += block_size;
            long write_size = fwrite(&buffer, sizeof(char), block_size, fw);
            if (write_size < block_size) {
                fprintf(stderr, "Error while writing data.\n");
                return 1;
            }
            if (block_size < BUFFER_SIZE) // end of file
                break;
            memset(buffer, 0, BUFFER_SIZE);
        }
    }
    debug_print("received: %lu bites\n", file_size);
    send(target_socket, FILE_RECEIVED, strlen(FILE_RECEIVED) + 1, 0);
    memset(buffer, 0, BUFFER_SIZE);
    recv(target_socket, &buffer, BUFFER_SIZE, 0);
    if (strcmp(buffer, FILE_SENT_OK) != 0) {
        fprintf(stderr, "File not received succesfully: '%s'.\n", buffer);
        return 1;
    }


    return 0;
}

int send_file(int target_socket, FILE *fr) {
    char buffer[BUFFER_SIZE] = {0};
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    block_size = fread(buffer, sizeof(char), BUFFER_SIZE, fr);
    if (block_size == 0) { // empty file
        send(target_socket, FILE_EMPTY, strlen(FILE_EMPTY) + 1, 0);
    } else {
        send(target_socket, FILE_NOT_EMPTY, strlen(FILE_EMPTY) + 1, 0);
        do {
            if (send(target_socket, &buffer, block_size, 0) < 0) {
                fprintf(stderr, "Failed to send file.\n");
                return 1;
            }
            memset(buffer, 0, BUFFER_SIZE);
        } while (((block_size = fread(buffer, sizeof(char), BUFFER_SIZE, fr)) > 0));
    }

    // wait until file was received
    debug_print("Waiting till everything is received.\n");
    recv(target_socket, buffer, BUFFER_SIZE, 0);
    if (strcmp(buffer, FILE_RECEIVED) != 0) {
        fprintf(stderr, "Client sent unknown message after file transfer: %s \n", buffer);
        exit(1);
    }
    send(target_socket, FILE_SENT_OK, strlen(FILE_SENT_OK) + 1, 0);


    return 0;
}