import 'snap_lib_platform_interface.dart';

class SnapLib {
  Future<String?> getPlatformVersion() {
    return SnapLibPlatform.instance.getPlatformVersion();
  }

  Future<void> startNewActivity() {
    return SnapLibPlatform.instance.startNewActivity();
  }
}
