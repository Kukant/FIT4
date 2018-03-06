//
// Created by zeusko on 03/03/18.
//
#include "server.h"
#include "shared.h"

#include <getopt.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <strings.h>
#include <arpa/inet.h>
#include <errno.h>
#include <stdbool.h>
#include <unistd.h> // fork

long int port_num;

int main(int argc, char *argv[]) {
    int ret = getParams(argc, argv);
    if (ret == 1) {
        fprintf(stderr, "Wrong params.\n");
        exit(1);
    }

    // create socket
    int server_socket;
    if ((server_socket = socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
        fprintf(stderr, "Can not create socket: %s\n", strerror(errno));
        exit(1);
    }

    struct sockaddr_in server_address;
    memset(&server_address, 0, sizeof(struct sockaddr));
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = INADDR_ANY;
    server_address.sin_port = htons((uint16_t)port_num);

    // bind port

    if (bind(server_socket, (struct sockaddr *)&server_address, sizeof(struct sockaddr)) == -1){
        fprintf(stderr, "Cannot bind port: %s\n", strerror(errno));
        exit(1);
    }

    if(listen(server_socket, 5) == -1) {
        fprintf(stderr, "Cannot listen to port: %s\n", strerror(errno));
        exit(1);
    }

    bool running = true;
    while (running) {
        int client_socket;
        struct sockaddr_in client_address;
        int client_address_size = sizeof(client_address);
        // waiting here until someone wants to connect
        client_socket = accept(server_socket, (struct sockaddr *)&client_address, (socklen_t *)&client_address_size);
        if (client_socket <= 0) {
            fprintf(stderr, "Cannot accept connection: %s\n", strerror(errno));
            exit(1);
        } else {
            debug_print("Someone connected!\n");
        }

        if (!fork()) {
            close(server_socket);
            send (client_socket, SERVER_HI, strlen(SERVER_HI) + 1, 0);
            char buffer[BUFFER_SIZE] = {0};
            char filename[BUFFER_SIZE] = {0};
            recv(client_socket, &buffer, sizeof(buffer), 0);

            if (strcmp(buffer, CLIENT_HI_READ) == 0) {

                // send file
                debug_print("Client wants to read a file.\n");
                bzero(buffer, sizeof(buffer));
                // expecting filename
                send (client_socket, SERVER_SEND_FILENAME, strlen(SERVER_SEND_FILENAME) + 1, 0);
                debug_print("Waiting for filename.\n");
                recv(client_socket, &filename, sizeof(filename), 0);
                debug_print("Filename: %s\n", filename);
                FILE *fr;
                if ((fr = fopen(filename, "r")) == NULL) {
                    fprintf(stderr, "Can not open file '%s'\n", filename);
                    exit(1);
                }

                if (send_file(client_socket, buffer, fr) != 0) {
                    fprintf(stderr, "Can not send file '%s'\n", filename);
                    exit(1);
                }
                fclose(fr);
                debug_print("File sent successfully.\n");

            } else if (strcmp(buffer, CLIENT_HI_WRITE) == 0) {

                // receive file
                debug_print("Client wants to write a file.\n");
                bzero(buffer, sizeof(buffer));
                // expecting filename
                recv(client_socket, &filename, sizeof(buffer), 0);
                FILE *fw;
                if ((fw = fopen(filename, "w")) == NULL) {
                    fprintf(stderr, "Can not open file '%s'\n", filename);
                    exit(1);
                }
                if (receive_file(client_socket, buffer, fw) != 0) {
                    fprintf(stderr, "Error while receiving file\n");
                    exit(1);
                }
                debug_print("File received successfully.\n");
                fclose(fw);

            } else {
                fprintf(stderr, "Unknown message: '%s'\n", buffer);
                exit(1);
            }

            send (client_socket, SERVER_HI, strlen(SERVER_BYE) + 1, 0);
            close(client_socket);
            exit(0);
        }
        close(client_socket);
    }
    exit(0);
}

/**
 * Handles command line parameters.
 * @param argc
 * @param argv
 * @return 0 in case of success
 */
int getParams(int argc, char *argv[]) {
    int c, pflag = 0;
    while((c = getopt(argc, argv, "p:")) != -1){
        switch (c) {
            case 'p':
                pflag = 1;
                port_num = atoi(optarg);
                break;

            default:
                return 1;
        }
    }

    if(pflag != 1 || port_num == 0 || argc != 3)
        return 1;

    return 0;
}