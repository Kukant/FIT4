

CC=gcc
CFLAGS=-std=gnu99 -Wall -Wextra -pedantic -g
LINKS=-lpthread


all: ipk-lookup

ipk-lookup: main.c functions.c dns_header.c
		$(CC) $(CFLAGS) -o ipk-lookup main.c functions.c dns_header.c $(LINKS)

clean:
		rm ipk-lookup

run:
		./ipk-lookup -s 127.0.0.1 www.google.com
