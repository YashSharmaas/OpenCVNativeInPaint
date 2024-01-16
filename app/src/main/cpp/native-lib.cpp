#include <jni.h>
#include <string>
#include "opencv-utils.h"
#include "android/bitmap.h"
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include "inc/inpaint/criminisi_inpainter.h"
#include <android/log.h>


void bitmapToMat(JNIEnv * env, jobject bitmap, Mat& dst, jboolean needUnPremultiplyAlpha)
{
AndroidBitmapInfo  info;
void*              pixels = 0;

try {
CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
CV_Assert( pixels );
dst.create(info.height, info.width, CV_8UC4);
if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
{
Mat tmp(info.height, info.width, CV_8UC4, pixels);
if(needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
else tmp.copyTo(dst);
} else {
// info.format == ANDROID_BITMAP_FORMAT_RGB_565
Mat tmp(info.height, info.width, CV_8UC2, pixels);
cvtColor(tmp, dst, COLOR_BGR5652RGBA);
}
AndroidBitmap_unlockPixels(env, bitmap);
return;
} catch(cv::Exception e) {
AndroidBitmap_unlockPixels(env, bitmap);
jclass je = env->FindClass("java/lang/Exception");
env->ThrowNew(je, e.what());
return;
} catch (...) {
AndroidBitmap_unlockPixels(env, bitmap);
jclass je = env->FindClass("java/lang/Exception");
env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
return;
}
}

void matToBitmap(JNIEnv * env, Mat src, jobject bitmap, jboolean needPremultiplyAlpha)
{
AndroidBitmapInfo  info;
void*              pixels = 0 ;

try {
CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width == (uint32_t)src.cols );
CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
CV_Assert( pixels );
if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
{
Mat tmp(info.height, info.width, CV_8UC4, pixels);
if(src.type() == CV_8UC1)
{
cvtColor(src, tmp, COLOR_GRAY2RGBA);
} else if(src.type() == CV_8UC3){
cvtColor(src, tmp, COLOR_RGB2RGBA);
} else if(src.type() == CV_8UC4){
if(needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
else src.copyTo(tmp);
}
} else {
// info.format == ANDROID_BITMAP_FORMAT_RGB_565
Mat tmp(info.height, info.width, CV_8UC2, pixels);
if(src.type() == CV_8UC1)
{
cvtColor(src, tmp, COLOR_GRAY2BGR565);
} else if(src.type() == CV_8UC3){
cvtColor(src, tmp, COLOR_RGB2BGR565);
} else if(src.type() == CV_8UC4){
cvtColor(src, tmp, COLOR_RGBA2BGR565);
}
}
AndroidBitmap_unlockPixels(env, bitmap);
return;
} catch(cv::Exception e) {
AndroidBitmap_unlockPixels(env, bitmap);
jclass je = env->FindClass("java/lang/Exception");
env->ThrowNew(je, e.what());
return;
} catch (...) {
AndroidBitmap_unlockPixels(env, bitmap);
jclass je = env->FindClass("java/lang/Exception");
env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
return;
}
}

/*extern "C" JNIEXPORT jstring JNICALL
Java_com_example_yrmultimediaco_opencvnativeinpaint_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject *//* this *//*) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}*/

extern "C" JNIEXPORT void JNICALL
Java_com_example_yrmultimediaco_opencvnativeinpaint_MainActivity_myFlip(JNIEnv* env, jobject, jobject bitmapIn, jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    myFlip(src);
    matToBitmap(env, src, bitmapOut, false);

}

extern "C" JNIEXPORT void JNICALL
Java_com_example_yrmultimediaco_opencvnativeinpaint_MainActivity_myBlur(JNIEnv* env, jobject, jobject bitmapIn, jobject bitmapOut, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    myBlur(src, sigma);
    matToBitmap(env, src, bitmapOut, false);
}


//extern "C" JNIEXPORT void JNICALL
//Java_com_example_yrmultimediaco_opencvnativeinpaint_MainActivity_myInpaint(
//        JNIEnv* env, jobject,
//        jobject bitmapIn, jobject sourceMaskBitmap, jobject targetMaskBitmap, jint patchSize) {
//
//    // Convert input bitmaps to Mat
//    Mat src, sourceMask, targetMask;
//    bitmapToMat(env, bitmapIn, src, false);
//    bitmapToMat(env, sourceMaskBitmap, sourceMask, false);
//    bitmapToMat(env, targetMaskBitmap, targetMask, false);
//
//    /*// Ensure the input image format is 3 channels (RGB) with 8-bit depth (CV_8U)
//    if (src.channels() == 4) {
//        // If the image has 4 channels (likely ARGB or RGBA), convert it to 3 channels (RGB)
//        cvtColor(src, src, cv::COLOR_RGBA2RGB);
//    } else if (src.channels() != 3 || src.depth() != CV_8U) {
//        // If the image doesn't meet the required format, handle the error accordingly
//        // Log an error or throw an exception
//        __android_log_print(ANDROID_LOG_ERROR, "MyInpaint", "Invalid input image format");
//        return;
//    }*/
//
//    // Check properties before inpainting
//
//    // Log properties of the Mats obtained after conversion
//    __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Input image channels: %d, depth: %d", src.channels(), src.depth());
//    __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Input image size: %dx%d", src.cols, src.rows);
//    __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Target Mask channels: %d, depth: %d", targetMask.channels(), targetMask.depth());
//    __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Target Mask size: %dx%d", targetMask.cols, targetMask.rows);
//    __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Source Mask channels: %d, depth: %d", sourceMask.channels(), sourceMask.depth());
//    __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Source Mask size: %dx%d", sourceMask.cols, sourceMask.rows);
//
//    // Check number of channels and depth in src (input image)
//    if (src.channels() == 3 && src.depth() == CV_8U) { // For ARGB_8888 format
//
//        __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Image properties are valid.");
//
//        // Check mask sizes
//        if (targetMask.size() == src.size() && (sourceMask.empty() || targetMask.size() == sourceMask.size())) {
//
//            __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Mask sizes are valid.");
//
//            // Check patch size
//            if (patchSize > 0) {
//
//                __android_log_print(ANDROID_LOG_DEBUG, "MyInpaint", "Patch size is valid.");
//
//                // Perform inpainting using the Criminisi algorithm
//                Inpaint::inpaintCriminisi(src, sourceMask, targetMask, patchSize);
//
//                // Convert the output Mat back to a Bitmap
//                matToBitmap(env, src, bitmapIn, false);
//
//            } else {
//                __android_log_print(ANDROID_LOG_ERROR, "MyInpaint", "Invalid patch size.");
//                // Handle error: Patch size is not valid
//                // Log an error or throw an exception
//            }
//
//        } else {
//            __android_log_print(ANDROID_LOG_ERROR, "MyInpaint", "Mask sizes don't meet the required conditions.");
//            // Handle error: Mask sizes don't meet the required conditions
//            // Log an error or throw an exception
//        }
//
//    } else {
//        __android_log_print(ANDROID_LOG_ERROR, "MyInpaint", "Image doesn't meet the required conditions.");
//        // Handle error: Image doesn't meet the required conditions
//        // Log an error or throw an exception
//    }
//
//
//
//
//}

extern "C" JNIEXPORT void JNICALL
Java_com_example_yrmultimediaco_opencvnativeinpaint_InpaintActivity_myInpaint(JNIEnv *env, jobject thiz, jstring source_img,
                                                                    jstring mask_img, jstring inpaint_img,
                                                                    jint patch_size) {
    const char *source_path = env->GetStringUTFChars(source_img, nullptr);
    const char *mask_path = env->GetStringUTFChars(mask_img, nullptr);
    const char *inpaint_path = env->GetStringUTFChars(inpaint_img, nullptr);

    std::cout << "* Lalit 1 "  << std::endl;

    cv::Mat maskImage = cv::imread(mask_path, cv::IMREAD_GRAYSCALE);
    cv::Mat inputImage = cv::imread(source_path);
    cv::Mat inpaitMat;

    std::cout << "* Lalit 2 "  << std::endl;
    env->ReleaseStringUTFChars(source_img, source_path);
    env->ReleaseStringUTFChars(mask_img, mask_path);

//    cv::Mat newMask;
//    cv::bitwise_not(maskImage, newMask);
//
//    cv::Mat convertedMask;
//    newMask.convertTo(convertedMask, CV_8UC1);

    std::cout << "* Lalit 3 "  << std::endl;
    inpaint(inputImage, maskImage, inpaitMat, patch_size, INPAINT_NS);

    std::cout << "* Lalit 4 "  << std::endl;
    cv::imwrite(inpaint_path, inpaitMat);
}


//extern "C" JNIEXPORT void JNICALL
//Java_com_example_yrmultimediaco_opencvnativeinpaint_InpaintActivity_myInpaintImage(JNIEnv *env,
//                                                                              jobject thiz,
//                                                                              jstring source_img,
//                                                                              jstring mask_img,
//                                                                              jstring inpaint_img,
//                                                                              jint patch_size) {
//    const char *source_path = env->GetStringUTFChars(source_img, nullptr);
//    const char *mask_path = env->GetStringUTFChars(mask_img, nullptr);
//    const char *inpaint_path = env->GetStringUTFChars(inpaint_img, nullptr);
//
//    std::cout << "* Lalit 1 "  << std::endl;
//
//    cv::Mat maskImage = cv::imread(mask_path, cv::IMREAD_GRAYSCALE);
//    cv::Mat inputImage = cv::imread(source_path);
//    cv::Mat inpaitMat;
//
//    std::cout << "* Lalit 2 "  << std::endl;
//    env->ReleaseStringUTFChars(source_img, source_path);
//    env->ReleaseStringUTFChars(mask_img, mask_path);
//
//    cv::Mat newMask;
//    cv::bitwise_not(maskImage, newMask);
//
//    cv::Mat convertedMask;
//    newMask.convertTo(convertedMask, CV_8UC1);
//
//    std::cout << "* Lalit 3 "  << std::endl;
//    inpaint(inputImage, convertedMask, inpaitMat, patch_size, INPAINT_NS);
//
//    std::cout << "* Lalit 4 "  << std::endl;
//    cv::imwrite(inpaint_path, inpaitMat);
//}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_yrmultimediaco_opencvnativeinpaint_InpaintActivity_MyInPaintExample(JNIEnv *env,
                                                                                     jobject thiz,
                                                                                     jstring source_img,
                                                                                     jstring mask_img,
                                                                                     jstring inpaint_img,
                                                                                     jint patch_size) {
    const char *source_path = env->GetStringUTFChars(source_img, nullptr);
    const char *mask_path = env->GetStringUTFChars(mask_img, nullptr);
    const char *inpaint_path = env->GetStringUTFChars(inpaint_img, nullptr);

    std::cout << "* Lalit 1 "  << std::endl;

    cv::Mat maskImage = cv::imread(mask_path, cv::IMREAD_GRAYSCALE);
    cv::Mat inputImage = cv::imread(source_path);
    cv::Mat inpaitMat;

    std::cout << "* Lalit 2 "  << std::endl;
    env->ReleaseStringUTFChars(source_img, source_path);
    env->ReleaseStringUTFChars(mask_img, mask_path);

//    cv::Mat newMask;
//    cv::bitwise_not(maskImage, newMask);
//
//    cv::Mat convertedMask;
//    newMask.convertTo(convertedMask, CV_8UC1);

    std::cout << "* Lalit 3 "  << std::endl;
    inpaint(inputImage, maskImage, inpaitMat, patch_size, INPAINT_NS);

    std::cout << "* Lalit 4 "  << std::endl;
    cv::imwrite(inpaint_path, inpaitMat);
}