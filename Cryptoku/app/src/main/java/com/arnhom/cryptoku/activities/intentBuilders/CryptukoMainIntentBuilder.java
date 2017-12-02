package com.arnhom.cryptoku.activities.intentBuilders;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.arnhom.cryptoku.activities.cryptokuMain;

/**
 * Created by arnHom on 17/11/30.
 */

public class CryptukoMainIntentBuilder {
    public enum DifficultyLevels {
        easy,
        medium,
        hard
    }

    public static Intent buildCryptukoMainIntent(AppCompatActivity activity,DifficultyLevels difficulty){
        Intent intent = new Intent(activity, cryptokuMain.class);
        int n;
        switch(difficulty){
            case easy:
                n=0;
                break;
            case medium:
                n=1;
                break;
            case hard:
                n=2;
                break;
            default:
                n=0;
        }
        intent.putExtra("difficulty",n);
        intent.putExtra("depth",-1);
        intent.putExtra("operators",-1);

        return intent;
    }

    public static Intent buildCryptukoMainIntent(AppCompatActivity activity, int depth, int operators){
        Intent intent = new Intent(activity,cryptokuMain.class);
        intent.putExtra("difficulty",-1);
        intent.putExtra("depth",depth);
        intent.putExtra("operators",operators);

        return intent;
    }
}
