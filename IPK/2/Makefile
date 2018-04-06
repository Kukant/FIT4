
CC=gcc
CFLAGS=-std=gnu99 -Wall -Wextra -pedantic -g
LINKS=-lpthread
bla=$$(cat /etc/resolv.conf | grep nameserver | cut -d " " -f2)

all: ipk-lookup

ipk-lookup: main.c functions.c dns_header.c
		$(CC) $(CFLAGS) -o ipk-lookup main.c functions.c dns_header.c $(LINKS)

clean:
		rm ipk-lookup

run:
		./ipk-lookup -s $(bla) -i www.google.com
