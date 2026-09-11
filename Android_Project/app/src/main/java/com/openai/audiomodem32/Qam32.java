package com.openai.audiomodem32;

public final class Qam32 {
    static final double PI=Math.PI, AMP=26000.0, SCALE=AMP/52.0;

    static double level(int v){
        switch(v&3){case 0:return -3;case 1:return -1;case 3:return 1;default:return 3;}
    }
    static int bits(double x){
        if(x<-2)return 0; if(x<0)return 1; if(x<2)return 3; return 2;
    }

    public static short[] symbol(byte[] seven,int pilotPhase){
        double[] u=new double[Protocol32.USEFUL];
        int d=0;
        for(int c=0;c<Protocol32.TOTAL_CARRIERS;c++){
            double I,Q;
            if(Protocol32.pilot(c)){ I=((pilotPhase&1)==0)?3:-3; Q=0; }
            else{
                int nib=((d&1)==0)?((seven[d/2]>>4)&15):(seven[d/2]&15);
                I=level((nib>>2)&3); Q=level(nib&3); d++;
            }
            double w=2*PI*Protocol32.freq(c)/Protocol32.SR;
            for(int n=0;n<Protocol32.USEFUL;n++) u[n]+=I*Math.cos(w*n)-Q*Math.sin(w*n);
        }
        short[] out=new short[Protocol32.SYM];
        for(int n=0;n<Protocol32.CP;n++) out[n]=clip(u[Protocol32.USEFUL-Protocol32.CP+n]*SCALE);
        for(int n=0;n<Protocol32.USEFUL;n++) out[Protocol32.CP+n]=clip(u[n]*SCALE);
        return out;
    }

    static short clip(double x){ if(x>32767)x=32767;if(x<-32768)x=-32768;return (short)Math.round(x); }

    public static byte[] decode(short[] s,int start,int pilotPhase){
        byte[] out=new byte[Protocol32.BYTES_PER_SYM];
        int d=0;
        for(int c=0;c<Protocol32.TOTAL_CARRIERS;c++){
            double w=2*PI*Protocol32.freq(c)/Protocol32.SR,ia=0,qa=0;
            for(int n=0;n<Protocol32.USEFUL;n++){
                double x=s[start+Protocol32.CP+n];
                ia+=x*Math.cos(w*n); qa-=x*Math.sin(w*n);
            }
            double I=(2*ia/Protocol32.USEFUL)/SCALE;
            double Q=(2*qa/Protocol32.USEFUL)/SCALE;
            if(!Protocol32.pilot(c)){
                int nib=bits(I)*4+bits(Q);
                if((d&1)==0) out[d/2]=(byte)(nib<<4);
                else out[d/2]|=(byte)nib;
                d++;
            }
        }
        return out;
    }

    public static short[] known(int idx){
        byte[] b=new byte[7];
        java.util.Arrays.fill(b,(byte)(((idx&1)==0)?0x22:0xdd));
        return symbol(b,idx);
    }
}
