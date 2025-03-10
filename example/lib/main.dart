import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:snap_lib/snap_lib.dart';

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
  Uint8List? _imageBytes;
  String? _base64String;
  double? _brightness;
  String? _savedFilePath;

  Future<void> _pickImage() async {
    final pickedFile =
        await ImagePicker().pickImage(source: ImageSource.gallery);
    if (pickedFile == null) return;

    final imageBytes = await pickedFile.readAsBytes();
    setState(() {
      _imageBytes = imageBytes;
    });

    _processImage(imageBytes);
  }

  Future<void> _processImage(Uint8List imageBytes) async {
    double? brightness = await SnapLib.calculateBrightness(imageBytes);
    String? base64String = await SnapLib.convertMatToBase64(imageBytes);
    String? savedPath = await SnapLib.convertMatToFile(
        imageBytes, "/storage/emulated/0/Download/snap_image.jpg");

    setState(() {
      _brightness = brightness;
      _base64String = base64String;
      _savedFilePath = savedPath;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('SnapLib Example')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
              onPressed: _pickImage,
              child: const Text("Pick Image"),
            ),
            const SizedBox(height: 16),
            if (_imageBytes != null) Image.memory(_imageBytes!, height: 200),
            const SizedBox(height: 16),
            if (_brightness != null) Text("Brightness: $_brightness"),
            if (_base64String != null)
              Text("Base64 Length: ${_base64String!.length}"),
            if (_savedFilePath != null) Text("Saved Image: $_savedFilePath"),
          ],
        ),
      ),
    );
  }
}
