%Default 7x6 board
%-1 is player 2, 0 is empty, 1 is player 1
function x = Connect4AI()    
clc; clear;
%Default initial, empty board
rowOne =   [0,0,0,0,0,0,0];
rowTwo =   [0,0,0,0,0,0,0];
rowThree = [0,0,0,0,0,0,0];
rowFour =  [0,0,0,0,0,0,0];
rowFive =  [0,0,0,0,0,0,0];
rowSix =   [0,0,0,0,0,0,0];
boardB = [rowOne;rowTwo;rowThree;rowFour;rowFive;rowSix;];
gameplayManager(boardB);
x=0;
end

function gameplay = gameplayManager(Board)
    player = 1;
    value = 0;
    fprintf('You are #. The AI is X. Play carefully');
    %First move is randomized just so it's harder to make winning
    %strategies
    randomFirstMove = randi(7);
    Board = makeAMove(Board,randomFirstMove,player);
    player = -1;
    displayBoard(Board);
    %If abs(value) > 60000, someone has won. Otherwise game ends when board
    %is full
    while(abs(value) < 60000 && ~isBoardFull(Board))
        %If AI, calls AI to depth 5
        if player == 1
            %Note that variable depth makes the best move
            Board = variableDepth(Board,player,5);
            %Displays the board after every AI move
            displayBoard(Board);
        %If player, bases it on player input
        else
            x = input('Make your move. Pick an empty column from 1-7 by typing a number\n');
            %If the input it invalid, it tells the player and gives them
            %another chance. 
            %Note: will break if the input is NaN
            while ~(x>=1 && x<=7) || (isColFull(Board,x))
                fprintf('Invalid number. Do not pick a full column or one out of range\n');
                x = input('Make your move. Pick a column from 1-7\n');
            end
            Board = makeAMove(Board, x, player);
        end
        value = evaluateBoard(Board);
        player = player * -1;
    end
    %Switches player to determine win condition. Whoever made the last move
    %is the winner, unless the board is full
    player = player * -1;
    fprintf('\n\n');
    displayBoard(Board);
    %Checks who won the game
    if(abs(value)>60000)
        if player == 1
            fprintf('\nGame over. AI Won');
        else
            fprintf('\nGame over. Human Won');
        end
    else
        fprintf('Tie Game');
    end
    gameplay = Board;
end

%Basic Algorithm: Pass in the full Board and player (as 1/-1)
%Returns the next best move looking at 0 depth
%Deprecated function but still here for testing
function gameplay = noDepth(Board,player)
    possibleMoves = [0,0,0,0,0,0,0];
    for column = 1:7
        if ~isColFull(Board,column)
            possibleMoves(column) = evaluateBoard(makeAMove(Board,column,player));
        else
            possibleMoves(column) = 0.5;
        end
    end
    gameplay = makeAMove(Board,minormax(possibleMoves,player),player);
end


%Pass in Board, which player, and how deep it should search
%Function determines the best move using a minmax algorithm
%and then makes that move
function bestBoard = variableDepth(Board,player,depth)
    if depth>0 && ~isBoardFull(Board)
        %Vector with values equals to the value of moving in each column
        possibleMoves = [0,0,0,0,0,0,0];
        for column = 1:7
            if ~isColFull(Board,column)
                %Recursively calls variableDepth and adds the best value
                %from that subtree to possibleMoves
                possibleMoves(column) = evaluateBoard(variableDepth(makeAMove(Board,column,player),player*-1,depth-1));
            else
                possibleMoves(column) = 0.5;
            end
        end
        %Sets the new board to whatever the best move was
        Board = makeAMove(Board,minormax(possibleMoves,player),player);
    end
    %At the bottom node, it just returns the board passed in
    bestBoard = Board;
end

%Moves is a vector which has the score of each potential column
%Max is 1 if the player is max or -1 if the player is min
function bestColumn = minormax(moves,max)
   %Switches to opposite values if minimizing
   moves = moves * max;
   maxIndex = 1;
   maxVal = -2980375980274359825;
   %Selects the max value
   for index = 1:length(moves)
       if (mod(moves(index),1) ==0 && moves(index)>maxVal)
           maxIndex = index;
           maxVal = moves(index);
       end
   end
   %Returns the index of the best value as the best column
   bestColumn = maxIndex;
end

%Board should be a valid board, 1<=column<=7, player = 1 or -1
%Takes in a column and a player and adds that player to the lowest open space 
%of the column
%Requires that illegal moves not be called
function newBoard = makeAMove(Board, column, player)
    %Selects the right column    
    chosenColumn = Board(:,column);
    %Increases size by one in case the bottom space is empty
    chosenColumn(end+1) = 1;
    %Finds the lowest open point on the column
    for k = 1:length(chosenColumn)
        if chosenColumn(k)~= 0
            newBoard = Board;
            newBoard(k-1,column) = player;
            break;
        end
    end
