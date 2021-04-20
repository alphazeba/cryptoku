package com.arnhom.cryptoku.output;

import java.util.Random;

//stores multiple phrases for each need and reandomly returns one of them.

    public class ResponseRandomizer {

    private Random rand;

    private String success [] = {
            "SUCCESS!!   SUCCESS!!  SUCCESS!!",
            "YOU WIN!!   YOU WIN!!  YOU WIN!!",
            "VICTORY ACHIEVED!!  VICTORY ACHIEVED!!",
            "PUZZLE VANQUISHED!!  PUZZLE VANQUISHED!!"
    };

    private String newPuzzle [] = {
            "A NEW CHALLENGE!",
            "HAVE ANOTHER!",
            "WAS THAT TOO EASY?",
            "THE NEXT WON'T BE SO EASY!",
            "INCONCEIVABLE!"
    };

    private String getSolution [] ={
            "TOO HARD?",
            "I WIN!",
            "YOU HAVE GIVEN UP!",
            "YOU LOSE!",
            "VICTORY MUST BE WON!",
            "TURN DOWN THE DIFFICULTY!"
    };

    private String reset [] = {
            "AGAIN!",
            "WHAT DID YOU DO?",
            "GOOD IDEA!",
            "OOPS!",
            "TRY SOMETHING ELSE!",
            "CONFUSING!",
            "ARE YOU DONE YET?",
            "NOT EVEN CLOSE!"
    };

    public ResponseRandomizer(){
        rand = new Random();
    }

    public String getNewPuzzleResponse(){
        return getRandomResponse(newPuzzle);
    }

    public String getGetSolutionResponse(){
        return getRandomResponse(getSolution);
    }

    public String getResetResponse(){
        return getRandomResponse(reset);
    }

    public String getSuccessResponse() {
        return getRandomResponse(success);
    }

    private String getRandomResponse(String s []){
        return s[rand.nextInt(s.length)];
    }
}
