package com.arnhom.cryptoku;

/**
 * Created by arnHom on 17/07/27.
 */

public class TouchAction {

    enum actionType{left,right,up,down,tap}
    actionType type;
    float x;
    float y;

    public TouchAction(actionType t,float x,float y){
        this.x = x;
        this.y = y;
        this.type = t;
    }

}
