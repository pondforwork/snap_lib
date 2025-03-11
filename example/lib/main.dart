import 'package:flutter/material.dart';
import 'package:snap_lib_example/examples/apply_gamma_page.dart';
import 'package:snap_lib_example/examples/calculate_brightness_page.dart';
import 'package:snap_lib_example/examples/calculate_contrast_page.dart';
import 'package:snap_lib_example/examples/calculate_glare_page.dart';
import 'package:snap_lib_example/examples/calculate_resolution_page.dart';
import 'package:snap_lib_example/examples/calculate_snr_page.dart';
import 'package:snap_lib_example/examples/enhance_sharpen_page.dart';
import 'package:snap_lib_example/examples/process_back_card_page.dart';
import 'package:snap_lib_example/examples/process_font_card_page.dart';
import 'package:snap_lib_example/examples/process_image_page.dart';
import 'package:snap_lib_example/examples/quality_check_page.dart';
import 'package:snap_lib_example/examples/reduce_noise_page.dart';

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
      home: const HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("SnapLib Functions")),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _buildNavButton(context, "Process Image", const ProcessImagePage()),
          _buildNavButton(
              context, "Process Font Card", const ProcessFontCardPage()),
          _buildNavButton(
              context, "Process Back Card", const ProcessBackCardPage()),
          _buildNavButton(
              context, "Image Quality Check", const QualityCheckPage()),
          _buildNavButton(
              context, "Calculate Brightness", const CalculateBrightnessPage()),
          _buildNavButton(
              context, "Calculate Glare", const CalculateGlarePage()),
          _buildNavButton(context, "Calculate SNR", const CalculateSNRPage()),
          _buildNavButton(
              context, "Calculate Contrast", const CalculateContrastPage()),
          _buildNavButton(
              context, "Calculate Resolution", const CalculateResolutionPage()),
          _buildNavButton(
              context, "Enhance Sharpen", const EnhanceSharpenPage()),
          _buildNavButton(context, "Reduce Noise", const ReduceNoisePage()),
          _buildNavButton(
              context, "Apply Gamma Correction", const ApplyGammaPage()),
        ],
      ),
    );
  }

  Widget _buildNavButton(BuildContext context, String title, Widget page) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: ElevatedButton(
        onPressed: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => page),
        ),
        child: Text(title),
      ),
    );
  }
}
