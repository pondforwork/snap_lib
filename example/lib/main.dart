import 'dart:convert';
import 'dart:ffi';
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
  bool _returnBase64 = false;
  double? _brightness;
  double? _noise;
  double? _glare;

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

  Future<void> _processImage(Uint8List imageBytes) async {
    try {
      setState(() {
        _isProcessing = true;
      });

      if (_returnBase64) {
        String? processedBase64 = await SnapLib.processImage(
          imageBytes,
          gamma: 1.5,
          noiseReduction: true,
          sharpening: true,
          returnBase64: true,
        );

        if (processedBase64 != null && processedBase64.isNotEmpty) {
          final sanitizedBase64 = processedBase64
              .replaceAll(RegExp(r'data:image/[^;]+;base64,'), '')
              .trim();

          setState(() {
            _base64String = sanitizedBase64;
            _processedImageBytes = null;
          });
        } else {
          _showErrorDialog("Failed to process image as Base64.");
        }
      } else {
        Uint8List? processedImage = await SnapLib.processImage(
          imageBytes,
          gamma: 1.0,
          noiseReduction: true,
          sharpening: true,
          returnBase64: false,
        );

        if (processedImage != null) {
          setState(() {
            _processedImageBytes = processedImage;
            _base64String = base64Encode(processedImage); // Proper encoding
          });
        } else {
          _showErrorDialog("Failed to process image as Uint8List.");
        }
      }

      // Calculate brightness, noise, and glare
      await _calculateBrightness(imageBytes);
      await _calculateNoise(imageBytes);
      await _calculateGlare(imageBytes);
    } catch (e) {
      _showErrorDialog("Error processing image: ${e.toString()}");
    } finally {
      setState(() {
        _isProcessing = false;
      });
    }
  }
// // get brightness image calculateBrightness

  Future<void> _calculateBrightness(Uint8List imageBytes) async {
    try {
      double? brightness = await SnapLib.calculateBrightness(imageBytes);
      if (brightness != null) {
        setState(() {
          _brightness = brightness;
        });
      } else {
        _showErrorDialog("Failed to calculate brightness.");
      }
    } catch (e) {
      _showErrorDialog("Error calculating brightness: ${e.toString()}");
    }
  }

  // noise
  Future<void> _calculateNoise(Uint8List imageBytes) async {
    try {
      double? noise = await SnapLib.calculateSNR(imageBytes);
      if (noise != null) {
        setState(() {
          _noise = noise;
        });
      } else {
        _showErrorDialog("Failed to calculate noise.");
      }
    } catch (e) {
      _showErrorDialog("Error calculating noise: ${e.toString()}");
    }
  }

  // calculateGlare
  Future<void> _calculateGlare(Uint8List imageBytes) async {
    try {
      double? glare = await SnapLib.calculateGlare(imageBytes);
      if (glare != null) {
        setState(() {
          _glare = glare;
        });
      } else {
        _showErrorDialog("Failed to calculate glare.");
      }
    } catch (e) {
      _showErrorDialog("Error calculating glare: ${e.toString()}");
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

            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text("Return as Uint8List"),
                Switch(
                  value: _returnBase64,
                  onChanged: (value) {
                    setState(() {
                      _returnBase64 = value;
                    });
                  },
                ),
                const Text("Return as Base64"),
              ],
            ),
            const SizedBox(height: 16),

            // **Show original image**
            if (_originalImageBytes != null)
              Column(
                children: [
                  const Text("Original Image"),
                  Image.memory(_originalImageBytes!, height: 150),
                  const SizedBox(height: 16),
                ],
              ),

            if (!_returnBase64 && _processedImageBytes != null)
              Column(
                children: [
                  const Text("Processed Image"),
                  Image.memory(_processedImageBytes!, height: 150),
                  const SizedBox(height: 16),
                ],
              ),

            if (_returnBase64 && _base64String != null)
              Column(
                children: [
                  const Text("Base64 Output (Shortened)"),
                  if (_base64String != null && _base64String!.isNotEmpty)
                    Column(
                      children: [
                        const SizedBox(height: 16),
                        Text(
                          _base64String!.substring(0, 100) + "...",
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                ],
              ),

            // Display brightness, noise, and glare values
            if (_brightness != null) Text("Brightness: $_brightness"),
            if (_noise != null) Text("Noise: $_noise"),
            if (_glare != null) Text("Glare: $_glare"),
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
