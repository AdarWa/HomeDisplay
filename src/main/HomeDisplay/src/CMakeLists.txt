# This file was automatically generated for projects
# without default 'CMakeLists.txt' file.

#FILE(GLOB_RECURSE app_sources ${CMAKE_SOURCE_DIR}/src/*.*)

idf_component_register(SRCS main.c lcd/lcd.c lcd/touch.c
        INCLUDE_DIRS "."
        REQUIRES esp_lcd esp_timer driver
)
