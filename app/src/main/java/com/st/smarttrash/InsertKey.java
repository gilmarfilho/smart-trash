package com.st.smarttrash;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jorge teixeira on 05/07/17.
 */

public class InsertKey extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.insert_key_window);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        //Pega o tamanho da tela
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //Define o tamanho da tela
        getWindow().setLayout((int) (width*.8), (int) (height*.7));

        Button btn = (Button) findViewById(R.id.button_inserir);

        final TextView key = (TextView) findViewById(R.id.key);

        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                //Armazena a chave do thingspeak
                armazenar(key.getText().toString());

            }
        });

    }

    /**
     * Armazena um valor a SharedPreferences
     * @param key valor a ser armazenado
     */
    private void armazenar(String key){

        //Verifica se a chave é do tamanho correto
        if(key.length() == 16) {

            // Precisamos de um objeto Editor para fazer mudanças de preferência.
            // Todos os objetos são de android.context.Context
            SharedPreferences settings = getSharedPreferences("chave", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString(key.toUpperCase(), "chave");

            // Commit as edições
            editor.commit();
        }

    }
}
