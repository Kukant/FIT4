//
// Created by zeusko on 03/03/18.
//

#ifndef INC_1_SERVER_H
#define INC_1_SERVER_H

#include <stdio.h>

int get_params(int argc, char *argv[]);
void* create_shared_memory(size_t size);
int set_resources();
void free_resources();
bool can_i_write_into(char *filename);
int add_wopen_file(char *filename);
int remove_wopen_file(char *filename);
void print_shared_table();
#endif //INC_1_SERVER_H
