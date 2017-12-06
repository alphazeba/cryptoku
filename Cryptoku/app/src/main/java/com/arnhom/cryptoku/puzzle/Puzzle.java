package com.arnhom.cryptoku.puzzle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.arnhom.cryptoku.activities.cryptokuMain;
import com.arnhom.cryptoku.input.TouchAction;
import com.arnhom.cryptoku.puzzle.moveContainers.Move;
import com.arnhom.cryptoku.puzzle.moveContainers.MoveList;

import java.util.LinkedList;

/**
 * Created by arnHom on 17/07/28.
 */


public class Puzzle {

    /*
    the goal is that nothing in the puzzle class loops.
    additionally, the update would return a boolean true or false to determine whether or not a redraw is required.

    a redraw should only be necessary when things are moving.  So that is fairly rare.

    while i'm thinking about it, i redraw flag should be set when the device is returning from on pause.

    an additional idea is to allow for different sized boards.  likely a 3x3 will already be really hard, but making it larger would make it even harder.

     */


    private int [][] board;
    private int [][] initialBoard;

    private MoveList originalSolution;
    private MoveList dynamicSolution;

    private Operations[]rowOperations;
    private Operations []colOperations;

    private float width,height;
    private boolean tweenRedraw;

    private boolean success;

    private float x;
    private float y;

    private float drawingGapMultiplier = 0.85f;

    private TouchAction storedInput;
    private BoardAnimator boardAnimator;

    private cryptokuMain context;

    public Puzzle(int scrambleDepth, int operationComplexity, cryptokuMain context){
        this.x = 0;
        this.y = 0;

        success = false;

        this.context = context;

        Log.i("puzzle","begin building puzzle");
        boardAnimator = new BoardAnimator();
        storedInput = null;
        width = 0;
        height = 0;
        //pump row and col operations full of operations.
        rowOperations = new Operations[3];
        colOperations = new Operations[3];
        //setup row and col operations
        for(int i =0 ; i < 3 ; ++i){
            rowOperations[i] = new Operations(operationComplexity);
            colOperations[i] = new Operations(operationComplexity);
        }

        //prep board.
        board = new int[3][3];
        for(int iy = 0 ; iy < 3; ++iy){
            for(int ix = 0; ix < 3 ; ++ix){
                board[ix][iy] = 1;
            }
        }

        scramble(scrambleDepth);

        tweenRedraw = true;
        //will always need to redraw on the first frame.

        Log.i("puzzle","done building puzzle :)");
    }

    public Puzzle(int difficulty, cryptokuMain context){
        this(6*(int)Math.pow(2,difficulty),difficulty,context);
    }

    //returns how deep the puzzle actually is.
    public int scramble(int movesDeep){
        initialBoard = new int[3][3];
        originalSolution = new MoveList();
        dynamicSolution = new MoveList();

        int totalTries = 0;
        int i = 0;
        int maxTries = movesDeep * 20;
        for( ; i < movesDeep; ++totalTries){
            if(totalTries == maxTries){
                break;//stop building the puzzle at some point.
            }

            //make a random move.
            //then make sure that it does not undo a previous move.


            //get a random move.
            //there are 12 possible moves.
            Move randomMove = new Move((int)(Math.random() * 12));

            //won't accept a move that undoes a previous move.
            if(!dynamicSolution.doesMoveUndo(randomMove)){

                if(checkMove(randomMove)){ //if the move is a legal move.
                    makeMove(randomMove);
                    originalSolution.addFirst(randomMove.reverse());
                    ++i;
                }
            }

            //do not animate while scramblign.
            boardAnimator.clearAnimations();
        }

        //save the initial boardstate so that the game can be reset.
        for(int iy = 0; iy < 3; ++iy){
            for(int ix =0; ix < 3; ++ix){
                initialBoard[ix][iy] = board[ix][iy];
            }
        }

        return i;
    }

    public void reset(){
        for(int iy = 0; iy < 3; ++iy){
            for(int ix =0; ix < 3; ++ix){
                board[ix][iy] = initialBoard[ix][iy];
            }
        }

        resetSolution();
    }

    public boolean checkMove(Move move){
        if(move.col){
            return checkMoveLegalCol(move.option,move.forward);
        } else {
            return checkMoveLegalRow(move.option,move.forward);
        }
    }

