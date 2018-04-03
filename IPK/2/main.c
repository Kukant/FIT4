#include <stdio.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "functions.h"
#include "dns_header.h"
#include <errno.h>
#include <stdint.h>

#include "main.h"

int main(int argc, char **argv) {

    if (get_params(argc, argv) != 0) {
        fprintf(stderr, "Wrong parameters. use -h for help.\n");
        exit(0);
    }

    debug_print("Args:\n\tserver: %s\n\thostname: %s\n\ttimeout: %ld\n\tqType: %d\n", server, hostname, timeout_sec, type);

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
    
    // set dns query
    set_header((dnshdr *)buffer);
    int qlen = set_question(buffer + sizeof(dnshdr));

    debug_print("Sending Packet...\n");
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

    debug_print("\n Response code: %d.",header->rcode);
    debug_print("\nThe response contains : ");
    debug_print("\n %d Questions.",ntohs(header->qcount));
    debug_print("\n %d Answers.",ntohs(header->ancount));
    debug_print("\n %d Authoritative Servers.",ntohs(header->nscount));
    debug_print("\n %d Additional records.\n",ntohs(header->adcount));




    return 0;
}