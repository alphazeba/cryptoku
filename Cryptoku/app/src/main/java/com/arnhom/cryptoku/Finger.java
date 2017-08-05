package com.arnhom.cryptoku;

import android.view.MotionEvent;

/**
 * Created by arnHom on 17/07/27.
 */

public class Finger {

    public int id;
    public float startX;
    public float startY;

    float minimumSwipeSize;

    public Finger(MotionEvent me , float minScreenDimension){
        this.id = me.getPointerId(me.getActionIndex());
        int index = me.findPointerIndex(this.id);
        this.startX = me.getX(index);
        this.startY = me.getY(index);
        this.minimumSwipeSize = minScreenDimension/6;
    }


    //this is to be called when the finger is lifted.
    //returns whether the action was a swipe in a direction or a tap.
    public TouchAction CheckForSwipe(MotionEvent me){
        int index = me.findPointerIndex(this.id);
        float dx = me.getX(index) - startX;
        float dy = me.getY(index) - startY;

        float max = Math.max(Math.abs(dx),Math.abs(dy));
        if(max < minimumSwipeSize){
            // this is a tap event.
            return new TouchAction(TouchAction.actionType.tap,startX,startY);
        }

        if(max == Math.abs(dx)){ //if the max is in the x direction
            if(dx < 0){//swipe to the left.
                return new TouchAction(TouchAction.actionType.left,startX,startY);
            }

            //swipe to the right
            return new TouchAction(TouchAction.actionType.right,startX,startY);
        }

        //max is in the y direction
        if(dy < 0){  //swipe is up
            return new TouchAction(TouchAction.actionType.up,startX,startY);
        }
        //swipe is downwards
        return new TouchAction(TouchAction.actionType.down,startX,startY);
    }
}
