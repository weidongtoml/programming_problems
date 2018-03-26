import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Implementationm of Point QuadTree
 */
public class QuadTree<E> {
    public static class Point {
        private final int x;
        private final int y;

        public Point(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public boolean equals(final Object p) {
            if (p instanceof Point) {
                final Point point = (Point)p;
                return x == point.getX() && y == point.getY();
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", x, y);
        }
    }

    private class Node {
        private final Point position;
        private final E data;

        public Node(final Point p, final E d) {
            position = p;
            data = d;
        }

        public Point getPosition() {
            return position;
        }

        public E getData() {
            return data;
        }
    }

    private final Point topLeft;
    private final Point bottomRight;
    private Node data;
    QuadTree<E> topLeftTree;
    QuadTree<E> topRightTree;
    QuadTree<E> bottomLeftTree;
    QuadTree<E> bottomRightTree;
    QuadTree<E> parentTree;

    /**
     * Create an empty QuadTree that covers the rectangular area specified
     * by the two points.
     */
    public QuadTree(final Point topLeft, final Point bottomRight) {
        this(topLeft, bottomRight, null);
    }

    private QuadTree(final Point topLeft, final Point bottomRight, final QuadTree parent) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        parentTree = parent;
    }

    /**
     * Determine if a given point is bounded within the area spanned by this QuadTree.
     */
    private boolean withinBoundary(final Point p) {
        return isPointWithinBoundary(p, topLeft, bottomRight);
    }

    /**
     * Inserts the given point and the associate data into the tree.
     *
     * @return true if the given point has been successuly inserted into the tree,
     * false if the given point is outside the boundary.
     */
    public boolean insert(final Point p, final E data) {
        if (!withinBoundary(p)) {
            return false;
        }

        QuadTree<E> root = this;
        // searching down the tree to find a subtree that has space to
        // insert the data, when enountered a leaf node, subdivide it
        // into 4 quadrants to make space.
        while (root != null) {
            // this is a leaf node, we can just add the data point.
            if (root.data == null) {
                root.data = new Node(p, data);
                return true;
            }
            root = getSubTree(root, p, true);
        }
        return false;
    }

    /**
     * Deletes the given point from the quadtree
     *
     * @return true on success, false if point not found.
     */
    public boolean delete(final Point p) {
        // a simple idea is setting data to null when we find the point
        // if this is a leaf node, we can delete the curent node from
        // parent, and do similar check for parent node 
        QuadTree node = searchNode(p);
        if (node != null) {
            node.data = null; // remove data
            while (node != null) { // remove any node that has empty data
                final QuadTree parent = node.parentTree;
                if (node.data == null && !Arrays.asList(node.getChildren()).contains(null)) {
                    if (parent != null) {
                        node.parentTree = null;
                        if (parent.topLeftTree == node) {
                            parent.topLeftTree = null;
                        } else if (parent.bottomLeftTree == node) {
                            parent.bottomLeftTree = null;
                        } else if (parent.topRightTree == node) {
                            parent.topRightTree = null;
                        } else if (parent.bottomRightTree == node) {
                            parent.bottomRightTree = null;
                        }
                    }
                }
                node = parent;
            }
            return true;
        }
        return false;
    }

    public E searchPoint(final Point p) {
        final QuadTree<E> node = searchNode(p);
        return node != null ? node.data.getData() : null;
    }

    private QuadTree searchNode(final Point p) {
        if (!withinBoundary(p)) {
            return null;
        }

        QuadTree node = this;
        while (node != null) {
            if (node.data != null && node.data.getPosition().equals(p)) {
                return node;
            }
            node = getSubTree(node, p, false);
        }

        return null;
    }

    private QuadTree<E>[] getChildren() {
        return new QuadTree[] {
            topLeftTree, bottomLeftTree, topRightTree, bottomRightTree
        };
    }

    public List<E> searchArea(final Point topLeft, final Point bottomRight) {
        final List<E> result = new LinkedList<>();

        final Queue<QuadTree> queue = new LinkedList<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            final QuadTree<E> root = queue.poll();
            if (root.data != null) {
                final Node node = root.data;
                if (isPointWithinBoundary(node.getPosition(), topLeft, bottomRight)) {
                    result.add(node.getData());
                }
            }
            for (final QuadTree<E> child : root.getChildren()) {
                // further improvement could be make by checking if the subtree is completely contained 
                // by the given area, if so, simply add all the children to the result.
                if (child != null && intersects(child.topLeft, child.bottomRight, topLeft, bottomRight)) {
                    queue.add(child);
                }
            }
        }
        return result;
    }

