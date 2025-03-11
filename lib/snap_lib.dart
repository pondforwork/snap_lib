import 'dart:typed_data';
import 'package:flutter/services.dart';

class SnapLib {
  static const MethodChannel _channel = MethodChannel('image_processor_plugin');

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

  static Future<double?> calculateBrightness(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateBrightness', {'image': imageBytes});
  }

  static Future<double?> calculateGlare(Uint8List imageBytes) async {
    return await _channel.invokeMethod('calculateGlare', {
      'image': imageBytes,
    });
  }

  static Future<double?> calculateSNR(Uint8List imageBytes) async {
    return await _channel.invokeMethod('calculateSNR', {'image': imageBytes});
  }

  static Future<double?> calculateContrast(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateContrast', {'image': imageBytes});
  }

  static Future<String?> calculateResolution(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateResolution', {'image': imageBytes});
  }

  static Future<dynamic> applyGammaCorrection(
      Uint8List imageBytes, double gamma,
      {bool returnBase64 = true}) async {
    return await _channel.invokeMethod('applyGammaCorrection',
        {'image': imageBytes, 'gamma': gamma, 'returnBase64': returnBase64});
  }

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

  static Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('convertMatToBase64', {'image': imageBytes});
  }

  /// **Start Front Camera Snap**
  static Future<void> startFrontSnap(String titleMessage) async {
    return await _channel
        .invokeMethod('startFrontSnap', {'titleMessage': titleMessage});
  }
}
