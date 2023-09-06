#include "Interpreter.h"
#include <string.h>
#include <stdlib.h>

typedef enum{ADD,MUL,MAT_ADD,SCAL_MUL,MAT_MUL,TRAN,SOLVE}FUNCTION;


/*Translates a string into a function name*/
FUNCTION translate_func_name(char* func_name){
    if(strcmp(func_name,"ADD")== 0){
        return ADD;
    } else if(strcmp(func_name,"MUL") == 0){
        return MUL;
    } else if(strcmp(func_name,"SCAL_MUL") == 0){
        return SCAL_MUL;
    } else if(strcmp(func_name,"MAT_ADD") == 0){
        return MAT_ADD;
    } else if(strcmp(func_name,"MAT_MUL") == 0){
        return MAT_MUL;
    } else if(strcmp(func_name,"TRAN") == 0){
        return TRAN;
    } else if(strcmp(func_name,"SOLVE") == 0){
        return SOLVE;
    }
}

/*Returns a function object correspoding to a given function name*/
Function* get_func(char* func_name){
    FUNCTION f = translate_func_name(func_name);
    Function* new_func = (Function*)calloc(1,sizeof(Function));
    switch(f){
        case ADD:
            new_func->type = NUMBER;
            new_func->f_ptr = &add_wrapper;
            new_func->opNum = ADD_OPS;
            new_func->opList = (Function**)calloc(ADD_OPS,sizeof(Function*));
            /*Should be 0 based on calloc, but just in case*/
            new_func->returnVal = NULL;
            break;
        case MUL:
            new_func->type = NUMBER;
            new_func->f_ptr = &mul_wrapper;
            new_func->opNum = MUL_OPS;
            new_func->opList = (Function**)calloc(MUL_OPS,sizeof(Function*));
            /*Should be 0 based on calloc, but just in case*/
            new_func->returnVal = NULL;
            break;
        case MAT_ADD:
            new_func->type = MATRIX;
            new_func->f_ptr = &mat_add_wrapper;
            new_func->opNum = ADD_OPS;
            new_func->opList = (Function**)calloc(ADD_OPS,sizeof(Function*));
            new_func->returnVal = NULL;
            break;
        case SCAL_MUL:
            new_func->type = MATRIX;
            new_func->f_ptr = &scal_mul_wrapper;
            new_func->opNum = MUL_OPS;
            new_func->opList = (Function**)calloc(MUL_OPS,sizeof(Function*));
            new_func->returnVal = NULL;
            break;
        case MAT_MUL:
            new_func->type = MATRIX;
            new_func->f_ptr = &mat_mul_wrapper;
            new_func->opNum = MUL_OPS;
            new_func->opList = (Function**)calloc(MUL_OPS,sizeof(Function*));
            new_func->returnVal = NULL;
            break;
        case TRAN:
            new_func->type = MATRIX;
            new_func->f_ptr = &transpose_wrapper;
            new_func->opNum = TRN_OPS;
            new_func->opList = (Function**)calloc(TRN_OPS,sizeof(Function*));
            new_func->returnVal = NULL;
            break;
        case SOLVE:
            new_func->type = MATRIX;
            new_func->f_ptr = &solve_wrapper;
            new_func->opNum = SLV_OPS;
            new_func->opList = (Function**)calloc(SLV_OPS,sizeof(Function*));
            new_func->returnVal = NULL;
            break;
    }
    return new_func;
}
