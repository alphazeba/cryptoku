package com.arnhom.cryptoku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.arnhom.cryptoku.R;
import com.arnhom.cryptoku.activities.intentBuilders.CryptukoMainIntentBuilder;

/**
 * Created by arnHom on 17/11/30.
 */

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main_menu);
    }

    public void onPlayEasy(View view){
        Intent intent = CryptukoMainIntentBuilder.buildCryptukoMainIntent(this, CryptukoMainIntentBuilder.DifficultyLevels.easy);
        startActivity(intent);
    }

    public void onPlayMedium(View view){
        Intent intent = CryptukoMainIntentBuilder.buildCryptukoMainIntent(this, CryptukoMainIntentBuilder.DifficultyLevels.medium);
        startActivity(intent);
    }

    public void onPlayHard(View view){
        Intent intent = CryptukoMainIntentBuilder.buildCryptukoMainIntent(this, CryptukoMainIntentBuilder.DifficultyLevels.hard);
        startActivity(intent);
    }

    public void onPlayCustom(View view){
        Intent intent = new Intent(this, CustomPuzzleMenu.class);
        startActivity(intent);
    }

    public void onHowTo(View view){
        Intent intent = new Intent(this, HowTo.class);
        startActivity(intent);
    }
}
