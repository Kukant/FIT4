/**
 * Created by Tomas Kukan, xkukan00, 11. 3. 2018
 */

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
#include <unistd.h>
#include <netinet/in.h>
#include <libgen.h>


long int port_num;
char host_name[BUFFER_SIZE];
char filename[BUFFER_SIZE];
bool want_to_read;

int main(int argc, char *argv[]) {
    int ret = getParams(argc, argv);
    if (ret == 1) {
        fprintf(stderr, "Wrong params.\n");
        exit(1);
    }
    char *f_basename = basename(filename);

    //debug_print("Params: \nhost: '%s'\nport: '%d'\nfilename: %s\n", host_name, (int)port_num, filename);

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

    if ((client_socket = socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
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
    char buffer[BUFFER_SIZE];
    bzero(buffer, BUFFER_SIZE);
    recv_packet(client_socket, buffer, NULL);
    if (strcmp(buffer, SERVER_HI) != 0) {
        fprintf(stderr, "Unexpected server response: %s\n", buffer);
        exit(1);
    }

    if (want_to_read) {
        // receive a file
        send_packet(client_socket, CLIENT_HI_READ, strlen(CLIENT_HI_READ) + 1, 0);
        send_packet(client_socket, f_basename, strlen(f_basename) + 1, 0);
        debug_print("Sending filename '%s'.\n", f_basename);

        FILE *fw = fopen(filename, "w");
        if (!fw) {
            fprintf(stderr, "Could not open file %s\n", filename);
            exit(1);
        }
        bzero(buffer, BUFFER_SIZE);
        recv_packet(client_socket, buffer, NULL);
        if (strcmp(buffer, SERVER_OK) != 0) {
            fprintf(stderr, "Server probably could not open file, his response: %s\n", buffer);
            fclose(fw);
            exit(1);
        }

        send_packet(client_socket, CLIENT_OK, strlen(CLIENT_OK) + 1, 0);

        if (receive_file(client_socket, fw) != 0) {
            exit(1);
        }
        debug_print("File %s received successfully.\n", filename);
        fclose(fw);

    } else {
        // send a file
        FILE *fr = fopen(filename, "r");
        if (!fr) {
            fprintf(stderr, "Could not open file %s\n", filename);
            send_packet(client_socket, CLIENT_ERR, strlen(CLIENT_ERR) + 1, 0);
            exit(1);
        }

        send_packet(client_socket, CLIENT_HI_WRITE, strlen(CLIENT_HI_WRITE) + 1, 0);
        send_packet(client_socket, f_basename, strlen(f_basename) + 1, 0);
        debug_print("Sending filename '%s'.\n", f_basename);

        memset(buffer, 0, BUFFER_SIZE);
        recv_packet(client_socket, buffer, NULL);
        if (strcmp(buffer, SERVER_OK) != 0) {
            fprintf(stderr, "Server responed: '%s'\nFile is probably already open.\n", buffer);
            exit(1);
        }

        if (send_file(client_socket, fr) != 0) {
            exit(1);
        }
        debug_print("File %s sent successfully.\n", filename);
        fclose(fr);
    }

    close(client_socket);
    exit(0);
}

int getParams(int argc, char *argv[]) {
    int c;
    bool pflag = false, hflag = false, rflag = false, wflag = false;
    while((c = getopt(argc, argv, "h:p:r:w:")) != -1) {
        switch (c) {
            case 'h':
                hflag = true;
                if (strlen(optarg) > BUFFER_SIZE - 1) {
                    fprintf(stderr, "host_name is way too long. ( > %d)\n",BUFFER_SIZE);
                    return 1;
                }
                strcpy(host_name, optarg);
                break;

            case 'p':
                pflag = true;
                port_num = atoi(optarg);
                break;

            case 'r':
                rflag = true;
                if (strlen(optarg) > BUFFER_SIZE - 1) {
                    fprintf(stderr, "Filename is way too long. ( > %d)\n",BUFFER_SIZE);
                    return 1;
                }
                strcpy(filename, optarg);
                break;

            case 'w':
                wflag = true;
                if (strlen(optarg) > BUFFER_SIZE - 1) {
                    fprintf(stderr, "Filename is way too long. ( > %d)\n",BUFFER_SIZE);
                    return 1;
                }
                strcpy(filename, optarg);
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
