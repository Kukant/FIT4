# Makefile for IPK1 project, version 2
# Tomas Kukan

CC=gcc
CFLAGS=-std=c99 -Wall -Wextra -pedantic


all: client server

client: client.c
		$(CC) $(CFLAGS) -o ipk-client client.c

server: server.c
		$(CC) $(CFLAGS) -o ipk-server server.c
