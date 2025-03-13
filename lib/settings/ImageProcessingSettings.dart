class ImageProcessingSettings {
  final bool isDetectNoise;
  final bool isDetectBrightness;
  final bool isDetectGlare;

  final double maxNoiseValue;
  final double maxBrightnessValue;
  final double minBrightnessValue;
  final double maxGlarePercent;

  ImageProcessingSettings({
    this.isDetectNoise = true,
    this.isDetectBrightness = true,
    this.isDetectGlare = true,
    this.maxNoiseValue = 3.0,
    this.maxBrightnessValue = 200.0,
    this.minBrightnessValue = 80.0,
    this.maxGlarePercent = 1.0,
  });

  Map<String, dynamic> toMap() {
    return {
      'isDetectNoise': isDetectNoise,
      'isDetectBrightness': isDetectBrightness,
      'isDetectGlare': isDetectGlare,
      'maxNoiseValue': maxNoiseValue,
      'maxBrightnessValue': maxBrightnessValue,
      'minBrightnessValue': minBrightnessValue,
      'maxGlarePercent': maxGlarePercent,
    };
  }
}
