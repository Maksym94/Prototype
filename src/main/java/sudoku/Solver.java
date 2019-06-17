package sudoku;

import com.google.common.primitives.Ints;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static sudoku.Type.COLUMN;
import static sudoku.Type.ROW;

@Log4j2
public class Solver {

    private static final int LINE_SIZE = 9;
    private static final int HEIGHT_SQUARE = 3;
    private static final int NUMBER_MIN = 1;
    private static final int NUMBER_MAX = 9;
    private static final int MIN_8_DIGIT_SUM = 36;
    private static final int MAX_8_DIGIT_SUM = 44;
    private static final int SQUARE_BOUNDARY = 3;
    private static final int WIDTH_SQUARE = 3;
    private static final int NUMBERS_FOR_GUESSING = 2;
    private static final String ARRAY_SIZE_INCORRECT = "Size of two dimensional array should be 9x9, but";

    //Sum of numbers in section to find which number is not exists in a row or a column;
    private static final int SUM_OF_NUMBERS_TO_MAX_ELEMENT = NUMBER_MAX * (NUMBER_MAX + 1) / 2;

    private int horizontalSquaresCount;
    private int verticalSquaresCount;

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
    }

    private static void prettyPrint(int[][] array) {
        for (int i = 0; i < array.length; i++) {
            int[] innerArray = array[i];
            for (int j = 0; j < innerArray.length; j++) {
                System.out.print(innerArray[j]);
                if (j == innerArray.length - 1) {
                    continue;
                }
                System.out.print(", ");
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
        checkArraySize(sudoku);
        if (!isCorrectValuesInArray(sudoku)) {
            System.out.println("Array of values is not in a range of 1...9");
            return;
        }
        horizontalSquaresCount = getAmountHorizontalSquares(sudoku[0]);
        verticalSquaresCount = getAmountVerticalSquares(sudoku);

        invokeBaseSolveMethods(sudoku);
        invokeAmbiguousSolveMethods(sudoku);

        checkRepeatingNumbers(sudoku);
    }

    private void invokeBaseSolveMethods(int[][] sudoku) {
        smallSquaresMethod(sudoku);
        if (hasEmptyCells(sudoku)) {
            columnRowsMethod(sudoku);
        }
        if (hasEmptyCells(sudoku)) {
            localTablesMethod(sudoku);
        }
    }

    private void invokeAmbiguousSolveMethods(int[][] sudoku) {
        if (hasEmptyCells(sudoku)) {
            guessNumberMethod(sudoku);
        }
    }

    private void checkRepeatingNumbers(int[][] sudoku) {
        if (isNumbersRepeatForSquares(sudoku) || isNumbersRepeatForLine(sudoku)) {
            prettyPrint(sudoku);
            throw new SudokuException("Wrong input data. Sudoku can't be solved");
        }
    }

    private static void checkArraySize(int[][] sudoku) {
        if (sudoku == null || sudoku.length == 0) {
            throw new SudokuException(String.format("%s array is empty", ARRAY_SIZE_INCORRECT));
        }
        if (sudoku.length != LINE_SIZE) {
            throw new SudokuException(String.format("%s actual outer array size is %d", ARRAY_SIZE_INCORRECT,
                    sudoku.length));
        }
        for (int i = 0; i < sudoku.length; i++) {
            int[] arr = sudoku[i];
            if (arr == null || arr.length == 0) {
                throw new SudokuException(String.format("%s line %d is empty", ARRAY_SIZE_INCORRECT, i));
            }
            if (arr.length != LINE_SIZE) {
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
        for (int i = 0; i < verticalSquaresCount; i++) {
            for (int j = 0; j < horizontalSquaresCount; j++) {
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

    private static boolean isNumberUniqueForSquare(int number, int indexSquareVertical,
                                                   int indexSquareHorizontal, int[][] sudoku) {
        int shiftVertical = getShiftVertical(indexSquareVertical);
        int shiftHorizontal = getShiftHorizontal(indexSquareHorizontal);
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] == number) {
                    return false;
                }
            }
        }
        return true;
    }

    private void columnRowsMethod(int[][] sudoku) {
        boolean isFoundNumberToFill = findOutToFillColumns(sudoku) | findOutNumberToFillRows(sudoku);
        if (isFoundNumberToFill) {
            smallSquaresMethod(sudoku);
        }
        if (hasEmptyCells(sudoku) && isFoundNumberToFill) {
            columnRowsMethod(sudoku);
        }
        if (!isFoundNumberToFill) {
            checkRepeatingNumbers(sudoku);
        }
    }

    private boolean findOutToFillColumns(int[][] sudoku) {
        boolean isColumnFilled = false;
        for (int i = 0; i < sudoku.length; i++) {
            int amountOfFreeNumbersInColumn = getAmountOfFreeNumbersInLine(true, sudoku, i);
            if (amountOfFreeNumbersInColumn == 0) {
                continue;
            }
            int[] numbersToCheckInColumn = new int[amountOfFreeNumbersInColumn];
            if (isFillNumbersToCheckInLine(true, sudoku, i, numbersToCheckInColumn)) {
                return false;
            }
            if (checkOutToFillNumbersInCurrentLine(true, i, numbersToCheckInColumn, sudoku)) {
                isColumnFilled = true;
            }
        }
        return isColumnFilled;
    }

    private boolean isFillNumbersToCheckInLine(boolean isColumn, int[][] sudoku, int outerIndex,
                                               int[] numbersToCheckInColumn) {
        int numbersToCheckInColumnIdx = 0;
        for (int k = NUMBER_MIN; k <= NUMBER_MAX; k++) {
            for (int j = 0; j < sudoku.length; j++) {
                if (isColumn ? sudoku[j][outerIndex] == k : sudoku[outerIndex][j] == k) {
                    break;
                }
                if (j == LINE_SIZE - 1) {
                    if (numbersToCheckInColumnIdx >= numbersToCheckInColumn.length) {
                        return true;
                    }
                    numbersToCheckInColumn[numbersToCheckInColumnIdx++] = k;
                }
            }
        }
        return false;
    }

    private int getAmountOfFreeNumbersInLine(boolean isColumn, int[][] sudoku, int outerIndex) {
        int amountOfFreeNumbersInColumn = 0;
        for (int j = 0; j < sudoku[outerIndex].length; j++) {
            if (isColumn ? sudoku[j][outerIndex] == 0 : sudoku[outerIndex][j] == 0) {
                amountOfFreeNumbersInColumn++;
            }
        }
        return amountOfFreeNumbersInColumn;
    }

    private boolean checkOutToFillNumbersInCurrentLine(boolean isColumn, int outerIndex, int[] numbersToCheck,
                                                       int[][] sudoku) {
        boolean isColumnFilled = false;
        Map<Integer, Set<Integer>> occupiedNumbersForChars = new HashMap<>();
        for (int number : numbersToCheck) {
            int countOfAvailablePositions = 0;
            int possiblePosition = -1;
            for (int i = 0; i < sudoku.length; i++) {
                if (isColumn ? sudoku[i][outerIndex] == 0 : sudoku[outerIndex][i] == 0) {
                    if (isColumn ? isNumberBusyForSquare(number, i, outerIndex, sudoku)
                            : isNumberBusyForSquare(number, outerIndex, i, sudoku)) {
                        occupiedNumbersForChars.putIfAbsent(i, new HashSet<>());
                        occupiedNumbersForChars.get(i).add(number);
                        continue;
                    }
                    for (int j = 0; j < sudoku.length; j++) {
                        if (isColumn ? sudoku[i][j] == number : sudoku[j][i] == number) {
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
                if (isColumn) {
                    sudoku[possiblePosition][outerIndex] = number;
                } else {
                    sudoku[outerIndex][possiblePosition] = number;
                }
                isColumnFilled = true;
            }
        }
        Item item = new Item(outerIndex, isColumn);
        isColumnFilled = isColumnFilled | fillUnoccupiedNumber(item, numbersToCheck, sudoku, occupiedNumbersForChars);
        return isColumnFilled;
    }

    private boolean findOutNumberToFillRows(int[][] sudoku) {
        boolean isRowFilled = false;
        for (int i = 0; i < sudoku.length; i++) {
            int amountOfFreeNumbersInRow = getAmountOfFreeNumbersInLine(false, sudoku, i);
            if (amountOfFreeNumbersInRow == 0) {
                continue;
            }
            int[] numbersToCheckInRow = new int[amountOfFreeNumbersInRow];
            if (isFillNumbersToCheckInLine(false, sudoku, i, numbersToCheckInRow)) {
                return false;
            }
            if (checkOutToFillNumbersInCurrentLine(false, i, numbersToCheckInRow, sudoku)) {
                isRowFilled = true;
            }
        }
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
                        if (item.isColumn()) {
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
        boolean isFoundNumberToFill = columnLocalTable(sudoku) | rowLocalTable(sudoku) | squareLocalTable(sudoku);
        if (isFoundNumberToFill) {
            columnRowsMethod(sudoku);
        }
        if (hasEmptyCells(sudoku) && isFoundNumberToFill) {
            localTablesMethod(sudoku);
        }
        if (!isFoundNumberToFill) {
            checkRepeatingNumbers(sudoku);
        }
    }

    private boolean rowLocalTable(int[][] sudoku) {
        boolean result = false;
        for (int i = 0; i < sudoku.length; i++) {
            int[] row = sudoku[i];
            Map<Integer, Set<Integer>> rowTable = getRowLocalTable(sudoku, i, row);
            boolean reduced = reduceLocalTable(rowTable);
            if (reduced) {
                result = result | fillFromLocalTable(ROW, sudoku, i, rowTable);
            }
        }
        return result;
    }

    private boolean columnLocalTable(int[][] sudoku) {
        boolean result = false;
        for (int i = 0; i < sudoku.length; i++) {
            int[] column = getColumn(i, sudoku);
            Map<Integer, Set<Integer>> columnTable = getColumnLocalTable(sudoku, i, column);
            boolean reduced = reduceLocalTable(columnTable);
            if (reduced) {
                result = result | fillFromLocalTable(COLUMN, sudoku, i, columnTable);
            }
        }
        return result;
    }

    private boolean squareLocalTable(int[][] sudoku) {
        boolean result = false;
        for (int i = 0; i < sudoku.length; i++) {
            Cell squarePosition = getSquarePositionFromNumber(i);
            Set<Integer> filledNumbers = getSquareFilledNumbers(squarePosition, sudoku);
            Map<Integer, Set<Integer>> table = getSquareLocalTable(sudoku, squarePosition, filledNumbers);
            boolean reduced = reduceLocalTable(table);
            if (reduced) {
                result = result | fillFromSquareLocalTable(sudoku, squarePosition, table);
            }
        }
        return result;
    }

    private boolean fillFromSquareLocalTable(int[][] sudoku, Cell squarePosition, Map<Integer, Set<Integer>> table) {
        for (Map.Entry<Integer, Set<Integer>> entry : table.entrySet()) {
            Set<Integer> row = entry.getValue();
            if (row.size() == 1) {
                Cell cellPosition = getSquarePositionFromNumber(entry.getKey());
                int shiftVertical = getShiftVertical(squarePosition.getIndexRow());
                int shiftHorizontal = getShiftHorizontal(squarePosition.getIndexColumn());
                int indexRow = shiftVertical + cellPosition.getIndexRow();
                int indexColumn = shiftHorizontal + cellPosition.getIndexColumn();
                sudoku[indexRow][indexColumn] = row.iterator().next();
                return true;
            }
        }
        return false;
    }

    Map<Integer, Set<Integer>> getSquareLocalTable(int[][] sudoku, Cell squarePosition,
                                                           Set<Integer> filledSquareNumbers) {
        int shiftVertical = getShiftVertical(squarePosition.getIndexRow());
        int shiftHorizontal = getShiftHorizontal(squarePosition.getIndexColumn());
        Map<Integer, Set<Integer>> table = new HashMap<>();
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] == 0) {
                    int[] column = getColumn(j + shiftHorizontal, sudoku);
                    Set<Integer> numbersColumn = getKnownNumbersFromArray(column);
                    Set<Integer> numbersRow = getKnownNumbersFromArray(sudoku[i + shiftVertical]);
                    for (int k = NUMBER_MIN; k <= NUMBER_MAX; k++) {
                        if (containsNumberInCollections(k, filledSquareNumbers, numbersColumn, numbersRow)) {
                            continue;
                        }
                        int positionNumber = getNumberFromSquarePosition(i, j);
                        table.putIfAbsent(positionNumber, new HashSet<>());
                        table.get(positionNumber).add(k);
                    }
                }
            }
        }
        return table;
    }

    private int getNumberFromSquarePosition(int outerIndex, int innerIndex) {
        return outerIndex * HEIGHT_SQUARE + innerIndex;
    }

    @SafeVarargs
    private final boolean containsNumberInCollections(int number, Collection<Integer>... storages) {
        for (Collection<Integer> storage : storages) {
            if (storage.contains(number)) {
                return true;
            }
        }
        return false;
    }

    private static Cell getSquarePositionFromNumber(int number) {
        return new Cell(number / HEIGHT_SQUARE, number % WIDTH_SQUARE);
    }

    private boolean fillFromLocalTable(Type type, int[][] sudoku, int index, Map<Integer, Set<Integer>> table) {
        for (Map.Entry<Integer, Set<Integer>> entry : table.entrySet()) {
            Integer tableIndex = entry.getKey();
            Set<Integer> row = entry.getValue();
            if (row.size() == 1) {
                if (type.equals(COLUMN)) {
                    sudoku[tableIndex][index] = row.iterator().next();
                } else {
                    sudoku[index][tableIndex] = row.iterator().next();
                }
                return true;
            }
        }
        return false;
    }

    private boolean reduceLocalTable(Map<Integer, Set<Integer>> table) {
        boolean result = false;
        Map<Integer, Set<Integer>> repeatedRows = getRepeatedTableRows(table);

        for (Map.Entry<Integer, Set<Integer>> entry : table.entrySet()) {
            Integer indexRow = entry.getKey();
            Set<Integer> row = entry.getValue();
            Set<Integer> repeatedRowsIndexes = repeatedRows.get(indexRow);
            if (row.size() != repeatedRowsIndexes.size()) {
                continue;
            }
            for (Map.Entry<Integer, Set<Integer>> innerEntry : table.entrySet()) {
                Integer innerRowIndex = innerEntry.getKey();
                if (!repeatedRowsIndexes.contains(innerRowIndex)) {
                    Set<Integer> innerRow = table.get(innerRowIndex);
                    for (Integer number : row) {
                        innerRow.remove(number);
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    private Map<Integer, Set<Integer>> getRepeatedTableRows(Map<Integer, Set<Integer>> table) {
        Map<Integer, Set<Integer>> repeatedRows = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : table.entrySet()) {
            Integer indexRow = entry.getKey();
            Set<Integer> row = entry.getValue();
            for (Map.Entry<Integer, Set<Integer>> innerEntry : table.entrySet()) {
                Set<Integer> innerRow = innerEntry.getValue();
                if (row.equals(innerRow)) {
                    repeatedRows.putIfAbsent(indexRow, new HashSet<>());

                    Integer innerIndexRow = innerEntry.getKey();
                    repeatedRows.get(indexRow).add(innerIndexRow);
                }
            }
        }
        return repeatedRows;
    }

    private Map<Cell, Set<Integer>> getSquaresFilledNumbers(boolean isColumn, int index, int[][] sudoku) {
        Map<Cell, Set<Integer>> squaresKnownNumbers = new HashMap<>();
        int[] prevSquareIndexes = null;
        Set<Integer> prevSquareKnownNumbers = null;
        for (int i = 0; i < sudoku.length; i++) {
            int[] squareIndexes = isColumn ? findOutSquareFromIndexes(i, index) : findOutSquareFromIndexes(index, i);
            Cell cell = getCell(isColumn, index, i);
            if (Arrays.equals(squareIndexes, prevSquareIndexes)) {
                squaresKnownNumbers.put(cell, prevSquareKnownNumbers);
            } else {
                Set<Integer> squareKnownNumbers = getSquareFilledNumbers(new Cell(squareIndexes[0], squareIndexes[1]),
                        sudoku);
                squaresKnownNumbers.put(cell, squareKnownNumbers);
                prevSquareIndexes = squareIndexes;
                prevSquareKnownNumbers = squareKnownNumbers;
            }
        }
        return squaresKnownNumbers;
    }

    private Set<Integer> getSquareFilledNumbers(Cell squarePosition, int[][] sudoku) {
        Set<Integer> squareKnownNumbers = new HashSet<>();
        int shiftVertical = getShiftVertical(squarePosition.getIndexRow());
        int shiftHorizontal = getShiftHorizontal(squarePosition.getIndexColumn());
        for (int i = 0; i < HEIGHT_SQUARE; i++) {
            for (int j = 0; j < WIDTH_SQUARE; j++) {
                if (sudoku[i + shiftVertical][j + shiftHorizontal] != 0) {
                    squareKnownNumbers.add(sudoku[i + shiftVertical][j + shiftHorizontal]);
                }
            }
        }
        return squareKnownNumbers;
    }

    Map<Integer, Set<Integer>> getColumnLocalTable(int[][] sudoku, int columnIndex, int[] array) {
        int[] lineUnknownNumbers = getUnknownNumbersFromLine(array);
        return getLocalTable(true, sudoku, columnIndex, lineUnknownNumbers);
    }

    Map<Integer, Set<Integer>> getRowLocalTable(int[][] sudoku, int rowIndex, int[] array) {
        int[] lineUnknownNumbers = getUnknownNumbersFromLine(array);
        return getLocalTable(false, sudoku, rowIndex, lineUnknownNumbers);
    }

    private Map<Integer, Set<Integer>> getLocalTable(boolean isColumn, int[][] sudoku, int outerIndex,
                                                     int[] lineUnknownNumbers) {
        Map<Integer, Set<Integer>> table = new HashMap<>();
        Map<Cell, Set<Integer>> squareKnownNumbers = getSquaresFilledNumbers(isColumn, outerIndex, sudoku);
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
                            table.putIfAbsent(j, new HashSet<>());
                            table.get(j).add(unknownNumber);
                        }
                    }
                }
            }
        }
        return table;
    }

    private void guessNumberMethod(int[][] sudoku) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < sudoku.length; i++) {
            for (int j = 0; j < sudoku[i].length; j++) {

            }
        }
    }

    private int getValueFromIndexes(boolean isColumn, int outerIndex, int innerIndex, int[][] sudoku) {
        return isColumn ? sudoku[innerIndex][outerIndex] : sudoku[outerIndex][innerIndex];
    }

    private Cell getCell(boolean isColumn, int index, int j) {
        return isColumn ? new Cell(j, index) : new Cell(index, j);
    }

    private int[] getUnknownNumbersFromLine(int[] array) {
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
        return Ints.toArray(unknownNumbers);
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

    int[] getColumn(int columnIndex, int[][] sudoku) {
        int[] columnValues = new int[sudoku.length];
        for (int i = 0; i < columnValues.length; i++) {
            columnValues[i] = sudoku[i][columnIndex];
        }
        return columnValues;
    }

    private boolean isNumberBusyForSquare(int number, int indexRow, int indexColumn, int[][] sudoku) {
        int[] indexesSquare = findOutSquareFromIndexes(indexRow, indexColumn);
        return !isNumberUniqueForSquare(number, indexesSquare[0], indexesSquare[1], sudoku);
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
            if (isOneElementInColumnEmpty && sumOfColumnElements >= MIN_8_DIGIT_SUM
                    && sumOfColumnElements <= MAX_8_DIGIT_SUM) {
                int unknownNumber = SUM_OF_NUMBERS_TO_MAX_ELEMENT - sumOfColumnElements;
                sudoku[columnIndexEmptyElement][i] = unknownNumber;
                isFilledWithNewNumber = true;
            }
        }
        return isFilledWithNewNumber;
    }

    private boolean hasEmptyCells(int[][] sudoku) {
        for (int[] innerArray : sudoku) {
            for (int value : innerArray) {
                if (value == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNumbersRepeatForLine(int[][] sudoku) {
        for (int i = 0; i < sudoku.length; i++) {
            for (int j = 0; j < sudoku[i].length; j++) {
                int val = sudoku[i][j];
                for (int k = j + 1; k < sudoku[i].length; k++) {
                    if (val != 0 && val == sudoku[i][k]) {
                        return true;
                    }
                }
                for (int k = i + 1; k < sudoku.length; k++) {
                    if (val != 0 && val == sudoku[k][j]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isNumbersRepeatForSquares(int[][] sudoku) {
        for (int m = 0; m < verticalSquaresCount; m++) {
            int shiftVertical = getShiftVertical(m);
            for (int n = 0; n < horizontalSquaresCount; n++) {
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
                            for (int p = 0; p < WIDTH_SQUARE; p++) {
                                int indexHorizontalOfComparedValue = p + shiftHorizontal;
                                if (indexVerticalOfCurrentValue == indexVerticalOfComparedValue
                                        && (indexHorizontalOfComparedValue < indexHorizontalOfCurrentValue ||
                                        indexHorizontalOfCurrentValue == indexHorizontalOfComparedValue)) {
                                    continue;
                                }
                                if (val == sudoku[indexVerticalOfComparedValue][indexHorizontalOfComparedValue]) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
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
