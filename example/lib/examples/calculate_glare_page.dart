import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class CalculateGlarePage extends StatefulWidget {
  const CalculateGlarePage({super.key});

  @override
  _CalculateGlarePageState createState() => _CalculateGlarePageState();
}

class _CalculateGlarePageState extends State<CalculateGlarePage> {
  Uint8List? _image;
  double? _glare;

  Future<void> _pickImageAndCalculate() async {
    try {
      final pickedFile =
          await ImagePicker().pickImage(source: ImageSource.gallery);
      if (pickedFile == null) return;

      final imageBytes = await pickedFile.readAsBytes();
      setState(() => _image = imageBytes);

      final glare = await SnapLib.calculateGlare(imageBytes);
      setState(() => _glare = glare);
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
      appBar: AppBar(title: const Text("Calculate Glare")),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
                onPressed: _pickImageAndCalculate,
                child: const Text("Pick Image")),
            const SizedBox(height: 10),
            if (_image != null) Image.memory(_image!, height: 150),
            const SizedBox(height: 10),
            if (_glare != null) Text("Glare: ${_glare!.toStringAsFixed(2)}"),
          ],
        ),
      ),
    );
  }
}
