import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/settings/card_process_setting.dart';
import 'package:snap_lib/snap_lib.dart';

/// Model class for Image Processing Settings
class BackCardProcessingSettings {
  final double gamma;
  final bool useBilateralFilter;
  final bool useSharpening;
  final int d;
  final double sigmaColor;
  final double sigmaSpace;
  final double sharpenStrength;
  final double blurKernelSize;
  final bool returnBase64;
  final double glarePercent;

  BackCardProcessingSettings({
    this.gamma = 1.8,
    this.useBilateralFilter = true,
    this.useSharpening = true,
    this.d = 9,
    this.sigmaColor = 75.0,
    this.sigmaSpace = 75.0,
    this.sharpenStrength = 1.0,
    this.blurKernelSize = 3.0,
    this.returnBase64 = true,
    this.glarePercent = 1.0,
  });

  Map<String, dynamic> toMap(Uint8List imageBytes, String resolution) {
    return {
      'image': imageBytes,
      'resolution': resolution,
      'gamma': gamma,
      'useBilateralFilter': useBilateralFilter,
      'd': d,
      'sigmaColor': sigmaColor,
      'sigmaSpace': sigmaSpace,
      'useSharpening': useSharpening,
      'sharpenStrength': sharpenStrength,
      'blurKernelSize': blurKernelSize,
      'returnBase64': returnBase64,
      'glarePercent': glarePercent,
    };
  }
}

class ProcessBackCardPage extends StatefulWidget {
  const ProcessBackCardPage({super.key});

  @override
  _ProcessBackCardPageState createState() => _ProcessBackCardPageState();
}

class _ProcessBackCardPageState extends State<ProcessBackCardPage> {
  Uint8List? _originalImage;
  Uint8List? _processedImage;
  String? _base64String;

  // Image Processing Settings
  BackCardProcessingSettings _settings = BackCardProcessingSettings();

  /// Pick image from gallery and process it
  Future<void> _pickAndProcessImage() async {
    try {
      final pickedFile =
          await ImagePicker().pickImage(source: ImageSource.gallery);
      if (pickedFile == null) return;

      final imageBytes = await pickedFile.readAsBytes();
      setState(() => _originalImage = imageBytes);

      final resolutionImage = await SnapLib.calculateResolution(imageBytes);
      if (resolutionImage == null)
        throw Exception("Resolution calculation failed.");

      final result = await SnapLib.processBackCard(CardProcessingSettings(
          imageBytes: imageBytes,
          resolution: resolutionImage,
          gamma: _settings.gamma,
          useBilateralFilter: _settings.useBilateralFilter,
          useSharpening: _settings.useSharpening,
          d: _settings.d,
          sigmaColor: _settings.sigmaColor,
          sigmaSpace: _settings.sigmaSpace,
          sharpenStrength: _settings.sharpenStrength,
          blurKernelSize: _settings.blurKernelSize,
          returnBase64: _settings.returnBase64,
          glarePercent: _settings.glarePercent));

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

  /// Show error dialog
  void _showErrorDialog(String message) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Error"),
        content: Text(message),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("OK"),
          )
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Process Back Card")),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              // Pick & Process Button
              ElevatedButton(
                onPressed: _pickAndProcessImage,
                child: const Text("Pick & Process Image"),
              ),
              const SizedBox(height: 10),

              // Display Original Image
              if (_originalImage != null)
                _buildImageSection("Original Image", _originalImage!),

              // Display Processed Image
              if (_processedImage != null)
                _buildImageSection("Processed Image", _processedImage!),

              // Display Base64 String (First 100 characters)
              if (_base64String != null) _buildBase64Section(),

              const SizedBox(height: 20),

              // Controls (Sliders & Toggles)
              _buildSlider("Gamma Correction", _settings.gamma, 1.0, 10.0,
                  (value) {
                setState(() => _settings = _settings.copyWith(gamma: value));
              }),

              _buildSlider(
                  "Sharpening Strength", _settings.sharpenStrength, 0.1, 3.0,
                  (value) {
                setState(() =>
                    _settings = _settings.copyWith(sharpenStrength: value));
              }),

              _buildSlider(
                  "Blur Kernel Size", _settings.blurKernelSize, 3.0, 9.0,
                  (value) {
                setState(() =>
                    _settings = _settings.copyWith(blurKernelSize: value));
              }),

              _buildSwitch("Use Bilateral Filter", _settings.useBilateralFilter,
                  (value) {
                setState(() =>
                    _settings = _settings.copyWith(useBilateralFilter: value));
              }),

              _buildSwitch("Use Sharpening", _settings.useSharpening, (value) {
                setState(
                    () => _settings = _settings.copyWith(useSharpening: value));
              }),

              _buildSwitch("Return as Base64", _settings.returnBase64, (value) {
                setState(
                    () => _settings = _settings.copyWith(returnBase64: value));
              }),
            ],
          ),
        ),
      ),
    );
  }

  /// Builds an image display section
  Widget _buildImageSection(String title, Uint8List image) {
    return Column(
      children: [
        Text(title,
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        Image.memory(image, height: 150),
        const SizedBox(height: 10),
      ],
    );
  }

  /// Builds a Base64 string display section
  Widget _buildBase64Section() {
    return Column(
      children: [
        const Text("Processed Image (Base64)"),
        Text(_base64String!.substring(0, 100) + "..."),
      ],
    );
  }

  /// Builds a slider with label
  Widget _buildSlider(String title, double value, double min, double max,
      Function(double) onChanged) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title,
            style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
        Slider(
          value: value,
          min: min,
          max: max,
          divisions: 10,
          label: value.toStringAsFixed(1),
          onChanged: onChanged,
        ),
      ],
    );
  }

  /// Builds a toggle switch
  Widget _buildSwitch(String title, bool value, Function(bool) onChanged) {
    return SwitchListTile(
      title: Text(title),
      value: value,
      onChanged: onChanged,
    );
  }
}

extension BackCardProcessingSettingsCopyWith on BackCardProcessingSettings {
  BackCardProcessingSettings copyWith({
    double? gamma,
    bool? useBilateralFilter,
    bool? useSharpening,
    int? d,
    double? sigmaColor,
    double? sigmaSpace,
    double? sharpenStrength,
    double? blurKernelSize,
    bool? returnBase64,
    double? glarePercent,
  }) {
    return BackCardProcessingSettings(
      gamma: gamma ?? this.gamma,
      useBilateralFilter: useBilateralFilter ?? this.useBilateralFilter,
      useSharpening: useSharpening ?? this.useSharpening,
      d: d ?? this.d,
      sigmaColor: sigmaColor ?? this.sigmaColor,
      sigmaSpace: sigmaSpace ?? this.sigmaSpace,
      sharpenStrength: sharpenStrength ?? this.sharpenStrength,
      blurKernelSize: blurKernelSize ?? this.blurKernelSize,
      returnBase64: returnBase64 ?? this.returnBase64,
      glarePercent: glarePercent ?? this.glarePercent,
    );
  }
}
