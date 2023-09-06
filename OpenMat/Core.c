#include "lin_alg_mac.h"
#include <math.h>
#include <pthread.h>
#include <stdlib.h>

/*Returns the given matrix multiplied by a scalar
@extern
@functional*/
Mat* scal_mul(Mat* mat, double scalar){
    Mat* newMat = zero_constructor(mat->row, mat->col);
    pthread_t p[mat->row];
    for(int a = 0; a<mat->row; a++){
		/*Transfer over to Genesis... also rename*/
        arr_num_pair* newPair = calloc(1,sizeof(arr_num_pair));
		newPair->arr = mat->matrix[a];
        newPair->newArr = newMat->matrix[a];
		newPair->val = scalar;
        newPair->size = mat->col; 
		pthread_create(&p[a], NULL, vec_mult, (void*)newPair);
	}
	for(int a = 0; a<mat->row; a++){
		pthread_join(p[a], NULL);
	}
    return newMat;
}

/*@requires: A and B have the same size*/
Mat* add(Mat* A, Mat* B){
    Mat* result = zero_constructor(A->row, A->col);
    pthread_t p[A->row];
    for(int r = 0; r<A->row; r++){
        vec_pair* newPair = calloc(1,sizeof(vec_pair));
        newPair->VecA = A->matrix[r];
        newPair->VecB = B->matrix[r];
        newPair->VecSum = result->matrix[r];
        newPair->size = A->col;
        pthread_create(&p[r], NULL,vec_add,(void*)newPair);
    }
    for(int a = 0; a<A->row; a++){
        pthread_join(p[a],NULL);
    }
    return result;
}

/*Returns the result of matrix multiplying A and B
@requires A.col = B.row*/
Mat* compose(Mat* A, Mat* B){
    /*TODO: Fix potential error here with overflows*/
    pthread_t p[A->row * B->col];
    Mat* result = zero_constructor(A->row,B->col);
    int x = -1;
    /*Loops through each column of the second matrix*/
    for(int r = 0; r<B->col; r++){
        /*Create a column array and fill it with values*/
        /*TODO: This should absolutely be a secondary function*/
        double* col_array = calloc(B->row,sizeof(double));
        for(int i = 0; i<B->row; i++){
            col_array[i] = B->matrix[i][r];
        }
        for(int m = 0; m<A->row; m++){
            vec_pair* newPair = calloc(1,sizeof(vec_pair));
            newPair->VecA = A->matrix[m];
            newPair->VecB = col_array;
            newPair->VecSum = &(result->matrix[m][r]);
            newPair->size = B->row;
            x++;
            pthread_create(&p[x],NULL,vec_compress,(void*)newPair);
        }
    }
    for(int a = 0; a<=x; a++){
        pthread_join(p[a], NULL);
    }
    return result;
}


/*Reads in values col-wise and writes to a new matrix row-wise
This is done because different cores can read from the same cacheline at the same time but
must communicate in order to write to the same cacheline*/
Mat* transpose(Mat* mat){
    Mat* newMat = zero_constructor(mat->col,mat->row);
    short lines = mat->row/LINE;
    pthread_t p[lines*mat->col];
    int x = -1;
    for(int i = 0; i<mat->col; i++){
        for(int j = 0; j<lines; j++){
            vec_line* newLine = calloc(1,sizeof(vec_line));
            newLine->read = mat;
            newLine->write = newMat;
            newLine->r = j*LINE;
            newLine->c = i;
            x++;
            pthread_create(&p[x],NULL,line_transpose,(void*)newLine);
        }
        for(int k = lines*LINE; k<mat->row; k++){
            newMat->matrix[i][k] = mat->matrix[k][i];
        }
    }
    for(int a = 0; a<=x; a++){
        pthread_join(p[a],NULL);
    }
    return newMat;
}

Mat* naive_transpose(Mat* mat){
    Mat* newMat = zero_constructor(mat->col, mat->row);
    for(int i = 0; i<mat->row; i++){
        for(int j = 0; j<mat->col; j++){
            newMat->matrix[j][i] = mat->matrix[i][j];
        }
    }
    return newMat;
}

/*
param: mat
    Must be a vector
param: orientation
    True equals a row vector
    False equals a column vector
returns: the "length" of the vector via euclid normalization
@extern
@functional    */
double euclid_norm(Mat* mat, bool orientation){
    double square_sum;
    if(orientation){
        for(int i = 0; i<mat->col; i++){
            double num = mat->matrix[0][i];
            square_sum += num*num;
        }
    } else {
        for(int i = 0; mat->row; i++){
            double num = mat->matrix[i][0];
            square_sum += num*num;
        }
    }
    return sqrt(square_sum);   
}




/*Returns the submatrix of size [row_len][col_len] with row_index and col_index indicating the 
upper-left hand element of mat where the submatrix begins*/
Mat* sub_matrix(Mat* mat, short row_len, short col_len, short row_index, short col_index){
    Mat* subMat = zero_constructor(row_len, col_len);
    for(int r = 0; r<row_len; r++){
        for(int c = 0; c<col_len; c++){
            subMat->matrix[r][c] = mat->matrix[row_index+r][col_index+c];
        }
    }
    return subMat;
}
