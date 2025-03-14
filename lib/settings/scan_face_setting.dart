class ScanFaceSettings {
  final String guideText;
  final String instructionText;
  final String successText;
  final int borderColorSuccess;
  final int borderColorDefault;
  final int textColorDefault;
  final int textColorSuccess;
  final double guideFontSize;
  final double instructionFontSize;
  final int guideTextColor;
  final int instructionTextColor;

  ScanFaceSettings({
    this.guideText = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
    this.instructionText = "ไม่มีปิดตา จมูก ปาก และคาง",
    this.successText = "ถือค้างไว้",
    this.borderColorSuccess = 0xFF00FF00,
    this.borderColorDefault = 0xFFFF0000,
    this.textColorDefault = 0xFFFFFFFF,
    this.textColorSuccess = 0xFF00FF00,
    this.guideFontSize = 22.0,
    this.instructionFontSize = 18.0,
    this.guideTextColor = 0xFFFFFF00,
    this.instructionTextColor = 0xFF00FFFF,
  });

  /// ✅ **Convert settings to Map**
  Map<String, dynamic> toMap() {
    return {
      'guideText': guideText,
      'instructionText': instructionText,
      'successText': successText,
      'borderColorSuccess': borderColorSuccess,
      'borderColorDefault': borderColorDefault,
      'textColorDefault': textColorDefault,
      'textColorSuccess': textColorSuccess,
      'guideFontSize': guideFontSize,
      'instructionFontSize': instructionFontSize,
      'guideTextColor': guideTextColor,
      'instructionTextColor': instructionTextColor,
    };
  }
}
