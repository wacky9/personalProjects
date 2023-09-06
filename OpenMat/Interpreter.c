#include "Interpreter.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

/*This is only used for registering errors
it does not get changed in Interpreter.c--only in Executor.c*/
int* line_number;

void setup_line_num(int* num){
    line_number = num;
}
/*Takes in a string and determines what is happening in the line
Returns a complete line object */
Line* interpret_line(char* line){
    char** tokens = tokenize_line(line);
    if(validate_tokens(tokens)){
        Line* newLine = (Line*)calloc(1,sizeof(Line));
        char* first_token = tokens[0];
        if(strcmp(first_token,"MAT") == 0){
            newLine->new = true;
            newLine->out = false;
            newLine->type = MATRIX;
            newLine->name = tokens[1];
        } else if(strcmp(first_token,"NUM") == 0) {
            newLine->new = true;
            newLine->out = false;
            newLine->type = NUMBER;
            newLine->name = tokens[1];
        } else if(strcmp(first_token,"OUT") == 0){
            newLine->new = false;
            newLine->out = true;
        } else {
            newLine->new = false;
            newLine->out = false;
            newLine->name = tokens[0];
        }
        Function* func;
        if(newLine->new){
            func = interpret_func(tokens[3]);
            if(func->type != newLine->type){
                return NULL;
            }
        } else {
            func = interpret_func(tokens[2]);
            /*If it isn't new, the type should be inferred from resolving the function*/
            newLine->type = func->type;
        }
        /*Resolves the function tree, setting func->returnVal to the value of the line*/
        resolve_func(func);
        newLine->v = func->returnVal;
        //free(tokens);
        return newLine;
    } else {
        add_error(BAD_LINE,*line_number);
        return NULL;
    }
}

/*Takes in a string and creates a function object from that string. Declarations are transformed into values, and variables are 
filled in with their corresponding values. Nested functions are setup and interpreted*/
Function* interpret_func(char* text){
    /*Determine whether the corresponding text is a function, variable, or declaration*/
    Function* new_func; 
    short type = val_type(text);

    if(type == 2){
        new_func = fetch_identity();
        Value* var_val = query(text);
        new_func->returnVal = var_val;
        new_func->type = new_func->returnVal->type;
    } else if (type == 1){
        new_func = fetch_identity();
        new_func->returnVal = interpret_declaration(text);
        new_func->type = new_func->returnVal->type;
    } else {
        /*Split up text into function name and parameter strings*/
        char** blocks = func_sub_blocks(text);
        /*Fetch function object based on function name from func_table*/
        new_func = get_func(blocks[0]);
        /*Go through each param and resolve it into a Function*/
        for(int i = 0; i<new_func->opNum; i++){
            /*blocks indexing is off-by-one b/c blocks[0] is the name*/
            new_func->opList[i] = interpret_func(blocks[i+1]);
        }
    }
    return new_func;    
}

/*Returns an identity function. This function has no operands and returns a single value*/
Function* fetch_identity(){
    Function* iden = (Function*)calloc(1,sizeof(Function));
    iden->f_ptr = NULL;
    iden->opList = NULL;
    iden->opNum = -1;
    return iden;
}

/*If a function, return 0. If a declaration, return 1. If a variable, return 2*/
short val_type(char* text){
    /*If a declaration, it will start with '[' or a number or '-'*/
    char init_char = text[0];
    if(init_char == '[' || init_char == '-' || (init_char>='0' && init_char <= '9')){
        return 1;
    } else {
        /*Since only functions are allowed to have parentheses, if it has one, it's a function*/
        char c = text[0];
        int index = 0;
        while(c != 0){
            index++;
            c = text[index];
            if(c == '('){
                return 0;
            }
        }
        /*If it reaches, here, it has neither parentheses nor is a declaration, so it must be a variable name*/
        return 2;
    }
}

/*Interprets a declaration, i.e. a pure numerical value, either a matrix or a num*/
Value* interpret_declaration(char* text){
    char c = text[0];
    Value* newVal = (Value*)calloc(1,sizeof(Value));
    /*If it starts with a bracket, it is a matrix. Otherwise it's a number*/
    newVal->type = (c=='[');
    if(newVal->type){
        int length = 0;
        while(c != 0){
            c = text[length];
            length++;
        }
        char* bracket_less = substring(text,1,length-1);
        Mat* newMat = interpret_mat(bracket_less);
        newVal->mat = newMat;
    } else {  
        newVal->num = atof(text);
    }
    return newVal;
}

