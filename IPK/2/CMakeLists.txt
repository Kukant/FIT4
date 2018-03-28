cmake_minimum_required(VERSION 3.8)
project(2)

set(CMAKE_C_STANDARD 99)

set(SOURCE_FILES main.c functions.c dns_header.c)
add_executable(2 ${SOURCE_FILES})