    private boolean checkMoveLegalRow(int row , boolean slideRight){
        //sliding right is the equivalent of doing an operation forward.
        for(int i = 0 ; i < 3 ; ++i){
            if(!rowOperations[row].isOperationLegal(board[i][row],!slideRight)){  //if any one is false return false.
                boardAnimator.shakeBoard();
                return false;
            }
        }
        return true;
    }

    private boolean checkMoveLegalCol(int col, boolean slideDown){
        //sliding down is the equivalent of doing an operation forward.
        for(int i = 0; i < 3 ; ++i){
            if(!colOperations[col].isOperationLegal(board[col][i],!slideDown)){
                boardAnimator.shakeBoard();
                return false;
            }
        }
        return true;
    }

    public void makeMove(Move move){
        if(move.col){
            moveCol(move.option,move.forward);
        } else {
            moveRow(move.option,move.forward);
        }

        //the solution form of a move is the reverse move.
        dynamicSolution.addFirst(move.reverse());

        context.onMoveWasMade();
    }

    public void moveRow(int row, boolean slideRight){
        boardAnimator.animateRow(row,slideRight);
        //duplicate the row
        int [] original = new int[3];

        if(slideRight){  //shift to the right while doing this.
            for(int i = 0 ; i < 3; ++i){
                original[(i+1)%3] = board[i][row];
            }
        } else { //shift to the left while doing this.
            for(int i = 0 ; i < 3; ++i){
                original[i] = board[(i+1)%3][row];
            }
        }


        //set the new values
        for(int i=0;i<3;++i){
            board[i][row] = rowOperations[row].operate(original[i],!slideRight);
        }
    }

    public void moveCol(int col, boolean slideDown){
        boardAnimator.animateCol(col,slideDown);
        //duplicate the col
        int [] original = new int[3];

        if(slideDown){
            for(int i=0;i<3;++i){
                original[(i+1)%3] = board[col][i];
            }
        } else { //sifht to the up
            for(int i =0;i<3;++i){
                original[i] = board[col][(i+1)%3];
            }
        }

        //set the new values
        for(int i=0;i<3;++i){
            board[col][i] = colOperations[col].operate(original[i],!slideDown);
        }
    }

    public boolean onInput(TouchAction touch){

        if(touch.type == TouchAction.actionType.tap){//ignores taps.
            return false;
        }
        //find if the x and y are even in the correct general area.
        if(touch.x < width/4 || touch.x > width || touch.y < height/4 || touch.y > height){
            return false;
        }


        storedInput = touch;
        return true;
    }

    // 0 is miss
    //right now manage input only returns 1 or 2.
    //this should be transferred into a boolean function
    public boolean manageInput(TouchAction touch){
        //if up or down find col

        Move move;
        if(touch.type == TouchAction.actionType.up || touch.type == TouchAction.actionType.down){
            int col = (int)Math.floor(touch.x/(width/4)) -1;
            if(col < 0 ||col >2){
                return false;
            }
            move = new Move(true,col,touch.type == TouchAction.actionType.down);
            /*
            if(touch.type == TouchAction.actionType.up){//up
                if(checkMoveLegalCol(col,false)){
                    moveCol(col,false);
                    tweenRedraw = true;
                    return 1;
                }
                return 2;
            } else { //down
                if(checkMoveLegalCol(col,true)){
                    moveCol(col,true);
                    tweenRedraw = true;
                    return 1;
                }
                return 2;
            }
            */
        } else{//if left or right find row
            int row = (int)Math.floor(touch.y/(height/4)) - 1;
            if( row < 0 || row > 2){
                return false;
            }
            move = new Move(false,row,touch.type == TouchAction.actionType.right);
            /*
            if(touch.type == TouchAction.actionType.left){//left
                if(checkMoveLegalRow(row,false)){
                    moveRow(row,false);
                    tweenRedraw = true;
                    return 1;
                }
                return 2;
            } else { //right
                if(checkMoveLegalRow(row,true)){
                    moveRow(row,true);
                    tweenRedraw = true;
                    return 1;
                }
                return 2;
            }
            */
        }

        if(checkMove(move)){
            makeMove(move);
            return true;
        }
        return false;
    }

