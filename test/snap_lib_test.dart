import 'package:flutter_test/flutter_test.dart';
import 'package:snap_lib/snap_lib.dart';
import 'package:snap_lib/snap_lib_platform_interface.dart';
import 'package:snap_lib/snap_lib_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockSnapLibPlatform
    with MockPlatformInterfaceMixin
    implements SnapLibPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final SnapLibPlatform initialPlatform = SnapLibPlatform.instance;

  test('$MethodChannelSnapLib is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSnapLib>());
  });

  test('getPlatformVersion', () async {
    SnapLib snapLibPlugin = SnapLib();
    MockSnapLibPlatform fakePlatform = MockSnapLibPlatform();
    SnapLibPlatform.instance = fakePlatform;

    expect(await snapLibPlugin.getPlatformVersion(), '42');
  });
}
