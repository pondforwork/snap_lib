import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
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
  bool _returnBase64 = true;
  bool _useBilateralFilter = true;
  bool _useSharpening = true;
  bool _useGammaCorrection = true;

  double _gamma = 1.0;
  int _d = 9;
  double _sigmaColor = 75.0;
  double _sigmaSpace = 75.0;
  double _sharpenStrength = 1.0;
  double _blurKernelSize = 3.0;
  double _glarePercent = 1.0;
  double _snr = 0.0;
  double _contrast = 0.0;
  double _brightness = 0.0;
  String _resolution = "0x0";

  Future<void> _pickAndProcessImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _originalImage = imageBytes);

    final resolution = await SnapLib.calculateResolution(imageBytes);
    final snr = await SnapLib.calculateSNR(imageBytes);
    final contrast = await SnapLib.calculateContrast(imageBytes);
    final brightness = await SnapLib.calculateBrightness(imageBytes);
    final glare = await SnapLib.calculateGlare(imageBytes);

    setState(() {
      _resolution = resolution ?? "0x0";
      _snr = snr ?? 0.0;
      _contrast = contrast ?? 0.0;
      _brightness = brightness ?? 0.0;
      _glarePercent = glare ?? 0.0;
    });

    if (_resolution == "0x0") {
      _showErrorDialog(
          "Image resolution is too low. Please select a higher resolution image.");
      return;
    }

    try {
      final result = await SnapLib.processFontCard(
        imageBytes,
        resolution: _resolution,
        snr: _snr,
        contrast: _contrast,
        brightness: _brightness,
        glarePercent: _glarePercent,
        gamma: _gamma,
        reduceNoise: _useBilateralFilter,
        d: _d,
        sigmaColor: _sigmaColor,
        sigmaSpace: _sigmaSpace,
        enhanceSharpen: _useSharpening,
        applyGamma: _useGammaCorrection,
        sharpenStrength: _sharpenStrength,
        blurKernelSize: _blurKernelSize,
        returnBase64: _returnBase64,
      );

      setState(() {
        if (_returnBase64) {
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
      appBar: AppBar(title: const Text("Process Font Card")),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              ElevatedButton(
                onPressed: _pickAndProcessImage,
                child: const Text("Pick Image"),
              ),
              const SizedBox(height: 10),
              if (_originalImage != null)
                Image.memory(_originalImage!, height: 150),
              const SizedBox(height: 10),
              if (_processedImage != null)
                Image.memory(_processedImage!, height: 150),
              if (_base64String != null) Text(_base64String!.substring(0, 100)),

              // Toggle Options
              SwitchListTile(
                title: const Text("Return as Base64"),
                value: _returnBase64,
                onChanged: (value) => setState(() => _returnBase64 = value),
              ),
              SwitchListTile(
                title: const Text("Use Bilateral Filter"),
                value: _useBilateralFilter,
                onChanged: (value) =>
                    setState(() => _useBilateralFilter = value),
              ),
              SwitchListTile(
                title: const Text("Use Sharpening"),
                value: _useSharpening,
                onChanged: (value) => setState(() => _useSharpening = value),
              ),
              SwitchListTile(
                title: const Text("Use Gamma Correction"),
                value: _useGammaCorrection,
                onChanged: (value) =>
                    setState(() => _useGammaCorrection = value),
              ),

              // Parameter Controls
              const SizedBox(height: 10),
              _buildSlider("Gamma", _gamma, 0.1, 10.0,
                  (value) => setState(() => _gamma = value)),
              _buildSlider("Sharpen Strength", _sharpenStrength, 0.1, 3.0,
                  (value) => setState(() => _sharpenStrength = value)),
              _buildSlider("Blur Kernel Size", _blurKernelSize, 3.0, 15.0,
                  (value) => setState(() => _blurKernelSize = value)),
              _buildSlider("d (Diameter)", _d.toDouble(), 1, 20,
                  (value) => setState(() => _d = value.toInt())),
              _buildSlider("Sigma Color", _sigmaColor, 10.0, 150.0,
                  (value) => setState(() => _sigmaColor = value)),
              _buildSlider("Sigma Space", _sigmaSpace, 10.0, 150.0,
                  (value) => setState(() => _sigmaSpace = value)),
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
