import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'snap_lib_platform_interface.dart';

/// An implementation of [SnapLibPlatform] that uses method channels.
class MethodChannelSnapLib extends SnapLibPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('snap_lib');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<void> startNewActivity() async {
    await methodChannel.invokeMethod<void>('startNewActivity');
  }

  Future<double?> calculateBrightness(Uint8List imageBytes) async {
    final brightness = await methodChannel.invokeMethod<double>(
      'calculateBrightness',
      {'image': imageBytes},
    );
    return brightness;
  }

  Future<String?> convertMatToBase64(Uint8List imageBytes) async {
    final base64String = await methodChannel.invokeMethod<String>(
      'convertMatToBase64',
      {'image': imageBytes},
    );
    return base64String;
  }

  Future<String?> convertMatToFile(
      Uint8List imageBytes, String filePath) async {
    final savedFilePath = await methodChannel.invokeMethod<String>(
      'convertMatToFile',
      {'image': imageBytes, 'filePath': filePath},
    );
    return savedFilePath;
  }
}
