import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'package:snap_lib/settings/front_snap_settings.dart';

class SnapLib {
  static const MethodChannel _channel = MethodChannel('image_processor_plugin');
  static const MethodChannel _snapChannel = MethodChannel('snap_plugin');

  /// **Process Image (General)**
  static Future<dynamic> processImage(Uint8List imageBytes,
      {double gamma = 1.0,
      int d = 9,
      double sigmaColor = 75.0,
      double sigmaSpace = 75.0,
      double sharpenStrength = 1.0,
      double blurKernelWidth = 3.0,
      double blurKernelHeight = 3.0,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processImage', {
      'image': imageBytes,
      'gamma': gamma,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
      'sharpenStrength': sharpenStrength,
      'blurKernelWidth': blurKernelWidth,
      'blurKernelHeight': blurKernelHeight,
      'returnBase64': returnBase64,
    });
  }

  /// **Process Front Card**
  static Future<dynamic> processFontCard(Uint8List imageBytes,
      {double snr = 0.0,
      double contrast = 0.0,
      double brightness = 0.0,
      double glarePercent = 0.0,
      String resolution = "0x0",
      double gamma = 1.0,
      bool useBilateralFilter = true,
      int d = 9,
      double sigmaColor = 75.0,
      double sigmaSpace = 75.0,
      bool useSharpening = true,
      double sharpenStrength = 1.0,
      double blurKernelWidth = 3.0,
      double blurKernelHeight = 3.0,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processFontCard', {
      'image': imageBytes,
      'snr': snr,
      'contrast': contrast,
      'brightness': brightness,
      'glarePercent': glarePercent,
      'resolution': resolution,
      'gamma': gamma,
      'useBilateralFilter': useBilateralFilter,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
      'useSharpening': useSharpening,
      'sharpenStrength': sharpenStrength,
      'blurKernelWidth': blurKernelWidth,
      'blurKernelHeight': blurKernelHeight,
      'returnBase64': returnBase64,
    });
  }

  /// **Process Back Card**
  static Future<dynamic> processBackCard(Uint8List imageBytes,
      {double snr = 0.0,
      double contrast = 0.0,
      double brightness = 0.0,
      double glarePercent = 0.0,
      String resolution = "0x0",
      double gamma = 1.8,
      bool useBilateralFilter = true,
      int d = 9,
      double sigmaColor = 75.0,
      double sigmaSpace = 75.0,
      bool useSharpening = true,
      double sharpenStrength = 1.0,
      double blurKernelWidth = 3.0,
      double blurKernelHeight = 3.0,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processBackCard', {
      'image': imageBytes,
      'snr': snr,
      'contrast': contrast,
      'brightness': brightness,
      'glarePercent': glarePercent,
      'resolution': resolution,
      'gamma': gamma,
      'useBilateralFilter': useBilateralFilter,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
      'useSharpening': useSharpening,
      'sharpenStrength': sharpenStrength,
      'blurKernelWidth': blurKernelWidth,
      'blurKernelHeight': blurKernelHeight,
      'returnBase64': returnBase64,
    });
  }

  /// **Calculate Brightness**
  static Future<double?> calculateBrightness(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateBrightness', {'image': imageBytes});
  }

  /// **Calculate Glare**
  static Future<double?> calculateGlare(Uint8List imageBytes,
      {double threshold = 230.0, double minGlareArea = 500.0}) async {
    return await _channel.invokeMethod('calculateGlare', {
      'image': imageBytes,
      'threshold': threshold,
      'minGlareArea': minGlareArea,
    });
  }

  /// **Calculate SNR**
  static Future<double?> calculateSNR(Uint8List imageBytes) async {
    return await _channel.invokeMethod('calculateSNR', {'image': imageBytes});
  }

  /// **Calculate Contrast**
  static Future<double?> calculateContrast(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateContrast', {'image': imageBytes});
  }

  /// **Calculate Resolution**
  static Future<String?> calculateResolution(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateResolution', {'image': imageBytes});
  }

  /// **Apply Gamma Correction**
  static Future<dynamic> applyGammaCorrection(
      Uint8List imageBytes, double gamma,
      {bool returnBase64 = true}) async {
    return await _channel.invokeMethod('applyGammaCorrection', {
      'image': imageBytes,
      'gamma': gamma,
      'returnBase64': returnBase64,
    });
  }

  /// **Reduce Noise**
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
      'returnBase64': returnBase64,
    });
  }

  /// **Enhance Sharpen**
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
      'returnBase64': returnBase64,
    });
  }

  /// **Convert Mat to Base64**
  static Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('convertMatToBase64', {'image': imageBytes});
  }

  static Future<String?> startFrontSnap(
      FrontSnapSettings frontSnapSettings) async {
    var result = await _snapChannel.invokeMethod('startFrontSnap', {
      'titleMessage': frontSnapSettings.titleMessage,
      'initialMessage': frontSnapSettings.initialMessage,
      'foundMessage': frontSnapSettings.foundMessage,
      'notFoundMessage': frontSnapSettings.notFoundMessage
    });
    if (result == null) {
      return null;
    } else {
      return result.toString();
    }
  }
}
