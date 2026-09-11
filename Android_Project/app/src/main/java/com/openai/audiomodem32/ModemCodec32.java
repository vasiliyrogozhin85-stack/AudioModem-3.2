package com.openai.audiomodem32;

import java.io.*;
import java.util.*;

public final class ModemCodec32 {
    public static short[] encodeFrame(Frame32 f){
        byte[] header=f.encode();
        byte[] payload=f.payload==null?new byte[0]:f.payload;
        ArrayList<short[]> parts=new ArrayList<>();

        for(int i=0;i<Protocol32.PREAMBLE;i++) parts.add(Qam32.known(i));
        for(int i=0;i<Protocol32.SYNC;i++) parts.add(Qam32.known(i+100));
        addBytes(parts,header);
        addBytes(parts,payload);

        int n=0; for(short[] p:parts)n+=p.length;
        short[] out=new short[n]; int pos=0;
        for(short[] p:parts){System.arraycopy(p,0,out,pos,p.length);pos+=p.length;}
        return out;
    }

    static void addBytes(ArrayList<short[]> parts, byte[] b){
        for(int p=0;p<b.length;p+=7){
            byte[] q=new byte[7];
            System.arraycopy(b,p,q,0,Math.min(7,b.length-p));
            parts.add(Qam32.symbol(q,p/7));
        }
    }

    public static Frame32 tryDecode(short[] pcm,int count){
        int min=(Protocol32.PREAMBLE+Protocol32.SYNC+
                (Protocol32.HEADER_BYTES+6)/7)*Protocol32.SYM;
        if(count<min)return null;

        // Prototype timing search. Header XOR sharply rejects wrong offsets.
        int maxStart=Math.min(count-min,Protocol32.SR*2);
        for(int start=0;start<=maxStart;start+=4){
            int hp=start+(Protocol32.PREAMBLE+Protocol32.SYNC)*Protocol32.SYM;
            byte[] h=decodeBytes(pcm,hp,Protocol32.HEADER_BYTES);
            Frame32 f=Frame32.decodeHeader(h);
            if(f==null || f.payloadLen<0 || f.payloadLen>Protocol32.PAYLOAD_MAX)continue;
            int pp=hp+((Protocol32.HEADER_BYTES+6)/7)*Protocol32.SYM;
            int need=pp+((f.payloadLen+6)/7)*Protocol32.SYM;
            if(need>count)return null;
            f.payload=decodeBytes(pcm,pp,f.payloadLen);
            if(Frame32.crc(f.payload)!=f.packetCrc)return null;
            return f;
        }
        return null;
    }

    static byte[] decodeBytes(short[] pcm,int start,int count){
        byte[] out=new byte[count]; int pos=0;
        int syms=(count+6)/7;
        for(int k=0;k<syms;k++){
            byte[] q=Qam32.decode(pcm,start+k*Protocol32.SYM,k);
            for(int i=0;i<7 && pos<count;i++)out[pos++]=q[i];
        }
        return out;
    }
}
