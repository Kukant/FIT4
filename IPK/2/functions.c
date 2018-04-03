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
#include <netinet/in.h>
#include <arpa/inet.h>

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

void set_header(dnshdr *header) {
    header->id = htons(123);
    header->rd = iterative == true? (uint16_t) 0 : (uint16_t) 1; // recursion desired
    header->tc = 0;
    header->aa = 0;
    header->opcode = DNS_OPCODE_QUERY;
    header->qr = 0;
    header->rcode = 0; // response code
    header->zero = 0;
    header->ra = 0;
    header->qcount = htons(1);	/* question count */
    header->ancount = 0;	/* Answer record count */
    header->nscount = 0;	/* Name Server (Autority Record) Count */
    header->adcount = 0;
}

int set_question(char * qstart) {
    int qlen = 0;
    if (type == DNS_QTYPE_PTR) {
        int ret = ipv4_to_dns_format(hostname, qstart);
        if (ret < 0) { // not ipv4 format
            fprintf(stderr, "not ipv4 format\n");
            exit(1);
        } else {
            printf("\n%d\n",ret);
            query_t* query = (query_t *) (qstart + ret);
            qlen += ret;
            query->qclass = htons(DNS_QCLASS_IN);
            query->qtype = htons(type);
        }

    } else {
        dns_format(hostname, qstart);
        qlen += strlen(hostname) + 2;
        query_t* query = (query_t *) (qstart + strlen(hostname) + 2);
        query->qclass = htons(DNS_QCLASS_IN);
        query->qtype = htons(type);
    }

    return qlen + sizeof(query_t);
}

