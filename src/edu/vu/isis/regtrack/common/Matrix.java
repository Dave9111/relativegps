/*
 * Copyright (C) 2014 Will Hedgecock
 * This file is part of RegTrack: A Relative GPS Tracking Solution
 * 
 * RegTrack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * RegTrack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with RegTrack.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.vu.isis.regtrack.common;

public final class Matrix
{
	private double[][] data;
	private int numRows, numCols;
	
	// All values are initialized to zero by default
	public Matrix(int rows)
	{
		data = new double[rows][1];
		numRows = rows;
		numCols = 1;
	}
	public Matrix(int rows, int cols)
	{
		data = new double[rows][cols];
		numRows = rows;
		numCols = cols;
	}
	
	public Matrix(int rows, double value)
	{
		data = new double[rows][1];
		numRows = rows;
		numCols = 1;
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = value;
	}
	public Matrix(int rows, int cols, double value)
	{
		data = new double[rows][cols];
		numRows = rows;
		numCols = cols;
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = value;
	}
	
	public Matrix(final Matrix toCopy)
	{
		data = new double[toCopy.numRows][toCopy.numCols];
		numRows = toCopy.numRows;
		numCols = toCopy.numCols;
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = toCopy.data[i][j];
	}
	
	public int numRows() { return numRows; }
	public int numCols() { return numCols; }
	
	// Dynamically change the size of the matrix
	//  Destroys any contents currently in the matrix
	public void setSize(int rows, int cols)
	{
		// Reinitialize memory
		numRows = rows;
		numCols = cols;
		data = new double[rows][cols];
		
		for (int i = 0; i < rows; ++i)
			for (int j = 0; j < cols; ++j)
				data[i][j] = 0.0;
	}
	
	// Dynamically change the size of the matrix
	//  Retains contents currently in the matrix
	public void resize(int rows, int cols)
	{
		double[][] newData = new double[rows][cols];
		for (int i = 0; i < rows; ++i)
			for (int j = 0; j < cols; ++j)
				newData[i][j] = ((i < numRows) && (j < numCols)) ? data[i][j] : 0.0;
				
		// Set data pointer to new data
		numRows = rows;
		numCols = cols;
		data = newData;
	}
	
	// Fill matrix with the specified values
	public void setValues(double ... args)
	{
		int index = 0;
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				if (index != args.length)
					data[i][j] = args[index++];
				else
					return;
	}
	public void setValues(final Matrix toCopy)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = toCopy.data[i][j];
	}
	
	// Sets all values to 0
	public void clearMatrix()
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = 0.0;
	}
	
	public void setZeroMatrix()
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = 0.0;
	}
	
	public void setIdentityMatrix()
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = (i == j) ? 1.0 : 0.0;
	}
	
	public void setValueMatrix(double value)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = value;
	}
	
	// Return value at specified position
	public double valueAt(int row, int col)
	{
		return data[row][col];
	}
	public double valueAt(int row)
	{
		return data[row][0];
	}
	
	// Set value at specified position
	public void setValueAt(int row, int col, double value)
	{
		data[row][col] = value;
	}
	public void setValueAt(int row, double value)
	{
		data[row][0] = value;
	}
	
	// Return submatrices given the specified indices
	public Matrix getSubmatrix(int rowStartIndex, int rowEndIndex, int colStartIndex, int colEndIndex)
	{
		Matrix resultMatrix = new Matrix(rowEndIndex-rowStartIndex+1, colEndIndex-colStartIndex+1);
		
		for (int i = rowStartIndex, currRow = 0; i <= rowEndIndex; ++i, ++currRow)
			for (int j = colStartIndex, currCol = 0; j <= colEndIndex; ++j, ++currCol)
				resultMatrix.data[currRow][currCol] = data[i][j];
		
		return resultMatrix;
	}
	public Matrix getSubmatrix(int[] rowIndices, int[] colIndices)
	{
		Matrix resultMatrix = new Matrix(rowIndices.length, colIndices.length);
		
		for (int i = 0; i < rowIndices.length; ++i)
			for (int j = 0; j < colIndices.length; ++j)
				resultMatrix.data[i][j] = data[rowIndices[i]][colIndices[j]];
		
		return resultMatrix;
	}
	public Matrix getSubmatrix(int rowStartIndex, int rowEndIndex, int[] colIndices)
	{
		Matrix resultMatrix = new Matrix(rowEndIndex-rowStartIndex+1, colIndices.length);
		
		for (int i = rowStartIndex, currRow = 0; i <= rowEndIndex; ++i, ++currRow)
			for (int j = 0; j < colIndices.length; ++j)
				resultMatrix.data[currRow][j] = data[i][colIndices[j]];
		
		return resultMatrix;
	}
	public Matrix getSubmatrix(int[] rowIndices, int colStartIndex, int colEndIndex)
	{
		Matrix resultMatrix = new Matrix(rowIndices.length, colEndIndex-colStartIndex+1);
		
		for (int i = 0; i < rowIndices.length; ++i)
			for (int j = colStartIndex, currCol = 0; j <= colEndIndex; ++j, ++currCol)
				resultMatrix.data[i][currCol] = data[rowIndices[i]][j];
		
		return resultMatrix;
	}
	public void getSubmatrix(Matrix resultMatrix, int rowStartIndex, int rowEndIndex, int colStartIndex, int colEndIndex)
	{
		for (int i = rowStartIndex, currRow = 0; i <= rowEndIndex; ++i, ++currRow)
			for (int j = colStartIndex, currCol = 0; j <= colEndIndex; ++j, ++currCol)
				resultMatrix.data[currRow][currCol] = data[i][j];
	}
	public void getSubmatrix(Matrix resultMatrix, int[] rowIndices, int[] colIndices)
	{
		for (int i = 0; i < rowIndices.length; ++i)
			for (int j = 0; j < colIndices.length; ++j)
				resultMatrix.data[i][j] = data[rowIndices[i]][colIndices[j]];
	}
	public void getSubmatrix(Matrix resultMatrix, int rowStartIndex, int rowEndIndex, int[] colIndices)
	{
		for (int i = rowStartIndex, currRow = 0; i <= rowEndIndex; ++i, ++currRow)
			for (int j = 0; j < colIndices.length; ++j)
				resultMatrix.data[currRow][j] = data[i][colIndices[j]];
	}
	public void getSubmatrix(Matrix resultMatrix, int[] rowIndices, int colStartIndex, int colEndIndex)
	{
		for (int i = 0; i < rowIndices.length; ++i)
			for (int j = colStartIndex, currCol = 0; j <= colEndIndex; ++j, ++currCol)
				resultMatrix.data[i][currCol] = data[rowIndices[i]][j];
	}

	// Set submatrices given the specified indices and values
	public void setSubmatrix(int rowStartIndex, int rowEndIndex, int colStartIndex, int colEndIndex, Matrix values)
	{
		for ( int i = rowStartIndex, currRow = 0; i <= rowEndIndex; ++i, ++currRow)
			for (int j = colStartIndex, currCol = 0; j <= colEndIndex; ++j, ++currCol)
				data[i][j] = values.data[currRow][currCol];
	}
	public void setSubmatrix(int rowIndices[], int colIndices[], int numRowIndices, int numColIndices, Matrix values)
	{
		for (int i = 0; i < numRowIndices; ++i)
			for (int j = 0; j < numColIndices; ++j)
				data[rowIndices[i]][colIndices[j]] = values.data[i][j];
	}
	public void setSubmatrix(int rowStartIndex, int rowEndIndex, int colIndices[], int numColIndices, Matrix values)
	{
		for (int i = rowStartIndex, currRow = 0; i <= rowEndIndex; ++i, ++currRow)
			for (int j = 0; j < numColIndices; ++j)
				data[i][colIndices[j]] = values.data[currRow][j];
	}
	public void setSubmatrix(int rowIndices[], int colStartIndex, int colEndIndex, int numRowIndices, Matrix values)
	{
		for (int i = 0; i < numRowIndices; ++i)
			for (int j = colStartIndex, currCol = 0; j <= colEndIndex; ++j, ++currCol)
				data[rowIndices[i]][j] = values.data[i][currCol];
	}
	
	public double trace()
	{
		double traceValue = 0.0;
		
		int endIndex = Math.min(numRows, numCols);
		for (int i = 0; i < endIndex; ++i)
				traceValue += data[i][i];
		
		return traceValue;
	}
	
	public Matrix roundToIntegers()
	{
		Matrix resultMatrix = new Matrix(numRows, numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = Math.round(data[i][j]);
		
		return resultMatrix;
	}
	
	// Negate all values in current matrix
	public Matrix matrixNegate()
	{
		Matrix resultMatrix = new Matrix(numRows, numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = -data[i][j];
		
		return resultMatrix;
	}
	public void matrixNegate(Matrix resultMatrix)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = -data[i][j];
	}

	// Transpose a Row-x-Cols matrix
	public Matrix matrixTranspose()
	{
		Matrix resultMatrix = new Matrix(numCols, numRows);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[j][i] = data[i][j];
		
		return resultMatrix;
	}
	public void matrixTranspose(Matrix resultMatrix)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[j][i] = data[i][j];
	}

	// Multiply this matrix by a scalar value
	public Matrix matrixMultiply(double value)
	{
		Matrix resultMatrix = new Matrix(numRows, numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] * value;
		
		return resultMatrix;
	}
	public void matrixMultiply(Matrix resultMatrix, double value)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] * value;
	}

	// Multiply two matrices together
	public Matrix matrixMultiply(Matrix matrix2)
	{
		Matrix resultMatrix = new Matrix(numRows, matrix2.numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numCols; ++j)
			{
				resultMatrix.data[i][j] = 0.0;
				
				for (int k = 0; k < numCols; ++k)
					resultMatrix.data[i][j] += (data[i][k] * matrix2.data[k][j]);
			}
		
		return resultMatrix;
	}
	public void matrixMultiply(Matrix resultMatrix, Matrix matrix2)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numCols; ++j)
			{
				resultMatrix.data[i][j] = 0.0;
				
				for (int k = 0; k < numCols; ++k)
					resultMatrix.data[i][j] += (data[i][k] * matrix2.data[k][j]);
			}
	}

	// Multiply this matrix by the transpose of another matrix
	public Matrix matrixMultiplyTrans(Matrix matrix2)
	{
		Matrix resultMatrix = new Matrix(numRows, matrix2.numRows);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numRows; ++j)
			{
				resultMatrix.data[i][j] = 0.0;
				
				for (int k = 0; k < numCols; ++k)
					resultMatrix.data[i][j] += data[i][k] * matrix2.data[j][k];
			}
		
		return resultMatrix;
	}
	public void matrixMultiplyTrans(Matrix resultMatrix, Matrix matrix2)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numRows; ++j)
			{
				resultMatrix.data[i][j] = 0.0;
				
				for (int k = 0; k < numCols; ++k)
					resultMatrix.data[i][j] += data[i][k] * matrix2.data[j][k];
			}
	}

	// Multiply this matrix by a scalar value
	public void matrixMultiplyInPlace(double value)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] *= value;
	}

	// Multiply this matrix by another matrix
	public void matrixMultiplyInPlace(Matrix matrix2)
	{
		Matrix resultMatrix = new Matrix(numRows, matrix2.numCols);

		// Perform multiplication
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numCols; ++j)
			{
				resultMatrix.data[i][j] = 0.0;
				
				for (int k = 0; k < numCols; ++k)
					resultMatrix.data[i][j] += (data[i][k] * matrix2.data[k][j]);
			}

		// Copy into this matrix
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numCols; ++j)
				data[i][j] = resultMatrix.data[i][j];
	}
	
	// Multiply this matrix by the transpose of another matrix
	public void matrixMultiplyTransInPlace(Matrix matrix2)
	{
		Matrix resultMatrix = new Matrix(numRows, matrix2.numCols);

		// Perform multiplication
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numCols; ++j)
			{
				resultMatrix.data[i][j] = 0.0;
				
				for (int k = 0; k < numCols; ++k)
					resultMatrix.data[i][j] += (data[i][k] * matrix2.data[j][k]);
			}

		// Copy into this matrix
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < matrix2.numCols; ++j)
				data[i][j] = resultMatrix.data[i][j];
	}

	// Invert a square matrix using Gauss-Jordan Elimination
	public Matrix matrixInvert()
	{
		Matrix scratchMatrix = new Matrix(this);
		Matrix output = new Matrix(numRows, numCols);
		output.setIdentityMatrix();

		for (int i = 0; i < numRows; ++i)
		{
			// Diagonal cannot be a zero-value
			if (scratchMatrix.data[i][i] == 0.0)
			{
				// Find next row that doesn't have a zero-diagonal
				int r = i + 1;
				for ( ; r < numRows; ++r)
					if (scratchMatrix.data[r][i] != 0.0)
						break;
				if (r == numRows)
					return output;

				// Swap rows
				scratchMatrix.swap_rows(i, r);
				output.swap_rows(i, r);
			}

			// Scale row so that diagonal equals 1.0
			double scalar = 1.0 / scratchMatrix.data[i][i];
			scratchMatrix.scale_row(i, scalar);
			output.scale_row(i, scalar);

			// Add rows together to remove non-diagonal values
			for (int j = 0; j < numRows; ++j)
			{
				if (i == j)
					continue;

				double shear_needed = -scratchMatrix.data[j][i];
				scratchMatrix.shear_row(j, i, shear_needed);
				output.shear_row(j, i, shear_needed);
			}
		}
		
		return output;
	}
	public void matrixInvert(Matrix output)
	{
		Matrix scratchMatrix = new Matrix(this);
		output.setIdentityMatrix();

		for (int i = 0; i < numRows; ++i)
		{
			// Diagonal cannot be a zero-value
			if (scratchMatrix.data[i][i] == 0.0)
			{
				// Find next row that doesn't have a zero-diagonal
				int r = i + 1;
				for ( ; r < numRows; ++r)
					if (scratchMatrix.data[r][i] != 0.0)
						break;
				if (r == numRows)
					return;

				// Swap rows
				scratchMatrix.swap_rows(i, r);
				output.swap_rows(i, r);
			}

			// Scale row so that diagonal equals 1.0
			double scalar = 1.0 / scratchMatrix.data[i][i];
			scratchMatrix.scale_row(i, scalar);
			output.scale_row(i, scalar);

			// Add rows together to remove non-diagonal values
			for (int j = 0; j < numRows; ++j)
			{
				if (i == j)
					continue;

				double shear_needed = -scratchMatrix.data[j][i];
				scratchMatrix.shear_row(j, i, shear_needed);
				output.shear_row(j, i, shear_needed);
			}
		}
	}
	
	// Invert a diagonal matrix in place
	public void matrixInvertDiagonalInPlace()
	{
		for (int i = 0; i < numRows; ++i)
			data[i][i] = 1.0 / data[i][i];
	}
		
	// Invert a square matrix in place using Gauss-Jordan Elimination
	public void matrixInvertInPlace()
	{
		Matrix scratchMatrix = new Matrix(this);
		setIdentityMatrix();

		for (int i = 0; i < numRows; ++i)
		{
			// Diagonal cannot be a zero-value
			if (scratchMatrix.data[i][i] == 0.0)
			{
				// Find next row that doesn't have a zero-diagonal
				int r = i + 1;
				for ( ; r < numRows; ++r)
					if (scratchMatrix.data[r][i] != 0.0)
						break;
				if (r == numRows)
					return;

				// Swap rows
				scratchMatrix.swap_rows(i, r);
				swap_rows(i, r);
			}

			// Scale row so that diagonal equals 1.0
			double scalar = 1.0 / scratchMatrix.data[i][i];
			scratchMatrix.scale_row(i, scalar);
			scale_row(i, scalar);

			// Add rows together to remove non-diagonal values
			for (int j = 0; j < numRows; ++j)
			{
				if (i == j)
					continue;

				double shear_needed = -scratchMatrix.data[j][i];
				scratchMatrix.shear_row(j, i, shear_needed);
				shear_row(j, i, shear_needed);
			}
		}
	}

	// Add a single value to all elements of a matrix
	public Matrix matrixAdd(double value)
	{
		Matrix resultMatrix = new Matrix(numRows, numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] + value;
		
		return resultMatrix;
	}
	public void matrixAdd(Matrix resultMatrix, double value)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] + value;
	}
	
	// Add two matrices together
	public Matrix matrixAdd(Matrix matrix2)
	{
		Matrix resultMatrix = new Matrix(numRows, numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] + matrix2.data[i][j];
		
		return resultMatrix;
	}
	public void matrixAdd(Matrix resultMatrix, Matrix matrix2)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] + matrix2.data[i][j];
	}

	// Add a value to all elements in this matrix
	public void matrixAddInPlace(double value)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] += value;
	}
	
	// Add another matrix to this matrix
	public void matrixAddInPlace(Matrix matrix2)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] += matrix2.data[i][j];
	}

	// Subtract two matrices
	public Matrix matrixSubtract(Matrix matrix2)
	{
		Matrix resultMatrix = new Matrix(numRows, numCols);
		
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] - matrix2.data[i][j];
		
		return resultMatrix;
	}
	public void matrixSubtract(Matrix resultMatrix, Matrix matrix2)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j] - matrix2.data[i][j];
	}

	// Subtract another matrix from this matrix
	public void matrixSubtractInPlace(Matrix matrix2)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] -= matrix2.data[i][j];
	}

	// Subtract this matrix from an identity matrix
	public void matrixSubFromIdentity()
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				data[i][j] = (i == j) ? (1.0 - data[i][j]) : -data[i][j];
	}
	
	// Copy this matrix into another matrix
	public void matrixCopyInto(Matrix resultMatrix)
	{
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
				resultMatrix.data[i][j] = data[i][j];
	}
	
	public double dotProduct(double[] vector2, boolean isRowVector, int rowIndex, int colIndex)
	{
		double result = 0.0;
		for (int i = (isRowVector ? colIndex : rowIndex); i < (isRowVector ? numCols : numRows); ++i)
			result += (isRowVector ? (data[rowIndex][i]*vector2[i]) : (data[i][colIndex]*vector2[i]));
		
		return result;
	}

	// Calculate the Lorentz Inner Product between this matrix and another
	// <u,v> = u1*v1 + u2*v2 + u3*v3 - u4*v4
	public double lorentzInnerProduct(Matrix matrix2, boolean isRowVector)
	{
		// Can only operate on a "vector" (nx1 matrix)
		if ((!isRowVector && ((numCols > 1) || (matrix2.numCols > 1))) ||
			(isRowVector && ((numRows > 1) || (matrix2.numRows > 1))))
			return Double.NaN;
		
		double result = 0.0;
		for (int i = 0; i < 3; ++i)
			result += (isRowVector ? (data[0][i] * matrix2.data[0][i]) : (data[i][0] * matrix2.data[i][0]));
		result -= (isRowVector ? (data[0][3] * matrix2.data[0][3]) : (data[3][0] * matrix2.data[3][0]));
		
		return result;
	}
	
	// Take the matrix-square-root of this matrix
	public Matrix matrixSqrt()
	{
		// Get eigenvalues and eigenvectors for the current matrix
		//   thisMatrix = V * E * V^T
		Matrix eigenvalueMatrix = new Matrix(numRows, numCols), eigenvectorMatrix = new Matrix(numRows, numCols), tempMatrix;
		getEigens(eigenvalueMatrix, eigenvectorMatrix);		// E and V

		// Take square root of each eigenvalue				// sqrt(E)
		for (int i = 0; i < numRows; ++i)
			eigenvalueMatrix.data[i][i] = Math.sqrt(eigenvalueMatrix.data[i][i]);

		// Calculate square root of matrix
		//   sqrtMatrix = V * sqrt(E) * V^T
		tempMatrix = eigenvectorMatrix.matrixMultiply(eigenvalueMatrix);
		return tempMatrix.matrixMultiplyTrans(eigenvectorMatrix);
	}
	public void matrixSqrt(Matrix resultMatrix)
	{
		// Get eigenvalues and eigenvectors for the current matrix
		//   thisMatrix = V * E * V^T
		Matrix eigenvalueMatrix = new Matrix(numRows, numCols), eigenvectorMatrix = new Matrix(numRows, numCols), tempMatrix;
		getEigens(eigenvalueMatrix, eigenvectorMatrix);		// E and V

		// Take square root of each eigenvalue				// sqrt(E)
		for (int i = 0; i < numRows; ++i)
			eigenvalueMatrix.data[i][i] = Math.sqrt(eigenvalueMatrix.data[i][i]);

		// Calculate square root of matrix
		//   sqrtMatrix = V * sqrt(E) * V^T
		tempMatrix = eigenvectorMatrix.matrixMultiply(eigenvalueMatrix);
		tempMatrix.matrixMultiplyTrans(resultMatrix, eigenvectorMatrix);
	}

	// Get eigenvalues/eigenvectors for this matrix
	//   Only succeeds if original matrix is symmetric
	public void getEigens(Matrix eigenvalueMatrix, Matrix eigenvectors)
	{
		Matrix eigenvalues = new Matrix(numRows, 1), diagonals = new Matrix(numRows, 1);

		// Make sure matrix is symmetric
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numRows; ++j)
			{
				if (data[i][j] != data[j][i])
					return;
				eigenvectors.data[i][j] = data[i][j];
			}

		// Tridiagonalize
		for (int j = 0; j < numRows; ++j)
			diagonals.data[j][0] = eigenvectors.data[numRows-1][j];

		// Householder reduction to tridiagonal form
		for (int i = numRows - 1; i > 0; --i)
		{
			// Scale to avoid under/overflow
			double scale = 0.0, h = 0.0;

			for (int k = 0; k < i; ++k)
				scale += Math.abs(diagonals.data[k][0]);

			if (scale == 0.0)
			{
				eigenvalues.data[i][0] = diagonals.data[i-1][0];

				for (int j = 0; j < i; ++j)
				{
					diagonals.data[j][0] = eigenvectors.data[i-1][j];
					eigenvectors.data[i][j] = 0.0;
					eigenvectors.data[j][i] = 0.0;
				}
			}
			else
			{
				// Generate Householder vector
				for (int k = 0; k < i; ++k)
				{
					diagonals.data[k][0] /= scale;
					h += (diagonals.data[k][0] * diagonals.data[k][0]);
				}

				double f = diagonals.data[i-1][0];
				double g = Math.sqrt(h);
						
				if (f > 0.0)
					g = -g;

				eigenvalues.data[i][0] = scale * g;
				h -= (f * g);
				diagonals.data[i-1][0] = f - g;

				for (int j = 0; j < i; ++j)
					eigenvalues.data[j][0] = 0.0;

				// Apply similarity transformation to remaining columns
				for (int j = 0; j < i; ++j)
				{
					f = diagonals.data[j][0];
					eigenvectors.data[j][i] = f;
					g = eigenvalues.data[j][0] + eigenvectors.data[j][j]*f;

					for (int k = j+1; k <= i-1; ++k)
					{
						g += eigenvectors.data[k][j] * diagonals.data[k][0];
						eigenvalues.data[k][0] += eigenvectors.data[k][j] * f;
					}
					eigenvalues.data[j][0] = g;
				}

				f = 0.0;
				for (int j = 0; j < i; ++j)
				{
					eigenvalues.data[j][0] /= h;
					f += eigenvalues.data[j][0] * diagonals.data[j][0];
				}

				double hh = f / (h + h);
				for (int j = 0; j < i; ++j)
					eigenvalues.data[j][0] -= hh * diagonals.data[j][0];
				
				for (int j = 0; j < i; ++j)
				{
					f = diagonals.data[j][0];
					g = eigenvalues.data[j][0];

					for (int k = j; k <= i - 1; ++k)
						eigenvectors.data[k][j] -= (f*eigenvalues.data[k][0] + g*diagonals.data[k][0]);

					diagonals.data[j][0] = eigenvectors.data[i-1][j];
					eigenvectors.data[i][j] = 0.0;
				}
			}

			diagonals.data[i][0] = h;
		}

		// Accumulate transformations
		for (int i = 0; i < numRows-1; i++)
		{
			eigenvectors.data[numRows-1][i] = eigenvectors.data[i][i];
			eigenvectors.data[i][i] = 1.0;
			double h = diagonals.data[i+1][0];

			if (h != 0.0)
			{
				for (int k = 0; k <= i; ++k)
					diagonals.data[k][0] = eigenvectors.data[k][i+1] / h;

				for (int j = 0; j <= i; ++j)
				{
					double g = 0.0;

					for (int k = 0; k <= i; ++k)
						g += eigenvectors.data[k][i+1] * eigenvectors.data[k][j];

					for (int k = 0; k <= i; ++k)
						eigenvectors.data[k][j] -= g * diagonals.data[k][0];
				}
			}

			for (int k = 0; k <= i; ++k)
				eigenvectors.data[k][i+1] = 0.0;
		}

		for (int j = 0; j < numRows; ++j)
		{
			diagonals.data[j][0] = eigenvectors.data[numRows-1][j];
			eigenvectors.data[numRows-1][j] = 0.0;
		}

		eigenvectors.data[numRows-1][numRows-1] = 1.0;
		eigenvalues.data[0][0] = 0.0;

		// Diagonalize
		for (int i = 1; i < numRows; ++i)
			eigenvalues.data[i-1][0] = eigenvalues.data[i][0];
		eigenvalues.data[numRows-1][0] = 0.0;

		double f = 0.0, tst1 = 0.0, eps = Math.pow(2.0,-52.0);
		for (int l = 0; l < numRows; ++l)
		{
			// Find small subdiagonal element
			tst1 = Math.max(tst1, Math.abs(diagonals.data[l][0]) + Math.abs(eigenvalues.data[l][0]));
			int m = l;

			// Original while-loop from Java code
			while (m < numRows)
			{
				if (Math.abs(eigenvalues.data[m][0]) <= eps*tst1)
					break;
				++m;
			}

			// if m == l, diagonals(l) is an eigenvalue, otherwise, iterate
			if (m > l)
			{
				do
				{
					// Compute implicit shift
					double g = diagonals.data[l][0];
					double p = (diagonals.data[l+1][0] - g) / (2.0 * eigenvalues.data[l][0]);
					double r = Math.hypot(p, 1.0);
					if (p < 0.0)
						r = -r;

					diagonals.data[l][0] = eigenvalues.data[l][0] / (p + r);
					diagonals.data[l+1][0] = eigenvalues.data[l][0] * (p + r);
					double dl1 = diagonals.data[l+1][0];
					double h = g - diagonals.data[l][0];

					for (int i = l + 2; i < numRows; ++i)
						diagonals.data[i][0] -= h;
					f += h;

					// Implicit QL transformation.
					p = diagonals.data[m][0];
					double c = 1.0;
					double c2 = c;
					double c3 = c;
					double el1 = eigenvalues.data[l+1][0];
					double s = 0.0;
					double s2 = 0.0;

					for (int i = m - 1; i >= l; --i)
					{
						c3 = c2;
						c2 = c;
						s2 = s;
						g = c * eigenvalues.data[i][0];
						h = c * p;
						r = Math.hypot(p, eigenvalues.data[i][0]);
						eigenvalues.data[i+1][0] = s * r;
						s = eigenvalues.data[i][0] / r;
						c = p / r;
						p = c*diagonals.data[i][0] - s*g;
						diagonals.data[i+1][0] = h + s * (c*g + s*diagonals.data[i][0]);
						
						// Accumulate transformation
						for (int k = 0; k < numRows; ++k)
						{
							h = eigenvectors.data[k][i+1];
							eigenvectors.data[k][i+1] = s*eigenvectors.data[k][i] + c*h;
							eigenvectors.data[k][i] = c*eigenvectors.data[k][i] - s*h;
						}
					}

					p = -s*s2*c3*el1*eigenvalues.data[l][0] / dl1;
					eigenvalues.data[l][0] = s * p;
					diagonals.data[l][0] = c * p;
				} while (Math.abs(eigenvalues.data[l][0]) > eps*tst1);
			}
				
			diagonals.data[l][0] += f;
			eigenvalues.data[l][0] = 0.0;
		}

		// Sort eigenvalues and corresponding vectors
		for (int i = 0; i < numRows - 1; ++i)
		{
			int k = i;
			double p = diagonals.data[i][0];

			for (int j = i + 1; j < numRows; ++j)
				if (diagonals.data[j][0] < p)
				{
					k = j;
					p = diagonals.data[j][0];
				}

			if (k != i)
			{
				diagonals.data[k][0] = diagonals.data[i][0];
				diagonals.data[i][0] = p;

				double temp;
				for (int j = 0; j < numRows; ++j)
				{
					temp = eigenvectors.data[j][i];
					eigenvectors.data[j][i] = eigenvectors.data[j][k];
					eigenvectors.data[j][k] = temp;
				}
			}
		}
		
		eigenvalueMatrix.clearMatrix();
		for (int i = 0; i < numRows; ++i)
			eigenvalueMatrix.data[i][i] = diagonals.data[i][0];
	}
	
	// Get LU Factorization of current matrix
	public void getLUFactorization(Matrix L, Matrix U)
	{
		Matrix LUMatrix = new Matrix(this);
		int maxSize = Math.min(numRows, numCols) - 1;
		
		for (int i = 0; i < maxSize; ++i)
			for (int j = i + 1; j < numRows; ++j)
			{
				LUMatrix.data[j][i] /= LUMatrix.data[i][i];

				for (int k = i + 1; k < numCols; ++k)
					LUMatrix.data[j][k] -= (LUMatrix.data[j][i] * LUMatrix.data[i][k]);
			}

		if (numRows > numCols)
			for (int i = numCols + 1; i < numRows; ++i)
				LUMatrix.data[i][numCols-1] /= LUMatrix.data[numCols-1][numCols-1];

		// Copy into L and U matrices
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
			{
				if (i > j)
				{
					L.data[i][j] = LUMatrix.data[i][j];
					U.data[i][j] = 0.0;
				}
				else if (i == j)
				{
					L.data[i][j] = 1.0;
					U.data[i][j] = LUMatrix.data[i][j];
				}
				else
				{
					L.data[i][j] = 0.0;
					U.data[i][j] = LUMatrix.data[i][j];
				}
			}
	}

	// Get LU Factorization of current matrix and only return
	//   either the upper or lower factorization
	public void getLUFactorization(Matrix LU, boolean returnLower)
	{
		Matrix LUMatrix = new Matrix(this);
		int maxSize = Math.min(numRows, numCols) - 1;
		
		for (int i = 0; i < maxSize; ++i)
			for (int j = i + 1; j < numRows; ++j)
			{
				LUMatrix.data[j][i] /= LUMatrix.data[i][i];

				for (int k = i + 1; k < numCols; ++k)
					LUMatrix.data[j][k] -= (LUMatrix.data[j][i] * LUMatrix.data[i][k]);
			}

		if (numRows > numCols)
			for (int i = numCols + 1; i < numRows; ++i)
				LUMatrix.data[i][numCols-1] /= LUMatrix.data[numCols-1][numCols-1];

		// Copy into L or U matrix, as specified
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
			{
				if (returnLower)
				{
					if (i > j)
						LU.data[i][j] = LUMatrix.data[i][j];
					else if (i == j)
						LU.data[i][j] = 1.0;
					else
						LU.data[i][j] = 0.0;
				}
				else
				{
					if (i <= j)
						LU.data[i][j] = LUMatrix.data[i][j];
					else
						LU.data[i][j] = 0.0;
				}
			}
	}

	// Get LU Factorization of current matrix using pivots
	public Matrix getLUFactorizationWithPivot(Matrix L, Matrix U)
	{
		Matrix LUMatrix = new Matrix(this), pivots = new Matrix(numRows, 1);
		double scale;
		int pivotSign = 1, kMax;
		for (int i = 0; i < numRows; ++i)
			pivots.data[i][0] = i;

		// Use a "left-looking", dot-product, Crout/Doolittle algorithm
		for (int j = 0; j < numCols; ++j)
		{
			// Apply previous transformations
			for (int i = 0; i < numRows; ++i)
			{
				kMax = Math.min(i, j);
	            scale = 0.0;

				for (int k = 0; k < kMax; ++k)
					scale += (LUMatrix.data[i][k] * LUMatrix.data[k][j]);

				LUMatrix.data[i][j] -= scale;
	         }

			// Find pivot
			int p = j;
			for (int i = j + 1; i < numRows; ++i)
				if (Math.abs(LUMatrix.data[i][j]) > Math.abs(LUMatrix.data[p][j]))
					p = i;

			// Exchange rows if necessary
			if (p != j)
			{
				LUMatrix.swap_rows(p, j);
				pivots.swap_rows(p, j);
	            pivotSign = -pivotSign;
			}

			// Compute multipliers
			if ((j < numRows) && (LUMatrix.data[j][j] != 0.0))
				for (int i = j + 1; i < numRows; ++i)
					LUMatrix.data[i][j] /= LUMatrix.data[j][j];
		}

		// Copy into L and U matrices
		for (int i = 0; i < numRows; ++i)
			for (int j = 0; j < numCols; ++j)
			{
				if (i > j)
				{
					L.data[i][j] = LUMatrix.data[i][j];
					U.data[i][j] = 0.0;
				}
				else if (i == j)
				{
					L.data[i][j] = 1.0;
					U.data[i][j] = LUMatrix.data[i][j];
				}
				else
				{
					L.data[i][j] = 0.0;
					U.data[i][j] = LUMatrix.data[i][j];
				}
			}

		return pivots;		
	}

	// Returns the QR factorization of the current matrix
	public void getQRFactorization(Matrix Q, Matrix R)
	{
		Matrix V = new Matrix(numRows, 1), VTrans, tempMatrix;
		double tempDouble;
		boolean allZeros;
		matrixCopyInto(R);
		Q.setIdentityMatrix();

		// Use Householder transformations to calculate QR
		for (int i = 0; i < numCols; ++i)
		{
			// Get Householder vector
			allZeros = true;
			V.clearMatrix();
			for (int j = i; j < numRows; ++j)
			{
				// Make sure we aren't already in upper triangular form
				if ((j != i) && (R.data[j][i] != 0.0))
					allZeros = false;
				V.data[j][0] = R.data[j][i];
			}
			if (allZeros)
				continue;
			tempDouble = 0.0;
			for (int j = i; j < numRows; ++j)
				tempDouble += (R.data[j][i] * R.data[j][i]);
			V.data[i][0] += (R.data[i][i] >= 0.0) ? Math.sqrt(tempDouble) : -Math.sqrt(tempDouble);
			tempDouble = 0.0;
			for (int j = i; j < numRows; ++j)
				tempDouble += (V.data[j][0] * V.data[j][0]);
			tempDouble = Math.sqrt(2.0 / tempDouble);
			for (int j = i; j < numRows; ++j)
				V.data[j][0] *= tempDouble;

			// Apply Householder transformation to R
			VTrans = V.matrixTranspose();
			tempMatrix = VTrans.matrixMultiply(R);
			tempMatrix = V.matrixMultiply(tempMatrix);
			R.matrixSubtractInPlace(tempMatrix);

			// Apply Householder transformation to Q
			tempMatrix = Q.matrixMultiply(V);
			tempMatrix = tempMatrix.matrixMultiply(VTrans);
			Q.matrixSubtractInPlace(tempMatrix);

			// Put 0's where they should be (due to rounding errors)
			for (int j = i + 1; j < numRows; ++j)
				R.data[j][i] = 0.0;
		}
	}

	// Returns the 'R' part of a QR factorization
	public void getQRFactorization(Matrix QR, boolean returnR)
	{
		Matrix V = new Matrix(numRows, 1), VTrans, tempMatrix;
		Matrix R = returnR ? QR : new Matrix(numRows, numCols), Q = returnR ? new Matrix(numRows, numRows) : QR;
		double tempDouble;
		boolean allZeros;
		matrixCopyInto(R);
		Q.setIdentityMatrix();

		// Use Householder transformations to calculate QR
		for (int i = 0; i < numCols; ++i)
		{
			// Get Householder vector
			allZeros = true;
			V.clearMatrix();
			for (int j = i; j < numRows; ++j)
			{
				// Make sure we aren't already in upper triangular form
				if ((j != i) && (R.data[j][i] != 0.0))
					allZeros = false;
				V.data[j][0] = R.data[j][i];
			}
			if (allZeros)
				continue;
			tempDouble = 0.0;
			for (int j = i; j < numRows; ++j)
				tempDouble += (R.data[j][i] * R.data[j][i]);
			V.data[i][0] += (R.data[i][i] >= 0.0) ? Math.sqrt(tempDouble) : -Math.sqrt(tempDouble);
			tempDouble = 0.0;
			for (int j = i; j < numRows; ++j)
				tempDouble += (V.data[j][0] * V.data[j][0]);
			tempDouble = Math.sqrt(2.0 / tempDouble);
			for (int j = i; j < numRows; ++j)
				V.data[j][0] *= tempDouble;

			// Apply Householder transformation
			VTrans = V.matrixTranspose();
			tempMatrix = VTrans.matrixMultiply(R);
			tempMatrix = V.matrixMultiply(tempMatrix);
			R.matrixSubtractInPlace(tempMatrix);

			// Apply Householder transformation to Q
			if (!returnR)
			{
				tempMatrix = Q.matrixMultiply(V);
				tempMatrix = tempMatrix.matrixMultiplyTrans(V);
				Q.matrixSubtractInPlace(tempMatrix);
			}

			// Put 0's where they should be (due to rounding errors)
			for (int j = i + 1; j < numRows; ++j)
				R.data[j][i] = 0.0;
		}
	}

	// Returns 'R' part of QR factorization using pivoting (ensures diagonals are in order of decreasing value)
	public Matrix getQRFactorizationWithPivoting(Matrix Q, Matrix R)
	{
		Matrix V = new Matrix(numRows, 1), pivots = new Matrix(numCols, numCols), norms = new Matrix(1, numCols), VTrans, tempMatrix;
		double largestNorm;
		int columnToSwap = 0;
		boolean allZeros;
		pivots.setIdentityMatrix();
		Q.setIdentityMatrix();
		matrixCopyInto(R);

		// Calculate norms
		for (int i = 0; i < numCols; ++i)
		{
			for (int j = 0; j < numRows; ++j)
				norms.data[0][i] += (data[j][i] * data[j][i]);
			norms.data[0][i] = Math.sqrt(norms.data[0][i]);
		}

		// Use pivots and Householder transformations to calculate QR
		for (int i = 0; i < numCols; ++i)
		{
			// Find remaining column with largest norm
			largestNorm = 0.0;
			for (int j = i; j < numCols; ++j)
				if (norms.data[0][j] > largestNorm)
				{
					largestNorm = norms.data[0][j];
					columnToSwap = j;
				}

			// Swap columns, if necessary
			if (columnToSwap != i)
			{
				R.swap_columns(i, columnToSwap);
				pivots.swap_columns(i, columnToSwap);
				norms.swap_columns(i, columnToSwap);
			}

			// Get Householder vector
			allZeros = true;
			V.clearMatrix();
			for (int j = i; j < numRows; ++j)
			{
				// Make sure we aren't already in upper triangular form
				if ((j != i) && (R.data[j][i] != 0.0))
					allZeros = false;
				V.data[j][0] = R.data[j][i];
			}
			if (allZeros)
				continue;
			largestNorm = 0.0;
			for (int j = i; j < numRows; ++j)
				largestNorm += (R.data[j][i] * R.data[j][i]);
			V.data[i][0] += (R.data[i][i] >= 0.0) ? Math.sqrt(largestNorm) : -Math.sqrt(largestNorm);
			largestNorm = 0.0;
			for (int j = i; j < numRows; ++j)
				largestNorm += (V.data[j][0] * V.data[j][0]);
			largestNorm = Math.sqrt(2.0 / largestNorm);
			for (int j = i; j < numRows; ++j)
				V.data[j][0] *= largestNorm;

			// Apply Householder transformation to R
			VTrans = V.matrixTranspose();
			tempMatrix = VTrans.matrixMultiply(R);
			tempMatrix = V.matrixMultiply(tempMatrix);
			R.matrixSubtractInPlace(tempMatrix);

			// Apply Householder transformation to Q
			tempMatrix = Q.matrixMultiply(V);
			tempMatrix = tempMatrix.matrixMultiply(VTrans);
			Q.matrixSubtractInPlace(tempMatrix);

			// Put 0's where they should be (due to rounding errors)
			for (int j = i + 1; j < numRows; ++j)
				R.data[j][i] = 0.0;

			// Update the vector of norms
			for (int j = i+1; j < numCols; ++j)
			{
				norms.data[0][j] = 0.0;
				for (int k = i+1; k < numRows; ++k)
					norms.data[0][j] += (R.data[k][j] * R.data[k][j]);
				norms.data[0][j] = Math.sqrt(norms.data[0][j]);
			}
		}

		return pivots;
	}

	// Get Lower Triangular Matrix from Cholesky Decomposition
	//   A = L*L^T
	public Matrix getLowerCholeskyDecomp()
	{
		Matrix resultMatrix = new Matrix(this);
		double total;

		// Carry out Cholesky decomposition algorithm
		for (int i = 0; i < numRows; ++i)
			for (int j = i; j < numCols; ++j)
			{
				total = resultMatrix.data[i][j];

				for (int k = i - 1; k >= 0; --k)
					total -= (resultMatrix.data[i][k] * resultMatrix.data[j][k]);

				if (i == j)				// Diagonal (squared) value
				{
					if (total <= 0.0)
						return resultMatrix;
					resultMatrix.data[i][i] = Math.sqrt(total);
				}
				else
					resultMatrix.data[j][i] = total / resultMatrix.data[i][i];
			}

		// Set upper triangular part to all 0's
		for (int i = 0; i < numRows; ++i)
			for (int j = i + 1; j < numCols; ++j)
				resultMatrix.data[i][j] = 0.0;
		
		return resultMatrix;
	}
	public void getLowerCholeskyDecomp(Matrix resultMatrix)
	{
		double total;
		
		// Copy current matrix into result matrix
		matrixCopyInto(resultMatrix);

		// Carry out Cholesky decomposition algorithm
		for (int i = 0; i < numRows; ++i)
			for (int j = i; j < numCols; ++j)
			{
				total = resultMatrix.data[i][j];

				for (int k = i - 1; k >= 0; --k)
					total -= (resultMatrix.data[i][k] * resultMatrix.data[j][k]);

				if (i == j)				// Diagonal (squared) value
				{
					if (total <= 0.0)
						return;
					resultMatrix.data[i][i] = Math.sqrt(total);
				}
				else
					resultMatrix.data[j][i] = total / resultMatrix.data[i][i];
			}

		// Set upper triangular part to all 0's
		for (int i = 0; i < numRows; ++i)
			for (int j = i + 1; j < numCols; ++j)
				resultMatrix.data[i][j] = 0.0;
	}
	public Matrix getLowerCholeskyDecompLDL(Matrix D)
	{
		Matrix L = new Matrix(numRows, numCols), Q = new Matrix(this);
		double temp;
		
		D.setZeroMatrix();
		for (int i = numRows-1; i >= 0; --i)
		{
			D.data[i][i] = Q.data[i][i];
			temp = 1.0 / Math.sqrt(Q.data[i][i]);
			for (int j = 0; j <= i; ++j)
				L.data[i][j] = Q.data[i][j] * temp;
			
			for (int j = 0; j < i; ++j)
				for (int k = 0; k <= j; ++k)
					Q.data[j][k] -= (L.data[i][k]*L.data[i][j]);
			
			temp = 1.0 / L.data[i][i];
			for (int j = 0; j <= i; ++j)
				L.data[i][j] *= temp;
		}
		
		return L;
	}
	public void getLowerCholeskyDecompLDL(Matrix L, Matrix D)
	{
		Matrix Q = new Matrix(this);
		double temp;
		
		L.setZeroMatrix();
		D.setZeroMatrix();
		
		for (int i = numRows-1; i >= 0; --i)
		{
			D.data[i][i] = Q.data[i][i];
			temp = 1.0 / Math.sqrt(Q.data[i][i]);
			for (int j = 0; j <= i; ++j)
				L.data[i][j] = Q.data[i][j] * temp;
			
			for (int j = 0; j < i; ++j)
				for (int k = 0; k <= j; ++k)
					Q.data[j][k] -= (L.data[i][k]*L.data[i][j]);
			
			temp = 1.0 / L.data[i][i];
			for (int j = 0; j <= i; ++j)
				L.data[i][j] *= temp;
		}
	}

	// Update a Cholesky Factor with the specified update matrix and value
	public void choleskyUpdate(Matrix updateMatrix, double updateValue)
	{
		Matrix tempUpdateVector = new Matrix(1, 1);
		boolean update = (updateValue >= 0.0);
		double alpha, beta, beta2, delta, gamma;
		updateValue = Math.sqrt(Math.abs(updateValue));

		// Multiply updateMatrix by update value
		for (int col = 0; col < updateMatrix.numCols; ++col)
		{
			// Update current column of update matrix
			for (int i = 0; i < updateMatrix.numRows; ++i)
				updateMatrix.data[i][col] *= updateValue;

			beta = 1.0;
			for (int i = 0; i < numRows; ++i)
			{
				alpha = updateMatrix.data[i][col] / data[i][i];
				beta2 = Math.sqrt(beta*beta - alpha*alpha);
				gamma = alpha / (beta2 * beta);
				delta = (update ? (beta / beta2) : (beta2 / beta));
				data[i][i] *= delta;
				if (update)
					data[i][i] += (gamma * updateMatrix.data[i][col]);
				updateMatrix.data[i][col] = alpha;
				beta = beta2;

				if (i == (numRows-1))
					return;

				if (update)
				{
					tempUpdateVector.setSize(updateMatrix.numRows-i-1, 1);
					for (int j = i + 1; j < numRows; ++j)
						tempUpdateVector.data[j-i-1][0] = updateMatrix.data[j][col];
					for (int j = i + 1; j < numRows; ++j)
						updateMatrix.data[j][col] -= (alpha * data[j][i]);
					for (int j = i + 1; j < numRows; ++j)
						data[j][i] = delta*data[j][i] + gamma*tempUpdateVector.data[j-i-1][0];
				}
				else
				{
					for (int j = i + 1; j < numRows; ++j)
						updateMatrix.data[j][col] -= (alpha * data[j][i]);
					for (int j = i + 1; j < numRows; ++j)
						data[j][i] = delta*data[j][i] - gamma*updateMatrix.data[j][col];
				}
			}
		}
	}

	// Find a linear least squares solution to AX = B using QR factorization with pivoting
	public Matrix leastSquaresSolutionUsingQRWithPivot(Matrix B)
	{
		// Get QR factorization
		Matrix Q = new Matrix(numRows, numRows), R = new Matrix(numRows, numCols), QTrans, z = new Matrix(numCols, B.numCols);
		Matrix tempMatrix, P = getQRFactorizationWithPivoting(Q, R);

		// Ignore 0's part of Q/R matrices
		Q.resize(Q.numRows, R.numCols);
		R.resize(R.numCols, R.numCols);
		QTrans = Q.matrixTranspose();
		tempMatrix = QTrans.matrixMultiply(B);

		// Solve x by backward substitution, then undo pivoting
		z.clearMatrix();
		for (int j = 0; j < B.numCols; ++j)
			z.data[z.numRows-1][j] = (tempMatrix.data[z.numRows-1][j] / R.data[R.numRows-1][R.numCols-1]);
		for (int i = R.numRows - 2; i >= 0; --i)
		{
			for (int j = 0; j < B.numCols; ++j)
			{
				z.data[i][j] = tempMatrix.data[i][j];
				for (int k = i + 1; k < R.numCols; ++k)
					z.data[i][j] -= (R.data[i][k] * z.data[k][j]);
				z.data[i][j] /= R.data[i][i];
			}
		}
		
		return P.matrixMultiply(z);
	}
	public void leastSquaresSolutionUsingQRWithPivot(Matrix X, Matrix B)
	{
		// Get QR factorization
		Matrix Q = new Matrix(numRows, numRows), R = new Matrix(numRows, numCols), QTrans, z = new Matrix(numCols, B.numCols);
		Matrix tempMatrix, P = getQRFactorizationWithPivoting(Q, R);

		// Ignore 0's part of Q/R matrices
		Q.resize(Q.numRows, R.numCols);
		R.resize(R.numCols, R.numCols);
		QTrans = Q.matrixTranspose();
		tempMatrix = QTrans.matrixMultiply(B);

		// Solve x by backward substitution, then undo pivoting
		z.clearMatrix();
		for (int j = 0; j < B.numCols; ++j)
			z.data[z.numRows-1][j] = (tempMatrix.data[z.numRows-1][j] / R.data[R.numRows-1][R.numCols-1]);
		for (int i = R.numRows - 2; i >= 0; --i)
		{
			for (int j = 0; j < B.numCols; ++j)
			{
				z.data[i][j] = tempMatrix.data[i][j];
				for (int k = i + 1; k < R.numCols; ++k)
					z.data[i][j] -= (R.data[i][k] * z.data[k][j]);
				z.data[i][j] /= R.data[i][i];
			}
		}
		P.matrixMultiply(X, z);
	}

	// Swap rows of current matrix
	private void swap_rows(int row1, int row2)
	{
		double temp;
		
		for (int i = 0; i < numCols; ++i)
		{
			temp = data[row1][i];
			data[row1][i] = data[row2][i];
			data[row2][i] = temp;
		}
	}

	// Swap columns of current matrix
	private void swap_columns(int col1, int col2)
	{
		double temp;

		for (int j = 0; j < numRows; ++j)
		{
			temp = data[j][col1];
			data[j][col1] = data[j][col2];
			data[j][col2] = temp;
		}
	}

	// Multiply row of current matrix by a scalar value
	private void scale_row(int row, double scalar)
	{
		for (int i = 0; i < numCols; ++i)
			data[row][i] *= scalar;
	}

	// Shear a row of the current matrix
	private void shear_row(int row1, int row2, double scalar)
	{
		for (int i = 0; i < numCols; ++i)
			data[row1][i] += (scalar * data[row2][i]);
	}
}