end

%Checks to see if the column is full
%board is a valid board, 1<=column<=7
function colFull = isColFull(Board,column)
    %Since a valid Board is filled bottom-up, checking the top row is
    %sufficient to determine if it's empty
    colFull = Board(1,column) ~= 0;
end

%Checks to see if the board is full
%Board is a valid board
function boardFull = isBoardFull(Board)
    boardFull = true;
    for index = 1:7
        boardFull = boardFull * isColFull(Board,index);
        if ~boardFull
            break;
        end
    end
end

%Goes through every potential group of 4 and evaluates it using the
%evaluateGroup algorithm
%Board is a valid board
function score = evaluateBoard(Board)
    score = 0;
    score = score + iterateCols(Board);
    score = score + iterateRows(Board);
    score = score + iterateDiagonals(Board);
end

%Evaluates every vertical group of 4 using the EvaluateGroup algorithm
function colScore = iterateCols(Board)
    score = 0;
    for col = [1:7]
        boardColumn = Board(:,col);
        highPoint = 0;
        lowPoint = 3;
        for rowShift = [1:3]
            score = score + evaluateGroup(boardColumn(highPoint+rowShift:lowPoint+rowShift));
        end
    end
    colScore = score;
end

%Evaluates every horizontal group of 4 using the evaluateGroup algorithm
function rowScore = iterateRows(Board)
    score = 0;
    for row = [1:6]
        boardRow = Board(row,:);
        east = 0;
        west = 3;
        for colShift = [1:4]
            score = score + evaluateGroup(boardRow(east+colShift:west+colShift));
        end
    end
    rowScore = score;
end

%Evaluates every diagonal group of 4 using the evaluateGroup algorithm
function diagonalScore = iterateDiagonals(Board)
    score = 0;
    for row = [1:3]
        %Back-leaning diagonals
        for col = [1:4]
            score = score + evaluateGroup(getBackDiagonal(row,col,Board));
        end
        %Forward-leaning diagonals
        for col2 = [7:-1:4]
            score = score + evaluateGroup(getFrontDiagonal(row,col2,Board));
        end
    end
    diagonalScore = score;
    
end

%returns a vector of size 4 that is a diagonal starting at the given
%indices
%Requirement: 0<=rowIndex<=3 and 0<=colIndex<=4, Board is a valid board
function diagonalB = getBackDiagonal(rowIndex,colIndex,Board)
    startIndexLinear = rowIndex + (colIndex-1)*6;
    diagonalB = Board([startIndexLinear:7:startIndexLinear + 21]);
end

%returns a vector of size 4 that is a diagonal starting at the given
%indices
%Requirement: 0<=rowIndex<=3 and 4<=colIndex<=7
function diagonalF = getFrontDiagonal(rowIndex,colIndex,Board)
    startIndexLinear = rowIndex + (colIndex-1)*6;
    diagonalF = Board([startIndexLinear:-5:startIndexLinear-15]);
end

%Evaluates a group of 4.
% Group is a vector of size 4
%65537 is a four-in-a-row
%Note: This leads to odd behavior in the algorithm if both players are
%close to getting a 4-in-a-row
function value = evaluateGroup(Group)
    %Prints an error message if the group isn't of size 4
    if(length(Group) ~= 4)
        fprintf("Error: Vector not of size 4");
    else
        A = Group>0;
        B = Group<0;
        value = 0;
        p1Presence = sum(A);
        p2Presence = sum(B);
        %Checks to see if the group of 4 just has player 1's pieces in it
        if(p1Presence>0 && ~(p2Presence>0))
            value = p1Presence^2;
            if(p1Presence == 4)
                value = 65537;
            end
        %Checks to see if the group of 4 just has player 2's pieces in it
        elseif(~(p1Presence>0) && p2Presence>0)
            value = -1* p2Presence^2;
            if(p2Presence == 4)
                value = -65537;
            end
        end
    end
end

%Uses the cprintf function
%Standard Board requirements apply
%Prints out a much easier-to-view board
%(Note that cprintf has a lot of unexplained behavior that does not match
%the documentation. Be wary when using. Odd results are probably the result
%of misbehavior by this function)
function void = displayBoard(Board) 
    clc;
    for i = 1:7
        fprintf('%d   ',i);
    end
    for i = 1:6
       fprintf('\n');
        for j = 1:7
            val = Board(i,j);
            %If the space is occupied by the player, it'll be colored red.
            %Otherwise it will be black
            if(val == -1)
                cprintf('*red','#   ');
            elseif(val == 1)
                fprintf('X   ');
            else
                fprintf('·   ');
            end
        end
        fprintf('\n');
    end
    void = 0;
end

