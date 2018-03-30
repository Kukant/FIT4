//
// Created by zeusko on 28/03/18.
//
#include <string.h>
#include <stdlib.h>
#include <stdint.h>
#include "dns_header.h"

int dns_format ( char *url, char *dest)
{
    int i, len = (int) strlen(url);
    unsigned char c = 0;
    char *buf = dest;
    if (!buf) {
        return -1;
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
    return 0;
}

