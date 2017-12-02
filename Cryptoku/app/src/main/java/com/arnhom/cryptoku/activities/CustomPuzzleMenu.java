package com.arnhom.cryptoku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.arnhom.cryptoku.R;
import com.arnhom.cryptoku.activities.intentBuilders.CryptukoMainIntentBuilder;

/**
 * Created by arnHom on 17/12/01.
 */

public class CustomPuzzleMenu extends AppCompatActivity {

    private int depth = 6;
    private int operators  = 1;

    private TextView depthText;
    private TextView operatorText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_puzzle);

        depthText = (TextView)findViewById(R.id.depthValue);
        operatorText = (TextView)findViewById(R.id.operatorValue);
        updateValues();
        depthText.setText(Integer.toString(depth));
        setOperatorText();
    }


    public void onDepthDivide(View view){
        depth/=2;
        updateValues();
    }

    public void onDepthMinus(View view){
        depth--;
        updateValues();
    }

    public void onDepthPlus(View view){
        depth++;
        updateValues();
    }

    public void onDepthMultiply(View view){
        depth*=2;
        updateValues();
    }

    public void onOperatorLeft(View view){
        operators--;
        updateValues();
    }

    public void onOperatorRight(View view){
        operators++;
        updateValues();
    }

    public void onCustomPlay(View view){
        Intent intent = CryptukoMainIntentBuilder.buildCryptukoMainIntent(this,depth,operators);
        startActivity(intent);
    }

    private void setOperatorText(){
        String text = "+ -";
        if(operators > 0){
            text += " x ÷";
        }
        if(operators > 1){
            text += " x² √x";
        }

        operatorText.setText(text);
    }

    private void updateValues(){
        if(depth < 1){
            depth = 1;
        }
        //TODO decide and set a max depth value?

        if(operators < 0){
            operators = 0;
        }
        else if (operators > 2){
            operators = 2;
        }
        depthText.setText(Integer.toString(depth));
        setOperatorText();
    }

}
