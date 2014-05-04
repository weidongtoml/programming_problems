/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: sorted_array_to_btree.cc
 * Date: 2014.05.04
 * Description:
 *   Constructs a binary tree from an array that has been sorted in increasing
 *   order.
 */
#include <iostream>

using std::cout;
using std::endl;

struct Node {
  int val;
  Node* left;
  Node* right;
};

/**
 * SortedArrayToBTree creates a binary tree from the given sorted array using
 * only the sub-array within the range of [start, end).
 */
Node* SortedArrayToBTree(int* array, int start, int end) {
  if (start >= end || array == nullptr) {
    return nullptr;
  }
  Node* node = new Node;
  const int cur_index = start + (end - start) / 2;
  node->val = array[cur_index];
  node->left = SortedArrayToBTree(array, start, cur_index);
  node->right = SortedArrayToBTree(array, cur_index+1, end);
  return node;
}

/**
 * SortedArrayToBTree converts a given sorted array to a binary tree with
 * minimal height.
 * Current implementation has a time complexity of O(n).
 */
Node* SortedArrayToBTree(int* array, int array_size) {
  return SortedArrayToBTree(array, 0, array_size);
}

int main() {
  // Note: in the following, we do not delete the allocate objects to simplify
  // the code. We can get away with this because the allocate memory will be
  // return to the system once the program exists.
  int num_errors = 0;
  Node* tree_0 = SortedArrayToBTree(nullptr, 0);
  if (tree_0 != nullptr) {
    cout << "Failed for test case 0, expected a nullptr." << endl;
    ++num_errors;
  }
  int array_1[] = {0};
  Node* tree_1 = SortedArrayToBTree(array_1, 1);
  if (!(tree_1 != nullptr && tree_1->val == 0 && tree_1->left == nullptr 
    && tree_1->right == nullptr)) {
    cout << "Failed for test case 1. " << endl;
    ++num_errors;
  }
  int array_2[] = {0, 1};
  Node* tree_2 = SortedArrayToBTree(array_2, 2);
  if (!(tree_2 != nullptr && tree_2->val == 1 && tree_2->left != nullptr 
    && tree_2->left->val == 0 && tree_2->left->left == nullptr 
    && tree_2->left->right == nullptr && tree_2->right == nullptr)) {
    cout << "Failed for test case 2. " << endl;
    ++num_errors;
  }
  int array_3[] = {0, 1, 2};
  Node* tree_3 = SortedArrayToBTree(array_3, 3);
  if (!(tree_3 != nullptr && tree_3->val == 1 && tree_3->left != nullptr 
    && tree_3->left->val == 0 && tree_3->left->left == nullptr 
    && tree_3->left->right == nullptr && tree_3->right != nullptr 
    && tree_3->right->val == 2 && tree_3->right->left == nullptr 
    && tree_3->right->right == nullptr)) {
    cout << "Failed for test case 3." << endl;
    ++num_errors;
  }
  int array_4[] = {0, 1, 2, 3};
  Node* tree_4 = SortedArrayToBTree(array_4, 4);
  if (!(tree_4 != nullptr && tree_4->val == 2 && tree_4->left != nullptr 
    && tree_4->left->val == 1 && tree_4->left->left != nullptr 
    && tree_4->left->left->val == 0 && tree_4->left->right == nullptr 
    && tree_4->right != nullptr && tree_4->right->val == 3 
    && tree_4->right->left == nullptr && tree_4->right->right == nullptr)) {
    cout << "Failed for test case 4." << endl;
    ++num_errors;
  }
  return num_errors;
}