package com.mobilespark.slave;

public class MatrixMultiplication {
    private static String[][] matrixA = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "1" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "2" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "3" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "4" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "5" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "6" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "7" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "8" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "9" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "1" }
    };

    private static String[][] matrixB = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "1" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "2" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "3" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "4" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "5" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "6" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "7" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "8" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "9" },
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "1" }
    };

    public static int[][] multiply(int[][] matrixA,int[][] matrixB) {
        int[][] result = multiplyMatrix(matrixA, matrixB);
        return result;
    }

    private static int[][] multiplyMatrix(int[][] reducedA, int[][] reducedB) {
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

    private static int[][] splitMatrixVertically(int startY, int endY, String[][] matrixB) {
        int rowLength = matrixB.length;
        int[][] reducedB = new int[rowLength][endY - startY + 1];
        int columnIndex = 0;
        for (int i = 0; i < rowLength; i++) {
            for (int j = startY; j <= endY; j++) {
                reducedB[i][columnIndex] = Integer.parseInt(matrixB[i][j]);
                columnIndex++;
            }
            columnIndex = 0;
        }
        return reducedB;
    }

    private static int[][] splitMatrixHorizontally(int startX, int endX, String[][] matrixA) {
        int columnLength = matrixA[0].length;
        int[][] reducedA = new int[endX - startX + 1][columnLength];
        int rowIndex = 0;
        for (int i = startX; i <= endX; i++) {
            for (int j = 0; j < columnLength; j++) {
                reducedA[rowIndex][j] = Integer.parseInt(matrixA[i][j]);
            }
            rowIndex++;
        }
        return reducedA;
    }
}
