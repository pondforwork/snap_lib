# SnapLib

SnapLib เป็นไลบรารีสำหรับการประมวลผลภาพและการสแกนบัตรประชาชนหรือใบหน้า โดยมีการตั้งค่าต่างๆ ที่สามารถปรับแต่งได้ตามความต้องการ

## การติดตั้ง

เพิ่มไลบรารีนี้ใน `pubspec.yaml` ของโปรเจกต์ของคุณ:

```yaml
dependencies:
  snap_lib:
    path: ../snap_lib
```

## การใช้งาน

### การตั้งค่าการสแกนใบหน้า (ScanFaceSettings)

```dart
enum FaceSnapMode { normal, strict }

class ScanFaceSettings {
  final String guideText;
  final String instructionText;
  final String successText;
  final int borderColorSuccess;
  final int borderColorDefault;
  final int textColorDefault;
  final int textColorSuccess;
  final double guideFontSize;
  final double instructionFontSize;
  final int guideTextColor;
  final int instructionTextColor;
  final FaceSnapMode faceSnapMode;

  ScanFaceSettings({
    this.guideText = "ให้ใบหน้าอยู่ในกรอบที่กำหนด",
    this.instructionText = "ไม่มีปิดตา จมูก ปาก และคาง",
    this.successText = "ถือค้างไว้",
    this.borderColorSuccess = 0xFF00FF00, // สีเขียว
    this.borderColorDefault = 0xFFFF0000, // สีแดง
    this.textColorDefault = 0xFFFFFFFF, // สีขาว
    this.textColorSuccess = 0xFF00FF00, // สีเขียว
    this.guideFontSize = 22.0,
    this.instructionFontSize = 18.0,
    this.guideTextColor = 0xFFFFFF00, // สีเหลือง
    this.instructionTextColor = 0xFF00FFFF, // สีฟ้า
    this.faceSnapMode = FaceSnapMode.normal,
  });
}
```

### การตั้งค่าการสแกนบัตร (FrontSnapSettings)

```dart
class FrontSnapSettings {
  final String titleMessage;
  final double titleFontSize;
  final double guideMessageFontSize;
  final String initialMessage;
  final String foundMessage;
  final String notFoundMessage;
  final SnapMode snapMode;

  FrontSnapSettings({
    required this.titleMessage,
    required this.titleFontSize,
    required this.guideMessageFontSize,
    required this.initialMessage,
    required this.foundMessage,
    required this.notFoundMessage,
    required this.snapMode,
  });
}
```

### การตั้งค่าการประมวลผลภาพ (ImageProcessingSettings)

```dart
class ImageProcessingSettings {
  final bool isDetectNoise;
  final bool isDetectBrightness;
  final bool isDetectGlare;
  final double maxNoiseValue;
  final double maxBrightnessValue;
  final double minBrightnessValue;
  final double maxGlarePercent;

  ImageProcessingSettings({
    required this.isDetectNoise,
    required this.isDetectBrightness,
    required this.isDetectGlare,
    required this.maxNoiseValue,
    required this.maxBrightnessValue,
    required this.minBrightnessValue,
    required this.maxGlarePercent,
  });
}
```

### การตั้งค่าข้อความเตือน (WarningMessages)

```dart
class WarningMessages {
  final String warningMessage;
  final String warningNoise;
  final String warningBrightnessOver;
  final String warningBrightnessLower;
  final String warningGlare;

  WarningMessages({
    required this.warningMessage,
    required this.warningNoise,
    required this.warningBrightnessOver,
    required this.warningBrightnessLower,
    required this.warningGlare,
  });
}
```

### การตั้งค่าการแสดงผลของ Dialog (DialogStyleSettings)

```dart
class DialogStyleSettings {
  final int backgroundColor;
  final int titleColor;
  final int subtitleColor;
  final int buttonConfirmColor;
  final int buttonRetakeColor;
  final int buttonTextColor;
  final String extraMessage;
  final int extraMessageColor;

  DialogStyleSettings({
    required this.backgroundColor,
    required this.titleColor,
    required this.subtitleColor,
    required this.buttonConfirmColor,
    required this.buttonRetakeColor,
    required this.buttonTextColor,
    required this.extraMessage,
    required this.extraMessageColor,
  });
}
```

## ตัวอย่างการใช้งาน

### การสแกนบัตร

```dart
void startCardScan() {
  SnapLib.startCardSnap(
    FrontSnapSettings(
      titleMessage: "สแกนหน้าบัตร",
      titleFontSize: 10,
      guideMessageFontSize: 30,
      initialMessage: "กรุณาวางบัตรในกรอบ",
      foundMessage: "ถือนิ่งๆ",
      notFoundMessage: "กรุณาวางบัตรในกรอบ",
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
      backgroundColor: 0xFFFFFFFF,
      titleColor: 0xFF000000,
      subtitleColor: 0xFFFFFFFF,
      buttonConfirmColor: 0xFF00FF00,
      buttonRetakeColor: 0xFFFF0000,
      buttonTextColor: 0xFF000000,
      extraMessage: "ตรวจสอบให้แน่ใจว่ารูปภาพสามารถอ่านได้ชัดเจน",
      extraMessageColor: 0xFFFFA500,
    ),
  );
}
```

### การสแกนใบหน้า

```dart
void startFaceScan() {
  SnapLib.startFaceSnap(
    titleMessage: "สแกนหน้า",
    initialMessage: "กรุณาวางใบหน้าในกรอบ",
    foundMessage: "ถือนิ่งๆ",
    notFoundMessage: "กรุณาวางใบหน้าในกรอบ",
    snapMode: 'strict',
  );
}
```

## การสนับสนุน

หากคุณมีคำถามหรือพบปัญหาใดๆ สามารถติดต่อเราได้ที่ [support@example.com](mailto:support@example.com)

