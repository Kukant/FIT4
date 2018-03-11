/**
 * Created by Tomas Kukan, xkukan00, 11. 3. 2018
 */

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
#include <netinet/in.h>

// shared stuff
#include <sys/mman.h>
#include <semaphore.h>
#include <fcntl.h>
#include <signal.h>

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>

// my stuff
#include "server.h"
#include "shared.h"


typedef char shared_table_t[SH_TABLE_ROWS][BUFFER_SIZE];
long int port_num;
shared_table_t *sh_w_files;
int sh_w_files_id;
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
    if ((server_socket = socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
        fprintf(stderr, "Can not create socket: %s\n", strerror(errno));
        free_resources();
        exit(1);
    }

    setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, (const void *)1, sizeof(int));

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
            send_packet(client_socket, SERVER_HI, strlen(SERVER_HI) + 1, 0);
            char buffer[BUFFER_SIZE];
            char filename[BUFFER_SIZE];
            bzero(buffer, BUFFER_SIZE);
            bzero(filename, BUFFER_SIZE);

            recv_packet(client_socket, buffer, NULL);

            if (strcmp(buffer, CLIENT_READ) == 0) {

                // send file
                debug_print("Client wants to read a file.\n");

                recv_packet(client_socket, filename, NULL);
                debug_print("Filename: %s\n", filename);
                FILE *fr;
                if ((fr = fopen(filename, "rb")) == NULL) {
                    fprintf(stderr, "Can not open file '%s'\n", filename);
                    send_packet(client_socket, SERVER_ERR, strlen(SERVER_ERR) + 1, 0);
                    exit(1);
                }
                send_packet(client_socket, SERVER_OK, strlen(SERVER_OK) + 1, 0);
                bzero(buffer, BUFFER_SIZE);
                recv_packet(client_socket, buffer, NULL);
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

            } else if (strcmp(buffer, CLIENT_WRITE) == 0) {
                // receive file
                debug_print("Client wants to write a file.\n");
                recv_packet(client_socket, filename, NULL);
                debug_print("filename received\n");
                if (can_i_write_into(filename) == false) {
                    send_packet(client_socket, SERVER_ERR, strlen(SERVER_ERR) + 1, 0);
                    fprintf(stderr, "File '%s' already opened!---------------------\n", filename);
                    exit(1);
                }
                debug_print("Opening file for writing\n");
                FILE *fw;
                if ((fw = fopen(filename, "wb")) == NULL) {
                    fprintf(stderr, "Can not open file '%s', err: %s\n", filename, strerror(errno));
                    exit(1);
                }
                // everything ok, send it to me
                send_packet(client_socket, SERVER_OK, strlen(SERVER_OK) + 1, 0);
                printf("Receiving file: %s\n", filename);

                if (receive_file(client_socket, fw) != 0) {
                    fprintf(stderr, "Error while receiving file\n");
                    exit(1);
                }
                fclose(fw);
                printf("File received successfully.\n");
                debug_print("file closed\n");
                remove_wopen_file(filename);

            } else {
                fprintf(stderr, "Unknown message: '%s'\n", buffer);
                exit(1);
            }

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
    if (create_shared_memory(BUFFER_SIZE * SH_TABLE_ROWS) == NULL) {
        fprintf(stderr, "ERROR: Shared variable cound not be set: %s\n", strerror(errno));
        return -1;
    }

    for (int i = 0; i < SH_TABLE_ROWS; i++) {
        (*sh_w_files)[i][0] = '\0';
    }


    if((sh_w_files_mtx = sem_open("/xkukan00mujkrutejmutex", O_CREAT | O_EXCL, 0666, 1)) == SEM_FAILED) {
        sem_unlink("/xkukan00mujkrutejmutex");
        if((sh_w_files_mtx = sem_open("/xkukan00mujkrutejmutex", O_CREAT | O_EXCL, 0666, 1)) == SEM_FAILED) {
            fprintf(stderr, "ERROR: mutex cound not be open.\n");
            return -1;
        }
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
    shmctl( sh_w_files_id, IPC_RMID, NULL);
    resources_freed = true;
    exit(0);
}

void* create_shared_memory(size_t size) {
    sh_w_files_id = shmget(IPC_PRIVATE, size, IPC_CREAT | 0666);
    if (sh_w_files_id == -1) {
        fprintf(stderr, "ERROR: Shared variable cound not be set.\n");
        return NULL;
    }

    if ((sh_w_files = (shared_table_t *) shmat(sh_w_files_id, NULL, 0)) == NULL)
    {
        fprintf(stderr, "ERROR: Shared variable cound not be set.\n");
        return NULL;
    }

    return sh_w_files;
}

bool can_i_write_into(char *filename) {
    char real_path[BUFFER_SIZE];
    bzero(real_path, BUFFER_SIZE);
    char *p = realpath(filename, real_path);
    if (!p) // unsuccessful translation, file does not exist
    {
        debug_print("Unsuccessful translation by realpath: %s\n", strerror(errno));
        // create file
        FILE *fw = fopen(filename, "w");
        fclose(fw);
        p = realpath(filename, real_path);
    }

    filename = real_path;
    for (int i = 0; i < SH_TABLE_ROWS; i++) {
        if (strcmp((*sh_w_files)[i], filename) == 0) {
            return false;
        }
    }

    sem_wait(sh_w_files_mtx);
    int ret = add_wopen_file(filename);
    sem_post(sh_w_files_mtx);

    if (ret == 1) {
        fprintf(stderr, "Shared table is full.");
        exit(1);
    }

    return true;
}

int add_wopen_file(char *filename) {
    debug_print("adding: %s\n", filename);
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
