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
import com.arnhom.cryptoku.general.Point;

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

    private Board board;
    private Board initialBoard;

    private float width,height;
    private boolean tweenRedraw;

    private boolean success;

    private Point pos;

    private float drawingGapMultiplier = 0.85f;

    private TouchAction storedInput;
    private BoardAnimator boardAnimator;

    private cryptokuMain context;

    public Puzzle(int scrambleDepth, int operationComplexity, cryptokuMain context){
        this.pos = new Point(0,0);

        success = false;

        this.context = context;

        Log.i("puzzle","begin building puzzle");
        boardAnimator = new BoardAnimator();
        storedInput = null;
        width = 0;
        height = 0;

        board = new Board(3,operationComplexity);
        board.scramble(scrambleDepth);
        initialBoard = new Board(board);

        tweenRedraw = true;
        //will always need to redraw on the first frame.

        Log.i("puzzle","done building puzzle :)");
    }

    public Puzzle(int difficulty, cryptokuMain context){
        this(6*(int)Math.pow(2,difficulty),difficulty,context);
    }

    public void resetBoard(){
        board = new Board(initialBoard);
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
        } else{//if left or right find row
            int row = (int)Math.floor(touch.y/(height/4)) - 1;
            if( row < 0 || row > 2){
                return false;
            }
            move = new Move(false,row,touch.type == TouchAction.actionType.right);
        }

        if(board.checkMove(move)){
            board.makeMove(move);
            boardAnimator.animateMove(move);
            context.onMoveWasMade();
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
                drawNumber(dx+cellX,dy+cellY,board.getCell(ix,iy),canvas);

                //draw any potential mirrors
                if(cellX > max){
                    drawNumber(dx+cellX-max,dy+cellY,board.getCell(ix,iy),canvas);
                }
                if(cellY > max){
                    drawNumber(dx+cellX,dy+cellY-max,board.getCell(ix,iy),canvas);
                }
            }
        }

        //draw the operations
        for(int i = 0 ; i < 3; ++i){
            //draw row (down the left)
            drawTextInCircle(dx,dy+width/4*(i+1),board.getRowOperationString(i),canvas);

            //draw col (along the top)
            drawTextInCircle(dx+width/4*(i+1),dy,board.getColOperationString(i),canvas);
        }
    }

    private void drawTextInCircle(float x, float y, String text , Canvas canvas){
        Paint bgColor = new Paint();
        Paint txtColor = new Paint();

        bgColor.setColor(0xff444444);
        txtColor.setColor(Color.WHITE);
        txtColor.setTextSize(width/16);

        float radius = width/8 * drawingGapMultiplier; //this is 1/2*1/4 of the screen.
        //draw the background
        canvas.drawCircle(x+radius ,y+radius,radius,bgColor);
        // TODO set antialiasing on the text that is being drawn here.
        canvas.drawText(text,x+radius/2,y+radius,txtColor);
    }

    private void drawNumber(float x, float y, int num, Canvas canvas){
        Paint bgColor = new Paint();

        // TODO the whole color function seems pretty bad and mostly linear.  too much math, not enough cool.
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
        drawRoundRectWithinBox(new RectF(this.pos.getX() + width/4,this.pos.getY()+height/4,this.pos.getX()+width*3/4+cellSize,this.pos.getY()+width*3/4+cellSize),x,y,cellSize,cellSize,cellSize/9.708f,bgColor,canvas);
        //draw the text
        canvas.drawText(Integer.toString(num),x+cellSize/2,y+cellSize/2,txtColor);
    }

    private void manualRoundRect(Point topLeft, Point boxSize, float radius, Paint paint, Canvas canvas){
        //redefine radius so that the circles drawn are never larger than the square.

        //finds the largest radius size before the circles would extend past the box size.
        float maxRadius = Math.min(boxSize.getX(),boxSize.getY())/2;
        float x  = topLeft.getX();
        float y = topLeft.getY();
        float width = boxSize.getX();
        float height = boxSize.getY();
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
        if(drawnBox.right < drawnBox.left){
            return;
        }
        if(drawnBox.bottom < drawnBox.top){
            return;
        }

        manualRoundRect(
                new Point(drawnBox.left,drawnBox.top),
                new Point(drawnBox.right-drawnBox.left,drawnBox.bottom-drawnBox.top),
                radius,paint,canvas);
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
        return board.getSolutionString();
    }

    public void shake(){
        boardAnimator.shakeBoard();
    }

    //to be used by the puzzle.
    //use getSuccess to echeck for success.
    private boolean checkForSuccess(){
        return board.isSolved();
    }

    public boolean getSuccess(){
        return success;
    }
}
