#include "Interpreter.h"
#include <stdlib.h>

/*Requires val to be a function
  Val here is a double-pointer, not an array*/
void resolve_func(Function* func){
    /*If it doesn't have a return value, generate one*/
    if(func->returnVal == NULL){
        for(int i = 0; i<func->opNum; i++){
            /*All functions need to be resolved into identity functions*/
            if(func->opList[i]->opNum != -1){
                resolve_func(func->opList[i]);
            } 
        }
        /*Calculate the return value*/
        func->returnVal = func->f_ptr(func->opList);
        func->opList = NULL;
        func->opNum = -1;
    }  
}

void free_function(Function* f){
    free(f->opList);
    free(f);
}

void free_val(Value* v){
    free(v);
}