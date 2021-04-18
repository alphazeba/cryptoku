package com.arnhom.cryptoku.puzzle;

import com.arnhom.cryptoku.puzzle.moveContainers.Move;
import com.arnhom.cryptoku.puzzle.moveContainers.MoveList;

public class Board {
    private int [][] cells;
    private Operations[] rowOperations;
    private Operations[] colOperations;
    private MoveList solution;
    private int size;

    public Board(int size, int operationComplexity){
        initialize(size, operationComplexity);
    }
    public Board(Board other){
        this.size = other.size;
        this.solution = new MoveList(other.solution);
        // copy the cells
        this.cells = new int[size][size];
        for(int iy = 0; iy < size; ++iy){
            for(int ix = 0; ix < size; ++ix){
                cells[iy][ix] = other.cells[iy][ix];
            }
        }
        // copy the operations.
        rowOperations = new Operations[size];
        colOperations = new Operations[size];
        for(int i=0;i<size;++i){
            rowOperations[i] = new Operations(other.rowOperations[i]);
            colOperations[i] = new Operations(other.colOperations[i]);
        }
    }
    public void initialize(int size,int operationComplexity){
        cells = new int[size][size];
        this.size = size;
        for(int iy = 0; iy < size; ++iy){
            for(int ix = 0; ix < size; ++ix){
                cells[iy][ix] = 1;
            }
        }
        rowOperations = new Operations[size];
        colOperations = new Operations[size];
        for(int i=0;i<size;++i){
            rowOperations[i] = new Operations(operationComplexity);
            colOperations[i] = new Operations(operationComplexity);
        }
        solution = new MoveList();
    }

    public void scramble(int movesDeep){
        Board initialBoard = new Board(this);
        int totalTries = 0;
        int i = 0;
        int maxTries = movesDeep * 20;
        for (; i < movesDeep; ++ totalTries){
            if (totalTries == maxTries){
                break;
            }
            // get a random move.
            Move randomMove = new Move((int)(Math.random() * 12));
            if( !solution.doesMoveUndo(randomMove)){
                if( checkMove(randomMove) ){
                    makeMove(randomMove);
                    solution.addFirst(randomMove.reverse());
                    ++i;
                }
            }
        }
    }

    public int getCell(int x, int y){
        return cells[x][y];
    }

    public String getRowOperationString(int row){
        return rowOperations[row].toString();
    }

    public String getColOperationString(int col){
        return colOperations[col].toString();
    }

    public String getSolutionString(){
        return solution.toString();
    }

    public boolean checkMove(Move move){
        if(move.isColMove()){
            return checkMoveLegalCol(move.rowOrColOption,move.forward);
        } else {
            return checkMoveLegalRow(move.rowOrColOption,move.forward);
        }
    }

    public boolean isSolved(){
        for(int iy=0;iy<size;++iy){
            for(int ix=0;ix<size;++ix){
                if(getCell(ix,iy) != 1){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkMoveLegalRow(int row , boolean slideRight){
        //sliding right is the equivalent of doing an operation forward.
        for(int i = 0 ; i < size ; ++i){
            if(!rowOperations[row].isOperationLegal(cells[i][row],!slideRight)){  //if any one is false return false.
                return false;
            }
        }
        return true;
    }

    private boolean checkMoveLegalCol(int col, boolean slideDown){
        //sliding down is the equivalent of doing an operation forward.
        for(int i = 0; i < size ; ++i){
            if(!colOperations[col].isOperationLegal(cells[col][i],!slideDown)){
                return false;
            }
        }
        return true;
    }

    public void makeMove(Move move){
        if(move.isColMove()){
            moveCol(move.rowOrColOption,move.forward);
        } else {
            moveRow(move.rowOrColOption,move.forward);
        }
        //the solution form of a move is the reverse move.
        solution.addFirst(move.reverse());
    }

    private void moveRow(int row, boolean slideRight){
        //duplicate the row
        int [] original = new int[size];

        if(slideRight){  //shift to the right while doing this.
            for(int i = 0 ; i < size; ++i){
                original[(i+1)%size] = cells[i][row];
            }
        } else { //shift to the left while doing this.
            for(int i = 0 ; i < size; ++i){
                original[i] = cells[(i+1)%size][row];
            }
        }
        //set the new values
        for(int i=0;i<size;++i){
            cells[i][row] = rowOperations[row].operate(original[i],!slideRight);
        }
    }

    private void moveCol(int col, boolean slideDown){
        //duplicate the col
        int [] original = new int[size];

        if(slideDown){
            for(int i=0;i<size;++i){
                original[(i+1)%size] = cells[col][i];
            }
        } else { //sifht to the up
            for(int i =0;i<size;++i){
                original[i] = cells[col][(i+1)%size];
            }
        }

        //set the new values
        for(int i=0;i<size;++i){
            cells[col][i] = colOperations[col].operate(original[i],!slideDown);
        }
    }
}
