/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: btree_is_subtree.cc
 * Date: 2014.05.05
 * Description:
 *   Determine whether a given tree is a subtree of another.
 */
#include <iostream>

using std::cout;
using std::endl;

struct Node {
  Node* left;
  Node* right;
  int val;
};

/**
 * BTreeContainsSubtree determines whether tree1 matches tree2.
 */
bool BTreeContainsSubtree(Node* tree1, Node* tree2) {
  if (tree2 == nullptr) {
    return true;
  }
  if (tree1 == nullptr) {
    return false;
  }
  return tree1->val == tree2->val 
    && BTreeContainsSubtree(tree1->left, tree2->left)
    && BTreeContainsSubtree(tree1->right, tree2->right);
}

/**
 * BTreeHasSubtree check if tree2 is a subtree of tree1.
 * Current implementation has a time complexity of O(n*m) where n and m are the
 * number of nodes in tree1 and tree2 respectively.
 */
bool BTreeHasSubtree(Node* tree1, Node* tree2) {
  if (BTreeContainsSubtree(tree1, tree2)) {
    return true;
  }
  if (tree1 != nullptr) {
    return BTreeHasSubtree(tree1->left, tree2)
      || BTreeHasSubtree(tree1->right, tree2);
  }
  return false;
}

int main() {
  // Tree1
  //        n0
  //     n1    n2
  //  n3  n4 n5  n6
  //     n7
  //      n8
  Node nodes[9];
  nodes[0].val = 0;
  nodes[0].left = &nodes[1];
  nodes[0].right = &nodes[2];
  nodes[1].val = 1;
  nodes[1].left = &nodes[3];
  nodes[1].right = &nodes[4];
  nodes[3].val = 3;
  nodes[3].left = nodes[3].right = nullptr;
  nodes[4].val = 4;
  nodes[4].left = &nodes[7];
  nodes[4].right = nullptr;
  nodes[7].val = 7;
  nodes[7].left = nullptr;
  nodes[7].right = &nodes[8];
  nodes[8].val = 8;
  nodes[8].left = nodes[8].right = nullptr;
  nodes[2].val = 2;
  nodes[2].left = &nodes[5];
  nodes[2].right = &nodes[6];
  nodes[5].val = 5;
  nodes[5].left = nodes[5].right = nullptr;
  nodes[6].val = 6;
  nodes[6].left = nodes[6].right = nullptr;
  
  // Tree2
  //   n4
  //  n7
  //   n8
  Node nodes1[3];
  nodes1[0].val = 4;
  nodes1[0].left = &nodes1[1];
  nodes1[0].right = nullptr;
  nodes1[1].val = 7;
  nodes1[1].left = nullptr;
  nodes1[1].right = &nodes1[2];
  nodes1[2].val = 8;
  nodes1[2].left = nodes1[2].right = nullptr;
  
  int num_errors = 0;
  if (BTreeHasSubtree(&nodes[4], &nodes1[0]) != true) {
    cout << "Failed in test case 0" << endl;
    ++num_errors;
  }
  if (BTreeHasSubtree(&nodes[1], &nodes1[0]) != true) {
    cout << "Failed in test case 1" << endl;
    ++num_errors;
  }
  if (BTreeHasSubtree(&nodes[0], &nodes1[0]) != true) {
    cout << "Failed in test case 2" << endl;
    ++num_errors;
  }
  if (BTreeHasSubtree(&nodes[3], &nodes1[0]) != false) {
    cout << "Failed in test case 3" << endl;
    ++num_errors;
  }
  if (BTreeHasSubtree(&nodes[2], &nodes1[0]) != false) {
    cout << "Failed in test case 4" << endl;
    ++num_errors;
  }
  if (BTreeHasSubtree(nullptr, &nodes1[0]) != false) {
    cout << "Failed in test case 5" << endl;
    ++num_errors;
  }
  if (BTreeHasSubtree(&nodes[0], nullptr) != true) {
    cout << "Failed in test case 6" << endl;
    ++num_errors;
  }
  return num_errors;
}
