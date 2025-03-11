import 'dart:typed_data';

import 'package:flutter/material.dart';
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
      home: Scaffold(
        appBar: AppBar(title: const Text("SnapLib Example")),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () async {
                  // Example: Call SnapLib method
                  String? base64Image = await SnapLib.convertMatToBase64(
                      Uint8List.fromList([0, 1, 2, 3])); // Dummy image bytes
                  print("Base64 Image: $base64Image");
                },
                child: const Text("Test SnapLib"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
