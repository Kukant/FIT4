//
// Created by zeusko on 28/03/18.
//

#ifndef INC_2_FUNCTIONS_H
#define INC_2_FUNCTIONS_H

#include "dns_header.h"

int get_params(int argc, char** argv);

void print_help();

int set_question(char * qstart);

void set_header(dnshdr *header);
#endif //INC_2_FUNCTIONS_H
