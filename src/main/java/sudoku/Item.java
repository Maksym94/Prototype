package sudoku;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Item {

    private int index;
    private int sum;
    private boolean column;

    public Item(int index, boolean column) {
        this.index = index;
        this.column = column;
    }
}
