import java.util.ArrayList;
import java.util.function.Function;
import java.util.Iterator;
import java.util.Optional;
import java.lang.StringBuilder;

/**
 * Implementation of Red Black Tree.
 * A Red Black Tree is a balanced binary search tree that maintain the following properties:
 * (1) every node is either RED or BLACK
 * (2) root of the tree is always BLACK
 * (3) there are no two adjacent red nodes, i.e. a red node cannot have a red parent or red child
 * (4) every path from root to an empty node has same number of black nodes.
 *
 * Reference:
 * https://www.geeksforgeeks.org/red-black-tree-set-1-introduction-2/
 * https://www.geeksforgeeks.org/red-black-tree-set-2-insert/
 * https://www.geeksforgeeks.org/red-black-tree-set-3-delete-2/
 */
public class RBTree<K extends Comparable<K>, V> {
    private Optional<Node> root;

    public RBTree() {
        root = Optional.empty();
    }

    /**
     * @return max height of the tree
     */
    int height() {
        return getHeight(root);        
    }

    private int getHeight(Optional<Node> node) {
        if (node.isPresent()) {
            return 1 + Math.max(
                    getHeight(node.flatMap(Node::getLeft)), 
                    getHeight(node.flatMap(Node::getRight)));
        } else {
            return 0;
        }
    }
    
    /**
     * retrieve the value associated with the given key
     * @param key: key to search for
     * @return optional of value
     *
     * Note: simply iterate down the tree as a simple binary search tree.
     * Runtime complexity is O(log n), i.e. linear to the height of the tree.
     */
    public Optional<V> get(final K key) {
        return search(key).map(Node::getValue);
    }

    private Optional<Node> search(final K key) {
        Optional<Node> node = root;
        while (node.isPresent()) {
            final Node p = node.get();
            final int r = key.compareTo(p.getKey());
            if (r < 0) {
                node = p.getLeft();
            } else if (r > 0) {
                node = p.getRight();
            } else {
                return node;
            }
        }
        return Optional.empty();
    }

    /**
     * insert the given key and value into the tree
     * @param key: key
     * @param vallue: value
     *
     * Implementation note:
     * Insert the new node in the same manner as a simple binary search tree by traversing down the tree,
     * invoke rebalance method on the newly inserted node, which either do rotation or re-coloring, 
     * traversing up the tree.
     * Hence the runtime complexity is O(log n), where n is the number of elements in the tree,
     * i.e. linear to the height of the tree.
     */
    public void set(final K key, final V value) {
        final Optional<Node> insertedNode;
        if (root.isPresent()) {
            Optional<Node> node = root;
            while (true) {
                final Node p = node.get();
                final int r = key.compareTo(p.getKey());;
                if (r < 0) {
                    final Optional<Node> left = p.getLeft();
                    if (left.isPresent()) {
                        node = left;
                    } else {
                        insertedNode = Optional.of(new Node(key, value, node));
                        p.setLeft(insertedNode);
                        break;
                    }
                } else if (r > 0) {
                    final Optional<Node> right = p.getRight();
                    if (right.isPresent()) {
                        node = right;
                    } else {
                        insertedNode = Optional.of(new Node(key, value, node));
                        p.setRight(insertedNode);
                        break;
                    } 
                } else {
                    p.setValue(value);
                    insertedNode = Optional.empty();
                    break;
                }
            }
        } else {
            root = Optional.of(new Node(key, value, Optional.empty()));
            insertedNode = root;
        }
        System.out.printf("Before rebalance: \n%s\n", toString());
        rebalance(insertedNode);
        System.out.printf("After rebalance: \n%s\n\n", toString());
    }

