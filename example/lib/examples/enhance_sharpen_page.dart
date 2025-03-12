import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class EnhanceSharpenPage extends StatefulWidget {
  const EnhanceSharpenPage({super.key});

  @override
  _EnhanceSharpenPageState createState() => _EnhanceSharpenPageState();
}

class _EnhanceSharpenPageState extends State<EnhanceSharpenPage> {
  Uint8List? _originalImage;
  Uint8List? _processedImage;
  String? _base64String;
  bool _returnBase64 = true;
  double _strength = 1.5;
  double _blurKernelSize = 5.0;

  Future<void> _pickAndEnhanceSharpen() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _originalImage = imageBytes);

    final result = await SnapLib.enhanceSharpen(
      imageBytes,
      strength: _strength,
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
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Enhance Sharpen Example")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _pickAndEnhanceSharpen,
              child: const Text("Pick & Enhance Image"),
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

            // Strength Slider
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text("Sharpening Strength",
                    style:
                        TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                Slider(
                  value: _strength,
                  min: 0.1,
                  max: 3.0,
                  divisions: 10,
                  label: _strength.toStringAsFixed(1),
                  onChanged: (value) => setState(() => _strength = value),
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
