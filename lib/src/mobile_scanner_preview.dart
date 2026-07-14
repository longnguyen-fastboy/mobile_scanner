import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

/// A widget showing a live camera preview.
class CameraPreview extends StatelessWidget {
  /// Creates a preview widget for the given camera controller.
  const CameraPreview(this.controller, {super.key});

  /// The controller for the camera that the preview is shown for.
  final MobileScannerController controller;

  @override
  Widget build(BuildContext context) {
    if (!controller.value.isInitialized) {
      return const SizedBox();
    }

    return ValueListenableBuilder<MobileScannerState>(
      valueListenable: controller,
      builder: (BuildContext context, MobileScannerState value, Widget? child) {
        return SizedBox.fromSize(
          size:
              // value.deviceOrientation.isLandscape
              //     ? value.size.flipped
              //     :
              value.size,
          child: _wrapInRotatedBox(child: controller.buildCameraView()),
        );
      },
    );
  }

  Widget _wrapInRotatedBox({required Widget child}) {
    if (kIsWeb || defaultTargetPlatform != TargetPlatform.android) {
      return child;
    }
    // Always keep the preview in portrait so it never rotates with the device
    // (the app is portrait-only; only the in-call screen uses landscape).
    return RotatedBox(
      quarterTurns: 0,
      child: child,
    );
  }
}
