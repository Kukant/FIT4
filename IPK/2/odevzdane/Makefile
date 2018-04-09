
CC=gcc
CFLAGS=-std=c99 -Wall -Wextra
bla=$$(cat /etc/resolv.conf | grep nameserver | cut -d " " -f2)

all: ipk-lookup

ipk-lookup: main.c functions.c dns_header.c
		$(CC) $(CFLAGS) -o ipk-lookup main.c functions.c dns_header.c $(LINKS)

clean:
		rm ipk-lookup

run:
		./ipk-lookup -s $(bla) www.google.com

runi:
		./ipk-lookup -s 8.8.8.8 -i www.web-profesionalne.cz