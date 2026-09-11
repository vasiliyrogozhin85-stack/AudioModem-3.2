AudioModem 3.3 Android source

This source implements the universal-file application layer:
- accepts any MIME type through Android Storage Access Framework;
- calculates file CRC32;
- creates FILEINFO metadata;
- reads/writes DATA packets in 1024-byte blocks.

The acoustic PHY (QAM/audio framing) is inherited from 3.2 and must be connected
to Frame33 / UniversalFile33 for end-to-end acoustic transfer.
