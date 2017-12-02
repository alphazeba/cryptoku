package com.arnhom.cryptoku.input;

import android.view.MotionEvent;

import java.util.Vector;

/**
 * Created by arnHom on 17/07/27.
 */



public class TouchInput {
    private Vector<Finger> finger;
    private float minScreenDimension;

    public TouchInput(float height, float width){
        finger = new Vector<>();
        this.minScreenDimension = Math.min(height,width);
    }

    //returns a touchaction if an action was recorder.
    //otherwise it returns null
    public TouchAction update(MotionEvent me){

        TouchAction output = null;

        switch(me.getActionMasked()){
            //add a finger to the screen.
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                finger.add(new Finger(me,minScreenDimension));
                break;

            //remove a finger from the screen.
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                int id = me.getPointerId(me.getActionIndex());
                for(int i = 0; i < finger.size() ; ++i){
                    if(finger.elementAt(i).id == id){
                        output = finger.elementAt(i).CheckForSwipe(me);
                        finger.remove(i);
                        break;
                    }
                }
                break;
        }
        return output;
    }

}
