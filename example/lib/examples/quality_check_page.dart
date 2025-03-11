import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class QualityCheckPage extends StatefulWidget {
  const QualityCheckPage({super.key});

  @override
  _QualityCheckPageState createState() => _QualityCheckPageState();
}

class _QualityCheckPageState extends State<QualityCheckPage> {
  Uint8List? _image;
  double? _brightness;
  double? _glare;
  double? _snr;
  double? _contrast;
  String? _resolution;

  Future<void> _pickImageAndCheckQuality() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _image = imageBytes);

    final brightness = await SnapLib.calculateBrightness(imageBytes);
    final glare = await SnapLib.calculateGlare(imageBytes);
    final snr = await SnapLib.calculateSNR(imageBytes);
    final contrast = await SnapLib.calculateContrast(imageBytes);
    final resolution = await SnapLib.calculateResolution(imageBytes);

    setState(() {
      _brightness = brightness;
      _glare = glare;
      _snr = snr;
      _contrast = contrast;
      _resolution = resolution;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Image Quality Check")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
                onPressed: _pickImageAndCheckQuality,
                child: const Text("Pick Image")),
            const SizedBox(height: 10),
            if (_image != null) Image.memory(_image!, height: 150),
            const SizedBox(height: 10),
            if (_brightness != null)
              Text("Brightness: ${_brightness!.toStringAsFixed(2)}"),
            if (_glare != null) Text("Glare: ${_glare!.toStringAsFixed(2)}"),
            if (_snr != null) Text("SNR: ${_snr!.toStringAsFixed(2)}"),
            if (_contrast != null)
              Text("Contrast: ${_contrast!.toStringAsFixed(2)}"),
            if (_resolution != null) Text("Resolution: $_resolution"),
          ],
        ),
      ),
    );
  }
}
