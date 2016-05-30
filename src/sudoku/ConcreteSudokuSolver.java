package sudoku;

import java.util.ArrayList;
import java.util.Map;

public class ConcreteSudokuSolver extends AbstractSudokuSolver {
	private static final String NL = System.lineSeparator();

	public ConcreteSudokuSolver(String dataFile, String yicesCommand, String autoYicesFile) {
		super(dataFile, yicesCommand, autoYicesFile);
	}

	@Override
	protected String generateYicesCode(int[][] board) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(";; Auto-Generated Yices Code for Solving Sudoku Problems" + NL);


		// Step 1 - Define necessary variables
		for(int i=0; i<9; i++){
			for(int j=0; j<9; j++){
				buffer.append("(define sq" + i + "" + j + "    :: int)\n");
			}
		}

		// Step 2 - Define that all positions can have value in the range [1-9]
		buffer.append("(assert (and \n");
		for(int i=0; i<9; i++){
			for(int j=0; j<9; j++){
				buffer.append("(> sq" + i + "" + j + " 0) (< sq" + i + j + " 10)\n");
			}
		}

		buffer.append("))\n");


		// Step 3: Assign known values from the partial board to the variables
		for(int i=0; i<9; i++){
			for(int j=0; j<9; j++){
				if(board[i][j] != 0){
					buffer.append("(assert (= sq" + i + "" + j + " " + board[i][j] + "))\n");
				}
			}
		}

		// Step 4 - Defines rules for the outer-most 9x9 square

        for (int i=0; i<9; i++) {
            buffer.append("(assert (and ");
            for (int j=0; j<8; j++) {
                for (int k = j+1; k< 9; k++) {
                    buffer.append("(not (= sq" + j + "" + i + " sq" + k + "" + i + ")) \n");
                }
            }
            buffer.append("))\n");
        }

        for (int i=0; i<9; i++) {
            buffer.append("(assert (and ");
            for (int j=0; j<8; j++) {
                for (int k=j+1; k<9; k++) {
                    buffer.append("(not (= sq" + i + "" + j + " sq" + i + "" + k + ")) \n");
                }
            }
            buffer.append("))\n");
        }

		// Step 5 - Define the rules for 9 inner 3x3 squares

		buffer.append("\n\n\n\n\n");
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				innerSquares(buffer, i*3, j*3);
			}
		}


		// Check and show model
		buffer.append(NL);
		buffer.append(";; Check and show the identified solution" + NL);
		buffer.append("(check)" + NL);
		buffer.append("(show-model)" + NL);

		// Step 7: Return the Yices code for use by the superclass
		return buffer.toString();
	}

	public void innerSquares(StringBuffer buf, int startingRow, int startingCol){
		ArrayList<String> sqs = new ArrayList<String>();
		for(int i=startingRow; i<startingRow+3; i++){
			for(int j=startingCol; j<startingCol+3; j++){
				sqs.add("sq"+i + "" + j);
			}
		}
		buf.append("(assert (and \n");
		for(int i=0; i<8; i++){
			for(int j=i+1; j<9; j++){
				buf.append(" (not (= " + sqs.get(i) + " " + sqs.get(j)
						+ "))\n");
			}
		}
		buf.append("))\n");
	}

	@Override
	protected void interpretResult(Map<String, String> result, int[][] board) {
		if(result == null)
			return;

		result.entrySet().stream().forEach(e -> {
			String key = e.getKey();
			String value = e.getValue();

			int x = Integer.parseInt(key.substring(2, 3)) ; // Step 1 - extract x from e.getKey(), i.e, extractX(key)
			int y = Integer.parseInt(key.substring(3,4)) ; // Step 2- extractY(key)
			int v = Integer.parseInt(value); // Step 3 - extract value, i.e, Integer.parseInt(value)

			// Step 4 - Set x,y position of board to the value returned by Yices
			board[x][y] = v;
		});
	}
}
