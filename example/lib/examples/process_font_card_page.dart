import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/settings/card_process_setting.dart';
import 'package:snap_lib/snap_lib.dart';

class ProcessFontCardPage extends StatefulWidget {
  const ProcessFontCardPage({super.key});

  @override
  _ProcessFontCardPageState createState() => _ProcessFontCardPageState();
}

class _ProcessFontCardPageState extends State<ProcessFontCardPage> {
  Uint8List? _originalImage;
  Uint8List? _processedImage;
  String? _base64String;

  /// ✅ **Store all processing settings in a model**
  FontCardProcessingSettings _settings = FontCardProcessingSettings();

  Future<void> _pickAndProcessImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _originalImage = imageBytes);

    // ✅ Fetch Image Quality Metrics
    final resolution = await SnapLib.calculateResolution(imageBytes);
    final snr = await SnapLib.calculateSNR(imageBytes);
    final contrast = await SnapLib.calculateContrast(imageBytes);
    final brightness = await SnapLib.calculateBrightness(imageBytes);
    final glare = await SnapLib.calculateGlare(imageBytes);

    // ✅ Update settings with calculated values
    setState(() {
      _settings = _settings.copyWith(
        resolution: resolution ?? "0x0",
        snr: snr ?? 0.0,
        contrast: contrast ?? 0.0,
        brightness: brightness ?? 0.0,
        glarePercent: glare ?? 0.0,
      );
    });

    if (_settings.resolution == "0x0") {
      _showErrorDialog(
          "Image resolution is too low. Please select a higher resolution image.");
      return;
    }

    try {
      final result = await SnapLib.processBackCard(
        CardProcessingSettings(
          imageBytes: imageBytes,
          snr: _settings.snr,
          contrast: _settings.contrast,
          brightness: _settings.brightness,
          glarePercent: _settings.glarePercent,
          resolution: _settings.resolution,
          gamma: _settings.gamma,
          useBilateralFilter: _settings.useBilateralFilter,
          useSharpening: _settings.useSharpening,
          d: _settings.d,
          sigmaColor: _settings.sigmaColor,
          sigmaSpace: _settings.sigmaSpace,
          sharpenStrength: _settings.sharpenStrength,
          blurKernelSize: _settings.blurKernelSize,
          returnBase64: _settings.returnBase64,
        ),
      );

      setState(() {
        if (_settings.returnBase64) {
          _base64String = result as String;
          _processedImage = null;
        } else {
          _processedImage = result as Uint8List;
          _base64String = null;
        }
      });
    } catch (e) {
      _showErrorDialog("Error processing image: ${e.toString()}");
    }
  }

  void _showErrorDialog(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Error"),
        content: Text(message),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(context), child: const Text("OK"))
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Process Font Card")),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              ElevatedButton(
                  onPressed: _pickAndProcessImage,
                  child: const Text("Pick Image")),
              const SizedBox(height: 10),

              if (_originalImage != null)
                Image.memory(_originalImage!, height: 150),
              const SizedBox(height: 10),
              if (_processedImage != null)
                Image.memory(_processedImage!, height: 150),
              if (_base64String != null) Text(_base64String!.substring(0, 100)),

              // ✅ Toggle Switches
              SwitchListTile(
                title: const Text("Return as Base64"),
                value: _settings.returnBase64,
                onChanged: (value) => setState(
                    () => _settings = _settings.copyWith(returnBase64: value)),
              ),
              SwitchListTile(
                title: const Text("Use Bilateral Filter"),
                value: _settings.useBilateralFilter,
                onChanged: (value) => setState(() =>
                    _settings = _settings.copyWith(useBilateralFilter: value)),
              ),
              SwitchListTile(
                title: const Text("Use Sharpening"),
                value: _settings.useSharpening,
                onChanged: (value) => setState(
                    () => _settings = _settings.copyWith(useSharpening: value)),
              ),
              SwitchListTile(
                title: const Text("Use Gamma Correction"),
                value: _settings.useGammaCorrection,
                onChanged: (value) => setState(() =>
                    _settings = _settings.copyWith(useGammaCorrection: value)),
              ),

              // ✅ Sliders
              _buildSlider(
                  "Gamma",
                  _settings.gamma,
                  0.1,
                  10.0,
                  (value) => setState(
                      () => _settings = _settings.copyWith(gamma: value))),
              _buildSlider(
                  "Sharpen Strength",
                  _settings.sharpenStrength,
                  0.1,
                  3.0,
                  (value) => setState(() =>
                      _settings = _settings.copyWith(sharpenStrength: value))),
              _buildSlider(
                  "Blur Kernel Size",
                  _settings.blurKernelSize,
                  3.0,
                  15.0,
                  (value) => setState(() =>
                      _settings = _settings.copyWith(blurKernelSize: value))),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSlider(String label, double value, double min, double max,
      ValueChanged<double> onChanged) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text("$label: ${value.toStringAsFixed(2)}"),
        Slider(
          value: value,
          min: min,
          max: max,
          divisions: 10,
          label: value.toStringAsFixed(2),
          onChanged: onChanged,
        ),
      ],
    );
  }
}

/// ✅ **Model for Processing Settings**
class FontCardProcessingSettings {
  final double gamma;
  final bool useBilateralFilter;
  final bool useSharpening;
  final bool useGammaCorrection;
  final int d;
  final double sigmaColor;
  final double sigmaSpace;
  final double sharpenStrength;
  final double blurKernelSize;
  final bool returnBase64;
  final double glarePercent;
  final double snr;
  final double contrast;
  final double brightness;
  final String resolution;

  FontCardProcessingSettings({
    this.gamma = 1.0,
    this.useBilateralFilter = true,
    this.useSharpening = true,
    this.useGammaCorrection = true,
    this.d = 9,
    this.sigmaColor = 75.0,
    this.sigmaSpace = 75.0,
    this.sharpenStrength = 1.0,
    this.blurKernelSize = 3.0,
    this.returnBase64 = true,
    this.glarePercent = 1.0,
    this.snr = 0.0,
    this.contrast = 0.0,
    this.brightness = 0.0,
    this.resolution = "0x0",
  });

  /// ✅ Copy Model with New Values
  FontCardProcessingSettings copyWith({
    double? gamma,
    bool? useBilateralFilter,
    bool? useSharpening,
    bool? useGammaCorrection,
    int? d,
    double? sigmaColor,
    double? sigmaSpace,
    double? sharpenStrength,
    double? blurKernelSize,
    bool? returnBase64,
    double? glarePercent,
    double? snr,
    double? contrast,
    double? brightness,
    String? resolution,
  }) {
    return FontCardProcessingSettings(
      gamma: gamma ?? this.gamma,
      useBilateralFilter: useBilateralFilter ?? this.useBilateralFilter,
      useSharpening: useSharpening ?? this.useSharpening,
      useGammaCorrection: useGammaCorrection ?? this.useGammaCorrection,
      d: d ?? this.d,
      sigmaColor: sigmaColor ?? this.sigmaColor,
      sigmaSpace: sigmaSpace ?? this.sigmaSpace,
      sharpenStrength: sharpenStrength ?? this.sharpenStrength,
      blurKernelSize: blurKernelSize ?? this.blurKernelSize,
      returnBase64: returnBase64 ?? this.returnBase64,
      glarePercent: glarePercent ?? this.glarePercent,
      snr: snr ?? this.snr,
      contrast: contrast ?? this.contrast,
      brightness: brightness ?? this.brightness,
      resolution: resolution ?? this.resolution,
    );
  }
}
