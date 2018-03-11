# Makefile for IPK1 project, version 2
# Tomas Kukan

CC=gcc
CFLAGS=-std=gnu99 -Wall -Wextra -pedantic -g
LINKS=-lpthread


all: client server

client: client.c shared.c
		$(CC) $(CFLAGS) -o ipk-client shared.c client.c $(LINKS)

server: server.c shared.c
		$(CC) $(CFLAGS) -o ipk-server shared.c server.c $(LINKS)