    /**
     * Remove the given value associated with the key.
     * @param key: key to search for
     */
    public void remove(final K key) {
        System.out.printf("Before removal of %s:\n%s", key.toString(), toString());
        Optional<Node> node = search(key);
        while (node.isPresent()) {
            Node n = node.get();
            final int numChildren = n.numChildren();
            if (numChildren == 0) {
                // (1) no children --> simply remove
                //     p            p
                //     n     -->     
                //
                System.out.println("replaceWith empty");
                replaceWith(node, Optional.empty());
                break;
            } else if (numChildren == 1) {
                // (2) not left child  --> replace with right child
                //     p                p 
                //     n      -->       r 
                //        r           T1  T2 
                //      T1  T2
                //
                // (3) no right child --> replace with left child
                //      p               p
                //      n    -->        l
                //   l               T1   T2
                // T1 T2
                //
                if (n.getLeft().isPresent()) {
                    System.out.println("replaceWith left");
                    replaceWith(node, n.getLeft());
                } else {
                    System.out.println("replaceWith right");
                    replaceWith(node, n.getRight());
                }
                break;
            } else {
                // (4) two children   --> swap the cotent of node with its successor
                // then attempt to delete successor
                //        p                        p
                //        n            -->         s 
                //     l      r                l       r
                //  T1  T2  T3  T4          T1  T2   T3  T4
                final Optional<Node> sucessor = getSuccessor(node);
                System.out.printf("replaceWith successor: %s\n", sucessor.get().getKey().toString());
                n.swapWith(sucessor.get());
                node = sucessor;
            }
        }

        System.out.printf("After removal:\n%s\n", toString());
        // TOOD(weidong): do rebalance
    }

    /**
     * @return iterator to loop through keys in the RBTree in increasing order.
     */
    public Iterator<K> getIterator() {
        return new TreeIterator(getMinimum(root), n -> getSuccessor(n));
    }

    /**
     * @return iterator to loop through the keys in the RBTree in decreasing order.
     */
    public Iterator<K> getReverseIterator() {
        return new TreeIterator(getMaximum(root), n -> getPredecessor(n));
    }

    private void replaceWith(final Optional<Node> node, final Optional<Node> replace) {
        if (!node.isPresent()) {
            return;
        }
        final Node n = node.get();
        final Optional<Node> parent = n.getParent();

        if (node == root) {
            root = replace;
        } else {
            final Node p = parent.get();
            if (n.isLeftChild()) {
                p.setLeft(replace);
            } else {
                p.setRight(replace);
            }
        }
        n.setParent(Optional.empty());

        if (replace.isPresent()) {
            final Node r = replace.get();
            r.getParent().ifPresent(p -> {
                if (r.isLeftChild()) {
                    p.setLeft(Optional.empty());
                } else {
                    p.setRight(Optional.empty());
                }
            });
            r.setParent(parent);
        }
    }

    /**
     * @return the successor of the given node
     */
    private Optional<Node> getSuccessor(final Optional<Node> node) {
        if (!node.isPresent()) {
            return Optional.empty();
        }
        final Node n = node.get();
        if (n.getRight().isPresent()) {
            return getMinimum(n.getRight());
        } else {
            // find the ancestor who is a left child, then the successor would be the parent of this ancestor.
            //           s
            //        r       T2
            //     T1   .
            //            .
            //              n
            //  successor of n is r's parent, s.
            Optional<Node> r = node;
            while (r.isPresent() && r.get().isRightChild()) {
                r = r.flatMap(Node::getParent);
            }
            return r.flatMap(Node::getParent);
        }
    }

    /**
     * @return predecessor of the given node
     *
     * Note: this is just a mirror of getSuccessor
     */
    private Optional<Node> getPredecessor(final Optional<Node> node) {
        if (!node.isPresent()) {
            return Optional.empty();
        }
        final Node n = node.get();
        if (n.getLeft().isPresent()) {
            return getMaximum(n.getLeft());
        } else {
            Optional<Node> r = node;
            while (r.isPresent() && r.get().isLeftChild()) {
                r = r.flatMap(Node::getParent);
            }
            return r.flatMap(Node::getParent);
        }
    }

