import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class CalculateBrightnessPage extends StatefulWidget {
  const CalculateBrightnessPage({super.key});

  @override
  _CalculateBrightnessPageState createState() =>
      _CalculateBrightnessPageState();
}

class _CalculateBrightnessPageState extends State<CalculateBrightnessPage> {
  Uint8List? _image;
  double? _brightness;

  Future<void> _pickImageAndCalculate() async {
    try {
      final pickedFile =
          await ImagePicker().pickImage(source: ImageSource.gallery);
      if (pickedFile == null) return;

      final imageBytes = await pickedFile.readAsBytes();
      setState(() => _image = imageBytes);

      final brightness = await SnapLib.calculateBrightness(imageBytes);
      setState(() => _brightness = brightness);
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
      appBar: AppBar(title: const Text("Calculate Brightness")),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              ElevatedButton(
                  onPressed: _pickImageAndCalculate,
                  child: const Text("Pick Image")),
              const SizedBox(height: 10),
              if (_image != null) Image.memory(_image!, height: 150),
              const SizedBox(height: 10),
              if (_brightness != null)
                Text("Brightness: ${_brightness!.toStringAsFixed(2)}"),
            ],
          ),
        ),
      ),
    );
  }
}
