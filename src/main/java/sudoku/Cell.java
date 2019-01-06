package sudoku;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cell {

    private int indexRow;
    private int indexColumn;
}
