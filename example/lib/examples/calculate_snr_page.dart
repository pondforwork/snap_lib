import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class CalculateSNRPage extends StatefulWidget {
  const CalculateSNRPage({super.key});

  @override
  _CalculateSNRPageState createState() => _CalculateSNRPageState();
}

class _CalculateSNRPageState extends State<CalculateSNRPage> {
  Uint8List? _image;
  double? _snr;

  Future<void> _pickImageAndCalculate() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _image = imageBytes);

    final snr = await SnapLib.calculateSNR(imageBytes);
    setState(() => _snr = snr);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Calculate SNR")),
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
            if (_snr != null) Text("SNR: ${_snr!.toStringAsFixed(2)}"),
          ],
        ),
      ),
    );
  }
}
