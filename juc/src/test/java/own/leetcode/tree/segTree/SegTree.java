package own.leetcode.tree.segTree;

import own.Printer;

public class SegTree {

    private int[] tree;
    private int[] arr;
    private int size;

    public SegTree(int[] arr) {
        this.arr = arr;
        this.size = arr.length;
        tree = new int[size * 4];

        build_tree(0, 0, size - 1);
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 7, 9, 11};
        SegTree segTree = new SegTree(arr);
        Printer.print(segTree.getTree());
        System.out.println();
        segTree.update(1, 10);
        Printer.print(segTree.getTree());

        System.out.println(segTree.query(2, 5));
    }

    public int[] getTree() {
        return tree;
    }

    private void build_tree(int node, int start, int end) {

        if (start == end) {
            tree[node] = arr[start];
            return;
        }

        int left_node = 2 * node + 1;
        int right_node = 2 * node + 2;
        int mid = (start + end) >> 1;
        build_tree(left_node, start, mid);
        build_tree(right_node, mid + 1, end);

        tree[node] = tree[left_node] + tree[right_node];
    }

    public void update(int idx, int val) {
        update_tree(0, 0, size - 1, idx, val);
    }

    private void update_tree(int node, int start, int end, int idx, int val) {

        if (start == end) {
            arr[idx] = val;
            tree[node] = val;
            return;
        }

        int mid = (start + end) >> 1;
        int left_node = 2 * node + 1;
        int right_node = 2 * node + 2;
        if (idx >= start && idx <= mid) {
            update_tree(left_node, start, mid, idx, val);
        } else {
            update_tree(right_node, mid + 1, end, idx, val);
        }

        tree[node] = tree[left_node] + tree[right_node];
    }

    public int query(int L, int R) {
        return query_tree(0, 0, size - 1, L, R);
    }

    private int query_tree(int node, int start, int end, int L, int R) {
        System.out.println(start + ", " + end);
        if (L > end || R < start) {
            return 0;
        } else if (L <= start && end <= R) {
            return tree[node];
        } else if (start == end) {
            return tree[node];
        } else {

            int mid = (start + end) >> 1;
            int left_node = 2 * node + 1;
            int right_node = 2 * node + 2;

            int left_sum = query_tree(left_node, start, mid, L, R);
            int right_sum = query_tree(right_node, mid + 1, end, L, R);
            return left_sum + right_sum;
        }
    }

}
