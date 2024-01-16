//
// Created by yash on 20-12-2023.
//
#include <opencv2/opencv.hpp>

using namespace cv;

//int main() {
//    // Load source image and mask
//    Mat source = imread("source_image.jpg");
//    Mat mask = imread("mask_image.jpg", 0); // Ensure mask is grayscale
//
//    if (source.empty() || mask.empty()) {
//        std::cout << "Could not open or find the images!" << std::endl;
//        return -1;
//    }
//
//    // Apply inpainting to remove masked object from source image
//    Mat inpainted;
//    inpaint(source, mask, inpainted, 3, INPAINT_TELEA); // Using Telea's method
//
//    // Display the result
//    imshow("Original Image", source);
//    imshow("Mask", mask);
//    imshow("Inpainted Image", inpainted);
//    waitKey(0);
//
//    return 0;
//}
