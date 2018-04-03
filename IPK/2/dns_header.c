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
    int i, len = (int) strlen(in_str);
    unsigned char c = 0;
    char *buf = dest;
    if (!buf) {
        return -1;
    }

    buf[len+1] = 0;
    for(i=len - 1;i>=0;i--){
        if(in_str[i] == '.') {
            buf[i + 1] = c;
            c = 0;
        }
        else {
            buf[i + 1] = in_str[i];
            c++;
        }
    }
    buf[0] = c;

    return 0;
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
    dns_format(str, dst);

    return (int)strlen(str) + 2;
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
    dns_format(res, dst);

    return (int)strlen(res) + 2;
}
