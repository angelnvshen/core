package own;

public class Solution {

    public static void main(String[] args) {
        Solution solution = new Solution();
        //solution.sortIntegers2(new int[]{3, 2, 1});
        int[] arr = {0, 1, 1, 3, 3, 3, 4, 6, 8,8, 10};
        System.out.println(solution.binarySearch(arr, -1));
//        System.out.println(solution.binarySearch(arr, 0));
//        System.out.println(solution.binarySearch(arr, 1));
//        System.out.println(solution.binarySearch(arr, 3));
//        System.out.println(solution.binarySearch(arr, 4));
//        System.out.println(solution.binarySearch(arr, 8));
//        System.out.println(solution.binarySearch(arr, 10));
    }

    // 查找第一个值等于给定值的元素
    public int binarySearch(int[] A, int x) {
        int n = A.length;
        int l = 0;
        int r = n - 1;
        int m = -1;

        while (l + 1 < r) {
            m = l + (r - l) / 2;
            if (A[m] >= x) {
                r = m;
            } else {
                l = m;
            }
        }
        if (A[l] == x) return l;
        if (A[r] == x) return r;
        return -1;
    }

    /**
     * @param A: an integer array
     * @return: nothing
     */
    public void sortIntegers2(int[] A) {
        // write your code here
        if (A == null || A.length <= 1) {
            return;
        }

        mergeSort(A, 0, A.length - 1);
    }

    public void mergeSort(int[] A, int start, int end) {
        if (start >= end) return;

        int mid = (end - start) / 2 + start;
        mergeSort(A, start, mid);
        mergeSort(A, mid + 1, end);
        merge(A, start, mid, end);
    }

    private void merge(int[] A, int s, int m, int e) {
        //System.out.print("s : " + s + ", m : " + m +", e : " + e + " => ");

        int[] tmp = new int[e - s + 1];
        int i = s, j = m + 1;
        int k = 0;
        while (i <= m && j <= e) {
            if (A[i] <= A[j]) {
                tmp[k++] = A[i++];
            } else {
                tmp[k++] = A[j++];
            }
        }

        System.out.println();
        System.out.println("i : " + i + ",j : " + j);
        int p = i, q = m;

        if (i == m + 1) {
            p = j;
            q = e;
        }
        System.out.println();
        System.out.println("p : " + p + ",q : " + q);
        while (p <= q) {
            tmp[k++] = A[p++];
        }

        System.out.print("s : " + s + ", m : " + m + ", e : " + e + " => ");

        i = s;
        for (k = 0; k <= tmp.length - 1; ) {
            A[i++] = tmp[k++];
        }

        // System.out.print("s : " + s + ", m : " + m +", e : " + e + " => ");
        // print(A, s, e);
        print(tmp, 0, tmp.length - 1);

    }

    private void print(int[] A, int s, int e) {
        for (int i = s; i <= e; i++) {
            System.out.print(A[i] + ", ");
        }
        System.out.println();
    }
}