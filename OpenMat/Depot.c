#include "Interpreter.h"
#define HASHSIZE 127
#include <stdlib.h>
#include <string.h>

Variable** table;


Variable** setup_depot(){
    Variable** new_table = (Variable**)calloc(HASHSIZE,sizeof(Variable*));
    return new_table;
}

/*Should be called *after* setup_depot*/
void shutter_depot(){
    for(int i = 0; i<HASHSIZE; i++){
        Variable* var = table[i];
        while(var != NULL){
            Variable* next = var->nextVar;
            if(var->type){
                free(var->val->mat);
            }
            free(var);
            var = next;
        }
    }
}

/*newVal cannot be a function*/
void store(char* name, Value* newVal){
    int index = location(name);
    Variable* newVar = generate_entry();
    newVar->name = name;
    newVar->type = newVal->type;
    newVar->val = newVal;
    table[index] = newVar;
}

/*Takes in a variable name and returns the value associated with that name*/
Value* query(char* name){
    int index = location(name);
    Variable* v = table[index];
    while(v != NULL){
        if(strcmp(v->name,name) == 0){
            return v->val;
        } else {
            v  = v->nextVar;
        }
    }
}

void reassign(char* name, Value* newVal){
    int index = location(name);
    Variable* v = table[index];
    while(v != NULL){
        if(strcmp(v->name,name) == 0){
            v->val = newVal;
        }
        v  = v->nextVar;
    }
}

/*Hashes the string and returns the appropriate location in the array*/
int location(char* str){
    unsigned int hash = 2166136261;
    int i = 0;
    char c = str[i];
    while(c!= 0){
        hash ^= c;
        hash *= 16777619;
        i++;
        c = str[i];
    }

    return hash%HASHSIZE;
}

Variable* generate_entry(){
    Variable* newVar = (Variable*)calloc(1,sizeof(Variable));
    newVar->nextVar = NULL;
    newVar->name = NULL;
    newVar->val = NULL;
    return newVar;
}
