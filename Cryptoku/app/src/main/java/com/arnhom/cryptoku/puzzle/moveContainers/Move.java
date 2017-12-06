package com.arnhom.cryptoku.puzzle.moveContainers;

/**
 * Created by arnHom on 17/12/05.
 */

public class Move{

    public boolean col;
    public int option;
    public boolean forward;
    public Move(boolean col, int option, boolean forward){
        this.col = col;
        this.option = option;
        this.forward = forward;
    }

    public Move(Move other){
        this.col = other.col;
        this.option = other.option;
        this.forward = other.forward;
    }

    public Move(int randomN){
        randomN %= 12;//there are only 12 possible moves.

        if (randomN >= 6) { // a column move
            this.col = true;
            randomN -= 6;
        } else {
            this.col = false;
        }

        if (randomN >= 3) {
            this.forward = true;
            randomN -= 3;
        } else {
            this.forward = false;
        }

        this.option = randomN;
    }

    public boolean isEqualTo(Move other){
        return this.col==other.col && this.option==other.option && this.forward==other.forward;
    }

    public Move reverse(){
        return new Move(col,option,!forward);
    }
}
