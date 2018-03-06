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
    while((block_size = recv(target_socket, &buffer, BUFFER_SIZE, 0)) > 0) {
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
    debug_print("received: %lu bites\n", file_size);
    send(target_socket, FILE_RECEIVED, strlen(FILE_RECEIVED) + 1, 0);

    return 0;
}

int send_file(int target_socket, FILE *fr) {
    char buffer[BUFFER_SIZE] = {0};
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    while ((block_size = fread(buffer, sizeof(char), BUFFER_SIZE, fr)) > 0) {
        if (send(target_socket, &buffer, block_size, 0) < 0) {
            fprintf(stderr, "Failed to send file.\n");
            return 1;
        }
        memset(buffer, 0, BUFFER_SIZE);
    }
    // wait until file was received
    debug_print("Waiting till everything is received.\n");
    recv(target_socket, buffer, BUFFER_SIZE, 0);
    if (strcmp(buffer, FILE_RECEIVED) != 0) {
        fprintf(stderr, "Client sent unknown message after file transfer: %s \n", buffer);
        exit(1);
    }

    return 0;
}