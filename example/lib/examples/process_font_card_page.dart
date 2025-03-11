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

  Future<void> _pickAndProcessImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() => _originalImage = imageBytes);

    final resolution = await SnapLib.calculateResolution(imageBytes);
    print("Resolution: $resolution");
    if (resolution == "0x0") {
      _showErrorDialog(
          "Image resolution is too low. Please select a higher resolution image.");
      return;
    }

    try {
      final result = await SnapLib.processFontCard(imageBytes,
          returnBase64: _returnBase64, resolution: resolution!);

      setState(() {
        if (_returnBase64) {
          _base64String = result as String;
        } else {
          _processedImage = result as Uint8List;
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
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
                onPressed: _pickAndProcessImage,
                child: const Text("Pick Image")),
            const SizedBox(height: 10),
            if (_originalImage != null)
              Image.memory(_originalImage!, height: 150),
            const SizedBox(height: 10),
            if (_processedImage != null)
              Image.memory(_processedImage!, height: 150),
            if (_base64String != null) Text(_base64String!.substring(0, 100)),
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
