package com.example.employeecheckin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.szsicod.print.escpos.PrinterAPI;
import com.szsicod.print.io.USBAPI;
import com.szsicod.print.log.Utils;

public class MainActivity2 extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Button printButton = findViewById(R.id.print);
        printButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!PrinterAPI.getInstance().isConnect())
                {
                    USBAPI usbapi = new USBAPI(MainActivity2.this);
                    usbapi.openDevice();
                    Utils.init(MainActivity2.this);
                    PrinterAPI.getInstance().setOutput(true);
                    PrinterAPI.getInstance().connect(usbapi);
                }

                PrinterAPI.getInstance().printQRCode("www.2mintek.com", 16, true);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (PrinterAPI.getInstance().isConnect())
        {
            PrinterAPI.getInstance().disconnect();
        }
    }
}