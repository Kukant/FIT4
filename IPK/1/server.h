/**
 * Created by Tomas Kukan, xkukan00, 11. 3. 2018
 */

#ifndef INC_1_SERVER_H
#define INC_1_SERVER_H

#include <stdio.h>

/**
 * Gets parameters.
 * @param argc
 * @param argv
 * @return
 */
int get_params(int argc, char *argv[]);

/**
 * Helpful function for creation of shared memory.
 * @param size
 * @return
 */
void* create_shared_memory(size_t size);

/**
 * Sets resources, f.e. mutex, shared variables..
 * @return
 */
int set_resources();

/**
 * Frees resources.
 */
void free_resources();

/**
 * Function that checks the shared memory, if there already is opened the file for writing.
 * @param filename
 * @return
 */
bool can_i_write_into(char *filename);

/**
 * Adds filename to shared memory table.
 * @param filename
 * @return
 */
int add_wopen_file(char *filename);

/**
 * Removes filename from shared memory.
 * @param filename
 * @return
 */
int remove_wopen_file(char *filename);
#endif //INC_1_SERVER_H
