//
// Created by zeusko on 03/03/18.
//
#include "client.h"
#include "shared.h"
#include <stdio.h>
#include <string.h>
#include <getopt.h>
#include <stdbool.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <errno.h>
#include <netdb.h>
#include <strings.h>

long int port_num;
char *host_name;
char *filename;
bool want_to_read;

int main(int argc, char *argv[]) {
    int ret = getParams(argc, argv);
    if (ret == 1) {
        fprintf(stderr, "Wrong params.\n");
        exit(1);
    }
    atexit(clean);

    debug_print("Params: \nhost: '%s'\nport: '%d'\nfilename: %s\n", host_name, (int)port_num, filename);

    // get address from hostname
    struct hostent *he;
    if ( (he = gethostbyname(host_name)) == NULL)
        exit(1);


    // create amd fill remote addr struct
    struct sockaddr_in remote_addr;
    memset(&remote_addr, 0, sizeof(remote_addr));
    remote_addr.sin_family = AF_INET;
    memcpy(&remote_addr.sin_addr, he->h_addr_list[0], (size_t) he->h_length);
    remote_addr.sin_port = htons((uint16_t)port_num);

    // create socket
    int client_socket;

    if ((client_socket = socket(AF_INET, SOCK_STREAM, 6)) <= 0) {
        fprintf(stderr, "Can not create socket: %s\n", strerror(errno));
        exit(1);
    }

    // connect to server
    if (connect(client_socket, (struct sockaddr *) &remote_addr, sizeof(struct sockaddr)) == -1) {
        fprintf(stderr, "Error while connecting: %s\n", strerror(errno));
        exit(1);
    }

    debug_print("Successfully connected to server.\n");

    // handshake
    char buffer[BUFFER_SIZE] = {0};
    recv(client_socket, &buffer, BUFFER_SIZE, 0);
    debug_print("Server sent: %s\n", buffer);
    if (strcmp(buffer, SERVER_HI) != 0) {
        fprintf(stderr, "Unexpected server response: %s\n", buffer);
        exit(1);
    }

    if (want_to_read) {
        // receive a file
        send(client_socket, CLIENT_HI_READ, strlen(CLIENT_HI_READ) + 1, 0);
        bzero(buffer, BUFFER_SIZE);
        // expecting filename request from server
        recv(client_socket, &buffer, BUFFER_SIZE, 0);
        if (strcmp(buffer, SERVER_SEND_FILENAME) != 0) {
            fprintf(stderr, "Unexpected server response: %s\n", buffer);
            exit(1);
        }
        send(client_socket, filename, strlen(filename) + 1, 0);

        FILE *fw = fopen(filename, "w");
        if (!fw) {
            fprintf(stderr, "Could not open file %s\n", filename);
            exit(1);
        }
        if (receive_file(client_socket, fw) != 0) {
            exit(1);
        }
        debug_print("File %s received successfully.\n", filename);
        fclose(fw);

    } else {
        // send a file
        send(client_socket, CLIENT_HI_WRITE, strlen(CLIENT_HI_WRITE) + 1, 0);
        bzero(buffer, BUFFER_SIZE);
        // expecting filename request from server
        recv(client_socket, &buffer, BUFFER_SIZE, 0);
        if (strcmp(buffer, SERVER_SEND_FILENAME) != 0) {
            fprintf(stderr, "Unexpected server response: %s\n", buffer);
            exit(1);
        }
        send(client_socket, filename, strlen(filename) + 1, 0);

        FILE *fr = fopen(filename, "r");
        if (!fr) {
            fprintf(stderr, "Could not open file %s\n", filename);
            exit(1);
        }
        if (send_file(client_socket, fr) != 0) {
            exit(1);
        }
        debug_print("File %s sent successfully.\n", filename);
        fclose(fr);
    }

    exit(0);
}

void clean() {
    free(host_name);
    free(filename);
}

int getParams(int argc, char *argv[]) {
    int c;
    bool pflag = false, hflag = false, rflag = false, wflag = false;
    while((c = getopt(argc, argv, "h:p:r:w:")) != -1) {
        switch (c) {
            case 'h':
                hflag = true;
                host_name = strdup(optarg);
                if (host_name == NULL)
                    return 1;
                break;

            case 'p':
                pflag = true;
                port_num = atoi(optarg);
                break;

            case 'r':
                rflag = true;
                filename = strdup(optarg);
                if (filename == NULL)
                    return 1;
                break;

            case 'w':
                wflag = true;
                filename = strdup(optarg);
                if (filename == NULL)
                    return 1;
                break;

            default:
                return 1;
        }
    }

    want_to_read = rflag;

    if(!pflag || port_num == 0 || argc != 7 || wflag == rflag || !hflag)
        return 1;

    return 0;
}

char *strdup(const char *str)
{
    size_t n = strlen(str) + 1;
    char *dup = malloc(n);
    if(dup)
    {
        strcpy(dup, str);
    }
    return dup;
}