    private static boolean isPointWithinBoundary(final Point point, final Point topLeft, final Point bottomRight) {
        return topLeft.getX() <= point.getX() &&
            topLeft.getY() >= point.getY() &&
            bottomRight.getX() >= point.getX() &&
            bottomRight.getY() <= point.getY();
    }

    private static boolean intersects(
            final Point topLeft1,
            final Point bottomRight1,
            final Point topLeft2,
            final Point bottomRight2) 
    {
        // two rectangle intersects if one of them contains a point from the other
        for (final Point p : getCorners(topLeft1, bottomRight1)) {
            if (isPointWithinBoundary(p, topLeft2, bottomRight2)) {
                return true;
            }
        }
        for (final Point p : getCorners(topLeft2, bottomRight2)) {
            if (isPointWithinBoundary(p, topLeft1, bottomRight1)) {
                return true;
            }
        }
        return false;   
    }

    private static Point[] getCorners(final Point topLeft, final Point bottomRight) {
        return new Point[]{
            topLeft,
            new Point(bottomRight.getX(), topLeft.getY()),
            bottomRight,
            new Point(topLeft.getX(), bottomRight.getY())
        };
    }

    private static QuadTree getSubTree(final QuadTree node, final Point p, final boolean doDivision) {
        // check which subtree to insert into
        //   xDivide
        //      ^
        //   Q4 | Q1
        //   ---0----> yDivide
        //   Q3 | Q2
        //
        final Point topLeft = node.topLeft;
        final Point bottomRight = node.bottomRight;
        final int xDivide = (node.topLeft.getX() + node.bottomRight.getX()) / 2;
        final int yDivide = (node.topLeft.getY() + node.bottomRight.getY()) / 2;
        final QuadTree child;
        if (p.getX() <= xDivide) {
            if (p.getY() <= yDivide) { 
                // we are in Q3
                if (node.bottomLeftTree == null && doDivision) {
                    node.bottomLeftTree = new QuadTree<>(
                            new Point(topLeft.getX(), yDivide),
                            new Point(xDivide, bottomRight.getY()),
                            node);
                }
                child = node.bottomLeftTree;
            } else {
                // we are in Q4
                if (node.topLeftTree == null && doDivision) {
                    node.topLeftTree = new QuadTree<>(
                            topLeft,
                            new Point(xDivide, yDivide),
                            node);
                }
                child = node.topLeftTree;
            }
        } else {
            if (p.getY() <= yDivide) {
                // we are in Q2
                if (node.bottomRightTree == null && doDivision) {
                   node.bottomRightTree = new QuadTree<>(
                            new Point(xDivide, yDivide),
                            bottomRight,
                            node);
                }
                child = node.bottomRightTree;
            } else {
                // we are in Q1
                if (node.topRightTree == null && doDivision) {
                    node.topRightTree = new QuadTree<>(
                            new Point(xDivide, topLeft.getY()),
                            new Point(bottomRight.getX(), yDivide),
                            node);
                }
                child = node.topRightTree;
            }
        }
        return child;
    }

    public static void main(final String[] args) {
        final QuadTree<Integer> quadTree = new QuadTree<>(
                new Point(0, 10),
                new Point(10, 0));
        final Point p = new Point(5, 5);
        quadTree.insert(p, 10);
        System.out.println(quadTree.searchPoint(p));  // => 10
        System.out.println(quadTree.searchPoint(new Point(2, 2))); // => null

        quadTree.insert(new Point(0, 0), 0);
        quadTree.insert(new Point(8, 8), 16);
        quadTree.insert(new Point(1, 1), 2);
        quadTree.insert(new Point(9, 9), 18);
        quadTree.insert(new Point(2, 2), 4);
        quadTree.insert(new Point(1, 2), 3);
        quadTree.insert(new Point(3, 3), 6);
        System.out.println(quadTree.searchArea(new Point(0, 5), new Point(5, 0))); // => [10, 0, 2, 6, 3, 4]

        quadTree.delete(new Point(2, 2));
        quadTree.delete(new Point(1, 2));
        System.out.println(quadTree.searchArea(new Point(0, 5), new Point(5, 0))); // => [10, 0, 2, 6]

        quadTree.delete(new Point(0, 0));
        quadTree.delete(new Point(8, 8));
        quadTree.delete(new Point(1, 1));
        quadTree.delete(new Point(9, 9));
        quadTree.delete(new Point(3, 3));
        System.out.println(quadTree.searchArea(new Point(0, 5), new Point(5, 0))); // => [10]

        quadTree.delete(new Point(5, 5));
        System.out.println(quadTree.searchArea(new Point(0, 5), new Point(5, 0))); // => []
        System.out.println(quadTree.searchArea(new Point(0, 10), new Point(10, 0))); // => []
    }
}

