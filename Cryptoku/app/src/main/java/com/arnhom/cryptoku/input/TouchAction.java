package com.arnhom.cryptoku.input;

/**
 * Created by arnHom on 17/07/27.
 */

public class TouchAction {

    public enum actionType{left,right,up,down,tap}
    public actionType type;
    public float x;
    public float y;

    public TouchAction(actionType t,float x,float y){
        this.x = x;
        this.y = y;
        this.type = t;
    }

}
