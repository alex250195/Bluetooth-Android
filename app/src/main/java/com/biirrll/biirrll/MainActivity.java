package com.biirrll.biirrll;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Bluetooth bt;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            this.bt = new Bluetooth();
            this.bt.ativaBluetooth();
        } catch (Exception ex) {
            Log.i("Bluetooth -> ", "Erro - [" + ex.getMessage() + "]");
        }

        final Button btnLigar = (Button) findViewById(R.id.button);
        final Button btnDesligar = (Button) findViewById(R.id.button2);

        btnLigar.setBackgroundResource(R.color.desligar);
        btnDesligar.setBackgroundResource(R.color.desligar);

        btnLigar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLigar.setBackgroundResource(R.color.ligar);
                btnDesligar.setBackgroundResource(R.color.desligar);
                Ligar();
            }
        });

        btnDesligar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLigar.setBackgroundResource(R.color.desligar);
                btnDesligar.setBackgroundResource(R.color.ligar);
                Desligar();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            bt.desabilitarBluetooth();
        }catch (Exception ex){
            Log.i("Bluetooth", "Erro ao desativar bluetooth - [" + ex.getMessage() + "]");
        }

        finish();
    }

    public void Ligar(){
        toast = Toast.makeText(getBaseContext(), "Ligando...", Toast.LENGTH_SHORT);
        toast.show();
        Log.i("Bluetooth", "Ligando");
        bt.enviaDados("botao", "ligar");
    }

    public void Desligar(){
        toast = Toast.makeText(getBaseContext(), "Desligando...", Toast.LENGTH_SHORT);
        toast.show();
        Log.i("Bluetooth", "Desligando");
        bt.enviaDados("botao", "desligar");
    }
}
