enum FaceSnapMode { normal, strict }

/// Model Class for Face Scan Settings
class ScanFaceSettings {
  /// Guide text displayed on the screen
  final String guideText;

  /// Instruction text for users
  final String instructionText;

  /// Success text when face is detected correctly
  final String successText;

  /// Border color when face is detected
  final int borderColorSuccess;

  /// Default border color
  final int borderColorDefault;

  /// Default text color
  final int textColorDefault;

  /// Success text color
  final int textColorSuccess;

  /// Guide text font size
  final double guideFontSize;

  /// Instruction text font size
  final double instructionFontSize;

  /// Guide text color
  final int guideTextColor;

  /// Instruction text color
  final int instructionTextColor;

  /// Mode for face scanning (Normal / Strict)
  final FaceSnapMode faceSnapMode;

  /// Constructor with default values
  ScanFaceSettings({
    this.guideText = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
    this.instructionText = "ไม่มีปิดตา จมูก ปาก และคาง",
    this.successText = "ถือค้างไว้",
    this.borderColorSuccess = 0xFF00FF00, // Green
    this.borderColorDefault = 0xFFFF0000, // Red
    this.textColorDefault = 0xFFFFFFFF, // White
    this.textColorSuccess = 0xFF00FF00, // Green
    this.guideFontSize = 22.0,
    this.instructionFontSize = 18.0,
    this.guideTextColor = 0xFFFFFF00, // Yellow
    this.instructionTextColor = 0xFF00FFFF, // Cyan
    this.faceSnapMode = FaceSnapMode.normal,
  });
}
