//
// Created by zeusko on 28/03/18.
//
#include <stdbool.h>

#ifndef INC_2_MAIN_H
#define INC_2_MAIN_H

#define BUFFER_SIZE 65536
// globals
char server[128];
long timeout;
char type[64];
bool iterative;

char hostname[256];

#define DEBUG 1
#define debug_print(...) \
            do { if (DEBUG) fprintf(stderr,__VA_ARGS__); } while (0)

// struct definitions

#endif //INC_2_MAIN_H
