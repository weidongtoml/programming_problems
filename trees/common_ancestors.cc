/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: common_ancestor.cc
 * Date: 2014.05.04
 * Description:
 *   Find the first common ancestor of two nodes in a binary without storing
 * additional node in a data structure.
 */
#include <iostream>

using std::cout;
using std::endl;

struct Node {
  Node* parent;
  Node* left;
  Node* right;
};

Node* CommonAncestor(Node* subtree, Node* from, Node* node) {
  if (subtree == nullptr || node == nullptr) {
    return nullptr;
  }
  if (subtree == node) {
    return subtree;
  }
  if (from == subtree->parent) {
    // Check if entire subtree contains the node.
    if (CommonAncestor(subtree->left, subtree, node) != nullptr ||
      CommonAncestor(subtree->right, subtree, node) != nullptr) {
      return from;
    } else {
      return nullptr;
    }
  } else if (from == subtree->left) {
    // The left subtree has been checked, we need to check the right subtree
    // and if failed, move up one level.
    if (CommonAncestor(subtree->right, subtree, node) != nullptr) {
      return subtree;
    } else {
      return CommonAncestor(subtree->parent, subtree, node);
    }
  } else if (from == subtree->right) {
    // The right subtree has been checked, we need to check the left subtree
    // and if failed, move up one level.
    if (CommonAncestor(subtree->left, subtree, node) != nullptr) {
      return subtree;
    } else {
      return CommonAncestor(subtree->parent, subtree, node);
    }
  }
  return nullptr;
}

/**
 * CommonAncestor finds the first common ancestor of node1 and node2, return
 * nullptr if no common ancestor could be found.
 * Current implementation has a time complexity of O(n), where n is the number
 * nodes in the binary tree within which node1 is located.
 */
Node* CommonAncestor(Node* node1, Node* node2) {
  if (node1 == nullptr || node2 == nullptr) {
    return nullptr;
  }
  if (node1 == node2) {
    return node1;
  }
  if (CommonAncestor(node1->left, node1, node2) != nullptr) {
    return node1;
  }
  if (CommonAncestor(node1->right, node1, node2) != nullptr) {
    return node1;
  }
  return CommonAncestor(node1->parent, node1, node2);
}

int main() {
  int num_errors = 0;
  Node nodes[6];
  // Construct two binary tree:
  //      n0       n6      
  //    n1  n2
  //   n3    n4
  //    n5
  nodes[0].parent = nullptr;
  nodes[0].left = &nodes[1];
  nodes[1].parent = &nodes[0];
  nodes[1].left = &nodes[3];
  nodes[1].right = nullptr;
  nodes[3].parent = &nodes[1];
  nodes[3].left = nullptr;
  nodes[3].right = &nodes[5];
  nodes[5].parent = &nodes[3];
  nodes[5].left = nodes[5].right = nullptr;
  nodes[0].right = &nodes[2];
  nodes[2].parent = &nodes[0];
  nodes[2].left = nullptr;
  nodes[2].right = &nodes[4];
  nodes[4].parent = &nodes[2];
  nodes[4].left = nodes[4].right = nullptr;
  
  nodes[6].left = nodes[6].right = nodes[6].parent = nullptr;
  
  if (CommonAncestor(nullptr, nullptr) != nullptr) {
    cout << "Failed in case 0." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[0], nullptr) != nullptr) {
    cout << "Failed in case 1." << endl;
    ++num_errors;
  }
  if (CommonAncestor(nullptr, &nodes[0]) != nullptr) {
    cout << "Failed in case 2." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[0], &nodes[6]) != nullptr) {
    cout << "Failed in case 3." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[5], &nodes[6]) != nullptr) {
    cout << "Failed in case 3." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[0], &nodes[0]) != &nodes[0]) {
    cout << "Failed in case 4." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[1], &nodes[0]) != &nodes[0]) {
    cout << "Failed in case 5." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[1], &nodes[2]) != &nodes[0]) {
    cout << "Failed in case 6." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[4], &nodes[5]) != &nodes[0]) {
    cout << "Failed in case 7." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[1], &nodes[5]) != &nodes[1]) {
    cout << "Failed in case 8." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[5], &nodes[1]) != &nodes[1]) {
    cout << "Failed in case 9." << endl;
    ++num_errors;
  }
  if (CommonAncestor(&nodes[2], &nodes[5]) != &nodes[0]) {
    cout << "Failed in case 10." << endl;
    ++num_errors;
  }
  return num_errors;
}
