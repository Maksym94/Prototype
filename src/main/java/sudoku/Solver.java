package sudoku;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Solver {

    private static final int ARR_SIZE = 9;
    private static final int HEIGHT_SQUARE = 3;
    private static final int NUMBER_MIN = 1;
    private static final int NUMBER_MAX = 9;
    private static final int MIN_8_DIGIT_SUM = 36;
    private static final int MAX_8_DIGIT_SUM = 44;
    private static final int SQUARE_BOUNDARY = 3;
    private static final int WIDTH_SQUARE = 3;
    private static final String ARRAY_SIZE_INCORRECT = "Size of two dimensional array should be 9x9, but";

    //Sum of numbers in section to find which number is not exists in a row or a column;
    private static final int SUM_OF_NUMBERS_TO_MAX_ELEMENT = NUMBER_MAX * (NUMBER_MAX + 1) / 2;

    private int amountOfHorizontalSquares;
    private int amountOfVerticalSquares;

    public static void main(String[] args) {
        int[][] array = {
                {0, 0, 0,   0, 0, 0,   0, 0, 0},
                {0, 1, 0,   0, 7, 9,   0, 2, 0},
                {4, 0, 0,   0, 8, 0,   5, 0, 0},

                {0, 2, 1,   0, 0, 0,   0, 0, 9},
                {0, 7, 0,   0, 0, 0,   0, 0, 0},
                {0, 0, 0,   6, 2, 0,   7, 8, 0},

                {5, 6, 4,   0, 0, 0,   0, 0, 0},
                {0, 0, 0,   0, 5, 0,   0, 3, 0},
                {0, 0, 0,   0, 0, 0,   6, 0, 1}};

        new Solver().solve(array);
        prettyPrint(array);
        new Solver().localTablesMethod(array);
    }

    private static void prettyPrint(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            int[] innerArray = array[i];
            for (int j = 0; j < innerArray.length; j++) {
                System.out.print(array[i][j] + ", ");
                if ((j + 1) % SQUARE_BOUNDARY == 0) {
                    System.out.print("  ");
                }
            }
            System.out.println();
            if ((i + 1) % SQUARE_BOUNDARY == 0) {
                System.out.println();
            }
        }
    }

    void solve(int[][] sudoku) {
        checkCorrectArraySize(sudoku);
        if (!isCorrectValuesInArray(sudoku)) {
            System.out.println("Array of values is not in a range of 1...9");
            return;
        }
        amountOfHorizontalSquares = getAmountHorizontalSquares(sudoku[0]);
        amountOfVerticalSquares = getAmountVerticalSquares(sudoku);

        smallSquaresMethod(sudoku);
        if (!isSudokuFilled(sudoku)) {
            columnRowsMethod(sudoku);
        }
        if (!isNumbersUniqueForAllSquares(sudoku) || !isNumbersUniqueForLine(sudoku)) {
            prettyPrint(sudoku);
            throw new SudokuException("Wrong input data. Sudoku can't be solved");
        }
    }

    private static void checkCorrectArraySize(int[][] sudoku) {
        if (sudoku == null || sudoku.length == 0) {
            throw new SudokuException(String.format("%s array is empty", ARRAY_SIZE_INCORRECT));
        }
        if (sudoku.length != ARR_SIZE) {
            throw new SudokuException(String.format("%s actual outer array size is %d", ARRAY_SIZE_INCORRECT,
                    sudoku.length));
        }
        for (int i = 0; i < sudoku.length; i++) {
            int[] arr = sudoku[i];
            if (arr == null || arr.length == 0) {
                throw new SudokuException(String.format("%s line %d is empty", ARRAY_SIZE_INCORRECT, i));
            }
            if (arr.length != ARR_SIZE) {
                throw new SudokuException(String.format("line %d has size %d", i, arr.length));
            }
        }
    }

    private static boolean isCorrectValuesInArray(int[][] sudoku) {
        for (int[] line : sudoku) {
            for (int val : line) {
                if (val < NUMBER_MIN - 1 || val > NUMBER_MAX) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int getAmountVerticalSquares(int[][] sudoku) {
        return sudoku.length / HEIGHT_SQUARE;
    }

    private static int getAmountHorizontalSquares(int[] ints) {
        return ints.length / WIDTH_SQUARE;
    }

    private void smallSquaresMethod(int[][] sudoku) {
        for (int i = 0; i < amountOfVerticalSquares; i++) {
            for (int j = 0; j < amountOfHorizontalSquares; j++) {
                int amountFreeCells = getAmountOfFreeCellsInSquare(i, j, sudoku);
                if (amountFreeCells == 0) {
                    continue;
                }
                for (int k = NUMBER_MIN; k <= NUMBER_MAX; k++) {
                    boolean isUniqueNumber = isNumberUniqueForSquare(k, i, j, sudoku);
                    if (isUniqueNumber) {
                        fillSquareWithNumber(k, i, j, amountFreeCells, sudoku);
                    }
                }
            }
        }
    }

    private void fillSquareWithNumber(int number, int indexSquareVertical,
                                      int indexSquareHorizontal, int amountFreeCells, int[][] sudoku) {
        if (amountFreeCells == 1) {
            fill(number, indexSquareVertical, indexSquareHorizontal, sudoku);
            smallSquaresMethod(sudoku);
        }
        boolean[][] smallSquareWithPossiblePositionsOfNumber = new boolean[HEIGHT_SQUARE][WIDTH_SQUARE];
        int amountPossiblePositions = findOutPossiblePositions(number, indexSquareVertical, indexSquareHorizontal,
                smallSquareWithPossiblePositionsOfNumber, sudoku);
        if (amountPossiblePositions == 1) {
            fillWithConfidentPosition(number, indexSquareVertical, indexSquareHorizontal,
                    smallSquareWithPossiblePositionsOfNumber, sudoku);
            smallSquaresMethod(sudoku);
        }
        if (fillNumberInRowAndColumn(sudoku)) {
            smallSquaresMethod(sudoku);
        }
    }

    private int getAmountOfFreeCellsInSquare(int indexSquareVertical, int indexSquareHorizontal, int[][] sudoku) {
        int count = 0;
        int shiftVertical = getShiftVertical(indexSquareVertical);
        int shiftHorizontal = getShiftHorizontal(indexSquareHorizontal);

        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int getShiftHorizontal(int indexSquareHorizontal) {
        return indexSquareHorizontal * WIDTH_SQUARE;
    }

    private static int getShiftVertical(int indexSquareVertical) {
        return indexSquareVertical * HEIGHT_SQUARE;
    }

    /**
     * @return flag of free number for current square
     */
    private static boolean isNumberUniqueForSquare(int number, int indexSquareVertical,
                                                   int indexSquareHorizontal, int[][] sudoku) {
        int shiftVertical = getShiftVertical(indexSquareVertical);
        int shiftHorizontal = getShiftHorizontal(indexSquareHorizontal);

        for (int j = 0; j < HEIGHT_SQUARE; j++) {
            for (int k = 0; k < WIDTH_SQUARE; k++) {
                if (sudoku[j + shiftVertical][k + shiftHorizontal] == number) {
                    return false;
                } else if (j == HEIGHT_SQUARE - 1 && k == WIDTH_SQUARE - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private void columnRowsMethod(int[][] sudoku) {
        boolean isFindNumberWhichToFill = findOutToFillColumns(sudoku) | findOutNumberToFillRows(sudoku);
        if (isFindNumberWhichToFill) {
            smallSquaresMethod(sudoku);
        }
        if (!isSudokuFilled(sudoku) && isFindNumberWhichToFill) {
            columnRowsMethod(sudoku);
        }
        if (!isFindNumberWhichToFill) {
            if (!isNumbersUniqueForAllSquares(sudoku) || !isNumbersUniqueForLine(sudoku)) {
                prettyPrint(sudoku);
                throw new SudokuException("Wrong input data. Sudoku can't be solved");
            }
        }
    }

    private boolean findOutToFillColumns(int[][] sudoku) {
        boolean isColumnFilled = false;
        for (int i = 0; i < sudoku.length; i++) {
            int amountOfFreeNumbersInColumn = 0;
            for (int j = 0; j < sudoku[i].length; j++) {
                if (sudoku[j][i] == 0) {
                    amountOfFreeNumbersInColumn++;
                }
            }
            int[] numbersToCheckInColumn = new int[amountOfFreeNumbersInColumn];
            int numbersToCheckInColumnIdx = 0;
            for (int k = NUMBER_MIN; k <= NUMBER_MAX; k++) {
                for (int l = 0; l < sudoku[i].length; l++) {
                    if (sudoku[l][i] == k) {
                        break;
                    }
                    if (l == sudoku[i].length - 1) {
                        if (numbersToCheckInColumnIdx >= amountOfFreeNumbersInColumn) {
                            return false;
                        }
                        numbersToCheckInColumn[numbersToCheckInColumnIdx++] = k;
                    }
                }
            }
            if (checkOutToFillNumbersInCurrentColumn(i, numbersToCheckInColumn, sudoku)) {
                isColumnFilled = true;
            }
        }
        return isColumnFilled;
    }

    private boolean checkOutToFillNumbersInCurrentColumn(int indexColumn, int[] numbersToCheckInColumn, int[][] sudoku) {
        boolean isColumnFilled = false;
        Map<Integer, Set<Integer>> occupiedNumbersForChars = new HashMap<>();
        for (int number : numbersToCheckInColumn) {
            int countOfAvailablePositions = 0;
            int possiblePosition = -1;
            for (int i = 0; i < sudoku.length; i++) {
                if (sudoku[i][indexColumn] == 0) {
                    if (!isNumberFreeForSquare(number, i, indexColumn, sudoku)) {
                        occupiedNumbersForChars.putIfAbsent(i, new HashSet<>());
                        occupiedNumbersForChars.get(i).add(number);
                        continue;
                    }
                    for (int j = 0; j < sudoku.length; j++) {
                        if (sudoku[i][j] == number) {
                            occupiedNumbersForChars.putIfAbsent(i, new HashSet<>());
                            occupiedNumbersForChars.get(i).add(number);
                            break;
                        }
                        if (j == sudoku.length - 1) {
                            countOfAvailablePositions++;
                            possiblePosition = i;
                        }
                    }
                }
            }
            if (countOfAvailablePositions == 1) {
                System.out.println("You filled number " + number + " in position " + possiblePosition + ":"
                        + indexColumn);
                sudoku[possiblePosition][indexColumn] = number;
                isColumnFilled = true;
            }
        }
        Item item = new Item(indexColumn, true);
        isColumnFilled = isColumnFilled | fillUnoccupiedNumber(item, numbersToCheckInColumn, sudoku,
                occupiedNumbersForChars);
        return isColumnFilled;
    }

    private boolean findOutNumberToFillRows(int[][] sudoku) {
        boolean isRowFilled = false;
        for (int i = 0; i < sudoku.length; i++) {
            int amountOfFreeNumbersInRow = 0;
            for (int j = 0; j < sudoku[i].length; j++) {
                if (sudoku[i][j] == 0) {
                    amountOfFreeNumbersInRow++;
                }
            }
            if (amountOfFreeNumbersInRow == 0) {
                continue;
            }
            int[] numbersToCheckInRow = new int[amountOfFreeNumbersInRow];
            int numbersToCheckInRowIdx = 0;
            for (int k = NUMBER_MIN; k <= NUMBER_MAX; k++) {
                for (int l = 0; l < sudoku[i].length; l++) {
                    if (sudoku[i][l] == k) {
                        break;
                    }
                    if (l == sudoku[i].length - 1) {
                        if (numbersToCheckInRowIdx >= amountOfFreeNumbersInRow) {
                            return false;
                        }
                        numbersToCheckInRow[numbersToCheckInRowIdx++] = k;
                    }
                }
            }
            if (checkOutToFillNumbersInCurrentRow(i, numbersToCheckInRow, sudoku)) {
                isRowFilled = true;
            }
        }
        return isRowFilled;
    }

    private boolean checkOutToFillNumbersInCurrentRow(int indexRow, int[] numbersToCheckInRow, int[][] sudoku) {
        boolean isRowFilled = false;
        Map<Integer, Set<Integer>> occupiedNumbersForChars = new HashMap<>();
        for (int number : numbersToCheckInRow) {
            int countOfAvailablePositions = 0;
            int possiblePosition = -1;
            for (int i = 0; i < sudoku.length; i++) {
                if (sudoku[indexRow][i] == 0) {
                    if (!isNumberFreeForSquare(number, indexRow, i, sudoku)) {
                        occupiedNumbersForChars.putIfAbsent(i, new HashSet<>());
                        occupiedNumbersForChars.get(i).add(number);
                        continue;
                    }
                    for (int j = 0; j < sudoku.length; j++) {
                        if (sudoku[j][i] == number) {
                            occupiedNumbersForChars.putIfAbsent(i, new HashSet<>());
                            occupiedNumbersForChars.get(i).add(number);
                            break;
                        }
                        if (j == sudoku.length - 1) {
                            countOfAvailablePositions++;
                            possiblePosition = i;
                        }
                    }
                }
            }
            if (countOfAvailablePositions == 1) {
                System.out.println("You filled number " + number + " in position " + indexRow + ":" + possiblePosition);
                sudoku[indexRow][possiblePosition] = number;
                isRowFilled = true;
            }
        }
        Item item = new Item(indexRow, false);
        isRowFilled = isRowFilled | fillUnoccupiedNumber(item, numbersToCheckInRow, sudoku, occupiedNumbersForChars);
        return isRowFilled;
    }

    private boolean fillUnoccupiedNumber(Item item, int[] numbersToCheckInRow, int[][] sudoku,
                                         Map<Integer, Set<Integer>> occupiedNumbersForChars) {
        boolean isFilled = false;
        for (Map.Entry<Integer, Set<Integer>> entry : occupiedNumbersForChars.entrySet()) {
            int index = entry.getKey();
            Set<Integer> numbers = entry.getValue();
            if (numbers.size() == numbersToCheckInRow.length - 1) {
                for (int possibleNumber : numbersToCheckInRow) {
                    if (!numbers.contains(possibleNumber)) {
                        if(item.isColumn()){
                            sudoku[index][item.getIndex()] = possibleNumber;
                        } else {
                            sudoku[item.getIndex()][index] = possibleNumber;
                        }
                        isFilled = true;
                    }
                }
            }
        }
        return isFilled;
    }

    private void localTablesMethod(int[][] sudoku) {
        boolean result = columnLocalTable(sudoku);
        System.out.println("------------------------------");
        result = result | rowLocalTable(sudoku);
    }

    private boolean rowLocalTable(int[][] sudoku) {
        boolean result = false;
        for (int i = 0; i <sudoku.length; i++) {
            int[] row = sudoku[i];
            /*Set<Integer> rowUnknownNumbers = getUnknownNumbersFromArray(row);
            Map<Cell, Set<Integer>> squareKnownNumbers = getSquaresKnownNumbers(false, i, sudoku);*/
            Map<Integer, Set<Integer>> columnTable = fillLocalTable(false, sudoku, i, row);
            for (Map.Entry<Integer, Set<Integer>> entry:columnTable.entrySet()) {
                System.out.println(String.format("%d:%d, values: %s",entry.getKey(),i, entry.getValue()));
            }
            System.out.println();
        }
        return result;
    }

    private boolean columnLocalTable(int[][] sudoku) {
        boolean result = false;
        for (int i = 0; i < sudoku.length; i++) {
            int[] column = getColumn(i, sudoku);
            //Set<Integer> columnKnownNumbers = getKnownNumbersFromArray(column);
            Map<Integer, Set<Integer>> columnTable = fillLocalTable(true, sudoku, i, column);
            result = reduceLocalTableAndFillCell(true, sudoku, i, columnTable);
            for (Map.Entry<Integer, Set<Integer>> entry:columnTable.entrySet()) {
                System.out.println(String.format("%d:%d, values: %s",entry.getKey(),i, entry.getValue()));
            }
            System.out.println();
        }
        return result;
    }

    private boolean reduceLocalTableAndFillCell(boolean isColumn, int[][] sudoku, int index,
                                                Map<Integer, Set<Integer>> localTable) {
        boolean result = false;
        Map<Integer, Integer> numbersLength = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry: localTable.entrySet()) {
            int cellPossibleNumbersSize = entry.getValue().size();
            int repeats = numbersLength.getOrDefault(cellPossibleNumbersSize, 0);
            numbersLength.put(cellPossibleNumbersSize, ++repeats);
        }
        for (Map.Entry<Integer, Integer> entry: numbersLength.entrySet()) {
            int numberLength = entry.getKey();
            int numberRepeats = entry.getKey();
        }
        return result;
    }

    private Map<Cell, Set<Integer>> getSquaresKnownNumbers(boolean isColumn, int index, int[][] sudoku) {
        Map<Cell, Set<Integer>> squaresKnownNumbers = new HashMap<>();
        int[] prevSquareIndexes = null;
        Set<Integer> prevSquareKnownNumbers = null;
        for (int i = 0; i < sudoku.length; i++) {
            int[] squareIndexes = isColumn ? findOutSquareFromIndexes(i, index) : findOutSquareFromIndexes(index, i);
            Cell cell = getCell(isColumn, index, i);
            if(Arrays.equals(squareIndexes, prevSquareIndexes)) {
                squaresKnownNumbers.put(cell, prevSquareKnownNumbers);
            } else {
                Set<Integer> squareKnownNumbers = getKnownNumbersFromSquare(squareIndexes, sudoku);
                squaresKnownNumbers.put(cell, squareKnownNumbers);
                prevSquareIndexes = squareIndexes;
                prevSquareKnownNumbers = squareKnownNumbers;
            }
        }
        return squaresKnownNumbers;
    }

    private Set<Integer> getKnownNumbersFromSquare(int[] squareIndexes, int[][] sudoku) {
        Set<Integer> squareKnownNumbers = new HashSet<>();
        int shiftVertical = getShiftVertical(squareIndexes[0]);
        int shiftHorizontal = getShiftHorizontal(squareIndexes[1]);
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] != 0) {
                    squareKnownNumbers.add(sudoku[i + shiftVertical][j + shiftHorizontal]);
                }
            }
        }
        return squareKnownNumbers;
    }

    Map<Integer, Set<Integer>> fillLocalTable(boolean isColumn, int[][] sudoku, int outerIndex, int[] array) {
        Map<Integer, Set<Integer>> columnTable = new HashMap<>();
        Set<Integer> lineUnknownNumbers = getUnknownNumbersFromArray(array);
        System.out.println("Free numbers: "+ lineUnknownNumbers);
        Map<Cell, Set<Integer>> squareKnownNumbers = getSquaresKnownNumbers(isColumn, outerIndex, sudoku);
        for (int j = 0; j < sudoku.length; j++) {
            for (int unknownNumber : lineUnknownNumbers) {
                Cell cell = getCell(isColumn, outerIndex, j);
                if (getValueFromIndexes(isColumn, outerIndex, j, sudoku) == 0 &&
                        !squareKnownNumbers.get(cell).contains(unknownNumber)) {
                    for (int k = 0; k < sudoku[outerIndex].length; k++) {
                        if (getValueFromIndexes(!isColumn, j, k, sudoku) == unknownNumber) {
                            break;
                        }
                        if (k == sudoku[outerIndex].length - 1) {
                            columnTable.putIfAbsent(j, new HashSet<>());
                            columnTable.get(j).add(unknownNumber);
                        }
                    }
                }
            }
        }
        return columnTable;
    }

    private int getValueFromIndexes(boolean isColumn, int outerIndex, int innerIndex, int[][] sudoku) {
        return isColumn ? sudoku[innerIndex][outerIndex] : sudoku[outerIndex][innerIndex];
    }

    private Cell getCell(boolean isColumn, int index, int j) {
        return isColumn ? new Cell(j, index) : new Cell(index, j);
    }

    private Set<Integer> getUnknownNumbersFromArray(int[] array) {
        Set<Integer> unknownNumbers = new HashSet<>();
        for (int i = NUMBER_MIN; i <= NUMBER_MAX; i++) {
            for (int j = 0; j < array.length; j++) {
                if (i == array[j]) {
                    break;
                }
                if (j == array.length - 1) {
                    unknownNumbers.add(i);
                }
            }
        }
        return unknownNumbers;
    }

    private Set<Integer> getKnownNumbersFromArray(int[] array) {
        Set<Integer> knownNumbers = new HashSet<>();
        for (int number : array) {
            if (number != 0) {
                knownNumbers.add(number);
            }
        }
        return knownNumbers;
    }

    private Map<Integer, Integer> getKnownNumbersFromArraySkipEmptyCells(int[] array) {
        HashMap<Integer, Integer> knownNumbers = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            int number = array[i];
            if (number != 0) {
                knownNumbers.put(i, number);
            }
        }
        return knownNumbers;
    }

    int[] getColumn(int columnIndex, int[][] sudoku) {
        int[] columnValues = new int[sudoku.length];
        for (int i = 0; i < columnValues.length; i++) {
            columnValues[i] = sudoku[i][columnIndex];
        }
        return columnValues;
    }

    private boolean isNumberFreeForSquare(int number, int indexRow, int indexColumn, int[][] sudoku) {
        int[] indexesSquare = findOutSquareFromIndexes(indexRow, indexColumn);
        return isNumberUniqueForSquare(number, indexesSquare[0], indexesSquare[1], sudoku);
    }

    private int[] findOutSquareFromIndexes(int indexRow, int indexColumn) {
        int[] indexesSquare = new int[2];
        indexesSquare[0] = indexRow / HEIGHT_SQUARE;
        indexesSquare[1] = indexColumn / WIDTH_SQUARE;
        return indexesSquare;
    }

    boolean fillNumberInRowAndColumn(int[][] sudoku) {
        boolean isFilledWithNewNumber = false;
        for (int i = 0; i < sudoku.length; i++) {
            boolean isOneElementInRowEmpty = false;
            boolean isOneElementInColumnEmpty = false;
            int rowIndexEmptyElement = -1;
            int columnIndexEmptyElement = -1;
            int sumOfRowElements = 0;
            int sumOfColumnElements = 0;
            for (int j = 0; j < sudoku[i].length; j++) {
                if (sudoku[i][j] == 0 && !isOneElementInRowEmpty) {
                    isOneElementInRowEmpty = true;
                    rowIndexEmptyElement = j;
                } else if (sudoku[i][j] == 0 && isOneElementInRowEmpty) {
                    isOneElementInRowEmpty = false;
                    break;
                }
                sumOfRowElements += sudoku[i][j];
            }
            for (int j = 0; j < sudoku[i].length; j++) {
                if (sudoku[j][i] == 0 && !isOneElementInColumnEmpty) {
                    isOneElementInColumnEmpty = true;
                    columnIndexEmptyElement = j;
                } else if (sudoku[j][i] == 0 && isOneElementInColumnEmpty) {
                    isOneElementInColumnEmpty = false;
                    break;
                }
                sumOfColumnElements += sudoku[j][i];
            }
            if (isOneElementInRowEmpty && sumOfRowElements >= MIN_8_DIGIT_SUM && sumOfRowElements <= MAX_8_DIGIT_SUM) {
                int unknownNumber = SUM_OF_NUMBERS_TO_MAX_ELEMENT - sumOfRowElements;
                sudoku[i][rowIndexEmptyElement] = unknownNumber;
                isFilledWithNewNumber = true;
            }
            if (isOneElementInColumnEmpty && sumOfColumnElements >= MIN_8_DIGIT_SUM &&
                    sumOfColumnElements <= MAX_8_DIGIT_SUM) {
                int unknownNumber = SUM_OF_NUMBERS_TO_MAX_ELEMENT - sumOfColumnElements;
                sudoku[columnIndexEmptyElement][i] = unknownNumber;
                isFilledWithNewNumber = true;
            }
        }
        return isFilledWithNewNumber;
    }

    private boolean isSudokuFilled(int[][] sudoku) {
        for (int[] innerArray : sudoku) {
            for (int value : innerArray) {
                if (value == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isNumbersUniqueForLine(int[][] sudoku) {
        for (int i = 0; i < sudoku.length; i++) {
            for (int j = 0; j < sudoku[i].length; j++) {
                int val = sudoku[i][j];
                for (int k = j + 1; k < sudoku[i].length; k++) {
                    if (val != 0 && val == sudoku[i][k]) {
                        return false;
                    }
                }
                for (int k = i + 1; k < sudoku.length; k++) {
                    if (val != 0 && val == sudoku[k][j]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isNumbersUniqueForAllSquares(int[][] sudoku) {
        for (int m = 0; m < amountOfVerticalSquares; m++) {
            int shiftVertical = getShiftVertical(m);
            for (int n = 0; n < amountOfHorizontalSquares; n++) {
                int shiftHorizontal = getShiftHorizontal(n);
                for (int i = 0; i < HEIGHT_SQUARE; i++) {
                    int indexVerticalOfCurrentValue = i + shiftVertical;
                    for (int j = 0; j < WIDTH_SQUARE; j++) {
                        int indexHorizontalOfCurrentValue = j + shiftHorizontal;
                        int val = sudoku[indexVerticalOfCurrentValue][indexHorizontalOfCurrentValue];
                        if (val == 0) {
                            continue;
                        }
                        for (int k = i; k < HEIGHT_SQUARE; k++) {
                            int indexVerticalOfComparedValue = k + shiftVertical;
                            for (int l = 0; l < WIDTH_SQUARE; l++) {
                                int indexHorizontalOfComparedValue = l + shiftHorizontal;
                                if (indexVerticalOfCurrentValue == indexVerticalOfComparedValue
                                        && (indexHorizontalOfComparedValue < indexHorizontalOfCurrentValue ||
                                        indexHorizontalOfCurrentValue == indexHorizontalOfComparedValue)) {
                                    continue;
                                }
                                if (val == sudoku[indexVerticalOfComparedValue][indexHorizontalOfComparedValue]) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private int findOutPossiblePositions(int number, int indexSquareVertical, int indexSquareHorizontal,
                                         boolean[][] smallSquareWithPossiblePositionsOfNumber, int[][] sudoku) {
        int countPositions = 0;
        int shiftVertical = getShiftVertical(indexSquareVertical);
        int shiftHorizontal = getShiftHorizontal(indexSquareHorizontal);
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            nextPosition:
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] == 0) {
                    int possibleNumberPositionVertical = i + shiftVertical;
                    int possibleNumberPositionHorizontal = j + shiftHorizontal;
                    for (int k = 0; k < sudoku.length; k++) {
                        if (sudoku[k][possibleNumberPositionHorizontal] == number ||
                                sudoku[possibleNumberPositionVertical][k] == number) {
                            continue nextPosition;
                        }
                        if (k == sudoku.length - 1) {
                            smallSquareWithPossiblePositionsOfNumber[i][j] = true;
                            countPositions++;
                        }
                    }
                }
            }
        }

        return countPositions;
    }

    private void fill(int number, int indexSquareVertical, int indexSquareHorizontal, int[][] sudoku) {
        int shiftVertical = getShiftVertical(indexSquareVertical);
        int shiftHorizontal = getShiftHorizontal(indexSquareHorizontal);
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] == 0) {
                    sudoku[i + shiftVertical][j + shiftHorizontal] = number;
                    break;
                }
            }
        }
    }

    private void fillWithConfidentPosition(int number, int indexSquareVertical, int indexSquareHorizontal,
                                           boolean[][] smallSquareWithPossiblePositionsOfNumber, int[][] sudoku) {
        int shiftVertical = getShiftVertical(indexSquareVertical);
        int shiftHorizontal = getShiftHorizontal(indexSquareHorizontal);
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (smallSquareWithPossiblePositionsOfNumber[i][j]) {
                    sudoku[i + shiftVertical][j + shiftHorizontal] = number;
                    break;
                }
            }
        }
    }

}
