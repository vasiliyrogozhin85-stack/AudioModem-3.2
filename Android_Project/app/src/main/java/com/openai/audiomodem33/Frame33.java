package com.openai.audiomodem33;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public final class Frame33 {
    public final int type, mode, sessionId, packetNo, total, payloadLen, fileSize;
    public final long fileCrc, packetCrc;
    public final byte[] payload;

    public Frame33(int type, int mode, int sessionId, int packetNo, int total,
                   int payloadLen, int fileSize, long fileCrc, long packetCrc, byte[] payload) {
        this.type=type; this.mode=mode; this.sessionId=sessionId; this.packetNo=packetNo;
        this.total=total; this.payloadLen=payloadLen; this.fileSize=fileSize;
        this.fileCrc=fileCrc; this.packetCrc=packetCrc; this.payload=payload;
    }

    public static long crc32(byte[] b) {
        CRC32 c = new CRC32(); c.update(b); return c.getValue();
    }

    public byte[] header() {
        ByteBuffer b = ByteBuffer.allocate(Protocol33.HEADER_BYTES).order(ByteOrder.LITTLE_ENDIAN);
        b.put((byte)'A').put((byte)'M').put((byte)'3').put((byte)'3');
        b.put((byte)type).put((byte)mode);
        b.putInt(sessionId).putInt(packetNo).putInt(total);
        b.putShort((short)payloadLen).putInt(fileSize).putInt((int)fileCrc).putInt((int)packetCrc);
        while (b.position() < 39) b.put((byte)0);
        byte[] a = b.array(); int x=0; for(int i=0;i<39;i++) x ^= a[i] & 0xff; a[39]=(byte)x;
        return a;
    }
}
