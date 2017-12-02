package com.arnhom.cryptoku.puzzle;

import android.util.Log;
import android.util.MalformedJsonException;

/**
 * Created by arnHom on 17/07/29.
 */

public class BoardAnimator {

    float [] rowShift;
    float [] colShift;

    float boardShake;

    float animationSpdScale = 6.f;

    public BoardAnimator(){
        rowShift = new float[3];
        colShift = new float[3];

        for(int i= 0 ; i< 3 ; ++i){
            rowShift[i] = 0.f;
            colShift[i] = 0.f;
        }
    }

    public void clearAnimations(){
        for(int i= 0 ; i< 3 ; ++i){
            rowShift[i] = 0.f;
            colShift[i] = 0.f;
        }
    }

    public void animateRow(int row, boolean right){
        if(right){
            rowShift[row] = -1.f;
        } else{
            rowShift[row] = 1.f;
        }
    }

    public void animateCol(int col, boolean down){
        if(down){
            colShift[col] = -1.f;
        } else{
            colShift[col] = 1.f;
        }
    }

    public void shakeBoard(){
        boardShake = (float)Math.PI/4;
    }

    public int getXShake(float screenWidth){
        return (int)(Math.sin(boardShake*16) * screenWidth/64);
    }

    public int getYshake(float screenWidth){
        return (int)(Math.sin(boardShake*8) * screenWidth/128);
    }

    public boolean update(float frameTime){
        boolean movement = false;

        if(boardShake != 0.f){
            movement= true;
            if(boardShake < frameTime * animationSpdScale){
                boardShake = 0;
            } else{
                boardShake -= frameTime * animationSpdScale;
            }
        }


        for(int i =0 ;i < 3; ++i){
            //TODO i copied code, i should restructure the data structure so that i only have to write the code once.

            //rows
            if(rowShift[i] != 0.f){
                movement = true;
                if(Math.abs(rowShift[i]) < frameTime * animationSpdScale){  //if the distance is small enough to squash.
                    rowShift[i] = 0.f;
                } else {  //else move.
                    if(rowShift[i] < 0){  //movign right
                        rowShift[i] += frameTime * animationSpdScale;
                    } else {  //moving left.
                        rowShift[i] -= frameTime * animationSpdScale;
                    }
                }
            }
            //cols
            if(colShift[i] != 0.f){
                movement = true;
                if(Math.abs(colShift[i]) < frameTime * animationSpdScale){  //if the distance is small enough to squash.
                    colShift[i] = 0.f;
                } else {  //else move.
                    if(colShift[i] < 0){  //movign down
                        colShift[i] += frameTime * animationSpdScale;
                    } else {  //moving up.
                        colShift[i] -= frameTime * animationSpdScale;
                    }
                }
            }
        }

        return movement;//informs the game that there has been movement, so the screen will have to be redrawn.
    }

    public int getRowShift(int row, int cellSize){
        return (int)(rowShift[row] * cellSize);
    }

    public int getColShift(int col, int cellSize){
        return (int)(colShift[col] * cellSize);
    }
}
