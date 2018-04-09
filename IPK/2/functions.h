//
// Created by zeusko on 28/03/18.
//

#ifndef INC_2_FUNCTIONS_H
#define INC_2_FUNCTIONS_H

#include <stdbool.h>
#include <netinet/in.h>
#include "dns_header.h"

int get_params(int argc, char** argv);

void print_help();

int set_question(unsigned char * qstart, char *name, int qtype, int ptype);
int get_root_servers_question(char *qstart);

void set_header(dnshdr *header);

void print_A(unsigned char *ptr);
void print_AAAA(unsigned char *ptr);

void print_header (dnshdr *header);
void print_answers(char *buffer, int qlen, bool *satisfied);
void print_out_line(unsigned char* name, rrdata * p_r_data, unsigned char* buffer, unsigned char*reader, int name_len);
/**
 *
 * @param first_ip
 * @param dest
 * @param s
 * @param buffer
 * @param this_hostname
 * @return Pointer at IP address or NULL.
 */
ipv4addr get_rr(char* first_ip, struct sockaddr_in dest, int s, unsigned char *buffer, char *this_hostname, int qtype, int this_type);

#endif //INC_2_FUNCTIONS_H
