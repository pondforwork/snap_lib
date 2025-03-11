import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class CalculateResolutionPage extends StatefulWidget {
  const CalculateResolutionPage({super.key});

  @override
  _CalculateResolutionPageState createState() =>
      _CalculateResolutionPageState();
}

class _CalculateResolutionPageState extends State<CalculateResolutionPage> {
  Uint8List? _image;
  String? _resolution;

  Future<void> _pickImageAndCalculate() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _image = imageBytes);

    final resolution = await SnapLib.calculateResolution(imageBytes);
    setState(() => _resolution = resolution);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Calculate Resolution")),
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
            if (_resolution != null) Text("Resolution: $_resolution"),
          ],
        ),
      ),
    );
  }
}
