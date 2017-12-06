package com.arnhom.cryptoku.puzzle.moveContainers;

import java.util.LinkedList;

/**
 * Created by arnHom on 17/12/05.
 */
//stores a list of moves organized into moveTiers.
    //designed to replace a linkedlist of Moves. so the interface will have similar function names such as isEmpty and addFirst.
    //this, unlike a linked list, compacts similar moves into tiers of Moves.
    //this is also important because parallel moves do not affect one another.  if there are a series of row/column moves,
    //these moves can be undone in any order without effecting the eventual outcome.
    //if a perpendicular move is made, a new movetier is added.
    //if a moveTier is emptied (all moves have been undone) then a movetier is removed from the movetiers list.
public class MoveList{
    //contains a number of moves that are all column or all row moves.  These are contained together because they do not affect one another.
    private class MoveTier{
        boolean col = false;
        int [] moves = {0,0,0}; //postive is forward, negative is reverse

        MoveTier(){
        }

        //deep copies a moveTier.
        MoveTier(MoveTier other){
            this.col = other.col;
            for(int i=0;i<3;++i){
                this.moves[i] = other.moves[i];
            }
        }

        boolean isEmpty(){
            return moves[0] == 0 && moves[1] == 0 && moves[2] == 0;
        }

        LinkedList<Move> getMoves(){
            LinkedList<Move> output = new LinkedList<>();
            for(int i= 0; i< 3; ++i){
                int m = moves[i];
                if(m!=0){
                    boolean forward = true;
                    if(m < 0){ //if the number of moves held is negative
                        forward = false;
                        m *= -1; //m will now always be positive.
                    }
                    for(int j=0;j<m;++j){//adds m times the i move to the output list of moves.
                        output.add(new Move(col,i,forward));
                    }
                }
            }

            return output;
        }

        //this adds the move to this tier
        //return 0: the move was added successfully
        //return 1: the move needs a new tier.
        //return 2: the move was added, but the tier is now empty
        int addMove(Move m){
            //if this is an empty list, the move is added regardless of col.
            if(this.isEmpty()){
                this.col = m.col;
                moves[m.option] += m.forward ? 1 : -1; //adds 1 if the move is forward, removes 1 if the move is reverse.
                return this.isEmpty() ? 2 : 0;
            }
            //this is not a new movetier.
            if(this.col != m.col){ //if the col value is mismatched, the move cannot be added to this tier.
                return 1;
            }
            //col values align, we can add the move.
            this.col = m.col;
            moves[m.option] += m.forward ? 1 : -1; //adds 1 if the move is forward, removes 1 if the move is reverse.
            return this.isEmpty() ? 2 : 0; //successfully added the move.
        }

        //this checks if the input move undoes a previous move.
        boolean doesMoveUndo(Move m){
            if(this.col != m.col){ //if cols do not align then the move does not undo.
                return false;
            }

            //if the move is forward, then it undoes negative values.
            //if the move is negative, then it undoes positive values.
            if(m.forward){
                if(moves[m.option] < 0){
                    return true;
                }
            }
            //!m.forward
            if(moves[m.option] > 0){
                return true;
            }

            //the move slot was either empty, or this move is a repeat move. (not an undoing move)
            return false;
        }
    }

    private LinkedList<MoveTier> moveTiers;

    public MoveList(){
        moveTiers = new LinkedList<>();
    }

    //deep copies a movelist.
    public MoveList(MoveList other){
        this.moveTiers = new LinkedList<>();
        for(MoveTier mt: other.moveTiers){
            this.moveTiers.add(new MoveTier(mt));
        }
    }

    //adding a move both adds new moves and also handles the instances when the new move undoes an old move.
    //this also dynamically extends and cleans up the moveTier list.
    public void addFirst(Move m){
        if(moveTiers.isEmpty()){
            moveTiers.add(new MoveTier());
        }

        //now a moveTier is guaranteed to exist.

        //we will now loop until we have successfully added a move.
        boolean moveHasBeenAdded = false;
        while(!moveHasBeenAdded){

            switch(moveTiers.getFirst().addMove(m)){
                case 1: //the new move being added is perpendicular
                    moveTiers.addFirst(new MoveTier()); //add a new moveTier and then try again.
                    break;
                case 2: //the new move added emptied the current moveTier
                    //remove the first move tier and then move on.
                    moveTiers.removeFirst();
                case 0: //the move was added. nothing special occurred.
                    moveHasBeenAdded = true; //move on.
            }
        }
    }

    public boolean doesMoveUndo(Move m){
        if(moveTiers.isEmpty()){
            return false; // cannot undo an empty movelist.
        }

        return moveTiers.getFirst().doesMoveUndo(m);
    }

    public LinkedList<Move> getMoves(){
        LinkedList<Move> output = new LinkedList<>();
        for(MoveTier mt: moveTiers){
            output.addAll(mt.getMoves());
        }
        return output;
    }

    public boolean isEmpty(){
        return moveTiers.isEmpty();
    }
}
