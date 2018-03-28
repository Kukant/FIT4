//
// Created by zeusko on 28/03/18.
//

#include "functions.h"
#include "main.h"
#include "dns_header.h"
#include <getopt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int get_params(int argc, char **argv) {

    bool sflag = false;

    int c;
    while((c = getopt(argc, argv, "hs:T:t:i")) != -1) {
        switch (c) {
            case 'h':
                print_help();
                exit(0);
            case 's':
                strncpy(server, optarg, sizeof(server));
                sflag = true;
                break;
            case 'T':
                timeout = atol(optarg);
                break;
            case 't':
                strncpy(type, optarg, sizeof(type));
                break;
            case 'i':
                iterative = true;
                break;
            default:
                return 2;
        }
    }

    if (!sflag || optind >= argc || optind + 1 != argc) {
        return 2;
    }

    strncpy(domain_name, argv[optind], sizeof(domain_name));

    return 0;
}

void print_help() {
    printf("Usage:\n"
                   "\t./ipk-lookup [-h]\n"
                   "\t./ipk-lookup -s server [-T timeout] [-t type] [-i] name\n"
                   "Author: Tomas Kukan, xkukan00\n");
}