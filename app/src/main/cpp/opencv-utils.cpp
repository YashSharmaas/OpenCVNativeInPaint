//
// Created by yash on 08-12-2023.
//
#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>

void myFlip(Mat src){

flip(src, src, 0);

}

void myBlur(Mat src, float sigma) {

GaussianBlur(src, src, Size(), sigma);

}