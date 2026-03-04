cmake_minimum_required(VERSION 3.16.0)

if(DEFINED ENV{PIOENV})
    set(PIO_LIBDEPS_DIR "${CMAKE_CURRENT_LIST_DIR}/.pio/libdeps/$ENV{PIOENV}")
    if(EXISTS "${PIO_LIBDEPS_DIR}/lvgl")
        list(APPEND EXTRA_COMPONENT_DIRS "${PIO_LIBDEPS_DIR}/lvgl")
    endif()
endif()

file(GLOB PIO_LVGL_DIRS "${CMAKE_CURRENT_LIST_DIR}/.pio/libdeps/*/lvgl")
if(PIO_LVGL_DIRS)
    list(APPEND EXTRA_COMPONENT_DIRS ${PIO_LVGL_DIRS})
endif()


include($ENV{IDF_PATH}/tools/cmake/project.cmake)
project(HomeDisplay)
