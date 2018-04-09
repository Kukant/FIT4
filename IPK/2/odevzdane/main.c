#include <stdio.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "functions.h"
#include "dns_header.h"
#include <errno.h>
#include <stdint.h>
#include <sys/time.h>
#include <time.h>

#include "main.h"

int main(int argc, char **argv) {

    if (get_params(argc, argv) != 0) {
        fprintf(stderr, "Wrong parameters. use -h for help.\n");
        exit(0);
    }

    bool satisfied = false;

    debug_print("Args:\n\tserver: %s\n\thostname: %s\n\ttimeout: %ld\n\tqType: %d\n\tIterative: %d\n\n", server, hostname, timeout_sec, type, iterative);

    // open socket
    int s = socket(AF_INET , SOCK_DGRAM , IPPROTO_UDP); //UDP packet for DNS queries

    struct timeval timeout;
    timeout.tv_sec = timeout_sec;
    timeout.tv_usec = 0;

    if (setsockopt (s, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout)) < 0) {
        fprintf(stderr, "Error: setsockopt failed\n");
        return 2;
    }

    struct sockaddr_in dest;
    dest.sin_family = AF_INET;
    dest.sin_port = htons(53);
    dest.sin_addr.s_addr = inet_addr(server);

    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    
    // set dns query and header
    set_header((dnshdr *)buffer);

    int qlen;
    if (iterative) {
        qlen = get_root_servers_question(buffer + sizeof(dnshdr));
    } else {
        qlen = set_question((unsigned char *)(buffer + sizeof(dnshdr)), hostname, -1, type);
    }

    debug_print("Sending packet of size: dnshdr: %d qlen %d:...\n", (int)sizeof(dnshdr), qlen);
    if( sendto(s,(char*)buffer,sizeof(dnshdr) + qlen,0,(struct sockaddr*)&dest,sizeof(dest)) < 0) {
        fprintf(stderr, "Sent failed: %s\n", strerror(errno));
        return 1;
    } else {
        debug_print("Packet sent successfully.\n");
    }

    int i = sizeof dest;
    if(recvfrom (s,(char*)buffer , BUFFER_SIZE , 0 , (struct sockaddr*)&dest , (socklen_t*)&i ) < 0) {
        fprintf(stderr, "Recvfrom failed: %s\n", strerror(errno));
        return 1;
    }

    dnshdr* header;
    header=(dnshdr*)buffer;

    print_header(header);

    unsigned char name[1024];
    rrdata *p_r_data;
    unsigned char *reader = (unsigned char *) ((char *)buffer + sizeof(dnshdr) + qlen);

    if (iterative) {
        int name_len = read_name(name, (unsigned char*)buffer, reader);
        p_r_data = (rrdata *) (reader + name_len);
        print_out_line( name, p_r_data, (unsigned char*) buffer, reader, name_len);

        // ask the root server for the first ip adress
        if (ntohs(header->ancount) == 0) return 1;
        set_header((dnshdr *)buffer);
        int first_rr_len = readRR(name, (unsigned char*)buffer, reader, &p_r_data);
        read_name(name, (unsigned char*)buffer, reader + first_rr_len - ntohs(p_r_data->rd_length));
        qlen = set_question((unsigned char *)(buffer + sizeof(dnshdr)), (char *) name, DNS_QTYPE_A, 153);

        if( sendto(s,(char*)buffer,sizeof(dnshdr) + qlen,0,(struct sockaddr*)&dest,sizeof(dest)) < 0) {
            fprintf(stderr, "Sent failed: %s\n", strerror(errno));
            return 1;
        }

        i = sizeof dest;
        if(recvfrom (s,(char*)buffer , BUFFER_SIZE , 0 , (struct sockaddr*)&dest , (socklen_t*)&i ) < 0) {
            fprintf(stderr, "Recvfrom failed: %s\n", strerror(errno));
            return 1;
        }

        reader = (unsigned char *) ((char *)buffer + sizeof(dnshdr) + qlen);
        // just received the root ip address

        readRR(name, (unsigned char*)buffer, reader, &p_r_data);
        name_len = read_name(name, (unsigned char *) buffer, reader);
        print_out_line( name, p_r_data, (unsigned char*) buffer, reader, name_len);

        // copy ipv4 root address
        memcpy(root_ip, reader + name_len + sizeof(rrdata), 4);
        memcpy(&(dest.sin_addr.s_addr), root_ip, 4);

        ipv4addr ciel = get_rr((char *)root_ip, dest, s, (unsigned char *)buffer, hostname, type, type);

        if (ciel.ok == true) {
            satisfied = true;
        }
    } else {
        print_answers(buffer, qlen, &satisfied);
    }

    if (satisfied)
        return 0;
    else
        return 1;
}