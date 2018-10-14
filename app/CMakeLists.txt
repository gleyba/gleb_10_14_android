cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_STANDARD 14)

include_directories(
    src/main/cpp/src/
)

file(
    GLOB
    SRCS
    src/main/cpp/jni/*.cpp
    src/main/cpp/jni/*.hpp
    src/main/cpp/src/*.cpp
    src/main/cpp/src/*.hpp
)

include_directories(src/main/cpp/deps/include)

add_library(
    native-lib
    SHARED
    ${SRCS}
)

find_library(
    log-lib
    log
)

target_link_libraries(
    native-lib
    ${log-lib}
)