    /**
     * Output the binary search tree
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        final ArrayList<ArrayList<Optional<Node>>> lists = new ArrayList<>();
        lists.add(new ArrayList<>());
        lists.add(new ArrayList<>());
        int i = 0;
        lists.get(i).add(root);
        while (true) {
            final int j = (i + 1) % 2;
            
            int lastNonEmpty = -1;
            final ArrayList<Optional<Node>> curList = lists.get(i);
            for (int k = 0; k < curList.size(); k++) {
                if (curList.get(k).isPresent()) {
                    lastNonEmpty = k;
                }
            }
            if (lastNonEmpty == -1) {
                break;
            }

            for (int k = 0; k <= lastNonEmpty; k++) {
                final Optional<Node> n = curList.get(k);
                if (n.isPresent()) {
                    builder.append(String.format("%-13s", n.get().toString()));
                    lists.get(j).add(n.flatMap(Node::getLeft));
                    lists.get(j).add(n.flatMap(Node::getRight));
                } else {
                    builder.append(String.format("%-13s", "(nil)"));
                    lists.get(j).add(Optional.empty());
                    lists.get(j).add(Optional.empty());
                }
                builder.append(' ');
            }
            builder.append("\n");
            lists.get(i).clear();
            i = j;
        }

        return builder.toString();
    }

    /**
     * Rebalance the Red Black Tree using either re-coloring or rotation,
     * depending on the color of uncle node.
     * If uncle node is RED, then do re-color, otherwise, do rotation.
     * Rotation depends on the location of parent and current node as related to grandparent node.
     * left-left: 
     *      parent is left child of grandparent, and node is left child of parent
     *      rotate grandparent to the right, swap color for grandparent and parent
     * right-right: 
     *      parent is right child of grandparent, and node is right child of parent
     *      rotate grandparant to the left, swap color for grandparent and parent
     * left-right:
     *      parent is left child of grandparent, and node is right child of parent
     *      rotate parent to the left, set current node to the rotated parent, becomes left-left case
     * right-left:
     *      parent is right child of grandparent, and node is the left child of parent
     *      rotate parent to the right, set current node to the rotated parent, becomes right-right case
     */
    void rebalance(Optional<Node> node) {
        // since node is the newly inserted node, we expect it always be RED.
        // we adjust the color or do rotation from this node all the way up
        // to root node to ensure the properties are satisified.
        while (node.isPresent()) {
            final Node n = node.get();
            if (n.getColor().equals(Color.BLACK)) { // there's nothing needed to be done here
                break;
            }

            final Optional<Node> parent = n.getParent();
            // root node is always black
            if (!parent.isPresent()) {
                n.setColor(Color.BLACK);
                break;
            } 

            final Node p = parent.get();
            // nothing further need to get done since the parent is BLACK
            if (p.getColor().equals(Color.BLACK)) {
                break;
            }

            // here we have both node and its parent RED, need to adjust this RED-RED
            // to maintain property 3.
            // grand parent must exist according to property 2 and 4.
            final Optional<Node> uncle = n.getUncle();
            final Optional<Node> grand = p.getParent();
            final Node g = grand.get();
            if (uncle.filter(u -> u.getColor().equals(Color.RED)).isPresent()) {
                // must change p an p's sibling to BLACK and g to RED
                //           g(B)                  g(R)
                //      p(R)      u(R) -->    p(B)      u(B)
                //   x(R)  T3   T4  T5      x(R) T3   T4   T5
                // T1  T2                 T1  T2
                //
                g.getLeft().ifPresent(v -> v.setColor(Color.BLACK));
                g.getRight().ifPresent(v -> v.setColor(Color.BLACK));
                g.setColor(Color.RED);
                // move up to grand parent
                node = grand;
                System.out.println("Re-color");
            } else {
                // either uncle is BLACK, or uncle is empty, which by definition is also BLACK
                // there are 4 different cases
                if (parent == g.getLeft()) {
                    if (node == p.getLeft()) {
                        // (1) Left - Left
                        //           g(B)                  p(B)
                        //      p(R)      u(B) -->    x(R)      g(R)
                        //   x(R)  T3   T4  T5      T1   T2   T3   u(B)
                        // T1  T2                                T4   T5
                        //               (1) right rotate g
                        //               (2) swap colors of g and p
                        System.out.println("left-left");
                        Optional<Node> res = rightRotate(grand);
                        if (grand == root) {
                            root = res;
                        }
                        swapColor(grand, parent);
                        break;
                    } else {
                        // (2) Left - Right
                        //              g(B)                     g(B)                      x(B)
                        //        p(R)       u(B)    -->     x(R)    u(B)    -->      p(R)      g(R)
                        //      T1   x(R)  T4   T5       p(R)   T3  T4  T5         T1    T2   T3   u(B)
                        //          T2  T3             T1   T2                                    T4   T5
                        //               (1) left rotate p             (2) apply Left-Left case above
                        System.out.println("left-right");
                        leftRotate(parent);
                        node = parent;
                    }
                } else {
                    if (node == p.getRight()) {
                        // (3) Right - Right
                        //           g(B)                          p(B)
                        //     u(B)       p(R)        -->    g(R)         x(R)
                        //   T1   T2    T3   x(R)         u(B)  T3      T4   T5
                        //                 T4   T5       T1  T2
                        //              (1) left rotate g
                        //              (2) swap colors of g and p
                        System.out.println("right-right");
                        Optional<Node> res = leftRotate(grand);
                        if (grand == root) {
                            root = res;
                        }
                        swapColor(grand, parent);
                        break;
                    } else {
                        // (4) Right - Left
                        //              g(B)                     g(B)                       x(B)
                        //        u(B)       p(R)    -->     u(B)    x(R)    -->       g(R)      p(R)
                        //      T1   T2   x(R)   T5        T1  T2  T3   p(R)        u(B)   T3   T4   T5
                        //               T3  T4                       T4   T5      T1  T2
                        //              (1) right rotate p            (2) apply Right-Right case above
                        System.out.println("right-left");
                        rightRotate(parent);
                        node = parent; 
                    }
                }
            }
        }
    }

