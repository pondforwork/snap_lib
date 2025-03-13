import 'dart:typed_data';
import 'package:flutter/services.dart';
import 'package:snap_lib/settings/ImageProcessingSettings.dart';
import 'package:snap_lib/settings/WarningMessages.dart';
import 'package:snap_lib/settings/card_process_setting.dart';
import 'package:snap_lib/settings/dialog_setting.dart';
import 'package:snap_lib/settings/front_snap_settings.dart';

class SnapLib {
  static const MethodChannel _channel = MethodChannel('image_processor_plugin');
  static const MethodChannel _snapChannel = MethodChannel('snap_plugin');

  static Future<dynamic> processImage(Uint8List imageBytes,
      {double gamma = 1.0,
      int d = 9,
      double sigmaColor = 75.0,
      double sigmaSpace = 75.0,
      double sharpenStrength = 1.0,
      double blurKernelSize = 3.0,
      bool applyGamma = true,
      bool reduceNoise = true,
      bool enhanceSharpen = true,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('processImage', {
      'image': imageBytes,
      'gamma': gamma,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
      'sharpenStrength': sharpenStrength,
      'blurKernelSize': blurKernelSize,
      'applyGamma': applyGamma,
      'reduceNoise': reduceNoise,
      'enhanceSharpen': enhanceSharpen,
      'returnBase64': returnBase64,
    });
  }

  static Future<dynamic> processFontCard(
      CardProcessingSettings settings) async {
    return await _channel.invokeMethod('processFontCard', settings.toMap());
  }

  static Future<dynamic> processBackCard(
      CardProcessingSettings settings) async {
    return await _channel.invokeMethod('processBackCard', settings.toMap());
  }

  static Future<double?> calculateBrightness(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateBrightness', {'image': imageBytes});
  }

  static Future<double?> calculateGlare(Uint8List imageBytes,
      {double threshold = 230.0, double minGlareArea = 500.0}) async {
    return await _channel.invokeMethod('calculateGlare', {
      'image': imageBytes,
      'threshold': threshold,
      'minGlareArea': minGlareArea,
    });
  }

  static Future<double?> calculateSNR(Uint8List imageBytes) async {
    return await _channel.invokeMethod('calculateSNR', {'image': imageBytes});
  }

  static Future<double?> calculateContrast(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateContrast', {'image': imageBytes});
  }

  static Future<bool> isImageQualityAcceptable(
    Uint8List imageBytes, {
    double snr = 0.0,
    double contrast = 0.0,
    double brightness = 0.0,
    double glarePercent = 0.0,
    String resolution = "0x0",
    int minResolution = 500,
    double snrThreshold = 3.0,
    double contrastThreshold = 50.0,
    int maxBrightness = 200,
    double maxGlarePercent = 1.0,
  }) async {
    return await _channel.invokeMethod('isImageQualityAcceptable', {
      'image': imageBytes,
      'snr': snr,
      'contrast': contrast,
      'brightness': brightness,
      'glarePercent': glarePercent,
      'resolution': resolution,
      'minResolution': minResolution,
      'snrThreshold': snrThreshold,
      'contrastThreshold': contrastThreshold,
      'maxBrightness': maxBrightness,
      'maxGlarePercent': maxGlarePercent,
    });
  }

  static Future<String?> calculateResolution(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateResolution', {'image': imageBytes});
  }

  static Future<dynamic> applyGammaCorrection(
      Uint8List imageBytes, double gamma,
      {bool returnBase64 = true}) async {
    return await _channel.invokeMethod('applyGammaCorrection', {
      'image': imageBytes,
      'gamma': gamma,
      'returnBase64': returnBase64,
    });
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
      'returnBase64': returnBase64,
    });
  }

  static Future<dynamic> enhanceSharpen(Uint8List imageBytes,
      {double strength = 1.5,
      double blurKernelSize = 5.0,
      bool returnBase64 = true}) async {
    return await _channel.invokeMethod('enhanceSharpen', {
      'image': imageBytes,
      'strength': strength,
      'blurKernelSize': blurKernelSize,
      'returnBase64': returnBase64,
    });
  }

  static Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('convertMatToBase64', {'image': imageBytes});
  }

  static Future<String?> startCardSnap(
    FrontSnapSettings frontSnapSettings,
    ImageProcessingSettings processingSettings,
    WarningMessages warningMessages,
    DialogStyleSettings dialogSettings, // ✅ Added dialog settings
  ) async {
    var result = await _snapChannel.invokeMethod('startFrontSnap', {
      ...frontSnapSettings.toMap(),
      ...processingSettings.toMap(),
      ...warningMessages.toMap(),
      ...dialogSettings.toMap(), // ✅ Added dialog settings
    });

    return result?.toString();
  }

  static Future<void> startFaceSnap({
    required String titleMessage,
    required String initialMessage,
    required String foundMessage,
    required String notFoundMessage,
    required String snapMode,
  }) async {
    try {
      await _channel.invokeMethod('startFaceSnap', {
        "titleMessage": titleMessage,
        "initialMessage": initialMessage,
        "foundMessage": foundMessage,
        "notFoundMessage": notFoundMessage,
        "snapMode": snapMode,
      });
    } catch (e) {
      print("Error: $e");
    }
  }

  /// Process an image and convert it to grayscale
  static Future<String?> convertToGray(String base64Image) async {
    try {
      final String? result =
          await _channel.invokeMethod('convertToGray', {"image": base64Image});
      return result;
    } catch (e) {
      print("Error: $e");
      return null;
    }
  }

  static Future<void> updateGuideText(String text) async {
    await _snapChannel.invokeMethod('updateGuideText', {'text': text});
  }

  static Future<void> updateInstructionText(String text) async {
    await _snapChannel.invokeMethod('updateInstructionText', {'text': text});
  }
}