/*Returns a matrix that may or may not be valid. If it is a valid matrix, it corresponds to the matrix in the text string
@requires text to be a matrix in string form, with no opening or closing brackets*/
Mat* interpret_mat(char* text){
    char* current = text;
    char* next = text;
    short* dimensions = dimension_string_mat(text);
    short r = dimensions[0];
    short c = dimensions[1];
    /*Create a new array with enough space for all values*/
    double* arr = (double*)calloc(r*c,sizeof(double));
    int i = 0;
    while(*next != 0){
        double a = strtod(current,&next);
        arr[i] = a;
        current = next+1;
        i++;
    }
    return arr_constructor(r,c,arr);
}

/*Determines the dimensions of a matrix in string form. Returns an array {r,c} where
r = the number of rows and c = the number of columns*/
short* dimension_string_mat(char* text){
    char* current = text;
    char* next = text;
    /*Counts the number of semi-colons*/
    short r = 0;
    /*The number of commas between semi-colons*/
    short c = 0;
    while(*next != 0){
        strtod(current,&next);
        if(*next == ',' && r == 0){
            c++;
        } else if (*next == ';'){
            r++;
        } else{
            //error
        }
        current = next+1;
    }
    //Add one to each
    r++; c++;
    short* result = (short*)calloc(2,sizeof(short));
    result[0] = r;
    result[1] = c;
    return result;
}



/*Has between 3-4 tokens, where the penultimate token is an equals sign. If 4 tokens, first one is MAT, or NUM. */
bool validate_tokens(char** tokens){
    bool correct = true;
    /*First check number of tokens*/
    int token_num = 0;
    for(token_num = 0; tokens[token_num] != NULL; token_num++){}
    if(token_num == 3){
        correct = strcmp(tokens[1],"=") == 0;
    } else if (token_num == 4) {
        correct = strcmp(tokens[2],"=") == 0;
        if(correct){
            correct = strcmp(tokens[0],"MAT") == 0 || strcmp(tokens[0],"NUM") == 0;
        }
    } else {
        correct = false;
    }
    return correct;
}

/*Splits a function into sub-blocks. Block 0 is the name of the function. The rest of the blocks are the arguments*/
char** func_sub_blocks(char* line){
    /*Tracks whether the current char is within an sub-function of the function*/
    int level = 0;
    char c = 1;
    char* name;
    int breakpoints[10];
    int str_index = -1;
    /*Set this to properly capture beginning of the string*/
    breakpoints[0] = -1;
    /*Since breakpoint[0] has already been set, we're now looking for breakpoint[1]*/
    int break_index = 1;
    /*Identify the location of the opening parenthesis*/
    while(c != '(' && c!= 0){
        str_index++;
        c = line[str_index];
    }
    breakpoints[break_index] = str_index;
    break_index++;
    /*Mark the location of every comma that is on level 0
    Increment levels whenever an opened parentheses or bracket is discovered.
    Decrement levels whenever a closing parentheses or bracket is discovered */
    while(c != 0){
        str_index++;
        c = line[str_index];
        if(c == '(' || c == '['){
            level++;
        } else if(c == ')'|| c== ']'){
            level--;
        } else if (c == ',' && level == 0){
            breakpoints[break_index] = str_index;
            break_index++;
        }
    }
    /*Must be -1 because we don't want closing parenthesis*/
    breakpoints[break_index] = str_index-1;
    char** blocks = (char**)calloc(break_index,sizeof(char*));
    /*Each block should be the range [last_breakpoint+1, next_breakpoint)*/
    for(int k = 0; k<break_index; k++){
        blocks[k] = substring(line,breakpoints[k]+1,breakpoints[k+1]);
    }
    return blocks;
}

/*Returns the substring [begin,end) of str*/
char* substring(char* str, int begin, int end){
    int size = end-begin;
    char* newStr = (char*)calloc(size+1,sizeof(char*));
    for(int i = 0; i<size; i++){
        newStr[i] = str[i+begin];
    }
    /*Null terminate*/
    newStr[size] = 0;
    return newStr;
}

/*Returns an array of strings from the given string, using space as the split character*/
char** tokenize_line(char* line){
    char split_char = ' ';
    char c = 1;
    int index = 0;
    int token_num = 0;
    /*Identify how many tokens there are*/
    while(c != 0){
        c = line[index];
        if(c == split_char){
            token_num++;
        }
        index++;
    }
    char** tokens = (char**)calloc(token_num+1,sizeof(char*));
    /*Tokenize the input string */
    char *token = strtok(line, " ");
    index = 0;
    while (token != NULL) {
        tokens[index] = strdup(token);
        token = strtok(NULL, " ");
        index++;
    }
    tokens[token_num+1] = NULL;
    return tokens;
}