    /**
     * swaps the color for the given 2 nodes if they are different.
     */
    private void swapColor(final Optional<Node> n1, final Optional<Node> n2) {
        final Color c1 = n1.isPresent() ? n1.get().getColor() : Color.BLACK;
        final Color c2 = n2.isPresent() ? n2.get().getColor() : Color.BLACK;
        if (c1 != c2) {
            n1.ifPresent(n -> n.setColor(c2));
            n2.ifPresent(n -> n.setColor(c1));
        }
    }

    // Rotate n to the left and return the root of the new subtree
    // note that tis does not change the binary search tree property
    //        n                T2
    //    T1    T2     -->   n   T4
    //        T3  T4       T1 T3
    private Optional<Node> leftRotate(final Optional<Node> n) {
        final Optional<Node> p = n.flatMap(Node::getParent);
        final Optional<Node> t2 = n.flatMap(Node::getRight);
        final Optional<Node> t3 = t2.flatMap(Node::getLeft);

        t2.ifPresent(t -> t.setParent(p));
        p.ifPresent(t -> {
            if (t.getLeft() == n) {
                t.setLeft(t2);
            } else {
                t.setRight(t2);
            }
        });

        t2.ifPresent(t -> t.setLeft(n));
        n.ifPresent(t -> t.setParent(t2));

        n.ifPresent(t -> t.setRight(t3));
        t3.ifPresent(t -> t.setParent(n));

        return t2;
    }

    // Rotate n to the right and return the root fo the new subtree
    // note that this does not change the binary search tree property
    //         n                   T1
    //     T1     T2      -->  T3      n 
    //   T3  T4                     T4   T2 
    private Optional<Node> rightRotate(final Optional<Node> n) {
        final Optional<Node> p = n.flatMap(Node::getParent);
        final Optional<Node> t1 = n.flatMap(Node::getLeft);
        final Optional<Node> t4 = t1.flatMap(Node::getRight); 
        t1.ifPresent(t -> t.setParent(p));
        p.ifPresent(t -> {
            if (t.getLeft() == n) {
                t.setLeft(t1);
            } else {
                t.setRight(t1);
            }
        });

        t1.ifPresent(t -> t.setRight(n));
        n.ifPresent(t -> t.setParent(t1));

        n.ifPresent(t -> t.setLeft(t4));
        t4.ifPresent(t -> t.setParent(n));
        return t1;
    }

    private static enum Color {
        RED,
        BLACK
    }

    private Optional<Node> getMinimum(Optional<Node> node) {
        return getLeafNode(Node::getLeft, node);
    }

    private Optional<Node> getMaximum(Optional<Node> node) {
        return getLeafNode(Node::getRight, node);
    }

    private Optional<Node> getLeafNode(final Function<Node, Optional<Node>> strategy, final Optional<Node> node) {
        Optional<Node> n = node;
        while (n.flatMap(strategy).isPresent()) {
            n = n.flatMap(strategy);
        }
        return n;
    }

    private class TreeIterator implements Iterator<K> {
        private Optional<Node> node;
        private Function<Optional<Node>, Optional<Node>> getNext;

        TreeIterator(final Optional<Node> node, final Function<Optional<Node>, Optional<Node>> getNext) {
            this.node = node;
            this.getNext = getNext;
        }

        @Override
        public boolean hasNext() {
            return node.isPresent();
        }

