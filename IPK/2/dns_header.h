//
// Created by zeusko on 28/03/18.
//

#ifndef INC_2_DNS_HEADER_H
#define INC_2_DNS_HEADER_H

#define DNS_OPCODE_QUERY	0

#include <endian.h>
#include <stdint.h>
#include <strings.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>


typedef struct {
    uint16_t id;

    uint16_t rd:1;
    uint16_t tc:1;
    uint16_t aa:1;
    uint16_t opcode:4;
    uint16_t qr:1;
    uint16_t rcode:4;
    uint16_t zero:3;
    uint16_t ra:1;
    uint16_t qcount;	/* question count */
    uint16_t ancount;	/* Answer record count */
    uint16_t nscount;	/* Name Server (Autority Record) Count */
    uint16_t adcount;	/* Additional Record Count */
} dnshdr;

typedef struct {
    uint16_t qtype;
    uint16_t qclass;
}query_t;

//#pragma pack(push, 1)
#pragma pack(2)
typedef struct {
    uint16_t type;
    uint16_t class;
    uint32_t ttl;
    uint16_t rd_length;
} rrdata;

typedef struct {
    bool ok;
    bool is_ipv6;
    unsigned char addr[16];
} ipv4addr;
//#pragma pack(pop)

#define DNS_QTYPE_A		    1
#define DNS_QTYPE_NS		2
#define DNS_QTYPE_CNAME		5
#define DNS_QTYPE_SOA		6
#define DNS_QTYPE_PTR		12

#define DNS_QTYPE_AAAA		28

#define DNS_QCLASS_IN		1


int dns_format ( char *in_str, char *dest);

int ipv4_to_dns_format(char *src, char *dst);

int ipv6_to_dns_format(char *src, char *dst);

/**
 *
 * @param name
 * @param buffer
 * @return count
 */
int read_name(unsigned char *name, unsigned char* buffer, unsigned char *reader);

int readRR(unsigned char *name, unsigned char* buffer, unsigned char *reader, rrdata **rr_data);

#endif //INC_2_DNS_HEADER_H
