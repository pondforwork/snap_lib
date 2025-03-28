import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

class ReduceNoisePage extends StatefulWidget {
  const ReduceNoisePage({super.key});

  @override
  _ReduceNoisePageState createState() => _ReduceNoisePageState();
}

class _ReduceNoisePageState extends State<ReduceNoisePage> {
  Uint8List? _originalImage;
  Uint8List? _processedImage;
  String? _base64String;
  bool _returnBase64 = true;

  // Noise Reduction Parameters
  int _d = 9;
  double _sigmaColor = 75.0;
  double _sigmaSpace = 75.0;

  Future<void> _pickAndReduceNoise() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _originalImage = imageBytes);

    final result = await SnapLib.reduceNoise(
      imageBytes,
      d: _d,
      sigmaColor: _sigmaColor,
      sigmaSpace: _sigmaSpace,
      returnBase64: _returnBase64,
    );

    setState(() {
      if (_returnBase64) {
        _base64String = result as String;
      } else {
        _processedImage = result as Uint8List;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Reduce Noise")),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              ElevatedButton(
                onPressed: _pickAndReduceNoise,
                child: const Text("Pick Image"),
              ),
              const SizedBox(height: 10),
              if (_originalImage != null)
                Image.memory(_originalImage!, height: 150),
              const SizedBox(height: 10),
              if (_processedImage != null)
                Image.memory(_processedImage!, height: 150),
              if (_base64String != null) Text(_base64String!.substring(0, 100)),

              // Noise Reduction Controls
              const SizedBox(height: 10),
              Text("d (Diameter): $_d"),
              Slider(
                value: _d.toDouble(),
                min: 1,
                max: 20,
                divisions: 19,
                label: "$_d",
                onChanged: (value) => setState(() => _d = value.toInt()),
              ),

              Text("Sigma Color: ${_sigmaColor.toStringAsFixed(1)}"),
              Slider(
                value: _sigmaColor,
                min: 10.0,
                max: 150.0,
                divisions: 14,
                label: "${_sigmaColor.toStringAsFixed(1)}",
                onChanged: (value) => setState(() => _sigmaColor = value),
              ),

              Text("Sigma Space: ${_sigmaSpace.toStringAsFixed(1)}"),
              Slider(
                value: _sigmaSpace,
                min: 10.0,
                max: 150.0,
                divisions: 14,
                label: "${_sigmaSpace.toStringAsFixed(1)}",
                onChanged: (value) => setState(() => _sigmaSpace = value),
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
