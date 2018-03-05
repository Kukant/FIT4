//
// Created by zeusko on 03/03/18.
//
#include "server.h"
#include <getopt.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <errno.h>
#include <stdbool.h>
#include <unistd.h> // fork

#define DEBUG 1
#define debug_print(...) \
            do { if (DEBUG) fprintf(stderr, __VA_ARGS__); } while (0)



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
            debug_print("Ahoj z forku!\n");
            int bytes_to_write = 5;
            while (bytes_to_write > 0) {
                int bytes_written = (int) write (client_socket, "ahoj", 5);
                debug_print("written %d bytes\n", bytes_written);
                bytes_to_write -= bytes_written;
            }

        }
    }




    return 0;
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