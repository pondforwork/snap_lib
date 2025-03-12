enum SnapMode { front, back }

class FrontSnapSettings {
  // ข้อความที่แสดงในหน้าต่าง
  final String titleMessage;
  // ข้อความเริ่มต้นก่อนเริ่มสแกน
  final String initialMessage;
  // ข้อความเมื่อพบบัตร
  final String foundMessage;
  // ข้อความเมื่อไม่พบบัตร
  final String notFoundMessage;
  // โหมดการสแกน (หน้า หรือ หลัง)
  final SnapMode snapMode;

  FrontSnapSettings({
    required this.snapMode,
    required this.titleMessage,
    required this.initialMessage,
    required this.foundMessage,
    required this.notFoundMessage,
  });
}
