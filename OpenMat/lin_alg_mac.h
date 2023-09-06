#include <stdbool.h>
#include <stdio.h>
#define ABSOLUTE_EPSILON 0.001
#define RELATIVE_EPSILON 0.001
/*Test mode: 0 = no testing, 1 = high-level testing; 2+ = detailed testing*/
#define TEST 2
/*This value is chosen to be 4 because 4 doubles = 4 bytes which is a single cache line*/
#define LINE 4

typedef struct {
    double** matrix;
    short row;
    short col;
    bool valid;
} Mat;

typedef struct {
	double* arr;
	double* newArr;
    double val;
    int size;
} arr_num_pair;

typedef struct {
    double* VecA;
    double* VecB;
    double* VecSum;
    int size;
} vec_pair;

typedef struct{
    Mat* read;
    Mat* write;
    short r;
    short c;
} vec_line;

Mat* arr_constructor(short row, short col, double* arr);
Mat* zero_constructor(short row, short col);
void unravel_mat(Mat* mat);
Mat *sub_matrix(Mat *mat, short row_len, short col_len, short row_index, short col_index);
double sign(double num);
Mat* e_vector(short col, short size);
double euclid_norm(Mat* mat, bool orientation);
Mat* scal_mul(Mat* mat, double scalar);
Mat* transpose(Mat* mat);
bool same_mat(Mat* A, Mat* B);
bool equal_double(double x, double y);
Mat* add(Mat* A, Mat* B);
int p_mat(Mat* mat);
Mat* compose(Mat* A, Mat* B);


/*Multi.c functions*/
void *vec_mult(void *pair);
void* vec_add(void *pair);
void* vec_compress(void* pair);
void* line_transpose(void* line);

/*Gauss.c functions*/
Mat* elimination(Mat* mat);
Mat* backsubstitution(Mat* mat);

/*Test functions*/
bool scal_mul_tests();
bool add_tests();
bool mul_tests();
bool transpose_tests();
bool gau_tests();
bool interpreter_tests();
bool depot_tests();

/*Execution*/
void start_program();
bool run_single_line(char* line);
void run_program(FILE* file);