    public void onResize(float width, float height){
        float minDimension = Math.min(width,height);

        this.width = minDimension;
        this.height = minDimension;


        tweenRedraw = true;
    }

    public void draw(Canvas canvas){
        //this should draw the current board state to the screen.
        int dx = boardAnimator.getXShake(width);
        int dy = boardAnimator.getYshake(width);
        //draw the numbers
        for(int iy = 0; iy < 3; ++iy){
            for(int ix = 0; ix < 3 ; ++ix){
                int cellX = (int)(width/4*(ix+1) + boardAnimator.getRowShift(iy,(int)width/4));
                int cellY = (int)(width/4*(iy+1) + boardAnimator.getColShift(ix,(int)width/4));
                int max = (int)(width*0.75f);


                if(cellX < width/4){
                    cellX += max;
                }
                if(cellY < width/4){
                    cellY += max;
                }

                //always draw the real one.
                drawNumber(dx+cellX,dy+cellY,board[ix][iy],canvas);

                //draw any potential mirrors
                if(cellX > max){
                    drawNumber(dx+cellX-max,dy+cellY,board[ix][iy],canvas);
                }
                if(cellY > max){
                    drawNumber(dx+cellX,dy+cellY-max,board[ix][iy],canvas);
                }


            }
        }

        //draw the operations
        for(int i = 0 ; i < 3; ++i){
            //draw row (down the left)
            drawOperation(dx,dy+width/4*(i+1),rowOperations[i],canvas);

            //draw col (along the top)
            drawOperation(dx+width/4*(i+1),dy,colOperations[i],canvas);
        }
    }

    private void drawOperation(float x, float y, Operations op , Canvas canvas){
        Paint bgColor = new Paint();
        Paint txtColor = new Paint();

        bgColor.setColor(0xff444444);
        txtColor.setColor(Color.WHITE);
        txtColor.setTextSize(width/16);

        float radius = width/8 * drawingGapMultiplier; //this is 1/2*1/4 of the screen.


        //draw the background
        canvas.drawCircle(x+radius ,y+radius,radius,bgColor);

        //set the text
        String text;
        switch(op.operation){
            case add:
                text = "+ " + op.operand;
                break;
            case subtract:
                text = "- " + op.operand;
                break;
            case multiply:
                text = "x " + op.operand;
                break;
            case divide:
                text = "÷ " + op.operand;
                break;
            case insert:
                text = "→" + op.operand;
                break;
            case backspace:
                text = "↚" + op.operand;
                break;
            default:
                text = "uh oh";
                break;
        }

        //draw the text
        canvas.drawText(text,x+radius/2,y+radius,txtColor);
    }

    private void drawNumber(float x, float y, int num, Canvas canvas){
        Paint bgColor = new Paint();

        int color = Color.WHITE;
        int curviness = 30;//the higher this number, the more linear the function. the lower the number, the more drastic the curve is.
        int functionScaler = (10000+curviness)/10;
        float colorScale =(float)((num-1)/(curviness+Math.sqrt((1+(num-1)*(num-1))))*functionScaler);
        //Log.i("colorscale",Float.toString(colorScale));
        if(colorScale > 0){
            float wRatio = (1000-colorScale)/1200.f;
            float pRatio = (colorScale)/1000.f;
            //color = Color.rgb((int)(255*wRatio + 131*pRatio),(int)(255*wRatio + 73*pRatio),255);
            color = Color.rgb((int)(255*wRatio),(int)(255*wRatio),255);
        }
        else if(colorScale < 0){
            colorScale *= -1;
            float wRatio = (1000-colorScale)/1100.f;
            float pRatio = (colorScale)/1100.f;
            //color = Color.rgb(255,(int)(255*wRatio + 73*pRatio),(int)(255*wRatio + 218*pRatio));
            color = Color.rgb(255,(int)(255*wRatio),(int)(255*wRatio));
        }

        bgColor.setColor(color);

        Paint txtColor = new Paint();
        txtColor.setColor(Color.BLACK);
        txtColor.setTextSize(width/14);
        txtColor.setTextAlign(Paint.Align.CENTER);

        float cellSize = width/4*drawingGapMultiplier;
        //draw background
        //canvas.drawRect(x,y,x+cellSize,y+cellSize,bgColor); //i would like to replace this with the roundedrect function eventually.
        //manualRoundRect(x,y,cellSize,cellSize,cellSize/9.708f,bgColor,canvas);
        drawRoundRectWithinBox(new RectF(this.x + width/4,this.y+height/4,this.x+width*3/4+cellSize,this.y+width*3/4+cellSize),x,y,cellSize,cellSize,cellSize/9.708f,bgColor,canvas);
        //draw the text
        canvas.drawText(Integer.toString(num),x+cellSize/2,y+cellSize/2,txtColor);
    }

