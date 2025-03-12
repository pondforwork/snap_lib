import 'package:flutter/material.dart';
import 'package:snap_lib/settings/front_snap_settings.dart';
import 'package:snap_lib/snap_lib.dart';
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
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatelessWidget {
  // การตั้งค่า หน้า FrontSnap
  FrontSnapSettings settingsFront = FrontSnapSettings(
      titleMessage: "สแกนหน้าบัตร",
      titleFontSize: 10,
      guideMessageFontSize: 30,
      initialMessage: "กรุณาวางบัตรในกรอบ",
      foundMessage: "ถือนิ่งๆ",
      notFoundMessage: " กรุณาวางบัตรในกรอบ",
      snapMode: SnapMode.front);

  FrontSnapSettings settingsBack = FrontSnapSettings(
      titleMessage: "สแกนหลังบัตร",
      titleFontSize: 17,
      guideMessageFontSize: 23,
      initialMessage: "กรุณาวางบัตรในกรอบ",
      foundMessage: "ถือนิ่งๆ",
      notFoundMessage: "กรุณาวางบัตรในกรอบ",
      snapMode: SnapMode.back);

  HomeScreen({super.key});

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
          //  normal button for openScanFace
          GestureDetector(
            onTap: () {
              SnapLib.openScanFace();
            },
            child: Container(
              height: 50,
              width: 200,
              decoration: BoxDecoration(
                color: Colors.blue,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Center(
                child: Text(
                  "Open Scan Face",
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                  ),
                ),
              ),
            ),
          )
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // ถ่ายภาพหน้าบัตร
          SnapLib.startCardSnap(settingsFront);

          // ถ่ายภาพหลังบัตร
          // SnapLib.startFrontSnap(settingsBack);

          // การเปลี่ยนจากหน้า เป็นหลัง ให้เปลี่ยนที่ settings ในตัวแปร
          // snapMode ให้เลือกระหว่าง SnapMode.front หรือ SnapMode.back
          // Front = สแกนหน้าบัตร , Back = สแกนหลังบัตร
        },
        child: Icon(Icons.camera_alt),
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
