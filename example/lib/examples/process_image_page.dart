import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class ProcessImagePage extends StatefulWidget {
  const ProcessImagePage({super.key});

  @override
  _ProcessImagePageState createState() => _ProcessImagePageState();
}

class _ProcessImagePageState extends State<ProcessImagePage> {
  Uint8List? _originalImage;
  Uint8List? _processedImage;
  String? _base64String;
  bool _returnBase64 = true;
  bool _useBilateralFilter = true;
  bool _useSharpening = true;
  bool _applyGammaCorrection = true;

  double _gamma = 1.0;
  int _d = 9;
  double _sigmaColor = 75.0;
  double _sigmaSpace = 75.0;
  double _sharpenStrength = 1.0;
  double _blurKernelSize = 3.0;

  Future<void> _pickAndProcessImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _originalImage = imageBytes);

    try {
      final result = await SnapLib.processImage(
        imageBytes,
        gamma: _gamma,
        applyGamma: _applyGammaCorrection,
        reduceNoise: _useBilateralFilter,
        enhanceSharpen: _useSharpening,
        d: _d,
        sigmaColor: _sigmaColor,
        sigmaSpace: _sigmaSpace,
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
      appBar: AppBar(title: const Text("Process Image")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _pickAndProcessImage,
              child: const Text("Pick & Process Image"),
            ),
            const SizedBox(height: 10),

            // Display Original Image
            if (_originalImage != null) ...[
              const Text("Original Image",
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              Image.memory(_originalImage!, height: 150),
              const SizedBox(height: 10),
            ],

            // Display Processed Image
            if (_processedImage != null) ...[
              const Text("Processed Image",
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              Image.memory(_processedImage!, height: 150),
              const SizedBox(height: 10),
            ],

            // Display Base64 String (First 100 characters)
            if (_base64String != null) ...[
              const Text("Processed Image (Base64)"),
              Text(_base64String!.substring(0, 100) + "..."),
            ],

            const SizedBox(height: 20),

            // Gamma Correction Slider
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text("Gamma Correction",
                    style:
                        TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                Slider(
                  value: _gamma,
                  min: 1.0,
                  max: 10.0,
                  divisions: 9,
                  label: _gamma.toStringAsFixed(1),
                  onChanged: (value) => setState(() => _gamma = value),
                ),
              ],
            ),

            // Sharpening Strength Slider
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text("Sharpening Strength",
                    style:
                        TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                Slider(
                  value: _sharpenStrength,
                  min: 0.1,
                  max: 3.0,
                  divisions: 10,
                  label: _sharpenStrength.toStringAsFixed(1),
                  onChanged: (value) =>
                      setState(() => _sharpenStrength = value),
                ),
              ],
            ),

            // Blur Kernel Size Slider
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text("Blur Kernel Size",
                    style:
                        TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                Slider(
                  value: _blurKernelSize,
                  min: 3.0,
                  max: 9.0,
                  divisions: 6,
                  label: _blurKernelSize.toStringAsFixed(1),
                  onChanged: (value) => setState(() => _blurKernelSize = value),
                ),
              ],
            ),

            // Toggle Use Bilateral Filter
            SwitchListTile(
              title: const Text("Use Bilateral Filter"),
              value: _useBilateralFilter,
              onChanged: (value) => setState(() => _useBilateralFilter = value),
            ),

            // Toggle Use Sharpening
            SwitchListTile(
              title: const Text("Use Sharpening"),
              value: _useSharpening,
              onChanged: (value) => setState(() => _useSharpening = value),
            ),

            // Toggle Apply Gamma Correction
            SwitchListTile(
              title: const Text("Apply Gamma Correction"),
              value: _applyGammaCorrection,
              onChanged: (value) =>
                  setState(() => _applyGammaCorrection = value),
            ),

            // Return as Base64 Toggle
            SwitchListTile(
              title: const Text("Return as Base64"),
              value: _returnBase64,
              onChanged: (value) => setState(() => _returnBase64 = value),
            ),
          ],
        ),
      ),
    );
  }
}
