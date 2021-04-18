package com.arnhom.cryptoku.puzzle.moveContainers;

/**
 * Created by arnHom on 17/12/05.
 */

public class Move{

    private boolean trueIfColumnMove;
    public int rowOrColOption;
    public boolean forward;
    public Move(boolean col, int rowOrColOption, boolean forward){
        this.trueIfColumnMove = col;
        this.rowOrColOption = rowOrColOption;
        this.forward = forward;
    }

    public boolean isColMove(){
        return trueIfColumnMove;
    }

    public boolean isRowMove(){
        return !trueIfColumnMove;
    }

    public Move(Move other){
        this.trueIfColumnMove = other.trueIfColumnMove;
        this.rowOrColOption = other.rowOrColOption;
        this.forward = other.forward;
    }

    public Move(int randomN){
        randomN %= 12;//there are only 12 possible moves.

        if (randomN >= 6) { // a column move
            this.trueIfColumnMove = true;
            randomN -= 6;
        } else {
            this.trueIfColumnMove = false;
        }

        if (randomN >= 3) {
            this.forward = true;
            randomN -= 3;
        } else {
            this.forward = false;
        }

        this.rowOrColOption = randomN;
    }

    public boolean isEqualTo(Move other){
        return this.trueIfColumnMove ==other.trueIfColumnMove && this.rowOrColOption ==other.rowOrColOption && this.forward==other.forward;
    }

    public Move reverse(){
        return new Move(trueIfColumnMove, rowOrColOption,!forward);
    }
}
