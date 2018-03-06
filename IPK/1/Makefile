# Makefile for IPK1 project, version 2
# Tomas Kukan

CC=gcc
CFLAGS=-std=c99 -Wall -Wextra -pedantic -g


all: client server

client: client.c shared.c
		$(CC) $(CFLAGS) -o ipk-client shared.c client.c
		cp ipk-client clientdir

server: server.c shared.c
		$(CC) $(CFLAGS) -o ipk-server shared.c server.c
