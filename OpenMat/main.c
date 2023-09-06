#include "lin_alg_mac.h"
/*Functions marked with extern can be called unsafely (i.e. by non-functions)
These functions require wrappings, contained in Wrapper.c. Wrapping methods 
are only necessary when called by a non-function*/

void test();

/*Main accepts 0 or 1 command-line arguments. No args means it is in test mode. One arg is a filename, so it is in regular mode. */
int main(int argc, char* argv[]){
    /*Check the mode*/
    if(argc == 1){
        test();
    } else if (argc == 2){
        /*Open and run the file*/
        const char* filename = argv[1];
        FILE* file  = fopen(filename, "r");
        if(file == NULL){
            printf("Not good. File path didn't work");
        } else {
            printf("Beginning execution\n\n");
            start_program();
            run_program(file);
            fclose(file);
            printf("Execution over\n");
        }
        
    } else {
        printf("Whoops, bad input");
    }
}

int p_mat(Mat* mat){
    for(int i = 0; i<mat->row; i++){
        for(int k = 0; k<mat->col; k++){
            printf("%f ",mat->matrix[i][k]);
        }
        printf("\r\n");
    }
}

void test(){
    //printf("SCAL MUL TESTS\n");
    //bool SM = scal_mul_tests();
   // printf("ADD TESTS\n");
    //bool ADD = add_tests();
    //printf("MUL TESTS\n");
    //bool MUL = mul_tests();
    //printf("TRANSPOSE TESTS\n");
   // bool TRN = transpose_tests();
    //printf("GAU TESTS\n");
    //bool GAU = gau_tests();
    printf("INTERPRETER TESTS\n");
    bool INT = interpreter_tests();
    printf("DEPOT TESTS\n");
    bool DPT = depot_tests();
}