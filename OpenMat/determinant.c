#include "lin_alg_mac.h"
#include "internal.h"
#include <math.h>

/*Computes the determinant
@requires: mat must be square
@external*/
double det(Mat* mat){

}

/*Computes the determinant of a 2x2 matrix*/
double base_det(Mat* mat){
    return mat->matrix[0][0]* mat->matrix[1][1] - mat->matrix[0][1]*mat->matrix[1][0];
}

/*Computes the determinant of a matrix naively (aka the extremely slow method)
@requires: mat must be square*/
double naive_det(Mat* mat){
    if(mat->row == 2){
        return base_det(mat);
    } else {
        double sign = -1.0;
        double val = 0.0;
        for(int i = 0; i<mat->row; i++){
            sign = -1.0*sign;
            val += sign * mat->matrix[0][i] * naive_det(sub_matrix(mat,mat->row-1,mat->col-1,1,i));
        }
        return val;
    }
}