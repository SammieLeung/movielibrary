package com.firelfy.util;

/**
 * 检测两个字符串相似度的算法
 *
 * @author lxp
 * @date 20-5-28
 */
public class EditorDistance {
    /**
     * 获取字符串相似度(忽略大小写）
     *
     * @param strA
     * @param strB
     * @return
     */

    public static float checkLevenshtein(String strA, String strB) {
        return checkLevenshtein(strA, strB, false);
    }

    /**
     * 获取字符串相似度
     *
     * @param strA
     * @param strB
     * @param isMatchCase 是否忽略大小写
     * @return
     */
    public static float checkLevenshtein(String strA, String strB, boolean isMatchCase) {
        if(strA==null||strB==null)
            return 0;
        if (!isMatchCase) {
            strA = strA.toLowerCase();
            strB = strB.toLowerCase();
        }
        char[] charAs = strA.toCharArray();
        char[] charBs = strB.toCharArray();
        //计算两个字符串的长度。
        int lenA = charAs.length;
        int lenB = charBs.length;
        //建二维数组，比字符长度大一个空间
        int[][] difIntDArr = new int[lenA + 1][lenB + 1];
        //赋初值
        for (int a = 0; a <= lenA; a++) {
            difIntDArr[a][0] = a;
        }
        for (int a = 0; a <= lenB; a++) {
            difIntDArr[0][a] = a;
        }
        //计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= lenA; i++) {
            for (int j = 1; j <= lenB; j++) {
                if (charAs[i - 1] == charBs[j - 1]) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //取三个值中最小的
                difIntDArr[i][j] = min(difIntDArr[i - 1][j - 1] + temp, difIntDArr[i][j - 1] + 1, difIntDArr[i - 1][j] + 1);
            }
        }
        //计算相似度
        float similarity = 1 - (float) difIntDArr[lenA][lenB] / Math.max(lenA, lenB);
        return similarity;
    }

    /**
     * 求最小值
     *
     * @param nums
     * @return
     */
    private static int min(int... nums) {
        int min = Integer.MAX_VALUE;
        for (int item : nums) {
            if (min > item) {
                min = item;
            }
        }
        return min;
    }
}
