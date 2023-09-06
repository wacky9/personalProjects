/*Maintains functional behavior by checking for potential errors before calling the function*/
#include "Interpreter.h"
#include <stdlib.h>
bool one_mat[1] = {MATRIX};
bool double_double[2]= {NUMBER,NUMBER};
bool double_mat[2] = {MATRIX,MATRIX};
bool num_mat[2] = {NUMBER,MATRIX};

bool check_operands (Function** ops, bool* expected, int num){
    for(int i = 0; i<num; i++){
        if(ops[i]->type != expected[i]){
            return false;
        }
    }
    return true;
}

/*Check if the dimensions are correct for matrix multiplication*/
bool mul_dimensions(Mat* A, Mat* B){
    return A->col == B->row;
}

/*Check if dimensions are correct for adding*/
bool add_dimensions(Mat* A, Mat* B){
    return A->row == B->row && A->col == B->col;
}

/*Takes in two doubles and adds them together*/
Value* add_wrapper(Function** ops){
    /*Check to see if operand types line up*/
    if(check_operands(ops,double_double,ADD_OPS)){
        Value* newVal = (Value*)calloc(1,sizeof(Value));
        newVal->type = NUMBER;
        /*Just add the two numbers together. No need to overcomplicate this*/
        newVal->num = ops[0]->returnVal->num + ops[1]->returnVal->num;
        return newVal;
    }
}

Value* mul_wrapper(Function** ops){
    if(check_operands(ops,double_double,MUL_OPS)){
        Value* newVal = (Value*)calloc(1,sizeof(Value));
        newVal->type = NUMBER;
        /*Just multiple the two numbers together*/
        newVal->num = ops[0]->returnVal->num * ops[1]->returnVal->num;
        return newVal;
    }
}

Value* mat_add_wrapper(Function** ops){
    if(check_operands(ops,double_mat,MA_OPS)){
        if(add_dimensions(ops[0]->returnVal->mat,ops[1]->returnVal->mat)){
            Value* newVal = (Value*)calloc(1,sizeof(Value));
            newVal->type = MATRIX;
            /*Call Core.c add*/
            newVal->mat = add(ops[0]->returnVal->mat,ops[1]->returnVal->mat);
            return newVal;
        } else{
            //wrong dimensions
            return NULL;
        }
    }
}

Value* scal_mul_wrapper(Function** ops){
    if(check_operands(ops,num_mat,MUL_OPS)){
        Value* newVal = (Value*)calloc(1,sizeof(Value));
        newVal->type = MATRIX;
        newVal->mat = scal_mul(ops[1]->returnVal->mat,ops[0]->returnVal->num);
        return newVal;
    }
}

Value* mat_mul_wrapper(Function** ops){
    if(check_operands(ops,double_mat,MUL_OPS)){
        /*Check multiplication dimensions*/
        if(mul_dimensions(ops[0]->returnVal->mat,ops[1]->returnVal->mat)){
            Value* newVal = (Value*)calloc(1,sizeof(Value));
            newVal->type = MATRIX;
            newVal->mat = compose(ops[0]->returnVal->mat,ops[1]->returnVal->mat);
            return newVal;
        } else{
            //wrong dimensions
            return NULL;
        }
    }
}

Value* transpose_wrapper(Function** ops){
    if(check_operands(ops,one_mat,TRN_OPS)){
        Value* newVal = (Value*)calloc(1,sizeof(Value));
        newVal->type = MATRIX;
        newVal->mat = transpose(ops[0]->returnVal->mat);
        return newVal;
    }
}

/*Does not yet have checks for possible errors*/
Value* solve_wrapper(Function** ops){
    if(check_operands(ops,one_mat,SLV_OPS)){
        Value* newVal = (Value*)calloc(1,sizeof(Value));
        newVal->type = MATRIX;
        Mat* system = ops[0]->returnVal->mat;
        system = backsubstitution(system);
        Mat* solution = elimination(system);
        newVal->mat = solution;
        return newVal;
    }
}
