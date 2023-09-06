#include "lin_alg_mac.h"

/*mat is an augmented matrix. Returns a row-reduced matrix*/
Mat* elimination(Mat* mat){
   Mat* newMat = sub_matrix(mat,mat->row,mat->col,0,0);
   for(int i = 0; i<mat->row; i++){
        Mat* upperRow = sub_matrix(newMat,1,newMat->col,i,0);
        for(int k = i+1; k<newMat->row; k++){
            /*Fetch the row to be modified*/
            Mat* replace_row = sub_matrix(newMat,1,newMat->col,k,0);
            double a = upperRow->matrix[0][i];
            double b = replace_row->matrix[0][i];
            Mat* elimination_row = scal_mul(upperRow,-b/a);
            Mat* newRow = add(elimination_row,replace_row);
            unravel_mat(replace_row);
            unravel_mat(elimination_row);
            newMat->matrix[k] = newRow->matrix[0];
        }
   }
   return newMat;
}

/*mat is a row-reduced, augmented, non-singular matrix w/no extra rows of zeroes
Returns a solution column vector*/
Mat* backsubstitution(Mat* mat){
    Mat* results = zero_constructor(mat->col-1,1);
    short end_column = mat->col-1;
    short c = end_column -1;
    for(int r = mat->row-1; r>=0; r--){
        double x = mat->matrix[r][end_column];
        for(int i = c+1; i<end_column; i++){
            double val = mat->matrix[r][i] * results->matrix[i][0];
            x-=val;
        }
        x /= mat->matrix[r][c];
        results->matrix[c][0] = x;
        c--;
    }
    return results;
}

/*Requires an augmented matrix*/
Mat* scaled_partial_pivoting(Mat* mat){
    
}