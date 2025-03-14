class DialogStyleSettings {
  final int backgroundColor;
  final int titleColor;
  final int subtitleColor;
  final int buttonConfirmColor;
  final int buttonRetakeColor;
  final int buttonTextColor;
  final String alignment;
  final String extraMessage;
  final int extraMessageColor;
  final int extraMessageFontSize;
  final String extraMessageAlignment;

  DialogStyleSettings({
    this.backgroundColor = 0xFFFFFFFF,
    this.titleColor = 0xFF2D3892,
    this.subtitleColor = 0xFF888888,
    this.buttonConfirmColor = 0xFF2D3892,
    this.buttonRetakeColor = 0xFFFFFFFF,
    this.buttonTextColor = 0xFF000000,
    this.alignment = "center",
    this.extraMessage = "โปรดตรวจสอบว่ารูปไม่เบลอและมีแสงที่เพียงพอ",
    this.extraMessageColor = 0xFFFF0000,
    this.extraMessageFontSize = 16,
    this.extraMessageAlignment = "center",
  });

  /// ✅ Convert settings to map
  Map<String, dynamic> toMap() {
    return {
      'dialogBackgroundColor': backgroundColor,
      'dialogTitleColor': titleColor,
      'dialogSubtitleColor': subtitleColor,
      'dialogButtonConfirmColor': buttonConfirmColor,
      'dialogButtonRetakeColor': buttonRetakeColor,
      'dialogButtonTextColor': buttonTextColor,
      'dialogAlignment': alignment,
      'extraMessage': extraMessage,
      'extraMessageColor': extraMessageColor,
      'extraMessageFontSize': extraMessageFontSize,
      'extraMessageAlignment': extraMessageAlignment,
    };
  }
}
