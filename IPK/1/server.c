//
// Created by zeusko on 03/03/18.
//
#include "server.h"
#include <getopt.h>
#include <stdlib.h>
#include <stdio.h>

int main(int argc, char *argv[]) {
    int ret = getParams(argc, argv);
    if (ret == 1) {
        fprintf(stderr, "Wrong params.\n");
        return 1;
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
    long int port_num;
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