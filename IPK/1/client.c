//
// Created by zeusko on 03/03/18.
//
#include "client.h"
#include <stdio.h>
#include <string.h>
#include <getopt.h>
#include <stdbool.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <errno.h>
#include <netdb.h>

long int portnum;
char *host_name;
char *filename;
bool want_to_read;

int main(int argc, char *argv[]) {
    int ret = getParams(argc, argv);
    if (ret == 1) {
        fprintf(stderr, "Wrong params.\n");
        clean();
        return 1;
    }

    // get address from hostname
    struct hostent *he;
    if ( (he = gethostbyname(host_name)) == NULL) {
        clean();
        return 1;
    }


    // create amd fill remote addr struct
    struct sockaddr_in remote_addr;
    memset(&remote_addr, 0, sizeof(remote_addr));
    remote_addr.sin_family = AF_INET;
    memcpy(&remote_addr.sin_addr, he->h_addr_list[0], (size_t) he->h_length);
    remote_addr.sin_port = htons((uint16_t)portnum);

    // create socket
    int client_socket;

    if ((client_socket = socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
        fprintf(stderr, "Can not create socket: %s\n", strerror(errno));
        clean();
        return 1;
    }

    // connect to server
    if (connect(client_socket, (struct sockaddr *) &remote_addr, sizeof(struct sockaddr)) == -1) {
        fprintf(stderr, "Error while connecting: %s\n", strerror(errno));
        clean();
        return 1;
    }

    clean();
    return 0;
}

void clean() {
    free(host_name);
    free(filename);
}

int getParams(int argc, char *argv[]) {
    int c;
    bool pflag = false, hflag = false, rflag = false, wflag = false;
    long int port_num;
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
    int n = strlen(str) + 1;
    char *dup = malloc(n);
    if(dup)
    {
        strcpy(dup, str);
    }
    return dup;
}