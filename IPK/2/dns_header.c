//
// Created by zeusko on 28/03/18.
//
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

char* dns_format ( char *url, int *size, unsigned short int qtype, unsigned short int qclass )
{
    int i, c = 0, len = (int) strlen(url);
    char *buf = (char *) malloc(len+6);
    if (!buf) {
        return NULL;
    }

    buf[len+1] = 0;
    for(i=len - 1;i>=0;i--){
        if(url[i] == '.') {
            buf[i + 1] = c;
            c = 0;
        }
        else {
            buf[i + 1] = url[i];
            c++;
        }
    }
    buf[0] = c;

    uint16_t* qt = buf+len+2;
    *qt = qtype;
    uint16_t* qc = buf+len+4;
    *qc = qclass;

    if(size) *size = len+6;
    return buf;
}

