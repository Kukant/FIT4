//
// Created by zeusko on 28/03/18.
//
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <arpa/inet.h>
#include "dns_header.h"
#include "main.h"

int dns_format ( char *in_str, char *dest)
{
    int len = (int) strlen(in_str);
    if (in_str[len - 1] != '.') {
        in_str[len] = '.';
        in_str[len + 1] = '\0';
        len++;
    }
    unsigned char c = 0;

    for(int i = len - 1;i >= 0;i--){
        if(in_str[i] == '.') {
            dest[i + 1] = c;
            c = 0;
        }
        else {
            dest[i + 1] = in_str[i];
            c++;
        }
    }
    dest[0] = c;

    return len + 1;
}

/**
 *
 * @param src
 * @param dst
 * @return -1 on invalid ipv4 adress
 */
int ipv4_to_dns_format(char *src, char *dst) {
    unsigned char address[16];
    if (inet_pton(AF_INET, src, address) != 1) {
        return -1;
    }
    char str[64];
    memset(str, 0, 64);

    sprintf(str, "%u.%u.%u.%u.in-addr.arpa", address[3], address[2], address[1], address[0]);
    int len = dns_format(str, dst);

    return len;
}

int ipv6_to_dns_format(char *src, char *dst) {
    unsigned char address[16];
    if (inet_pton(AF_INET6, src, address) != 1) {
        return -1;
    }

    char res[128] = {'\0'};
    for (int i = 31; i >= 0; i--) {
        char str[5];
        unsigned char n = address[i/2];
        if (i % 2 == 0) { // bottom 4 bits
            sprintf(str, "%x.", (n & 0xf0) >> 4);
        } else { // top 4 bits
            sprintf(str, "%x.", (n & 0x0f) );
        }
        strcat(res, str);
    }
    strcat(res, "ip6.arpa");
    int len = dns_format(res, dst);

    return len;
}

int read_name(unsigned char *name, unsigned char* buffer, unsigned char *reader) {

    int count = 1;
    int name_index = 0;
    bool jumped = false;
    unsigned char *old_reader = reader;

    while (*reader != '\0') {
        if (*reader >= 0b11000000) {
            int offset = (*reader - 0b11000000) * 256 + *(reader + 1);
            reader = buffer + offset;
            jumped = true;
            continue;
        }

        name[name_index++] = *reader;
        if (!jumped)
            count++;

        reader++;
    }

    name[name_index] = '\0';
    if (jumped)
        count++;

    int i;
    for (i = 0; i < (int) strlen((char *)name); i++) {

        unsigned char c = name[i];
        for (unsigned char j = 0; j < c; j++) {
            name[i] = name[i+1];
            i++;
        }
        name[i] = '.';
    }

    name[i]='\0';

    reader = old_reader;

    return count;
}

/**
 *
 * @param rr_start
 * @return rr lenght
 */
int readRR(unsigned char *name, unsigned char *buffer, unsigned char *reader, rrdata **rr_data) {
    int len = 0;
    len += read_name(name, buffer, reader);
    *rr_data = ((rrdata *) (reader + len));
    len += ntohs((*rr_data)->rd_length);
    len += sizeof(rrdata);

    return len;
}