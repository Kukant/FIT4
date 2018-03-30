#include <stdio.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "functions.h"
#include "dns_header.h"
#include <errno.h>

#include "main.h"

int main(int argc, char **argv) {
    if (get_params(argc, argv) != 0) {
        fprintf(stderr, "Wrong parameters. use -h for help.\n");
        exit(0);
    }

    debug_print("Args:\n\tserver: %s\n\thostname: %s\n", server, hostname);

    int s = socket(AF_INET , SOCK_DGRAM , IPPROTO_UDP); //UDP packet for DNS queries

    struct sockaddr_in dest;
    dest.sin_family = AF_INET;
    dest.sin_port = htons(53);
    dest.sin_addr.s_addr = inet_addr(server); //dns servers

    char buffer[BUFFER_SIZE];
    memset(buffer, 0, BUFFER_SIZE);
    
    // set header
    dnshdr *header = (dnshdr *) buffer;
    header->id = htons(123);
    header->rd = 1;
    header->tc = 0;
    header->aa = 0;
    header->opcode = DNS_OPCODE_QUERY;
    header->qr = 0;
    header->rcode = 0;
    header->zero = 0;
    header->ra = 0;
    header->qcount = htons(1);	/* question count */
    header->ancount = 0;	/* Answer record count */
    header->nscount = 0;	/* Name Server (Autority Record) Count */
    header->adcount = 0;

    dns_format(hostname, buffer + sizeof(dnshdr));
    query_t* query = (query_t *) buffer + sizeof(dnshdr) + strlen(hostname);
    query->qclass = htons(DNS_QCLASS_IN);
    query->qtype = htons(DNS_QTYPE_A);

    debug_print("Sending Packet...\n");
    if( sendto(s,(char*)buffer,sizeof(dnshdr) + (strlen((const char*)hostname)+1) + sizeof(query_t),0,(struct sockaddr*)&dest,sizeof(dest)) < 0)
    {
        fprintf(stderr, "Sent failed: %s\n", strerror(errno));
    } else {
        debug_print("Packet sent successfully.\n");
    }

    int i = sizeof dest;
    recv(s, buffer, BUFFER_SIZE, 0);
    //if(recvfrom (s,(char*)buffer , BUFFER_SIZE , 0 , (struct sockaddr*)&dest , (socklen_t*)&i ) < 0)
    //{
     //   perror("recvfrom failed");
    //}
    header=(dnshdr*)buffer;

    printf("nThe response contains : ");
    printf("n %d Questions.",ntohs(header->qcount));
    printf("n %d Answers.",ntohs(header->ancount));
    printf("n %d Authoritative Servers.",ntohs(header->nscount));
    printf("n %d Additional records.nn",ntohs(header->adcount));




    return 0;
}