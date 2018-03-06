cmake_minimum_required(VERSION 3.8)
project(1)

set(CMAKE_C_STANDARD 99)

set(SOURCE_FILES server.c client.c shared.c)
add_executable(1 ${SOURCE_FILES})