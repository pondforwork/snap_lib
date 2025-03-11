import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'snap_lib_method_channel.dart';

abstract class SnapLibPlatform extends PlatformInterface {
  /// Constructs a SnapLibPlatform.
  SnapLibPlatform() : super(token: _token);

  static final Object _token = Object();

  static SnapLibPlatform _instance = MethodChannelSnapLib();

  /// The default instance of [SnapLibPlatform] to use.
  ///
  /// Defaults to [MethodChannelSnapLib].
  static SnapLibPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [SnapLibPlatform] when
  /// they register themselves.
  static set instance(SnapLibPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<void> startFrontSnap() {
    throw UnimplementedError('startFrontSnap() has not been implemented.');
  }
}
