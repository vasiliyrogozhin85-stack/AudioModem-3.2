AudioModem 3.3 Universal File Transfer

Goal: transmit any file type without pre-archiving.

Included:
- VBA_TXT: 3.3 universal file/application-layer modules.
- Android_Project: buildable Android 3.3 application-layer project.
- PROTOCOL_3_3.txt: common metadata/data framing.
- GitHub Actions workflow for building a debug APK.

Important engineering status:
The universal file layer is implemented. The acoustic QAM/record/playback PHY remains
the 3.2 experimental layer and must be wired to Frame33/UniversalFile33 for complete
over-the-air transfer. This package should not be described as acoustically verified.