        @Override
        public K next() {
            final Node n = node.get();
            node = getNext.apply(node);
            return n.getKey();
        }

        @Override
        public void remove() {
        } 
    }

    private class Node {
        private K key;
        private V value;
        private Color color;
        private Optional<Node> parent;
        private Optional<Node> left;
        private Optional<Node> right;

        Node(final K key, final V value, final Optional<Node> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
            this.color = Color.RED; // newly inserted node gets RED as the color
            left = Optional.empty();
            right = Optional.empty();
        }

        void swapWith(final Node n) {
            final K key1 = key;
            final V value1 = value;

            key = n.key;
            value = n.value;

            n.key = key1;
            n.value = value1;
        }

        K getKey() {
            return key;
        }

        V getValue() {
            return value;
        }

        void setValue(V value) {
            this.value = value;
        }

        Color getColor() {
            return color;
        }

        void setColor(final Color color) {
            this.color = color;
        }

        Optional<Node> getLeft() {
            return left;
        }

        void setLeft(final Optional<Node> n) {
            left = n;
        }

        Optional<Node> getRight() {
            return right;
        }

        void setRight(final Optional<Node> n) {
            right = n;
        }

        Optional<Node> getParent() {
            return parent;
        }

        void setParent(final Optional<Node> n) {
            parent = n;
        }

        Optional<Node> getUncle() {
            return parent.flatMap(Node::getSibling);
        }

        boolean isLeftChild() {
            return parent.flatMap(Node::getLeft).filter(n -> n == this).isPresent();
        }

        boolean isRightChild() {
            return parent.flatMap(Node::getRight).filter(n -> n == this).isPresent();
        }

        Optional<Node> getSibling() {
            return parent.flatMap(isLeftChild() ? Node::getRight : Node::getLeft);
        }

        int numChildren() {
            return (left.isPresent() ? 1 : 0) + (right.isPresent() ? 1 : 0);
        }

        @Override
        public String toString() {
            return String.format("%s->%s(%s)", key.toString(), value.toString(), color.toString());
        }
    }

    public static void main(final String[] args) {
        final RBTree<Integer, Character> rbtree = new RBTree();
        System.out.println("Start insertion:");
        rbtree.set(0, 'a');
        rbtree.set(5, 'b');
        rbtree.set(10, 'c');  // right-right => rotate-left-g
        rbtree.set(-5, 'd');  // RED uncle => re-color => re-color-root
        rbtree.set(-10, 'e');  // left-left => rotate-right-g
        rbtree.set(-3, 'f');  // RED uncle => re-color
        rbtree.set(-2, 'g');  // left-right => rotate-left-p => left-left => rotate-right-g
        rbtree.set(-1, 'h');  // RED uncle => re-color => left-right => rotate-left-p => left-left => rotate-right-g
        rbtree.set(-7, 'i');
        rbtree.set(-9, 'j');  // right-left => rotate-right-p => right-right => rotate-left-g
        rbtree.set(0, 'k');

        System.out.println("Do search:");
        System.out.println(rbtree.get(0).get()); // => 'k'
        System.out.println(rbtree.get(5).get()); // => 'b'
        System.out.println(rbtree.get(10).get()); // => 'c'
        System.out.println(rbtree.get(-5).get()); // => 'd'
        System.out.println(rbtree.get(-10).get()); // => 'e'
        System.out.println(rbtree.get(-3).get()); // => 'f'
        System.out.println(rbtree.get(-2).get()); // => 'g'
        System.out.println(rbtree.get(-1).get()); // => 'h'
        System.out.println(rbtree.get(-7).get()); // => 'i'
        System.out.println(rbtree.get(-9).get()); // => 'j'
        System.out.println(rbtree.get(31).isPresent());  // => false

        System.out.println("Iterate through all keys in increasing order:");
        final Iterator<Integer> iter = rbtree.getIterator();
        while (iter.hasNext()) {
            System.out.printf("%d ", iter.next());
        }
        System.out.println();

        System.out.println("Iterate through all keys in decreasing order:");
        final Iterator<Integer> rIter = rbtree.getReverseIterator();
        while (rIter.hasNext()) {
            System.out.printf("%d ", rIter.next());
        }
        System.out.println();


        System.out.println("Start deletion:");
        rbtree.remove(0);
        rbtree.remove(-10);
        rbtree.remove(-9);
        rbtree.remove(-2);
    }
}

