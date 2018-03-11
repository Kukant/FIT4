/**
 * Created by Tomas Kukan, xkukan00, 11. 3. 2018
 */

#include "shared.h"

#include <sys/socket.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <stdbool.h>


int receive_file(int target_socket, FILE *fw) {
    char buffer[BUFFER_SIZE];
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    unsigned long file_size = 0;
    bool eof = false;
    while (!eof) {
        block_size = recv_packet(target_socket, buffer, &eof);
        file_size += block_size;
        long write_size = fwrite(buffer, sizeof(char), block_size, fw);
        if (write_size < block_size) {
            fprintf(stderr, "Error while writing data.\n");
            send_packet(target_socket, FILE_ERR, strlen(FILE_ERR) + 1, 0);
            return 1;
        }
        memset(buffer, 0, BUFFER_SIZE);
    }
    printf("File received: %lu bytes\n", file_size);
    send_packet(target_socket, FILE_RECEIVED, strlen(FILE_RECEIVED) + 1, 0);

    return 0;
}

int send_file(int target_socket, FILE *fr) {
    char buffer[BUFFER_SIZE] = {0};
    long block_size;
    memset(buffer, 0, BUFFER_SIZE);
    bool eof = false;
    while (!eof) {
        block_size = fread(buffer, sizeof(char), BUFFER_SIZE, fr);
        eof = block_size != BUFFER_SIZE;
        if (send_packet(target_socket, buffer, block_size, eof) < 0) {
            fprintf(stderr, "Failed to send file.\n");
            return 1;
        }
        memset(buffer, 0, BUFFER_SIZE);
    }

    // wait until file was received
    printf("Waiting till everything is received.\n");
    recv_packet(target_socket, buffer, NULL);
    if (strcmp(buffer, FILE_RECEIVED) != 0) {
        fprintf(stderr, "Client sent unknown message after file transfer: %s \n", buffer);
        exit(1);
    }
    printf("File was received successfully.\n");

    return 0;
}

/// Function that is wrapping send function. It firstly sends a message with size of transfered data in the packet.
/// \param target_socket
/// \param buffer
/// \param bytes
/// \return
int send_packet(int target_socket,char *buffer, long bytes, bool eof){
    struct packet_info pi;
    pi.packet_size = bytes;
    pi.eof = eof;
    send(target_socket, &pi, sizeof(struct packet_info), 0);
    send(target_socket, buffer, bytes, 0);

    return 0;
}

long recv_packet(int in_socket, char *buffer, bool *eof) {

    long received = 0;
    struct packet_info pi;
    do {
        received += recv(in_socket, &pi + received, sizeof(struct packet_info) - received, 0);
    } while (received != sizeof(struct packet_info));

    if (eof != NULL) {
        *eof = pi.eof;
    }
    received = 0;

    while (received != pi.packet_size) {
        received += recv(in_socket, buffer + received, pi.packet_size - (size_t)received, 0);
    }


    return received;
}