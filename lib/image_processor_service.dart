import 'dart:async';
import 'dart:typed_data';
import 'package:flutter/services.dart';

class ImageProcessorPlugin {
  static const MethodChannel _channel = MethodChannel('image_processor_plugin');

  static Future<double?> calculateBrightness(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('calculateBrightness', {'image': imageBytes});
  }

  static Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    return await _channel
        .invokeMethod('convertMatToBase64', {'image': imageBytes});
  }

  static Future<String?> convertMatToFile(
      Uint8List imageBytes, String filePath) async {
    return await _channel.invokeMethod(
        'convertMatToFile', {'image': imageBytes, 'filePath': filePath});
  }
}
