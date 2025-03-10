import 'dart:typed_data';
import 'package:flutter/services.dart';

class SnapLib {
  static const MethodChannel _channel = MethodChannel('image_processor_plugin');

  /// Process an image and return the processed result.
  static Future<Uint8List?> processImage(Uint8List imageBytes) async {
    return await _channel.invokeMethod('processImage', {'image': imageBytes});
  }

  /// Calculate the brightness of an image.
  static Future<double?> calculateBrightness(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateBrightness', {'image': imageBytes});
  }

  /// Calculate glare percentage in an image.
  static Future<double?> calculateGlare(
      Uint8List imageBytes, double maxGlareThreshold) async {
    return await _channel.invokeMethod('calculateGlare', {
      'image': imageBytes,
      'maxGlareThreshold': maxGlareThreshold,
    });
  }

  /// Calculate the Signal-to-Noise Ratio (SNR) of an image.
  static Future<double?> calculateSNR(Uint8List imageBytes) async {
    return await _channel.invokeMethod('calculateSNR', {'image': imageBytes});
  }

  /// Apply gamma correction to an image.
  static Future<Uint8List?> applyGammaCorrection(
      Uint8List imageBytes, double gamma) async {
    return await _channel.invokeMethod('applyGammaCorrection', {
      'image': imageBytes,
      'gamma': gamma,
    });
  }

  /// Reduce noise using Bilateral Filter.
  static Future<Uint8List?> reduceNoise(Uint8List imageBytes,
      {int d = 9, double sigmaColor = 75.0, double sigmaSpace = 75.0}) async {
    return await _channel.invokeMethod('reduceNoise', {
      'image': imageBytes,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
    });
  }

  /// Enhance an image using the Unsharp Mask sharpening technique.
  static Future<Uint8List?> enhanceSharpen(Uint8List imageBytes,
      {double strength = 1.5,
      double blurKernelWidth = 5.0,
      double blurKernelHeight = 5.0}) async {
    return await _channel.invokeMethod('enhanceSharpen', {
      'image': imageBytes,
      'strength': strength,
      'blurKernelWidth': blurKernelWidth,
      'blurKernelHeight': blurKernelHeight,
    });
  }

  /// Convert an image from Mat format to a Base64 string.
  static Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('convertMatToBase64', {'image': imageBytes});
  }

  /// Convert an image from Mat format to a file and return the file path.
  static Future<String?> convertMatToFile(
      Uint8List imageBytes, String filePath) async {
    return await _channel.invokeMethod('convertMatToFile', {
      'image': imageBytes,
      'filePath': filePath,
    });
  }
}
