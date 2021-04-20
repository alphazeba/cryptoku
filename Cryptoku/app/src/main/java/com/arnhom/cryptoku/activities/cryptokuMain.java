package com.arnhom.cryptoku.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.arnhom.cryptoku.output.ExciteInformer;
import com.arnhom.cryptoku.puzzle.Puzzle;
import com.arnhom.cryptoku.R;
import com.arnhom.cryptoku.output.ResponseRandomizer;
import com.arnhom.cryptoku.input.TouchAction;
import com.arnhom.cryptoku.input.TouchInput;


public class cryptokuMain extends AppCompatActivity{
//TODO this program is horribly organized.
    //variables
    //main components
    SurfaceView surfaceView;
    TextView solutionText;
    TouchInput input;
    Puzzle puzzle;
    ExciteInformer exciteInformer;
    ResponseRandomizer responseRandomizer;
    boolean showSolution;
    //drawing thread items.
    Thread gameThread;
    boolean gameThreadRunning;
    boolean canvasDirty;
    long lastFrameStartTime;
    long thisFrameStartTime;
    long onPauseOffsetTime;
    long minimumFrameLength;
    float frameTime;
    float oldWidth = 0;
    float oldHeight = 0;
    //used for input blocking.
    //only celebrate once newly entering the success state.
    boolean hasSucceeded;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cryptuko_main);

        surfaceView = (SurfaceView)findViewById(R.id.main_surface);
        solutionText = (TextView)findViewById(R.id.solutionTextView);
        gameThreadRunning = false;
        gameThread = null;
        this.setGoalFPS(60);
        onPauseOffsetTime = 0;
        canvasDirty = true;
        oldWidth = 0;
        oldHeight = 0;

        hasSucceeded = false;
        showSolution = false;

        //The drawing thread is started later in onResume.
        input = new TouchInput(720,1280);

        exciteInformer = new ExciteInformer(720,1280);
        responseRandomizer = new ResponseRandomizer();

        puzzle = buildPuzzleFromIntent(getIntent());

        surfaceView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view,MotionEvent me){
                TouchAction touch = input.update(me);
                if(touch != null){
                    //there was an action
                    onTouchAction(touch);
                }
                return true;
            }
        });
    }

    //passes touches on to the puzzle
    private void onTouchAction(TouchAction touch){
        puzzle.onInput(touch);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i("test","onresume has run.");
        flagForRedraw();
        //handle pause offset time
        //adjust last and this frame so that it runs as if the phone was never off.
        onPauseOffsetTime = System.currentTimeMillis() - onPauseOffsetTime;
        lastFrameStartTime += onPauseOffsetTime;
        thisFrameStartTime += onPauseOffsetTime;

        startGameThread();
    }

    @Override
    protected void onPause(){
        super.onPause();
        onPauseOffsetTime = System.currentTimeMillis();
        killGameThread();
    }

    private void startGameThread(){
        //do not start the thread if the thread is already running
        if(gameThread != null){
            Log.e("error","thread still alive");
            return;
        }

        Log.i("test","start thread has run.");

        //start the thread.
        gameThreadRunning = true;

        //start the drawing thread.
        gameThread = new Thread(new Runnable(){
            public void run(){
                mainGameThreadLoop();
            }
        });
        gameThread.start();
    }

    private void killGameThread(){
        gameThreadRunning = false;

        if(gameThread == null)
            return;

        boolean joined = false;
        while(!joined){
            try{
                gameThread.join();
                joined = true;
            }
            catch(java.lang.InterruptedException e){
                Log.e("error","join has been interrupted in killThread()");
            }
        }

        gameThread = null;
    }

    private void setGoalFPS(int fps){
        minimumFrameLength = (long)(1000.f/fps);
    }

    void drawAndUndirtyOnSuccess(SurfaceHolder holder){
        if(holder == null)
            return;
        Canvas canvas = holder.lockCanvas();
        if(canvas == null){
            Log.e("debug","we have failed to actualDraw this time :'(");
        } else {
            //check for resize
            if(oldWidth != canvas.getWidth() || oldHeight != canvas.getHeight()){
                onResize(canvas);
            }
            actualDraw(canvas);
            holder.unlockCanvasAndPost(canvas);
            canvasDirty = false;
        }
    }

    void gameUpdate(){
        //update should use frameTime.
        if(puzzle.update(frameTime))
            flagForRedraw();

        if(puzzle.getPuzzleIsSolved() && !hasSucceeded)
            exciteInformer.inform(responseRandomizer.getSuccessResponse());

        hasSucceeded = puzzle.getPuzzleIsSolved();

        if(exciteInformer.update(frameTime))
            flagForRedraw();
    }

    void onResize(Canvas canvas){
        oldHeight = canvas.getHeight();
        oldWidth = canvas.getWidth();

        //send resize events
        puzzle.onResize(canvas.getWidth(),canvas.getHeight());
        exciteInformer.onResize(canvas.getWidth(),canvas.getHeight());
    }

    void flagForRedraw(){
        //informs the program that something has changed on screen.
        canvasDirty = true;
    }

    //WARNING
    //runnableloop runs on a seperate thread from the ui thread.
    //This is the main loop of the side thread.
    void mainGameThreadLoop(){
        //thread Running is set to false by killThread().
        Log.i("test","runnable loop has begun");
        while(gameThreadRunning){
            //clock stuff.
            //if the frame went by too quickly (likely) the side thread will sleep to maintain a lower maximum fps.
            long time = System.currentTimeMillis() - thisFrameStartTime;
            if(time < minimumFrameLength)
                SystemClock.sleep(minimumFrameLength - time);

            lastFrameStartTime = thisFrameStartTime;
            thisFrameStartTime = System.currentTimeMillis();

            frameTime = ((float) (thisFrameStartTime - lastFrameStartTime))/1000; //frametime is in seconds.

            if(frameTime < 0)
                frameTime = 0;

            gameUpdate();

            if(canvasDirty)
                drawAndUndirtyOnSuccess(surfaceView.getHolder());
        }
        Log.i("test","runnable loop has exited.");
    }

    void actualDraw(Canvas canvas){
        bucketCanvas(canvas, new Paint(Color.BLACK));
        puzzle.drawBoard(canvas);
        exciteInformer.draw(canvas);
    }

    void bucketCanvas(Canvas canvas, Paint paint){
        canvas.drawRect(new Rect(0,0,canvas.getWidth(),canvas.getHeight()),paint);
    }

    public void onResetButton(View view){
        hideSolution();
        puzzle.resetBoard();
        puzzle.shake();
        exciteInformer.inform(responseRandomizer.getResetResponse());
        flagForRedraw();
    }

    public void onNewPuzzleButton(View view){
        hideSolution();
        killGameThread(); // TODO why do we start and kill the thread here? this is not obvious.
        puzzle = buildPuzzleFromIntent(getIntent());
        puzzle.onResize(oldWidth,oldHeight);
        puzzle.shake();
        exciteInformer.inform(responseRandomizer.getNewPuzzleResponse());
        startGameThread();
    }

    public void onGetSolutionButton(View view){
        revealSolution();
        puzzle.shake();
        exciteInformer.inform(responseRandomizer.getGetSolutionResponse());
    }

    private Puzzle buildPuzzleFromIntent(Intent intent){
        if(isCustomGame(intent))
            return new Puzzle(
                    intent.getIntExtra("depth", 0),
                    intent.getIntExtra("operators", 0),
                    this);
        // else
        return new Puzzle(
                intent.getIntExtra("difficulty",-1),
                this);//right now just builds a level 0 puzzle;
    }

    private boolean isCustomGame(Intent intent){
        return intent.getIntExtra("difficulty",-1) == -1;
    }

    private void revealSolution(){
        showSolution = true;
        solutionText.setText(puzzle.solutionToString());
    }

    private void hideSolution(){
        showSolution = false;
        solutionText.setText("");
    }

    public void onMoveWasMade(){
        if(showSolution){
            runOnUiThread(new Runnable(){
                public void run(){
                    revealSolution();
                }
            });
        }
    }
}
