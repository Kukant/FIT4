#include <stdio.h>
#include <stdlib.h>
#include "functions.h"
#include "dns_header.h"

#include "main.h"

int main(int argc, char **argv) {
    if (get_params(argc, argv) != 0) {
        fprintf(stderr, "Wrong parameters. use -h for help.\n");
        exit(0);
    }
    int size;
    char *ret = dns_format("www.google.com", &size, DNS_QTYPE_AAAA, DNS_QCLASS_IN);
    for (int i = 0; i < size; i++) {
        if (ret[i] < 123 && ret[i] > 96) printf("%c", ret[i]);
        else {
            printf("'%d'",ret[i]);
        }
    }
    return 0;
}