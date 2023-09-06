OpenMat is a multithreaded numerical linear algebra calculator written entirely in C. It uses a custom scripting language that is interpreted by a built-from-scratch interpreter. Currently the project is in the first stage. It can do basic operations with matrices and numbers and a single advanced operation: solving systems of linear equations.

The language is quite simple.
Observe an example script:

    MAT A = \[2,2,3;1,3,1]
    NUM factor = 3.0
    MAT B = SCAL_MUL(factor,A)
    OUT = A
    OUT = B
    OUT = SOLVE(A)
    OUT = SOLVE(B)

As you can see, each line is an assignment operation as values are totally immutable. There are two data types: NUM, which is a double-precision floating point type, and MAT, which is a matrix of NUMs. 

OpenMat requires *nix to run. It can be run on Windows using Cygwin or WSL. If using Cygwin, make sure to download a library to implement POSIX threads.
To run: download directory and unzip. Use the make command to make to compile. It should produce an executable called OpenMat.exe or OpenMat.out
Then run the application in the command line with the name of a script path as a command line argument.
