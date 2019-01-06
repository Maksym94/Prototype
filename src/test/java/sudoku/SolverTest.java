package sudoku;

import com.google.common.collect.Sets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SolverTest {

    private Solver solver = new Solver();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void assertFillVerticalHorizontalLines() throws Exception {
        int[][] result = new int[][]{
                {1, 7, 3, 2, 6, 4, 5, 1, 9,},
                {6, 9, 4, 8, 3, 5, 1, 7, 2,},
                {5, 2, 8, 9, 1, 7, 3, 6, 4,},

                {4, 5, 7, 8, 9, 3, 1, 2, 6,},
                {2, 3, 1, 4, 7, 6, 9, 5, 8,},
                {9, 8, 6, 5, 2, 1, 7, 4, 3,},

                {7, 6, 5, 3, 4, 9, 2, 8, 1,},
                {3, 4, 2, 1, 5, 8, 6, 9, 7,},
                {8, 1, 9, 7, 6, 2, 4, 3, 5,}};
        int[][] inputData = new int[][]{
                {1, 7, 3, 2, 6, 4, 5, 1, 0},
                {6, 9, 4, 8, 3, 5, 1, 7, 2,},
                {5, 2, 8, 9, 1, 7, 3, 6, 4,},

                {0, 5, 7, 8, 9, 3, 1, 2, 6,},
                {2, 3, 1, 4, 7, 6, 9, 5, 8,},
                {9, 8, 6, 5, 2, 1, 7, 4, 3,},

                {7, 6, 5, 3, 4, 0, 2, 8, 1,},
                {3, 4, 2, 1, 5, 8, 6, 9, 7,},
                {8, 1, 0, 7, 6, 2, 4, 3, 5,}};

        solver.fillNumberInRowAndColumn(inputData);

        assertTrue(Arrays.deepEquals(inputData, result));
    }

    @Test
    public void shouldThrowExceptionWhenWrongInputDataUsed() {
        int[][] inputData = {
                {0, 0, 0, 2, 0, 0, 5, 0, 9},
                {6, 0, 4, 0, 3, 5, 0, 7, 0},
                {0, 2, 8, 0, 1, 0, 0, 0, 4},

                {4, 0, 0, 0, 9, 3, 1, 0, 6},
                {2, 0, 0, 0, 7, 0, 0, 0, 8},
                {9, 0, 6, 5, 0, 0, 0, 0, 3},

                {7, 0, 0, 0, 4, 0, 2, 8, 0},
                {0, 4, 0, 1, 5, 0, 6, 0, 0},
                {8, 0, 9, 0, 0, 2, 0, 0, 5}};

        expectedException.expect(SudokuException.class);
        expectedException.expectMessage("Wrong input data. Sudoku can't be solved");

        solver.solve(inputData);
    }

    @Test
    public void shouldSolveWithThreeLevelsComplexity() {
        int[][] result = {
                {1, 5, 4, 2, 9, 3, 6, 7, 8},
                {6, 2, 7, 5, 1, 8, 3, 9, 4},
                {8, 9, 3, 4, 6, 7, 5, 1, 2},

                {7, 4, 5, 9, 2, 6, 8, 3, 1},
                {2, 6, 8, 1, 3, 4, 7, 5, 9},
                {9, 3, 1, 7, 8, 5, 2, 4, 6},

                {5, 7, 6, 8, 4, 9, 1, 2, 3},
                {3, 1, 9, 6, 5, 2, 4, 8, 7},
                {4, 8, 2, 3, 7, 1, 9, 6, 5}};
        int[][] inputData = {
                {1, 0, 4, 2, 0, 0, 0, 7, 8},
                {0, 0, 0, 5, 0, 0, 0, 0, 4},
                {0, 9, 3, 0, 0, 7, 0, 1, 0},

                {0, 0, 0, 9, 0, 0, 0, 3, 1},
                {2, 0, 8, 1, 3, 4, 0, 0, 0},
                {0, 0, 1, 0, 8, 5, 0, 4, 6},

                {0, 7, 6, 8, 4, 0, 0, 0, 0},
                {3, 0, 0, 0, 5, 2, 0, 0, 0},
                {0, 0, 2, 0, 0, 1, 9, 6, 5}};

        solver.solve(inputData);

        assertTrue(Arrays.deepEquals(inputData, result));

        result = new int[][]{
                {5, 1, 6, 2, 7, 3, 4, 8, 9},
                {7, 8, 9, 1, 5, 4, 2, 3, 6},
                {2, 3, 4, 6, 9, 8, 1, 5, 7},

                {9, 2, 7, 8, 6, 5, 3, 4, 1},
                {1, 6, 3, 7, 4, 2, 8, 9, 5},
                {4, 5, 8, 9, 3, 1, 6, 7, 2},

                {8, 9, 5, 4, 1, 6, 7, 2, 3},
                {3, 4, 1, 5, 2, 7, 9, 6, 8},
                {6, 7, 2, 3, 8, 9, 5, 1, 4}};
        inputData = new int[][]{
                {5, 1, 6, 0, 0, 3, 4, 8, 0},
                {0, 8, 0, 0, 5, 4, 2, 0, 0},
                {0, 0, 4, 0, 9, 0, 1, 0, 7},

                {9, 2, 0, 0, 0, 0, 3, 0, 1},
                {0, 6, 3, 7, 4, 0, 8, 0, 0},
                {0, 0, 8, 9, 3, 0, 6, 7, 2},

                {8, 9, 0, 0, 0, 6, 7, 2, 3},
                {0, 4, 0, 5, 0, 0, 0, 6, 8},
                {0, 0, 2, 3, 0, 0, 0, 0, 0}};

        solver.solve(inputData);

        assertTrue(Arrays.deepEquals(inputData, result));
    }

    @Test
    public void shouldSolveWithFourLevelsComplexity() {
        int[][] result = {
                {8, 9, 7, 4, 5, 1, 6, 2, 3},
                {2, 4, 1, 3, 7, 6, 5, 8, 9},
                {5, 3, 6, 8, 9, 2, 7, 1, 4},

                {4, 8, 5, 9, 6, 3, 1, 7, 2},
                {6, 1, 9, 5, 2, 7, 3, 4, 8},
                {7, 2, 3, 1, 4, 8, 9, 5, 6},

                {9, 7, 4, 6, 8, 5, 2, 3, 1},
                {1, 5, 8, 2, 3, 9, 4, 6, 7},
                {3, 6, 2, 7, 1, 4, 8, 9, 5}};
        int[][] inputData = {
                {8, 0, 7, 4, 0, 0, 0, 2, 0},
                {2, 0, 1, 0, 0, 0, 5, 0, 9},
                {0, 3, 0, 8, 0, 0, 7, 0, 0},

                {0, 0, 0, 9, 6, 3, 0, 0, 0},
                {0, 0, 9, 0, 2, 7, 0, 4, 8},
                {7, 2, 0, 0, 0, 0, 0, 0, 6},

                {0, 0, 0, 0, 0, 5, 2, 3, 0},
                {1, 5, 0, 0, 0, 9, 0, 0, 0},
                {0, 0, 0, 7, 0, 4, 8, 0, 0}};

        solver.solve(inputData);

        assertTrue(Arrays.deepEquals(inputData, result));
    }

    @Test
    public void shouldSolveWithSupportOfOccupiedNumbers() {
        int[][] result = {
                {5, 4, 6, 7, 3, 8, 9, 1, 2},
                {8, 7, 9, 4, 1, 2, 3, 5, 6},
                {1, 2, 3, 9, 5, 6, 7, 8, 4},

                {4, 8, 1, 2, 7, 3, 5, 6, 9},
                {6, 9, 5, 1, 8, 4, 2, 3, 7},
                {7, 3, 2, 5, 6, 9, 8, 4, 1},

                {9, 5, 4, 8, 2, 1, 6, 7, 3},
                {2, 6, 7, 3, 4, 5, 1, 9, 8},
                {3, 1, 8, 6, 9, 7, 4, 2, 5}};

        int[][] inputData = {
                {0, 0, 0, 7, 0, 0, 9, 1, 2},
                {8, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 3, 9, 0, 0, 0, 0, 0},

                {4, 0, 0, 0, 0, 0, 0, 0, 9},
                {6, 9, 0, 0, 0, 4, 2, 0, 7},
                {0, 0, 0, 5, 0, 0, 8, 0, 0},

                {0, 0, 0, 0, 0, 1, 0, 7, 3},
                {2, 0, 7, 3, 0, 0, 0, 0, 0},
                {0, 0, 8, 0, 0, 0, 4, 0, 0}};

        solver.solve(inputData);

        assertTrue(Arrays.deepEquals(inputData, result));
    }

    @Test
    public void shouldSolveWithFiveLevelsComplexity() {
        int[][] result = {
                {6, 9, 3, 5, 4, 7, 8, 1, 2},
                {1, 4, 7, 2, 3, 8, 6, 5, 9},
                {5, 2, 8, 9, 6, 1, 3, 7, 4},

                {4, 3, 9, 8, 5, 6, 7, 2, 1},
                {2, 6, 5, 7, 1, 9, 4, 3, 8},
                {7, 8, 1, 3, 2, 4, 9, 6, 5},

                {8, 5, 2, 4, 7, 3, 1, 9, 6},
                {9, 7, 6, 1, 8, 2, 5, 4, 3},
                {3, 1, 4, 6, 9, 5, 2, 8, 7}};
        int[][] inputData = {
                {0, 9, 0, 0, 0, 7, 0, 0, 2},
                {1, 4, 0, 0, 3, 0, 0, 0, 0},
                {5, 0, 8, 0, 0, 0, 0, 7, 0},

                {0, 3, 0, 8, 0, 0, 0, 0, 0},
                {0, 0, 5, 0, 0, 0, 4, 0, 0},
                {7, 0, 0, 0, 0, 4, 0, 0, 5},

                {0, 0, 0, 0, 7, 0, 1, 9, 0},
                {9, 0, 0, 0, 0, 2, 0, 0, 0},
                {0, 1, 0, 0, 9, 0, 0, 8, 0}};

        solver.solve(inputData);

        assertTrue(Arrays.deepEquals(inputData, result));
    }

    @Test
    public void shouldFillLocalColumnTable() {
        int[][] inputData = {
                {0, 5, 0, 0, 6, 0, 0, 0, 0},
                {0, 1, 0, 5, 7, 9, 0, 2, 0},
                {4, 0, 0, 0, 8, 0, 5, 0, 0},

                {8, 2, 1, 7, 0, 5, 0, 6, 9},
                {0, 7, 0, 0, 0, 0, 0, 0, 0},
                {0, 4, 0, 6, 2, 1, 7, 8, 0},

                {5, 6, 4, 0, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 5, 6, 0, 3, 0},
                {0, 0, 0, 0, 0, 0, 6, 5, 1}};
        Map<Integer, Set<Integer>> columnTable = new HashMap<>();
        columnTable.put(0, Sets.newHashSet(2, 3, 7, 9));
        columnTable.put(1, Sets.newHashSet(3, 6));
        columnTable.put(4, Sets.newHashSet(3, 6, 9));
        columnTable.put(5, Sets.newHashSet(3, 9));
        columnTable.put(8, Sets.newHashSet(2, 3, 7, 9));
        int columnIdx = 0;
        int[] column = solver.getColumn(columnIdx, inputData);

        Map<Integer, Set<Integer>> columnTableResult = solver.fillLocalTable(true, inputData, columnIdx, column);

        assertEquals(columnTable, columnTableResult);

        columnTable.clear();
        columnTable.put(0, Sets.newHashSet(1, 3, 4, 8, 9));
        columnTable.put(1, Sets.newHashSet(3, 4, 8));
        columnTable.put(3, Sets.newHashSet(3, 4));
        columnTable.put(4, Sets.newHashSet(1, 2, 3, 4));
        columnTable.put(6, Sets.newHashSet(2, 8, 9));
        columnTable.put(7, Sets.newHashSet(2, 4, 8, 9));
        columnIdx = 6;
        column = solver.getColumn(columnIdx, inputData);

        columnTableResult = solver.fillLocalTable(true, inputData, columnIdx, column);

        assertEquals(columnTable, columnTableResult);
    }

    @Test
    public void shouldFillLocalRowTable() {
        int[][] inputData = {
                {0, 5, 0, 0, 6, 0, 0, 0, 0},
                {0, 1, 0, 5, 7, 9, 0, 2, 0},
                {4, 0, 0, 0, 8, 0, 5, 0, 0},

                {8, 2, 1, 7, 0, 5, 0, 6, 9},
                {0, 7, 0, 0, 0, 0, 0, 0, 0},
                {0, 4, 0, 6, 2, 1, 7, 8, 0},

                {5, 6, 4, 0, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 5, 6, 0, 3, 0},
                {0, 0, 0, 0, 0, 0, 6, 5, 1}};
        Map<Integer, Set<Integer>> rowTable = new HashMap<>();
        rowTable.put(0, Sets.newHashSet(2, 3, 7, 9));
        rowTable.put(2, Sets.newHashSet(2, 3, 7, 8, 9));
        rowTable.put(3, Sets.newHashSet(1, 2, 3, 4));
        rowTable.put(5, Sets.newHashSet(2, 3, 4));
        rowTable.put(6, Sets.newHashSet(1, 3, 4, 8, 9));
        rowTable.put(7, Sets.newHashSet(1, 4, 7, 9));
        rowTable.put(8, Sets.newHashSet(3, 4, 7, 8));
        int rowIdx = 0;

        Map<Integer, Set<Integer>> columnTableResult = solver.fillLocalTable(false, inputData, rowIdx,
                inputData[rowIdx]);

        assertEquals(rowTable, columnTableResult);

        rowTable.clear();
        rowTable.put(0, Sets.newHashSet(2, 3, 7, 9));
        rowTable.put(1, Sets.newHashSet(3, 8, 9));
        rowTable.put(2, Sets.newHashSet(2, 3, 7, 8, 9));
        rowTable.put(3, Sets.newHashSet(2, 3, 4, 8, 9));
        rowTable.put(4, Sets.newHashSet(3, 4, 9));
        rowTable.put(5, Sets.newHashSet(2, 3, 4, 7, 8));
        rowIdx = 8;

        columnTableResult = solver.fillLocalTable(false, inputData, rowIdx, inputData[rowIdx]);

        assertEquals(rowTable, columnTableResult);
    }

}