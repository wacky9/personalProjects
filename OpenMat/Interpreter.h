#include "lin_alg_mac.h"
#include <stddef.h>
/*For type: true = mat, false = num*/
typedef struct{
    Mat* mat;
    double num;
    bool type;
} Value;

typedef struct Function{
    /*Array of values*/
    struct Function** opList;
    Value* (*f_ptr)(struct Function** operands);
    Value* returnVal;
    /*The number of operators. 0 means no operators, -1 means it's the identity function (only returns returnVal)*/
    int opNum;
    bool type;
} Function;

typedef struct Variable{
    Value* val;
    struct Variable* nextVar;
    char* name;
    bool type;
} Variable;

/*A completed Line struct will contain a value, the name of the variable, and have three variables that 
state what changes need to be made to fulfill the line*/
typedef struct{
    Value* v;
    char* name;
    bool new;
    bool out;
    bool type;
} Line;

/*Variable hashtable*/
extern Variable** table;

#define ADD_OPS 2
#define MUL_OPS 2
#define MA_OPS 2
#define SM_OPS 2
#define MM_OPS 2
#define TRN_OPS 1
#define SLV_OPS 1

#define MATRIX true
#define NUMBER false

/*Wrapper functions*/
Value* add_wrapper(Function** ops);
Value* mul_wrapper(Function** ops);
Value* mat_add_wrapper(Function** ops);
Value* mat_mul_wrapper(Function** ops);
Value* transpose_wrapper(Function** ops);
Value* scal_mul_wrapper(Function** ops);
Value* solve_wrapper(Function** ops);

/*Interpreter Functions*/
Mat* interpret_mat(char* text); //TESTED
char** func_sub_blocks(char* line); //TESTED
char* substring(char* str, int begin, int end); //TESTED
char** tokenize_line(char* line); //TESTED
short* dimension_string_mat(char* text); //TESTED
bool validate_tokens(char** tokens); //TESTED
Function* interpret_func(char* text);
Function* fetch_identity();
short val_type(char* text);
Value* interpret_declaration(char* text); //TESTED
Line* interpret_line(char* line);
void setup_line_num(int* num);

/*Func_table*/
Function* get_func(char* func_name);

/*Func_tree*/
void resolve_func(Function* func);

/*Depot*/
Value* query(char* name);
void store(char* name, Value* newVal);
Variable** setup_depot();
void shutter_depot();
void reassign(char* name, Value* newVal);
int location(char* str); //TESTED
Variable* generate_entry();

typedef enum{BAD_LINE, WRONG_PARAMS, NO_DECL, END}ERR_TYPE;

/*Error*/
void deconstruct_stack();
void add_error(ERR_TYPE e, int ln);
void setup_errors();
