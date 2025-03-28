import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class ApplyGammaPage extends StatefulWidget {
  const ApplyGammaPage({super.key});

  @override
  _ApplyGammaPageState createState() => _ApplyGammaPageState();
}

class _ApplyGammaPageState extends State<ApplyGammaPage> {
  Uint8List? _originalImage;
  Uint8List? _processedImage;
  String? _base64String;
  bool _returnBase64 = true;
  double _gammaValue = 1.0; // Default gamma value

  Future<void> _pickAndApplyGamma() async {
    try {
      final pickedFile =
          await ImagePicker().pickImage(source: ImageSource.gallery);
      if (pickedFile == null) return;

      final imageBytes = await pickedFile.readAsBytes();
      setState(() => _originalImage = imageBytes);

      final result = await SnapLib.applyGammaCorrection(imageBytes, _gammaValue,
          returnBase64: _returnBase64);

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
      print(e);
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
      appBar: AppBar(title: const Text("Apply Gamma Correction")),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              ElevatedButton(
                onPressed: _pickAndApplyGamma,
                child: const Text("Pick Image"),
              ),
              const SizedBox(height: 10),
              if (_originalImage != null)
                Image.memory(_originalImage!, height: 150),
              const SizedBox(height: 10),
              if (_processedImage != null)
                Image.memory(_processedImage!, height: 150),
              if (_base64String != null) Text(_base64String!.substring(0, 100)),
              const SizedBox(height: 20),
              Text("Gamma Value: ${_gammaValue.toStringAsFixed(1)}"),
              Slider(
                value: _gammaValue,
                min: 0.1,
                max: 10.0,
                divisions: 99,
                label: _gammaValue.toStringAsFixed(1),
                onChanged: (value) {
                  setState(() => _gammaValue = value);
                },
              ),
              SwitchListTile(
                title: const Text("Return as Base64"),
                value: _returnBase64,
                onChanged: (value) => setState(() => _returnBase64 = value),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
