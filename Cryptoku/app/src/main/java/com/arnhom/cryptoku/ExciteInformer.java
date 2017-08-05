package com.arnhom.cryptoku;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by arnHom on 17/08/01.
 */

public class ExciteInformer {

    String text;

    /*
    0  =  box growing onto the screen
    1  =  text flying accross
    2  =  box following the text.
    3  =  nothing.
     */

    float timer;
    int state;

    //animation timing stuff
    float boxGrowTime = 0.25f;
    float boxDieTime = 0.25f;
    float textTime;
    float textMovementSpd =  2.5f; //the screen in half a second

    RectF textBox;

    float textSize;
    float textXScale = 0.75f;

    float textX;

    Paint letters;
    Paint bgColor;

    int width, height;

    ExciteInformer(int w, int h){
        width = w;
        height = h;
        text = "";
        state = 3;
        timer = 0.f;
        textTime = 0.f;
        textX = 0;
        textBox = new RectF();
        textBox.top = width*3/8;
        textBox.bottom = width*7/8;

        bgColor = new Paint();
        bgColor.setColor(Color.BLACK);
        bgColor.setAlpha(125);

        letters = new Paint();
        letters.setColor(Color.WHITE);
        textSize = textBox.bottom - textBox.top;
        letters.setTextSize(textSize);
        letters.setTextScaleX(textXScale);
        letters.setTextSkewX(-0.3f);
    }

    public void inform(String t){
        text = t;
        textX = width;
        state = 0;
        timer = 0;
    }


    //return true if you need to redraw
    public boolean update(float frameTime){
        switch(state){
            case 0:  //growing
                timer += frameTime;
                textBox.left = (width * (boxGrowTime-timer)/boxGrowTime);
                textBox.right = width;
                if(timer > boxGrowTime){
                    //move to state 1
                    state = 1;
                    timer =0;

                    //calculate the text timer
                    textTime = (width + textSize * text.length() * textXScale/2) / (textMovementSpd * width);
                    textX = width;
                }
                return true;
            case 1: //text slidign
                timer += frameTime;
                textX -= textMovementSpd * width * frameTime;
                if(timer > textTime){
                    timer = 0;
                    state = 2;
                }
                return true;
            case 2: //shrinking
                timer += frameTime;
                textX -= textMovementSpd * width * frameTime;
                textBox.right = width * ((boxDieTime - timer)/boxDieTime);
                if(timer > boxDieTime){
                    timer = 0;
                    state = 3;
                }
                return true;
            default: //this should be state 3 or anything else.
                return false;
        }
    }

    public void draw(Canvas canvas){
        if(state == 3){
            return;
        }
        canvas.drawRect(textBox,bgColor);
        canvas.drawText(text,textX,(textBox.top+textBox.bottom)*5/8,letters);
    }

    public void onResize(int w, int h){
        this.width =  w;
        this.height = h;
        textBox.top = width*3/8;
        textBox.bottom = width*7/8;
        textSize = textBox.bottom - textBox.top;
        letters.setTextSize(textSize);
    }
}
