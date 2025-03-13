enum SnapMode {
  front,
  back,
}

class FrontSnapSettings {
  final String titleMessage;
  final int titleFontSize;
  final int guideMessageFontSize;
  final String initialMessage;
  final String foundMessage;
  final String notFoundMessage;
  final SnapMode snapMode;

  FrontSnapSettings({
    required this.titleMessage,
    this.titleFontSize = 20,
    this.guideMessageFontSize = 25,
    required this.initialMessage,
    required this.foundMessage,
    required this.notFoundMessage,
    required this.snapMode,
  });

  Map<String, dynamic> toMap() {
    return {
      'titleMessage': titleMessage,
      'titleFontSize': titleFontSize,
      'guideMessageFontSize': guideMessageFontSize,
      'initialMessage': initialMessage,
      'foundMessage': foundMessage,
      'notFoundMessage': notFoundMessage,
      'snapMode': snapMode.name,
    };
  }
}
