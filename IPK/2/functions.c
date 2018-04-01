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

    bool sflag = false, tflag = false, Tflag = false;

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
                timeout_sec = atol(optarg);
                Tflag = true;
                break;
            case 't':
                if (!strcmp(optarg, "A")) {
                    type = DNS_QTYPE_A;
                } else if (!strcmp(optarg, "AAAA")) {
                    type = DNS_QTYPE_AAAA;
                } else if (!strcmp(optarg, "NS")) {
                    type = DNS_QTYPE_NS;
                } else if (!strcmp(optarg, "PTR")) {
                    type = DNS_QTYPE_PTR;
                } else if (!strcmp(optarg, "CNAME")) {
                    type = DNS_QTYPE_CNAME;
                } else {
                    fprintf(stderr, "Wrong query type.\n");
                    return 2;
                }
                tflag = true;
                break;
            case 'i':
                iterative = true;
                break;
            default:
                return 2;
        }
    }

    // defaults
    if (!tflag)
        type = DNS_QTYPE_A;

    if (!Tflag)
        timeout_sec = 5;

    if (!sflag || optind >= argc || optind + 1 != argc) {
        return 2;
    }

    strncpy(hostname, argv[optind], sizeof(hostname));

    return 0;
}

void print_help() {
    printf("Usage:\n"
                   "\t./ipk-lookup [-h]\n"
                   "\t./ipk-lookup -s server [-T timeout] [-t type] [-i] name\n"
                   "Author: Tomas Kukan, xkukan00\n");
}