package own;

public class OWN {

    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 7, 9, 11};
        SegTree segTree = new SegTree(arr);
        Printer.print(segTree.sum); // 0 36 9 27 4 5 16 11 1 3 0 0 7 9 0 0 0 0 0 0 0 0 0 0
        System.out.println();
        segTree.change(1, 10);
        Printer.print(segTree.sum); // 0 43 16 27 11 5 16 11 1 10 0 0 7 9 0 0 0 0 0 0 0 0 0 0
    }

    private static class SegTree {
        int[] arr;
        int[] sum;

        public SegTree(int[] arr) {
            this.arr = arr;
            int size = arr.length;
            this.sum = new int[size * 4];
            built(0, size - 1, 1);
        }

        /**
         * 构建
         *
         * @param l 原数组的左端点
         * @param r 原数组的右端点
         * @param x 构建数组的下标
         */
        private void built(int l, int r, int x) {
            if (l == r) {
                sum[x] = arr[l];
                return;
            }
            int mid = ((r - l) >> 1) + l;
            built(l, mid, 2 * x);
            built(mid + 1, r, 2 * x + 1);
            update(x);
        }

        private void update(int x) {
            sum[x] = sum[2 * x] + sum[2 * x + 1];
        }

        private int query(int l, int r, int A, int B, int x) {
            if (A <= l && r <= B) {
                return sum[x];
            }
            int mid = ((r - l) >> 1) + l, ans = 0;

            if (A <= mid) {
                ans += query(l, mid, A, B, 2 * x);
            }
            if (mid < B) {
                ans += query(mid + 1, r, A, B, 2 * x + 1);
            }
            return ans;
        }

        private void change(int pos, int v){
            change(0, arr.length - 1, 1, pos, v);
        }

        private void change(int l, int r, int x, int pos, int v) {
            if (l == r) {
                sum[x] = v;
                return;
            }
            int mid = ((r - l) >> 1) + l;
            if (pos <= mid) {
                change(l, mid, 2 * x, pos, v);
            } else {
                change(mid + 1, r, 2 * x + 1, pos, v);
            }
            update(x);
        }

    }

}