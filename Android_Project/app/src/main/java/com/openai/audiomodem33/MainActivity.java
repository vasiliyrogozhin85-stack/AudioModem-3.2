package com.openai.audiomodem33;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.*;
import android.widget.*;
import java.io.*;

public class MainActivity extends Activity {
    private static final int PICK = 1001;
    private TextView status;
    private Uri selected;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(24,24,24,24);
        Button pick = new Button(this); pick.setText("Выбрать любой файл");
        Button info = new Button(this); info.setText("Подготовить FILEINFO");
        status = new TextView(this); status.setText("AudioModem 3.3 Universal");
        l.addView(pick); l.addView(info); l.addView(status);
        setContentView(l);
        pick.setOnClickListener(v -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE), PICK));
        info.setOnClickListener(v -> prepare());
    }

    @Override protected void onActivityResult(int r, int c, Intent d) {
        super.onActivityResult(r,c,d);
        if(r==PICK && c==RESULT_OK && d!=null){ selected=d.getData(); status.setText("Выбрано: "+nameOf(selected)); }
    }

    private String nameOf(Uri u) {
        try(Cursor c=getContentResolver().query(u,null,null,null,null)){
            if(c!=null && c.moveToFirst()){int i=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(i>=0)return c.getString(i);}
        }
        return "file.bin";
    }

    private void prepare() {
        if(selected==null){status.setText("Сначала выберите файл");return;}
        try {
            File tmp = new File(getCacheDir(),"am33_selected.bin");
            try(InputStream in=getContentResolver().openInputStream(selected); OutputStream out=new FileOutputStream(tmp)){
                byte[] b=new byte[32768]; int n; while((n=in.read(b))>0) out.write(b,0,n);
            }
            UniversalFile33.Info i=UniversalFile33.inspect(tmp);
            status.setText("Готово: "+nameOf(selected)+"\nРазмер: "+i.size+"\nПакетов: "+i.totalPackets+"\nCRC32: "+Long.toHexString(i.crc));
        } catch(Exception e){status.setText("Ошибка: "+e.getMessage());}
    }
}
