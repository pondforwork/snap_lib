import 'dart:typed_data';

class CardProcessingSettings {
  final Uint8List imageBytes; // Required
  final double? snr;
  final double? contrast;
  final double? brightness;
  final double? glarePercent;
  final String? resolution;
  final double? gamma;
  final bool? applyGamma;
  final bool? reduceNoise;
  final bool? enhanceSharpen;
  final int? d;
  final double? sigmaColor;
  final double? sigmaSpace;
  final bool? useBilateralFilter;
  final bool? useSharpening;
  final double? sharpenStrength;
  final double? blurKernelSize;
  final bool? returnBase64;

  CardProcessingSettings({
    required this.imageBytes, // ✅ Required
    this.snr,
    this.contrast,
    this.brightness,
    this.glarePercent,
    this.resolution,
    this.gamma,
    this.applyGamma,
    this.reduceNoise,
    this.enhanceSharpen,
    this.d,
    this.sigmaColor,
    this.sigmaSpace,
    this.useBilateralFilter,
    this.useSharpening,
    this.sharpenStrength,
    this.blurKernelSize,
    this.returnBase64,
  });

  Map<String, dynamic> toMap() {
    return {
      'image': imageBytes,
      if (snr != null) 'snr': snr,
      if (contrast != null) 'contrast': contrast,
      if (brightness != null) 'brightness': brightness,
      if (glarePercent != null) 'glarePercent': glarePercent,
      if (resolution != null) 'resolution': resolution,
      if (gamma != null) 'gamma': gamma,
      if (applyGamma != null) 'useBilateralFilter': applyGamma,
      if (reduceNoise != null) 'reduceNoise': reduceNoise,
      if (enhanceSharpen != null) 'useSharpening': enhanceSharpen,
      if (d != null) 'd': d,
      if (sigmaColor != null) 'sigmaColor': sigmaColor,
      if (sigmaSpace != null) 'sigmaSpace': sigmaSpace,
      if (useBilateralFilter != null) 'useBilateralFilter': useBilateralFilter,
      if (useSharpening != null) 'useSharpening': useSharpening,
      if (sharpenStrength != null) 'sharpenStrength': sharpenStrength,
      if (blurKernelSize != null) 'blurKernelSize': blurKernelSize,
      if (returnBase64 != null) 'returnBase64': returnBase64,
    };
  }
}
