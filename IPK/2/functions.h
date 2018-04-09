//
// Created by zeusko on 28/03/18.
//

#ifndef INC_2_FUNCTIONS_H
#define INC_2_FUNCTIONS_H

#include <stdbool.h>
#include "dns_header.h"

int get_params(int argc, char** argv);

void print_help();

int set_question(char * qstart, char *name);
int get_root_servers_question(char *qstart);

void set_header(dnshdr *header);

void print_A(unsigned char *ptr);
void print_AAAA(unsigned char *ptr);

void print_header (dnshdr *header);
void print_answers(char *buffer, int qlen, bool *satisfied);
#endif //INC_2_FUNCTIONS_H
