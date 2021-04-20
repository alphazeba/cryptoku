package com.arnhom.cryptoku.puzzle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.arnhom.cryptoku.activities.cryptokuMain;
import com.arnhom.cryptoku.input.TouchAction;
import com.arnhom.cryptoku.puzzle.moveContainers.Move;
import com.arnhom.cryptoku.general.Point;

/**
 * Created by arnHom on 17/07/28.
 */

public class Puzzle {
    /*
    the goal is that nothing in the puzzle class loops.
    additionally, the update would return a boolean true or false to determine whether or not a
    redraw is required.

    a redraw should only be necessary when things are moving.  So that is fairly rare.

    while i'm thinking about it, i redraw flag should be set when the device is returning from on
    pause.

    an additional idea is to allow for different sized boards.  likely a 3x3 will already be really
    hard, but making it larger would make it even harder.
     */

    private Board board;
    private Board initialBoard;
    private Point screenSize;
    private boolean tweenRedraw;
    private boolean puzzleIsSolved;
    private Point pos;
    private float drawingGapMultiplier = 0.85f;
    private TouchAction capturedInputToBeHandledDuringUpdate;
    private BoardAnimator boardAnimator;
    private cryptokuMain context;

    public Puzzle(int scrambleDepth, int operationComplexity, cryptokuMain context){
        this.pos = new Point(0,0);
        puzzleIsSolved = false;
        this.context = context;
        Log.i("puzzle","begin building puzzle");
        boardAnimator = new BoardAnimator();
        capturedInputToBeHandledDuringUpdate = null;
        screenSize = new Point(0,0);
        board = new Board(3,operationComplexity);
        board.scramble(scrambleDepth);
        initialBoard = new Board(board);
        tweenRedraw = true;
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
        if(touch.x < screenSize.getX()/4 || touch.x > screenSize.getX() || touch.y < screenSize.getY()/4 || touch.y > screenSize.getY()){
            return false;
        }
        capturedInputToBeHandledDuringUpdate = touch;
        return true;
    }
    // 0 is miss
    //right now manage input only returns 1 or 2.
    //this should be transferred into a boolean function
    public boolean manageInput(TouchAction touch){
        //if up or down find col

        Move move;
        if(touch.type == TouchAction.actionType.up || touch.type == TouchAction.actionType.down){
            int col = (int)Math.floor(touch.x/(screenSize.getX()/4)) -1;
            if(col < 0 ||col >2){
                return false;
            }
            move = new Move(true,col,touch.type == TouchAction.actionType.down);
        } else{//if left or right find row
            int row = (int)Math.floor(touch.y/(screenSize.getY()/4)) - 1;
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
        this.screenSize.set(minDimension,minDimension);
        tweenRedraw = true;
    }

    public void drawBoard(Canvas canvas){
        Point shakeDelta = new Point(boardAnimator.getXShake(screenSize.getX()),boardAnimator.getYshake(screenSize.getX()));
        drawNumbers(canvas, shakeDelta);
        drawOperations(canvas, shakeDelta);
    }

    public void drawNumbers(Canvas canvas, Point shakeDelta){
        float width = screenSize.getX();
        for(int iy = 0; iy < 3; ++iy){
            for(int ix = 0; ix < 3 ; ++ix){
                Point cellPos = new Point(
                        (int)(width/4*(ix+1) + boardAnimator.getRowShift(iy,(int)width/4)),
                        (int)(width/4*(iy+1) + boardAnimator.getColShift(ix,(int)width/4))
                );
                int max = (int)(width*0.75f);

                if(cellPos.getX() < width/4){
                    cellPos.addInPlace(new Point(max,0));
                }
                if(cellPos.getY() < width/4){
                    cellPos.addInPlace(new Point(0, max));
                }
                cellPos.addInPlace(shakeDelta);
                drawNumber(cellPos,board.getCell(ix,iy),canvas);
                // draw the cells that are wrapping a second time.
                if(cellPos.getX() > max){
                    drawNumber(cellPos.add(new Point(-max,0)) ,board.getCell(ix,iy),canvas);
                }
                if(cellPos.getY() > max){
                    drawNumber(cellPos.add(new Point(0,-max)),board.getCell(ix,iy),canvas);
                }
            }
        }
    }

    public void drawOperations(Canvas canvas, Point shakeDelta){
        for(int i = 0 ; i < 3; ++i){
            float drawingOffset = screenSize.getX()/3*(i+1);
            Point rowPos = new Point(0,drawingOffset).addInPlace(shakeDelta);
            Point colPos = new Point(drawingOffset, 0).addInPlace(shakeDelta);
            drawTextInCircle(rowPos,board.getRowOperationString(i),canvas);
            drawTextInCircle(colPos,board.getColOperationString(i),canvas);
        }
    }

    private void drawTextInCircle(Point pos, String text , Canvas canvas){
        Paint bgColor = new Paint();
        Paint txtColor = new Paint();

        bgColor.setColor(0xff444444);
        txtColor.setColor(Color.WHITE);
        txtColor.setTextSize(screenSize.getX()/16);

        float radius = screenSize.getX()/8 * drawingGapMultiplier; //this is 1/2*1/4 of the screen.
        //draw the background
        canvas.drawCircle(pos.getX()+radius ,pos.getY()+radius,radius,bgColor);
        // TODO set antialiasing on the text that is being drawn here.
        canvas.drawText(text,pos.getX()+radius/2,pos.getY()+radius,txtColor);
    }

    private void drawNumber(Point pos, int num, Canvas canvas){
        Paint bgPaint = new Paint();
        int bgColor = Color.WHITE;
        int curviness = 30;//the higher this number, the more linear the function. the lower the number, the more drastic the curve is.
        int functionScaler = (10000+curviness)/10;
        float colorScale =(float)((num-1)/(curviness+Math.sqrt((1+(num-1)*(num-1))))*functionScaler);
        if(colorScale > 0){
            float wRatio = (1000-colorScale)/1200.f;
            float pRatio = (colorScale)/1000.f;
            bgColor = Color.rgb((int)(255*wRatio),(int)(255*wRatio),255);
        }
        else if(colorScale < 0){
            colorScale *= -1;
            float wRatio = (1000-colorScale)/1100.f;
            float pRatio = (colorScale)/1100.f;
            bgColor = Color.rgb(255,(int)(255*wRatio),(int)(255*wRatio));
        }
        bgPaint.setColor(bgColor);
        Paint txtPaint = new Paint();
        txtPaint.setColor(Color.BLACK);
        txtPaint.setTextSize(screenSize.getX()/14);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        float cellSize = screenSize.getX()/4*drawingGapMultiplier;
        drawRoundRectWithinBox(
                new RectF(this.pos.getX() + screenSize.getX()/4,
                        this.pos.getY()+ screenSize.getY()/4,
                        this.pos.getX()+ screenSize.getX()*3/4+cellSize,
                        this.pos.getY()+ screenSize.getX()*3/4+cellSize),
                pos.getX(),pos.getY(),cellSize,cellSize,cellSize/9.708f,bgPaint,canvas);
        canvas.drawText(Integer.toString(num),pos.getX()+cellSize/2,pos.getY()+cellSize/2,txtPaint);
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

        if(capturedInputToBeHandledDuringUpdate != null){
            if(manageInput(capturedInputToBeHandledDuringUpdate)){
                redraw = true;
                puzzleIsSolved = checkPuzzleIsSolved();
            }
            capturedInputToBeHandledDuringUpdate = null;
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
    private boolean checkPuzzleIsSolved(){
        return board.isSolved();
    }

    public boolean getPuzzleIsSolved(){
        return puzzleIsSolved;
    }
}
