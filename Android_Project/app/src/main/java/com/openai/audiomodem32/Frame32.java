package com.openai.audiomodem32;

import java.nio.*;
import java.util.zip.CRC32;

public final class Frame32 {
    public int type,mode,session,packet,total,payloadLen,fileSize;
    public long fileCrc,packetCrc;
    public byte[] payload;

    public static long crc(byte[] b){
        CRC32 c=new CRC32(); c.update(b); return c.getValue();
    }

    public byte[] encode(){
        byte[] h=new byte[Protocol32.HEADER_BYTES];
        ByteBuffer q=ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN);
        q.put((byte)'V').put((byte)'3').put((byte)2);
        q.put((byte)type).put((byte)mode);
        q.putInt(session).putInt(packet).putInt(total);
        q.putShort((short)payloadLen).putInt(fileSize).putInt((int)fileCrc).putInt((int)packetCrc);
        q.putInt(0);
        int x=0; for(int i=0;i<35;i++) x^=h[i]&255; h[35]=(byte)x;
        return h;
    }

    public static Frame32 decodeHeader(byte[] h){
        if(h.length<36 || h[0]!='V' || h[1]!='3' || h[2]!=2) return null;
        int x=0; for(int i=0;i<35;i++) x^=h[i]&255;
        if((byte)x!=h[35]) return null;
        ByteBuffer q=ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN);
        q.position(3);
        Frame32 f=new Frame32();
        f.type=q.get()&255; f.mode=q.get()&255;
        f.session=q.getInt(); f.packet=q.getInt(); f.total=q.getInt();
        f.payloadLen=q.getShort()&0xffff; f.fileSize=q.getInt();
        f.fileCrc=q.getInt()&0xffffffffL; f.packetCrc=q.getInt()&0xffffffffL;
        return f;
    }
}
