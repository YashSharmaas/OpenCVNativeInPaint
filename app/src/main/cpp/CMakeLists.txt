# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

#project("native-lib")

#openCV
#set(OpenCV_STATIC on)
#set(OpenCV_DIR C:/tools/OpenCV-android-sdk/sdk/native/jni)
#find_package(OpenCV REQUIRED)

# ##################### OpenCV ############################

set( OpenCV_DIR C:/tools/OpenCV-android-sdk/sdk/native/jni )

find_package(OpenCV REQUIRED )

if(OpenCV_FOUND)
    include_directories(${OpenCV_DIR})
    message(STATUS "OpenCV library status:")
    message(STATUS "    version: ${OpenCV_VERSION}")
    message(STATUS "    libraries: ${OpenCV_LIBS}")
    message(STATUS "    include path: ${OpenCV_DIR}")
else(OpenCV_FOUND)
    message(FATAL_ERROR "OpenCV library not found")
endif(OpenCV_FOUND)

# ###################### End OpenCV ###########################

include_directories(${OpenCV_INCLUDE_DIRS} "inc")

if(CMAKE_VERSION VERSION_LESS "3.1")
    if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
        set(CMAKE_CXX_FLAGS "--std=gnu++11 ${CMAKE_CXX_FLAGS}")
    endif()
else()
    set(CMAKE_CXX_STANDARD 11)
endif()

if (WIN32)
    add_definitions("-D_SCL_SECURE_NO_WARNINGS")
endif()

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        examples/inpaint_image_criminisi.cpp
        examples/patch_match.cpp
        inc/inpaint/stats.h
        inc/inpaint/patch.h
        inc/inpaint/gradient.h
        inc/inpaint/integral.h
        inc/inpaint/timer.h
        inc/inpaint/criminisi_inpainter.h
        inc/inpaint/template_match_candidates.h
        inc/inpaint/patch_match.h
        src/criminisi_inpainter.cpp
        src/template_match_candidates.cpp
        src/patch_match.cpp
        opencv-utils.cpp
        native-lib.cpp

        image_inpaint.cpp)

# Samples

add_executable(inpaint_image_criminisi examples/inpaint_image_criminisi.cpp)
target_link_libraries(inpaint_image_criminisi native-lib ${OpenCV_LIBS})

add_executable(patch_match examples/patch_match.cpp)
target_link_libraries(patch_match native-lib ${OpenCV_LIBS})

# Tests

include_directories("tests")
add_executable(inpaint_tests
        tests/catch.hpp
        tests/gradient.cpp
        tests/patch.cpp
        tests/integral.cpp
        tests/criminisi_inpainter.cpp
        tests/template_match_candidates.cpp
        tests/patch_match.cpp
        )
target_link_libraries (inpaint_tests native-lib ${OpenCV_LIBS})

# Benchmarks

include_directories("benchmarks")
add_executable(inpaint_benchmarks
        benchmarks/catch.hpp
        benchmarks/patch.cpp
        )
target_link_libraries (inpaint_benchmarks native-lib ${OpenCV_LIBS})


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library(jnigraphics-lib jnigraphics)
#find_library(linpaint-lib linpaint)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        # Links the target library to the log library
        # included in the NDK.

        ${OpenCV_LIBS}
        ${jnigraphics-lib}
        ${log-lib})

