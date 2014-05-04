/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: btree_is_balanced.cc
 * Date: 2014.05.03
 * Description: check if the given binary tree is balanced or not.
 */
#include <algorithm>
#include <iostream>
#include <vector>

using std::cout;
using std::endl;
using std::vector;
using std::swap;
using std::max;
using std::min;

struct Node {
  Node* left;
  Node* right;
};

/**
 * BTreeIsBalanced check if the given binary is balanced or not.
 * Current implementation has a time complexity of O(n), and a space
 * complexity of O(2^lg(n)).
 */
bool BTreeIsBalanced(Node* tree) {
  if (tree == nullptr) {
    return true;
  }
  vector<Node*> levels[2];
  vector<Node*> *cur_level = &levels[0], *next_level = &levels[1];
  cur_level->push_back(tree);
  size_t cur_level_size = 1; // expected size of the cur level if it is full.
  while (!cur_level->empty()) {
    for (auto iter = cur_level->begin(); iter != cur_level->end(); ++iter) {
      if ((*iter)->left != nullptr) {
        next_level->push_back((*iter)->left);
      }
      if ((*iter)->right != nullptr) {
        next_level->push_back((*iter)->right);
      }
    }
    // cur level is not full, and next level is not empty, hence the tree
    // is not balanced.
    if (cur_level->size() != cur_level_size && !next_level->empty()) {
      return false;
    }
    cur_level_size <<= 1;
    swap(cur_level, next_level);
    next_level->clear();
  }
  return true;
}

size_t BTreeMaxDepth(Node* tree) {
  if (tree == nullptr) {
    return 0;
  }
  return 1 + max(BTreeMaxDepth(tree->left), BTreeMaxDepth(tree->right));
}

size_t BTreeMinDepth(Node* tree) {
  if (tree == nullptr) {
    return 0;
  }
  return 1 + min(BTreeMinDepth(tree->left), BTreeMinDepth(tree->right));
}

/**
 * BTreeIsBalanced2 is another implementation of BTreeIsBalanced which
 * has a time complexity of O(n).
 */
bool BTreeIsBalanced2(Node* tree) {
  const size_t max_depth = BTreeMaxDepth(tree);
  const size_t min_depth = BTreeMinDepth(tree);
  return (max_depth - min_depth) <= 1;
}

typedef bool (*BTreeIsBalancedFunc)(Node*);

int CheckImpl(BTreeIsBalancedFunc func) {
  int num_error = 0;
  Node nodes[5];
  Node* tree = &nodes[0];
  tree->left = tree->right = nullptr;
  if (!func(tree)) {
    //  n0
    cout << "Failed in test case 1" << endl;
    ++num_error;
  }
  tree->left = &nodes[1];
  nodes[1].left = nodes[1].right = nullptr;
  if (!func(tree)) {
    //   n0
    //  n1
    cout << "Failed in test case 2" << endl;
    ++num_error;
  }
  tree->right = &nodes[2];
  nodes[2].left = nodes[2].right = nullptr;
  if (!func(tree)) {
    //   n0
    // n1 n2
    cout << "Failed in test case 3" << endl;
    ++num_error;
  }
  nodes[1].left = &nodes[3];
  nodes[3].left = nodes[3].right = nullptr;
  nodes[0].right = nullptr;
  if (func(tree) != false) {
    //    n0
    //   n1
    // n3
    cout << "Failed in test case 4" << endl;
    ++num_error;
  }
  nodes[0].right = &nodes[2];
  if (func(tree) != true) {
    //     n0
    //   n1  n2
    // n3
    cout << "Failed in test case 5" << endl;
    ++num_error;
  }
  nodes[3].right = &nodes[4];
  nodes[4].left = nodes[4].right = nullptr;
  if (func(tree) != false) {
    //     n0
    //   n1  n2
    // n3
    //   n4
    cout << "Failed in test case 6" << endl;
    ++num_error;
  }
  nodes[2].right = &nodes[5];
  nodes[5].left = nodes[5].right = nullptr;
  if (func(tree) != false) {
    //     n0
    //   n1  n2
    // n3      n5
    //   n4
    cout << "Failed in test case 7" << endl;
    ++num_error;
  }
  return num_error;
}

int main() {
  return CheckImpl(&BTreeIsBalanced) + CheckImpl(&BTreeIsBalanced2);
}

