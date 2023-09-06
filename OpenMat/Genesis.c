#include "lin_alg_mac.h"
#include <stdlib.h>

/*Returns an invalid matrix. Used to handle errors gracefully*/
Mat* invalid_matrix(){
    Mat* invalid = (Mat*)calloc(1,sizeof(Mat));
    if(invalid == NULL){
        exit(EXIT_FAILURE);
    } else {
        invalid->valid = false;
        return invalid;
    }
}

/*Returns a matrix of size [row][col] filled with zeroes*/
Mat* zero_constructor(short row, short col){
    /*Create Mat struct*/
    Mat* newMat;
    void* potential_pointer = calloc(1,sizeof(Mat));
    if(potential_pointer != NULL){
        newMat = (Mat*)potential_pointer;
    } else {
        return invalid_matrix();        
    }
    /*Add row/col information*/
    newMat->row = row;
    newMat->col = col;
    newMat->valid = true;

    /*Array of double arrays*/
    void* mat_pointer = calloc(row,sizeof(double *));
    if(mat_pointer != NULL){
        newMat -> matrix = mat_pointer;
    } else{
        return invalid_matrix();
    }

    /*Fill out double arrays*/
    for(int i = 0; i<row; i++){
        void* row_pointer = calloc(col,sizeof(double));
        if(row_pointer != NULL){
            newMat->matrix[i] = row_pointer;
        } else{
            return invalid_matrix();
        }
    }
    return newMat;
}

/*Constructs a matrix from an array of doubles
@requires: |arr| = row*col */
Mat* arr_constructor(short row, short col, double* arr){
    /*Create Mat struct*/
    Mat* newMat;
    void* potential_pointer = calloc(1,sizeof(Mat));
    if(potential_pointer != NULL){
        newMat = (Mat*)potential_pointer;
    } else {
        return invalid_matrix();        
    }

    /*Add row/col information*/
    newMat->row = row;
    newMat->col = col;
    newMat->valid = true;

    /*Array of double arrays*/
    void* mat_pointer = calloc(row, sizeof(double *));
    if(mat_pointer != NULL){
        newMat -> matrix = mat_pointer;
    } else{
        return invalid_matrix();
    }

    /*Fill out double arrays*/
    for(int i = 0; i<row; i++){
        void* row_pointer = calloc(col,sizeof(double));
        if(row_pointer != NULL){
            newMat->matrix[i] = row_pointer;
            for(int j = 0; j<col; j++){
                newMat->matrix[i][j] = arr[i*col+j];
            }
        } else{
            return invalid_matrix();
        }
    }
    return newMat;
}

/*Returns the corresponding column vector of the identity matrix of size size*/
Mat* e_vector(short col, short size){
    Mat* result = zero_constructor(size,1);
    result->matrix[col][0] = 1;
    return result;
}

/*Free all memory associated with the matrix including the matrix*/
void unravel_mat(Mat* mat){
    /*Free each row*/
    for(int i = 0; i<mat->row; i++){
        free(mat->matrix[i]);
    }
    /*Free the array of row pointers*/
    free(mat->matrix);
    /*Free the struct itself*/
    free(mat);
    mat = NULL;
}