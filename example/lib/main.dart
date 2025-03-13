import 'package:flutter/material.dart';
import 'package:snap_lib/settings/ImageProcessingSettings.dart';
import 'package:snap_lib/settings/WarningMessages.dart';
import 'package:snap_lib/settings/dialog_setting.dart';
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
  // Front & Back Card Scan Settings
  final FrontSnapSettings settingsFront = FrontSnapSettings(
    titleMessage: "สแกนหน้าบัตร",
    titleFontSize: 10,
    guideMessageFontSize: 30,
    initialMessage: "กรุณาวางบัตรในกรอบ",
    foundMessage: "ถือนิ่งๆ",
    notFoundMessage: " กรุณาวางบัตรในกรอบ",
    snapMode: SnapMode.front,
  );

  final FrontSnapSettings settingsBack = FrontSnapSettings(
    titleMessage: "สแกนหลังบัตร",
    titleFontSize: 17,
    guideMessageFontSize: 23,
    initialMessage: "กรุณาวางบัตรในกรอบ",
    foundMessage: "ถือนิ่งๆ",
    notFoundMessage: "กรุณาวางบัตรในกรอบ",
    snapMode: SnapMode.back,
  );

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
              context, "Process Front Card", const ProcessFontCardPage()),
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

          const SizedBox(height: 10),

          // OpenScanFace Button

          const SizedBox(height: 10),

          // Normal Button for openScanFace
          _buildCustomButton(
            title: "Open Scan Face",
            onTap: () => SnapLib.startFaceSnap(
                titleMessage: "สแกนหน้า",
                initialMessage: "กรุณาวางใบหน้าในกรอบ",
                foundMessage: "ถือนิ่งๆ",
                notFoundMessage: "กรุณาวางใบหน้าในกรอบ",
                snapMode: 'back'),
          ),
        ],
      ),

      // Floating button for capturing front/back card
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          // Default to front card scan
          SnapLib.startCardSnap(
              FrontSnapSettings(
                titleMessage: "หลัง",
                initialMessage: "กรุณาวางบัตรในกรอบ",
                foundMessage: "พบบัตร ถือค้างไว้",
                notFoundMessage: "ไม่พบบัตร",
                snapMode: SnapMode.front,
              ),
              ImageProcessingSettings(
                isDetectNoise: true,
                isDetectBrightness: true,
                isDetectGlare: true,
                maxNoiseValue: 3.5,
                maxBrightnessValue: 130.0,
                minBrightnessValue: 90.0,
                maxGlarePercent: 1.0,
              ),
              WarningMessages(
                warningMessage: "⚠️ กรุณาปรับแสงให้เหมาะสม",
                warningNoise: "⚠️ โปรดลด Noise",
                warningBrightnessOver: "⚠️ แสงจ้าเกินไป",
                warningBrightnessLower: "⚠️ แสงน้อยเกินไป",
                warningGlare: "⚠️ ลดแสงสะท้อน",
              ),
              DialogStyleSettings(
                backgroundColor: 0xFFFFFFFF, // Black

                titleColor: 0xFF000000, // Yellow
                subtitleColor: 0xFFFFFFFF, // White
                buttonConfirmColor: 0xFF00FF00, // Green
                buttonRetakeColor: 0xFFFF0000, // Red
                buttonTextColor: 0xFF000000, // Black
                extraMessage: "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน",
                extraMessageColor: 0xFFFFA500, // Orange
              ));
          // To scan back card, change to settingsBack
        },
        child: const Icon(Icons.camera_alt),
      ),
    );
  }

  /// Builds navigation buttons for different processing pages
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

  /// Creates a custom button with configurable properties
  Widget _buildCustomButton(
      {required String title,
      required VoidCallback onTap,
      Color color = Colors.blue}) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        height: 50,
        width: double.infinity,
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Center(
          child: Text(
            title,
            style: const TextStyle(color: Colors.white, fontSize: 20),
          ),
        ),
      ),
    );
  }
}
