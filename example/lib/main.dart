import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart'; // Import SnapLib

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'SnapLib Example',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const ImageProcessorScreen(),
    );
  }
}

class ImageProcessorScreen extends StatefulWidget {
  const ImageProcessorScreen({super.key});

  @override
  _ImageProcessorScreenState createState() => _ImageProcessorScreenState();
}

class _ImageProcessorScreenState extends State<ImageProcessorScreen> {
  Uint8List? _originalImageBytes;
  Uint8List? _processedImageBytes;
  String? _base64String;
  bool _isProcessing = false;

  /// **Pick Image from Gallery**
  Future<void> _pickImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() {
      _originalImageBytes = imageBytes;
      _isProcessing = true;
    });

    await _processImage(imageBytes);
  }

  /// **Process the Image and Convert to Base64**
  Future<void> _processImage(Uint8List imageBytes) async {
    try {
      Uint8List? processedImage = await SnapLib.processImage(
        imageBytes,
        gamma: 1.2, // Adjust gamma for processing
        noiseReduction: true,
        sharpening: true,
      );

      if (processedImage != null) {
        setState(() {
          _processedImageBytes = processedImage;
          _base64String = base64Encode(processedImage);
          _isProcessing = false;
        });
      } else {
        setState(() {
          _isProcessing = false;
        });
        _showErrorDialog("Image processing failed. Please try again.");
      }
    } catch (e) {
      setState(() {
        _isProcessing = false;
      });
      _showErrorDialog("Error processing image: ${e.toString()}");
    }
  }

  /// **Show error message in a dialog**
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
      appBar: AppBar(title: const Text('SnapLib Image Processor')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: _pickImage,
              child: _isProcessing
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(color: Colors.white))
                  : const Text("Pick and Process Image"),
            ),
            const SizedBox(height: 16),
            if (_originalImageBytes != null)
              Column(
                children: [
                  const Text("Original Image"),
                  Image.memory(_originalImageBytes!, height: 150),
                  const SizedBox(height: 16),
                ],
              ),
            if (_processedImageBytes != null)
              Column(
                children: [
                  const Text("Processed Image"),
                  Image.memory(_processedImageBytes!, height: 150),
                  const SizedBox(height: 16),
                  const Text("Base64 Output (Shortened)"),
                  Text(
                    _base64String?.substring(0, 100) ??
                        "Error encoding image...",
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => SnapLib.startFrontSnap("ถ่ายภาพหน้าบัตรประชาชน"),
        child: const Icon(Icons.camera),
      ),
    );
  }
}
