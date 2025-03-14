enum FaceSnapMode { normal, strict }

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

  final FaceSnapMode faceSnapMode;

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
