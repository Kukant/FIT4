//
// Created by zeusko on 06/03/18.
//
#include "shared.h"

#include <sys/socket.h>
#include <string.h>
#include <strings.h>


int receive_file(int target_socket, char *buffer, FILE *fw) {
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    while((block_size = recv(target_socket, &buffer, BUFFER_SIZE, 0)) > 0) {
        long write_size = fwrite(&buffer, sizeof(char), block_size, fw);
        if (write_size < block_size) {
            fprintf(stderr, "Error while writing data.\n");
            return 1;
        }
        if (block_size < BUFFER_SIZE) // end of file
            break;
        memset(buffer, 0, BUFFER_SIZE);
    }

    return 0;
}

int send_file(int target_socket, char *buffer, FILE *fr) {
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    while ((block_size = fread(buffer, sizeof(char), BUFFER_SIZE, fr)) > 0) {
        if (send(target_socket, &buffer, block_size, 0) < 0) {
            fprintf(stderr, "Failed to send file.\n");
            return 1;
        }
        memset(buffer, 0, BUFFER_SIZE);
    }

    return 0;
}