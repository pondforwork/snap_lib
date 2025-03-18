# SnapLib

SnapLib เป็นไลบรารีสำหรับการประมวลผลภาพและการสแกนใบหน้า/บัตรประชาชน โดยสามารถตั้งค่าและปรับแต่งได้ตามความต้องการ รองรับการปรับแกมมา ลดเสียงรบกวน เพิ่มความคมชัด และตรวจสอบคุณภาพของภาพ

## การติดตั้ง

เพิ่ม SnapLib ลงใน `pubspec.yaml` ของโปรเจกต์ของคุณ:

```yaml
dependencies:
  snap_lib:
    path: ../snap_lib
```

---

## 📌 ฟังก์ชันการประมวลผลภาพ

### 🖼 processImage (ปรับแต่งภาพ)

ใช้สำหรับปรับแกมมา ลดเสียงรบกวน และเพิ่มความคมชัด

```dart
static Future<dynamic> processImage(
  Uint8List imageBytes, {
  double gamma = 1.0,
  int d = 9,
  double sigmaColor = 75.0,
  double sigmaSpace = 75.0,
  double sharpenStrength = 1.0,
  double blurKernelSize = 3.0,
  bool applyGamma = true,
  bool reduceNoise = true,
  bool enhanceSharpen = true,
  bool returnBase64 = true,
}) async
```

**🔹 Parameter อธิบาย**

- `imageBytes` : ข้อมูลรูปภาพที่รับเข้ามา
- `gamma` : ค่าแกมมาสำหรับปรับแสง (ค่าเริ่มต้น 1.0)
- `d` : ขนาดของเคอร์เนลสำหรับลดเสียงรบกวน (ค่าเริ่มต้น 9)
- `sigmaColor`, `sigmaSpace` : ค่าการลดเสียงรบกวนสีและเชิงพื้นที่
- `sharpenStrength` : ค่าการเพิ่มความคมชัด (ค่าเริ่มต้น 1.0)
- `blurKernelSize` : ขนาดของเคอร์เนลสำหรับเบลอภาพ
- `applyGamma`, `reduceNoise`, `enhanceSharpen` : เปิด/ปิดฟีเจอร์แต่ละตัว
- `returnBase64` : คืนค่าภาพเป็น Base64 หรือไม่

**🔹 ตัวอย่างการใช้งาน**

```dart
final processedImage = await SnapLib.processImage(
  imageBytes,
  gamma: 2.0,
  reduceNoise: true,
  enhanceSharpen: true,
);
```

---

### 🆔 processFontCard (ประมวลผลบัตรด้านหน้า)

```dart
static Future<dynamic> processFontCard(
  Uint8List imageBytes, {
  double snr = 0.0,
  double contrast = 0.0,
  double brightness = 0.0,
  double glarePercent = 0.0,
  String resolution = "0x0",
  double gamma = 1.0,
  bool useBilateralFilter = true,
  int d = 9,
  double sigmaColor = 75.0,
  double sigmaSpace = 75.0,
  bool useSharpening = true,
  double sharpenStrength = 1.0,
  double blurKernelSize = 3.0,
  bool returnBase64 = true,
}) async
```

**🔹 Parameter อธิบาย**

- `snr`, `contrast`, `brightness`, `glarePercent` : คุณภาพของภาพที่ได้รับ
- `resolution` : ขนาดของภาพ เช่น "1920x1080"
- `gamma`, `useBilateralFilter`, `useSharpening` : เปิด/ปิดฟีเจอร์ที่ต้องการ
- `d`, `sigmaColor`, `sigmaSpace` : ค่าการลด Noise
- `sharpenStrength`, `blurKernelSize` : ค่าการเพิ่มความคมชัด
- `returnBase64` : คืนค่าภาพเป็น Base64 หรือไม่

---

### 🔍 isImageQualityAcceptable (ตรวจสอบคุณภาพของภาพ)

```dart
static Future<bool> isImageQualityAcceptable(
  Uint8List imageBytes, {
  double snr = 0.0,
  double contrast = 0.0,
  double brightness = 0.0,
  double glarePercent = 0.0,
  String resolution = "0x0",
  int minResolution = 500,
  double snrThreshold = 3.0,
  double contrastThreshold = 50.0,
  int maxBrightness = 200,
  double maxGlarePercent = 1.0,
}) async
```

---

## 🎨 ฟังก์ชันเสริม

### 🔥 applyGammaCorrection (ปรับค่าความสว่างของภาพ)

```dart
static Future<dynamic> applyGammaCorrection(
    Uint8List imageBytes, double gamma,
    {bool returnBase64 = true}) async
```

### 🧹 reduceNoise (ลดเสียงรบกวน)

```dart
static Future<dynamic> reduceNoise(
  Uint8List imageBytes, {
  int d = 9,
  double sigmaColor = 75.0,
  double sigmaSpace = 75.0,
  bool returnBase64 = true,
}) async
```

### ✨ enhanceSharpen (เพิ่มความคมชัด)

```dart
static Future<dynamic> enhanceSharpen(
  Uint8List imageBytes, {
  double strength = 1.5,
  double blurKernelSize = 5.0,
  bool returnBase64 = true,
}) async
```

---

## 📌 ตัวอย่างการสแกนใบหน้า

```dart
void startFaceScan() {
  SnapLib.startFaceSnap(
    faceSettings: ScanFaceSettings(
      guideText: "ให้ใบหน้าอยู่ในกรอบ",
      successText: "สำเร็จ!",
      borderColorDefault: 0xFF008080,
      borderColorSuccess: 0xFF00FF00,
    ),
  );
}
```

---

## 📌 ตัวอย่างการสแกนบัตร

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
  );
}
```


