#include "Interpreter.h"
#include <stdio.h>
#include <stdlib.h>

/*Stack of errors*/
typedef struct{
    struct Error* next;
    ERR_TYPE err;
    int line_num;
} Error;

void prnt_err_msg(ERR_TYPE e, int ln);

Error* err_list;

void setup_errors(){
    err_list =  (Error*)calloc(1,sizeof(Error));
    err_list->err = END;
}


void add_error(ERR_TYPE e, int ln){
    /*Not allowed to add another bottom to the stack*/
    if(e == END){
        return;
    }
    Error* new_err = (Error*)calloc(1,sizeof(Error));
    /*Load info into new_err*/
    new_err->err = e;
    new_err->line_num = ln;
    /*Make new_err the top of the stack*/
    new_err->next = err_list;
    err_list = new_err;
}

void deconstruct_stack(){
    while(err_list->err != END){
        /*Print message*/
        prnt_err_msg(err_list->err,err_list->line_num);
        /*Go a layer lower on the stack*/
        Error* next_err = err_list->next;
        /*Free the used-up layer*/
        free(err_list);
        err_list = next_err;
    }
    /*Free the ending Error struct*/
    free(err_list);
}

void prnt_err_msg(ERR_TYPE e, int ln){
    switch(e){
        case WRONG_PARAMS:
            printf("Wrong number of parameters on line %d\n",ln);
            break;
        case BAD_LINE:
            printf("Malformed line: %d\n", ln);
            break;
        case NO_DECL:
            printf("Attempt to use a variable that has not been declared\n");
        default:
            printf("Unknown error\n");
            break;
    }

}