#include "Interpreter.h"
#include <stdio.h>
#include <stdlib.h>

char* read_single_line(FILE* file);

void start_program(){
    table = setup_depot();
    setup_errors();
}

void end_program(){
    shutter_depot();
}

void run_program(FILE* file){
    char* line = read_single_line(file);
    int line_num = 1;
    setup_line_num(&line_num);
    while(line != NULL){
        bool success = run_single_line(line);
        /*Need to free each loop b/c array is dynamically allocated in read_single_line*/
        free(line);
        if(!success){
            deconstruct_stack();
            return;    
        }
        line = read_single_line(file);
        line_num++;
    }
}

/*Reads and returns a single line from a file
 Must use /n not /r/n*/
char* read_single_line(FILE* file) {
    /*This is probably the largest buffer needed*/
    int buffer_size = 256;
    char* line = (char*)calloc(buffer_size, sizeof(char));
    if (line == NULL) {
        perror("Memory allocation error");
        exit(1);
    }
    int length = 0;
    char ch;
    while (1) {
        ch = fgetc(file);
        if (ch == EOF || ch == '\n') {
            line[length] = 0;
            break;
        }
        line[length++] = ch;
        /*Change line size as necessary
        This (ought) to be called only once or twice*/
        if (length >= buffer_size - 1) {
            buffer_size *= 2;
            line = (char*)realloc(line, buffer_size);
            if (line == NULL) {
                printf("Mem_err");
                free(line);
                exit(1);
            }
        }
    }
    /*If the last line is just an EOF character, return null*/
    if (length == 0 && ch == EOF) {
        free(line);
        return NULL;
    }
    return line;
}

bool execute_line(Line* L);

bool run_single_line(char* line){
    Line* L = interpret_line(line);
    return execute_line(L);
} 

/*Updates the Depot based on the results of the line*/
bool execute_line (Line* L){
    if(L == NULL) {return false;}
    
    /*If not null, process*/
    if(L->new){
        store(L->name,L->v);
        return true;
    } else if(L->out){
        if(L->type){
            p_mat(L->v->mat);
        } else {
            printf("%f\n",L->v->num);
        }
        printf("\n");
        return true;
    } else {
        /*TODO: Check if variable already exists inside Depot
        If not, return error*/
        reassign(L->name,L->v);
        return true;
    }
}