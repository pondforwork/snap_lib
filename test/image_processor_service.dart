import 'dart:typed_data';
import 'package:snap_lib/snap_lib_method_channel.dart';

class ImageProcessorService {
  static Future<double?> getBrightness(Uint8List imageBytes) async {
    return await MethodChannelSnapLib().calculateBrightness(imageBytes);
  }

  static Future<String?> convertImageToBase64(Uint8List imageBytes) async {
    return await MethodChannelSnapLib().convertMatToBase64(imageBytes);
  }

  static Future<String?> saveImageToFile(
      Uint8List imageBytes, String filePath) async {
    return await MethodChannelSnapLib().convertMatToFile(imageBytes, filePath);
  }
}
