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

    public int getNumAllPotentialMoves(int boardSize){
        return boardSize * 4;
    }

    public int getNumColumnPotentialMoves(int boardSize){
        return boardSize * 2;
    }

    public Move(int boardSize){
        int randomN = (int)(Math.random() * getNumAllPotentialMoves(boardSize));

        if (randomN >= getNumColumnPotentialMoves(boardSize)) { // a column move
            this.trueIfColumnMove = true;
            randomN -= getNumColumnPotentialMoves(boardSize);
        } else {
            this.trueIfColumnMove = false;
        }

        if (randomN >= boardSize) {
            this.forward = true;
            randomN -= boardSize;
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
