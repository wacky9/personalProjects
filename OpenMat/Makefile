#list of all c files to be included
OBJ = main.o Core.o Wrapper.o Genesis.o Helper.o Multi.o Test.o gauss.o Interpreter.o Depot.o Func_tree.o Func_table.o Executor.o Error.o

#The executable and zip to be created
all: OpenMat

%.o: %.c lin_alg_mac.h Interpreter.h
	gcc -g -c $< -o $@

OpenMat: $(OBJ)
	gcc $^ -g -o $@

clean:
	rm -rf *.o *.gch OpenMat.exe
