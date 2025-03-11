import 'dart:typed_data';
import 'package:flutter/services.dart';

class SnapLib {
  static const MethodChannel _channel = MethodChannel('image_processor_plugin');

  /// **Process Image**
  /// - `gamma`: Default **1.0** (no change).
  /// - `noiseReduction`: Apply noise reduction (**default: true**).
  /// - `sharpening`: Apply sharpening (**default: true**).
  /// - `returnBase64`: If **true**, returns **Base64 string** (default), else **Uint8List**.
  static Future<dynamic> processImage(Uint8List imageBytes,
      {double gamma = 1.0,
      bool noiseReduction = true,
      bool sharpening = true,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processImage', {
      'image': imageBytes,
      'gamma': gamma,
      'noiseReduction': noiseReduction,
      'sharpening': sharpening,
      'returnBase64': returnBase64
    });
  }

  /// **Process Font Card**
  /// - Applies **OCR-specific** preprocessing for Font Cards.
  /// - `snr`, `contrast`, and `resolution` are required for quality assessment.
  static Future<dynamic> processFontCard(Uint8List imageBytes,
      {double snr = 0.0,
      double contrast = 0.0,
      String resolution = "0x0",
      double gamma = 1.0,
      bool noiseReduction = true,
      bool sharpening = true,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processFontCard', {
      'image': imageBytes,
      'snr': snr,
      'contrast': contrast,
      'resolution': resolution,
      'gamma': gamma,
      'noiseReduction': noiseReduction,
      'sharpening': sharpening,
      'returnBase64': returnBase64
    });
  }

  /// **Process Back Card**
  /// - Applies **OCR-specific** preprocessing for Back Cards.
  static Future<dynamic> processBackCard(Uint8List imageBytes,
      {double snr = 0.0,
      double contrast = 0.0,
      String resolution = "0x0",
      double gamma = 1.8,
      bool noiseReduction = true,
      bool sharpening = true,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processBackCard', {
      'image': imageBytes,
      'snr': snr,
      'contrast': contrast,
      'resolution': resolution,
      'gamma': gamma,
      'noiseReduction': noiseReduction,
      'sharpening': sharpening,
      'returnBase64': returnBase64
    });
  }

  /// **Calculate Image Brightness**
  static Future<double?> calculateBrightness(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateBrightness', {'image': imageBytes});
  }

  /// **Calculate Glare Percentage**
  static Future<double?> calculateGlare(
      Uint8List imageBytes, double maxGlareThreshold) async {
    return await _channel.invokeMethod('calculateGlare', {
      'image': imageBytes,
      'maxGlareThreshold': maxGlareThreshold,
    });
  }

  /// **Calculate Signal-to-Noise Ratio (SNR)**
  static Future<double?> calculateSNR(Uint8List imageBytes) async {
    return await _channel.invokeMethod('calculateSNR', {'image': imageBytes});
  }

  /// **Calculate Image Contrast**
  static Future<double?> calculateContrast(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateContrast', {'image': imageBytes});
  }

  /// **Calculate Image Resolution**
  static Future<String?> calculateResolution(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateResolution', {'image': imageBytes});
  }

  /// **Apply Gamma Correction**
  static Future<dynamic> applyGammaCorrection(
      Uint8List imageBytes, double gamma,
      {bool returnBase64 = true}) async {
    return await _channel.invokeMethod('applyGammaCorrection',
        {'image': imageBytes, 'gamma': gamma, 'returnBase64': returnBase64});
  }

  /// **Reduce Image Noise Using Bilateral Filter**
  static Future<dynamic> reduceNoise(Uint8List imageBytes,
      {int d = 9,
      double sigmaColor = 75.0,
      double sigmaSpace = 75.0,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('reduceNoise', {
      'image': imageBytes,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
      'returnBase64': returnBase64
    });
  }

  /// **Enhance Image Sharpness**
  static Future<dynamic> enhanceSharpen(Uint8List imageBytes,
      {double strength = 1.5,
      double blurKernelWidth = 5.0,
      double blurKernelHeight = 5.0,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('enhanceSharpen', {
      'image': imageBytes,
      'strength': strength,
      'blurKernelWidth': blurKernelWidth,
      'blurKernelHeight': blurKernelHeight,
      'returnBase64': returnBase64
    });
  }

  /// **Convert Mat to Base64 String**
  static Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('convertMatToBase64', {'image': imageBytes});
  }

  /// **Convert Mat to File and Return File Path**
  static Future<String?> convertMatToFile(
      Uint8List imageBytes, String filePath) async {
    return await _channel.invokeMethod('convertMatToFile', {
      'image': imageBytes,
      'filePath': filePath,
    });
  }

  /// **Start Front Camera Snap**
  static Future<void> startFrontSnap(String titleMessage) async {
    return await _channel
        .invokeMethod('startFrontSnap', {'titleMessage': titleMessage});
  }
}
