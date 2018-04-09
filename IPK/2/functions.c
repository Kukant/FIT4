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
    header->id = htons(1234);
    header->rd = iterative?  0b0 : 0b1; // recursion desired
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

int set_question(char * qstart, char *name) {
    int qlen = 0;
    if (type == DNS_QTYPE_PTR) {
        int ret = ipv4_to_dns_format(name, qstart);
        if (ret < 0) { // not ipv4 format
            ret = ipv6_to_dns_format(name, qstart);
            if (ret > 0) {
                debug_print("Got ipv6 format\n");
                qlen += ret;
            } else {
                fprintf(stderr, "Wrong PTR format.\n");
                exit(2);
            }
        } else {
            debug_print("Got ipv4 format\n");
            qlen += ret;
        }

    } else {
        int len = dns_format(name, qstart);
        qlen += len;
    }

    query_t* query = (query_t *) (qstart + qlen);
    query->qclass = htons(DNS_QCLASS_IN);
    query->qtype = htons(type);

    return qlen + sizeof(query_t);
}

void print_A(unsigned char *ptr) {
    printf("A ");
    struct in_addr addr;
    char result[150];

    memcpy(&addr, ptr, 4);

    printf("%s", inet_ntop(AF_INET, &addr, result, 150));
}

void print_AAAA(unsigned char *ptr) {
    printf("A ");
    struct in_addr addr;
    char result[1024];

    memcpy(&addr, ptr, 16);

    printf("%s", inet_ntop(AF_INET6, &addr, result, 150));
}

int get_root_servers_question(char *qstart) {
    qstart[0] = 0;
    query_t* query = (query_t *) (qstart + 1);
    query->qclass = htons(DNS_QCLASS_IN);
    query->qtype = htons(DNS_QTYPE_NS);

    return 1 + sizeof(query_t);
}

void print_header (dnshdr *header) {
    debug_print("\n Response code: %d.",header->rcode);
    debug_print("\n The response contains : ");
    debug_print("\n %d Questions.",ntohs(header->qcount));
    debug_print("\n %d Answers.",ntohs(header->ancount));
    debug_print("\n %d Authoritative Servers.",ntohs(header->nscount));
    debug_print("\n %d Additional records.\n\n",ntohs(header->adcount));
}

void print_answers(char *buffer, int qlen, bool *satisfied) {

    dnshdr *header = (dnshdr *)buffer;
    unsigned char *reader = (unsigned char *) (buffer + sizeof(dnshdr) + qlen);
    unsigned char name[1024];

    for (int i = 0; i < ntohs(header->ancount); i++) {
        int count = read_name(name, (unsigned char *) buffer, reader);
        rrdata *p_r_data = (rrdata *) (reader + count);
        printf("%s IN ", name);
        switch(ntohs(p_r_data->type)){
            case DNS_QTYPE_A:
                print_A(reader + count + sizeof(rrdata));
                break;
            case DNS_QTYPE_AAAA:
                print_AAAA(reader + count + sizeof(rrdata));
                break;
            case DNS_QTYPE_CNAME:
                printf("CNAME ");
                read_name(name, (unsigned char *) buffer, reader + count + sizeof(rrdata));
                printf("%s", name);
                break;
            case DNS_QTYPE_PTR:
                printf("PTR ");
                read_name(name, (unsigned char *) buffer, reader + count + sizeof(rrdata));
                printf("%s", name);
                break;
            default:
                fprintf(stderr, "Unknown type %d\n", ntohs(p_r_data->type));
                break;
        }

        if (ntohs(p_r_data->type) == type)
            *satisfied = true;
        printf("\n");

        reader += count + sizeof(rrdata) + ntohs(p_r_data->rd_length);
    }
}