    private void manualRoundRect(float x,float y, float width, float height, float radius, Paint paint, Canvas canvas){
        //redefine radius so that the circles drawn are never larger than the square.

        //finds the largest radius size before the circles would extend past the box size.
        float maxRadius = Math.min(width,height)/2;

        //sets radius to whichever is
        radius = Math.min(radius,maxRadius);


        //left top circle
        canvas.drawCircle(x+radius,y+radius,radius,paint);
        //right top circle
        canvas.drawCircle(x+width-radius,y+radius,radius,paint);
        //left bot circle
        canvas.drawCircle(x+radius,y+height-radius,radius,paint);
        //right bot circle
        canvas.drawCircle(x+width-radius,y+height-radius,radius,paint);

        //mid box
        canvas.drawRect(x,y+radius,x+width,y+height-radius,paint);
        //topbox
        canvas.drawRect(x+radius,y,x+width-radius,y+radius,paint);
        //botbox
        canvas.drawRect(x+radius,y+height-radius,x+width-radius,y+height,paint);
    }

    private void drawRoundRectWithinBox(RectF box, float x, float y, float width, float height, float radius, Paint paint, Canvas canvas){
        RectF drawnBox = new RectF();

        //redefine the drawn square size so that it fits with box.
        drawnBox.left = Math.max(x,box.left);
        drawnBox.top = Math.max(y,box.top);
        drawnBox.right = Math.min(x+width,box.right);
        drawnBox.bottom = Math.min(y+height,box.bottom);

        //if the new width and height are less than 0, then do not draw the box.
        if(drawnBox.right-drawnBox.left < 0){
            return;
        }
        if(drawnBox.bottom-drawnBox.top < 0){
            return;
        }

        manualRoundRect(drawnBox.left,drawnBox.top,drawnBox.right-drawnBox.left,drawnBox.bottom-drawnBox.top,radius,paint,canvas);
    }

    //this called in the non ui thread.
    public boolean update(float frameTime){
        //returns true if the game needs to redraw.

        boolean redraw = tweenRedraw;
        tweenRedraw = false;

        //update the board animations.
        redraw |= boardAnimator.update(frameTime);

        if(storedInput != null){
            if(manageInput(storedInput)){
                redraw = true;
                success = checkForSuccess();
            }
            storedInput = null;
        }

        return  redraw;
    }

    public String solutionToString(){
        if(dynamicSolution.isEmpty()){
            return "Done!";
        }
        String output = "";

        LinkedList<Move> listedSolution = dynamicSolution.getMoves();
        for(Move nextStep:listedSolution) {
            output += "Swipe ";
            if(nextStep.col){
                if(nextStep.forward){
                    output += "down ";
                }
                else{
                    output += "up ";
                }
                output += "in column ";
            }
            else{
                if(nextStep.forward){
                    output+= "right ";
                }
                else{
                    output += "left ";
                }
                output += "in row ";
            }
            output += Integer.toString(nextStep.option + 1) + "\n";
        }

        return output;
    }

    public void shake(){
        boardAnimator.shakeBoard();
    }

    //to be used by the puzzle.
    //use getSuccess to echeck for success.
    private boolean checkForSuccess(){
        for(int iy = 0 ; iy < 3; ++iy){
            for(int ix = 0; ix < 3; ++ix){
                if(board[ix][iy] != 1){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean getSuccess(){
        return success;
    }

    private void resetSolution(){
        dynamicSolution = new MoveList(originalSolution);//copy the original solution into the dynamic solution.
        /*
        if(dynamicSolution!=null){
            dynamicSolution.clear();
        }
        dynamicSolution = new LinkedList<>();
        for(Move x : originalSolution){
            dynamicSolution.add(new Move(x));
        }
        */
    }
}
