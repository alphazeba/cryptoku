package com.arnhom.cryptoku.puzzle;

import static com.arnhom.cryptoku.puzzle.Operations.Operation.none;

/**
 * Created by arnHom on 17/07/28.
 */

public class Operations {
    enum Operation{add,subtract,multiply,divide,insert,backspace, none}
    Operation operation;
    int operand;

    public Operations(){
        operation = none;
        operand = 0;
    }

    public Operations(Operations other){
        operand = other.operand;
        operation = other.operation;
    }

    //random generation based on difficulty level
    public Operations(int difficulty){
        setup(difficulty);
    }

    //allows choosing the operation and operand
    public Operations(Operation op, int num){
        setup(op,num);
    }

    public void setup(int difficulty){
        switch(difficulty){
            case 0:
                this.operation = randomOperation(1);
                this.operand = randomInt(10);
                break;
            case 1:
                this.operation = randomOperation(3);
                this.operand = randomInt(10);
                break;
            case 2:
                this.operation = randomOperation(5);
                this.operand = randomInt((this.operation != Operation.insert && this.operation != Operation.backspace) ? 25 : 9); //as long as it is not insert or backspace, increase the max value.
                break;
            default:
                this.operation = Operation.add;
                this.operand = 0;
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        switch(operation){
            case add:
                sb.append("+ ");
                break;
            case subtract:
                sb.append("- ");
                break;
            case multiply:
                sb.append("x ");
                break;
            case divide:
                sb.append("÷ ");
                break;
            case insert:
                sb.append("→");
                break;
            case backspace:
                sb.append("↚");
                break;
            default:
                sb.append("uh oh");
                break;
        }
        sb.append(operand);
        return sb.toString();
    }

    public void setup(Operation op, int num){
        operation = op;
        operand = num;
    }

    public boolean isOperationLegal(int input, boolean forward){
        double operationResult = unCheckedOperate(input,forward);

        //can only have 4 or fewer digits.
        if(Math.abs(operationResult) >= 10000){
            return false;
        }

        //must be a whole number
        if(Math.floor(operationResult) != operationResult){
            return false;
        }

        return true;
    }

    private double unCheckedOperate(int input, boolean forward){
        if(forward){
            switch(this.operation){
                case add:
                    return input + operand;

                case subtract:
                    return input - operand;

                case multiply:
                    return input * operand;

                case divide:
                    return  input / (double)operand;

                case insert:
                    if(input >= 0){
                        return input * 10 + operand;
                    }
                    else { //input is negative
                        return input * 10 - operand;
                    }


                case backspace:
                    if(input == 0){//cannot backspace 0
                        return 0.5; // this will cause the legal check to fail.
                    }
                    else if(input > 0){
                        return (input-operand)/10.f;
                    }
                    else { //input is negative.
                        return (input+operand)/10.f;
                    }
                default:
                    return 0.123123;
            }

        } else{
            switch(this.operation){
                case add:
                    return input - operand;

                case subtract:
                    return input + operand;

                case multiply:
                    return input / (double)operand;

                case divide:
                    return  input * operand;

                case insert:
                    if(input == 0){//cannot backspace 0
                        return 0.5; // this will cause the legal check to fail.
                    }
                    else if(input > 0){
                        return (input-operand)/10.f;
                    }
                    else { //input is negative.
                        return (input+operand)/10.f;
                    }

                case backspace:
                    if(input >= 0){
                        return input * 10 + operand;
                    }
                    else { //input is negative
                        return input * 10 - operand;
                    }
                default:
                    return 0.13123;
            }
        }
    }

    //this returns a rounded value if the output is not legal.
    //you must check whether the operation is legal using isOperationLegal prior to doing the operation.
    public int operate(int input, boolean forward){
        return (int)unCheckedOperate(input,forward);
    }

    //returns a random operation up to the number provided.
    private Operation randomOperation(int maxOperation){
        return Operation.values()[(int)(Math.floor(Math.random() * (maxOperation+1)))];
    }

    private Operation randomOperation(Operation[] potentialOperations){
        return potentialOperations[(int)(Math.floor(Math.random() * (potentialOperations.length)))];
    }

    //returns a  1toMaxNum number.
    private int randomInt(int maxNum){
        return 1+(int)(Math.floor(Math.random()*(maxNum)));
    }

    private static final int[] primes = {0,1,2,3,5,7,11,13};
    private int randomPrime(){
        return primes[randomInt(primes.length-1)];
    }
}
