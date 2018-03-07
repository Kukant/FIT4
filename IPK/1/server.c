//
// Created by zeusko on 03/03/18.
//
#include <stdio.h>
#include <getopt.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <strings.h>
#include <string.h>
#include <arpa/inet.h>
#include <errno.h>
#include <stdbool.h>
#include <unistd.h> // fork

// shared stuff
#include <sys/mman.h>
#include <semaphore.h>
#include <fcntl.h>
#include <signal.h>

// my stuff
#include "server.h"
#include "shared.h"


typedef char shared_table_t[SH_TABLE_ROWS][BUFFER_SIZE];
long int port_num;
shared_table_t *sh_w_files;
sem_t *sh_w_files_mtx;
bool resources_freed = true;


int main(int argc, char *argv[]) {
    // set resources
    if (set_resources()) {
        fprintf(stderr, "Could not allocate (set) resources.\n");
        exit(1);
    } else {
        signal(SIGINT, free_resources);
    }

    // get params
    int ret = get_params(argc, argv);
    if (ret == 1) {
        fprintf(stderr, "Wrong params.\n");
        free_resources();
        exit(1);
    }

    // create socket
    int server_socket;
    if ((server_socket = socket(AF_INET, SOCK_STREAM, 6)) <= 0) {
        fprintf(stderr, "Can not create socket: %s\n", strerror(errno));
        free_resources();
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
        free_resources();
        exit(1);
    }

    if(listen(server_socket, 5) == -1) {
        fprintf(stderr, "Cannot listen to port: %s\n", strerror(errno));
        free_resources();
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
            free_resources();
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

                // expecting filename
                send (client_socket, SERVER_SEND_FILENAME, strlen(SERVER_SEND_FILENAME) + 1, 0);
                debug_print("Waiting for filename.\n");
                recv(client_socket, &filename, sizeof(filename), 0);
                debug_print("Filename: %s\n", filename);
                FILE *fr;
                if ((fr = fopen(filename, "r")) == NULL) {
                    fprintf(stderr, "Can not open file '%s'\n", filename);
                    send (client_socket, SERVER_ERR, strlen(SERVER_ERR) + 1, 0);
                    exit(1);
                }

                send (client_socket, SERVER_OK, strlen(SERVER_OK) + 1, 0);
                bzero(buffer, BUFFER_SIZE);
                recv(client_socket, &buffer, BUFFER_SIZE, 0);
                if (strcmp(buffer, CLIENT_OK) != 0) {
                    fprintf(stderr, "Client is not ok '%s'\n", buffer);
                    fclose(fr);
                    exit(1);
                }

                if (send_file(client_socket, fr) != 0) {
                    fprintf(stderr, "Can not send file '%s'\n", filename);
                    exit(1);
                }
                fclose(fr);

                debug_print("File sent successfully.\n");

            } else if (strcmp(buffer, CLIENT_HI_WRITE) == 0) {
                // receive file
                debug_print("Client wants to write a file.\n");
                // expecting filename
                send (client_socket, SERVER_SEND_FILENAME, strlen(SERVER_SEND_FILENAME) + 1, 0);
                debug_print("Waiting for filename.\n");
                recv(client_socket, &filename, sizeof(filename), 0);
                if (can_i_write_into(filename) == false) {
                    send (client_socket, SERVER_ERR, strlen(SERVER_ERR) + 1, 0);
                    fprintf(stderr, "File '%s' already opened!---------------------\n", filename);
                    exit(1);
                }

                FILE *fw;
                if ((fw = fopen(filename, "w")) == NULL) {
                    fprintf(stderr, "Can not open file '%s', err: %s\n", filename, strerror(errno));
                    exit(1);
                }
                // everything ok, end it to me
                send (client_socket, SERVER_OK, strlen(SERVER_OK) + 1, 0);
                debug_print("Receiving file: %s\n", filename);

                if (receive_file(client_socket, fw) != 0) {
                    fprintf(stderr, "Error while receiving file\n");
                    exit(1);
                }
                debug_print("File received successfully.\n");
                fclose(fw);
                debug_print("file closed\n");
                remove_wopen_file(filename);

            } else {
                fprintf(stderr, "Unknown message: '%s'\n", buffer);
                exit(1);
            }

            send (client_socket, SERVER_BYE, strlen(SERVER_BYE) + 1, 0);
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
int get_params(int argc, char *argv[]) {
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

int set_resources() {
    if ((sh_w_files = create_shared_memory(BUFFER_SIZE * SH_TABLE_ROWS)) == NULL) {
        fprintf(stderr, "ERROR: Shared variable cound not be set.\n");
        return -1;
    }


    if((sh_w_files_mtx = sem_open("/xkukan00mujkrutejmutex", O_CREAT | O_EXCL, 0666, 1)) == SEM_FAILED) {
        fprintf(stderr, "ERROR: mutex cound not be open.\n");
        return -1;
    }

    resources_freed = false;

    return 0;
}

void free_resources() {
    debug_print("freeing resources and exiting. \n");
    if (resources_freed)
        return;

    sem_close(sh_w_files_mtx);
    sem_unlink("/xkukan00mujkrutejmutex");
    munmap(sh_w_files, BUFFER_SIZE * SH_TABLE_ROWS);
    resources_freed = true;
    exit(0);
}

void* create_shared_memory(size_t size) {
    int protection = PROT_READ | PROT_WRITE;
    int visibility = MAP_ANONYMOUS | MAP_SHARED;
    return mmap(NULL, size, protection, visibility, 0, 0);
}

bool can_i_write_into(char *filename) {
    char real_path[BUFFER_SIZE];
    char *p = realpath(filename, real_path);
    if (!p) // unsuccessful translation
        return 1;

    sem_wait(sh_w_files_mtx);
    filename = real_path;
    for (int i = 0; i < SH_TABLE_ROWS; i++) {
        if (strcmp((*sh_w_files)[i], filename) == 0) {
            sem_post(sh_w_files_mtx);
            return false;
        }
    }

    int ret = add_wopen_file(filename);

    if (ret == 1) {
        fprintf(stderr, "Shared table is full.");
        exit(1);
    }

    sem_post(sh_w_files_mtx);

    return true;
}

int add_wopen_file(char *filename) {
    for (int i = 0; i < SH_TABLE_ROWS; i++) {
        if ((*sh_w_files)[i][0] == '\0') {
            strcpy((*sh_w_files)[i], filename);
            return 0;
        }
    }
    return 1;
}

int remove_wopen_file(char *filename) {
    char real_path[BUFFER_SIZE];
    char *p = realpath(filename, real_path);
    if (!p) // unsuccessful translation
        return 1;
    filename = real_path;
    sem_wait(sh_w_files_mtx);
    for (int i = 0; i < SH_TABLE_ROWS; i++) {
        if (strcmp((*sh_w_files)[i], filename) == 0) {
            (*sh_w_files)[i][0] = '\0';
            sem_post(sh_w_files_mtx);
            debug_print("Removing from table: %s\n", filename);
            return 0;
        }
    }
    sem_post(sh_w_files_mtx);
    return 1;
}

void print_shared_table() {
    debug_print("Shared table:\n");
    for (int i = 0; i < SH_TABLE_ROWS; i++) {
        if ((*sh_w_files)[i][0] != '\0') {
            debug_print("%d. %s\n", i, (*sh_w_files)[i]);
        }
    }
    debug_print("End of table:\n");
    fflush(stderr);
}