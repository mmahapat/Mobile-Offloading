package com.mobilespark.master;
import java.util.Random;

public class GenerateMatrix {

    public static int[][] createMatrix(int rows, int cols){
        Random rand = new Random();
        int[][] matrix = new int[rows][cols];
        for(int i = 0; i < rows; i++)
            for(int j = 0; j < cols; j++)
                matrix[i][j] = rand.nextInt(100);

        return matrix;
    }

    public static int[][] multiplyMatrix(int[][] reducedA, int[][] reducedB) {
        int l = reducedA.length;
        int m = reducedA[0].length;
        int n = reducedB[0].length;
        int[][] result = new int[l][n];
        for (int i = 0; i < l; ++i)
            for (int j = 0; j < n; ++j)
                for (int k = 0; k < m; ++k)
                    result[i][j] += reducedA[i][k] * reducedB[k][j];
        return result;
    }

    public static int[][] splitMatrixVertically(int startY, int endY, int[][] matrixB) {
        int rowLength = matrixB.length;
        int[][] reducedB = new int[rowLength][endY - startY];
        int columnIndex = 0;
        for (int i = 0; i < rowLength; i++) {
            for (int j = startY; j < endY; j++) {
                reducedB[i][columnIndex] = matrixB[i][j];
                columnIndex++;
            }
            columnIndex = 0;
        }
        return reducedB;
    }

    public static int[][] splitMatrixHorizontally(int startX, int endX, int[][] matrixA) {
        int columnLength = matrixA[0].length;
        int[][] reducedA = new int[endX - startX][columnLength];
        int rowIndex = 0;
        for (int i = startX; i < endX; i++) {
            for (int j = 0; j < columnLength; j++) {
                reducedA[rowIndex][j] = matrixA[i][j];
            }
            rowIndex++;
        }
        return reducedA;
    }
}
