class WarningMessages {
  final String warningMessage;
  final String warningNoise;
  final String warningBrightnessOver;
  final String warningBrightnessLower;
  final String warningGlare;

  WarningMessages({
    this.warningMessage = "กรุณาปรับแสงให้เหมาะสม",
    this.warningNoise = "🔹 ลด Noise ในภาพ",
    this.warningBrightnessOver = "🔹 ลดความสว่าง",
    this.warningBrightnessLower = "🔹 เพิ่มความสว่าง",
    this.warningGlare = "🔹 ลดแสงสะท้อน",
  });

  Map<String, dynamic> toMap() {
    return {
      'warningMessage': warningMessage,
      'warningNoise': warningNoise,
      'warningBrightnessOver': warningBrightnessOver,
      'warningBrightnessLower': warningBrightnessLower,
      'warningGlare': warningGlare,
    };
  }
}
