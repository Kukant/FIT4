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
#include <errno.h>

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

int set_question(unsigned char * qstart, char *name, int qtype, int ptype) {
    if (qtype < 0) {
        qtype = type;
    }
    if (ptype < 0) {
        ptype = type;
    }
    int qlen = 0;
    if (ptype == DNS_QTYPE_PTR) {
        int ret = ipv4_to_dns_format(name, (char *)qstart);
        if (ret < 0) { // not ipv4 format
            ret = ipv6_to_dns_format(name, (char *)qstart);
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
        int len = dns_format(name, (char *)qstart);
        qlen += len;
    }

    query_t* query = (query_t *) (qstart + qlen);
    query->qclass = htons(DNS_QCLASS_IN);
    query->qtype = htons(qtype);

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
    printf("AAAA ");
    struct in6_addr addr;
    char result[200];

    memcpy(&addr, ptr, 16);

    printf("%s", inet_ntop(AF_INET6, &addr, result, 200));
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

    if (type == DNS_QTYPE_NS) {

        for (int i = 0; i < ntohs(header->nscount); i++) {
            int name_len = read_name(name, (unsigned char *) buffer, reader);
            rrdata *p_r_data = (rrdata *) (reader + name_len);

            print_out_line(name, p_r_data, (unsigned char*) buffer, reader, name_len);
            reader += name_len + sizeof(rrdata) + ntohs(p_r_data->rd_length);

            if (ntohs(p_r_data->type) == type)
                *satisfied = true;
        }
    } else {
        for (int i = 0; i < ntohs(header->ancount); i++) {
            int name_len = read_name(name, (unsigned char *) buffer, reader);
            rrdata *p_r_data = (rrdata *) (reader + name_len);
            print_out_line(name, p_r_data, (unsigned char*) buffer, reader, name_len);

            if (ntohs(p_r_data->type) == type)
                *satisfied = true;

            reader += name_len + sizeof(rrdata) + ntohs(p_r_data->rd_length);
        }
    }
}

void print_out_line(unsigned char* name, rrdata * p_r_data, unsigned char* buffer, unsigned char*reader, int name_len) {

    switch (ntohs(p_r_data->type)) {
        case DNS_QTYPE_A:
            printf("%s IN ", name);
            print_A(reader + name_len + sizeof(rrdata));
            break;
        case DNS_QTYPE_AAAA:
            printf("%s IN ", name);
            print_AAAA(reader + name_len + sizeof(rrdata));
            break;
        case DNS_QTYPE_CNAME:
            printf("%s IN ", name);
            printf("CNAME ");
            read_name(name, buffer, reader + name_len + sizeof(rrdata));
            printf("%s", name);
            break;
        case DNS_QTYPE_PTR:
            printf("%s IN ", name);
            printf("PTR ");
            read_name(name, buffer, reader + name_len + sizeof(rrdata));
            printf("%s", name);
            break;
        case DNS_QTYPE_NS:
            printf("%s IN ", name);
            printf("NS ");
            read_name(name, buffer, reader + name_len + sizeof(rrdata));
            printf("%s", name);
            break;
        case DNS_QTYPE_SOA:
            fprintf(stderr, "\nError: got SOA\n");
            exit(1);
        default:
            fprintf(stderr, "Server error / Unknown type %d\n", ntohs(p_r_data->type));
            exit(1);

    }
    printf("\n");
}

ipv4addr get_rr(char* first_ip, struct sockaddr_in dest, int s, unsigned char *buffer, char *this_hostname, int qtype, int this_type){
    ipv4addr ip;
    dnshdr *header = (dnshdr *) buffer;
    memcpy(&(dest.sin_addr.s_addr), first_ip, 4);

    // set new question
    set_header((dnshdr *) buffer);
    int qlen = set_question(buffer + sizeof(dnshdr), this_hostname, qtype, this_type);

    if( sendto(s,(char*)buffer,sizeof(dnshdr) + qlen, 0, (struct sockaddr*)&dest,sizeof(dest)) < 0) {
        fprintf(stderr, "1Sent failed: %s\n", strerror(errno));
        ip.ok = false;
        return ip;
    }

    int i = sizeof dest;
    if(recvfrom (s,(char*)buffer , BUFFER_SIZE , 0 , (struct sockaddr*)&dest , (socklen_t*)&i ) < 0) {
        fprintf(stderr, "Recvfrom failed: %s\n", strerror(errno));
        ip.ok = false;
        return ip;
    }

    unsigned char name[1024];
    rrdata *p_r_data;
    unsigned char * reader;
    // main loop
    while (header->aa != 1 && ntohs(header->opcode) == 0) {
        if (ntohs(header->adcount) == 0) {
            unsigned char ns_hostname[1024];
            reader = buffer + sizeof(dnshdr) + qlen;
            rrdata *prdata;
            int rrlen = readRR(ns_hostname, buffer, reader, &prdata);
            read_name(ns_hostname, buffer, reader + rrlen - ntohs(prdata->rd_length));
            ip = get_rr((char *)root_ip, dest, s, buffer, (char *) ns_hostname, DNS_QTYPE_A, 125);
            reader = (unsigned char*) &(ip.addr);
            debug_print("Back from recursion.\n");
            if (!ip.ok) return ip;
        } else {
            reader = (unsigned char *) ((char *)buffer + sizeof(dnshdr) + qlen);

            // print ns
            readRR(name, buffer, reader, &p_r_data);
            int name_len = read_name(name,  buffer, reader);

            print_out_line( name, p_r_data, buffer, reader, name_len);

            // jump over all RRs to additional records
            int rrcount = ntohs(header->ancount) + ntohs(header->nscount);
            for (int i = 0; i < rrcount; i++) {
                int len = readRR(name, buffer, reader, &p_r_data);
                reader += len;
            }

            // first additional
            int rrlen = readRR(name, buffer, reader, &p_r_data);
            name_len = read_name(name, buffer, reader);

            if (ntohs(p_r_data->type) == DNS_QTYPE_AAAA) {
                // second will have ipv4 address
                reader += (unsigned char) rrlen;
                readRR(name, buffer, reader, &p_r_data);
                name_len = read_name(name, buffer, reader);
            }

            print_out_line(name, p_r_data,  buffer, reader, name_len);

            reader += readRR(name, buffer, reader, &p_r_data);
            reader -= ntohs(p_r_data->rd_length);
        }


        // set new question
        set_header((dnshdr *) buffer);
        qlen = set_question(buffer + sizeof(dnshdr), this_hostname, qtype, this_type);

        // copy ipv4 adress
        memcpy(&(dest.sin_addr.s_addr), reader, 4);

        if(sendto(s, (char*)buffer,sizeof(dnshdr) + qlen, 0, (struct sockaddr*)&dest, sizeof(dest)) < 0) {
            fprintf(stderr, "Sent failed: %s\n", strerror(errno));
            ip.ok = false;
            return ip;
        }

        i = sizeof dest;
        if(recvfrom (s,(char*)buffer , BUFFER_SIZE , 0 , (struct sockaddr*)&dest , (socklen_t*)&i ) < 0) {
            fprintf(stderr, "Recvfrom failed: %s\n", strerror(errno));
            ip.ok = false;
            return ip;
        }
    }

    if (header->aa == 1) {
        debug_print("Success\n");
        reader = (unsigned char *) ((char *)buffer + sizeof(dnshdr) + qlen);
        readRR(name, buffer, reader, &p_r_data);
        int name_len = read_name(name,  buffer, reader);

        print_out_line( name, p_r_data, buffer, reader, name_len);
        memcpy(&(ip.addr),  reader + name_len + sizeof(rrdata), 4);
        ip.ok = true;
        return ip;
    } else {
        debug_print("Nothing found\n");
        ip.ok = false;
        return ip;
    }
}
