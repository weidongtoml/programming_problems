/*
 * suffix_tree.cc
 *
 *  Created on: Aug 7, 2014
 *      Author: Weidong Liang
 */


#include <assert.h>
#include <iostream>
#include <sstream>
#include <stdlib.h>
#include <string>
#include <map>

namespace suffix_tree_impl {

struct Node;
struct Edge;

struct SuffixNode {
  int first_char_index;
  int last_char_index;
  Node* active_node;
  const std::string* text;

  bool IsExplicit() const;
  void Canonize();
};

struct Edge {
  size_t start_index;
  size_t end_index;
  Node* end_node;
  explicit Edge(size_t s_index, size_t e_index, Node* e_node = NULL);
  size_t Span() const;
};

struct Node {
  std::map<char, Edge*> edges;
  Node* suffix_node;

  Node(Node* s_node = NULL): suffix_node(s_node) {
  }

  Edge* GetEdge(char c);
};

bool SuffixNode::IsExplicit() const {
  return first_char_index > last_char_index;
}

void SuffixNode::Canonize() {
  while (!IsExplicit()) {
    assert(text != NULL);
    assert(active_node != NULL);
    const char t = (*text)[first_char_index];
    const Edge* edge = active_node->GetEdge(t);
    assert(edge != NULL);
    if (edge->Span() < (last_char_index - first_char_index)) {
      active_node = edge->end_node;
      first_char_index += edge->Span() + 1;
    } else {
      break;
    }
  }
}

Edge::Edge(size_t s_index, size_t e_index, Node* e_node):
  start_index(s_index), end_index(e_index), end_node(e_node) {
}

size_t Edge::Span() const {
  return end_index - start_index;
}

Edge* Node::GetEdge(char c) {
  std::map<char, Edge*>::iterator iter = edges.find(c);
  if (iter != edges.end()) {
    return iter->second;
  } else {
    return NULL;
  }
}

}  // namespace suffix_tree_impl

class SuffixTree {
 public:
  SuffixTree() {
    root_ = new suffix_tree_impl::Node;
  }

  ~SuffixTree() {
    DestroyTree(root_);
    root_ = NULL;
  }

  void AddString(const std::string& text) {
    text_ = text;
    suffix_tree_impl::SuffixNode suffix_node = {0, -1, root_, &text_};
    for (size_t i = 0; i < text_.length(); ++i) {
      AddPrefix(&suffix_node, i);
    }
  }

  bool Contains(const std::string& text) const {
    using namespace suffix_tree_impl;
    const Node* p = root_;
    const Edge* e = NULL;
    bool is_in_tree = true;
    size_t cur_span_index = 0;
    for (size_t i = 0; i < text.size() && is_in_tree;) {
      char c = text[i];
      if (p == NULL) {
        is_in_tree = false;
        break;
      }
      if (e == NULL) {
        std::map<char, Edge*>::const_iterator iter = p->edges.find(c);
        if (iter == p->edges.end()) {
          is_in_tree = false;
          break;
        } else {
          e = iter->second;
          cur_span_index = 1;
          ++i;
        }
      } else {
        if (cur_span_index <= e->Span()) {
          if (c != text_[e->start_index + cur_span_index]) {
            is_in_tree = false;
            break;
          } else {
            ++i;
            ++cur_span_index;
          }
        } else {
          p = e->end_node;
          e = NULL;
          cur_span_index = 0;
        }
      }
    }
    return is_in_tree;
  }

  void PrintTree() const {
    Print(root_, 0, true);
    std::cout << std::endl;
  }

 private:

  void AddPrefix(suffix_tree_impl::SuffixNode* suffix_node, size_t char_index) {
    suffix_tree_impl::Node *prev_parent_node = NULL, *parent_node = NULL;
    const char current_char = text_[char_index];
    const size_t last_index = text_.length() - 1;
    for (;;) {
      parent_node = suffix_node->active_node;
      if (suffix_node->IsExplicit()) {
        if (suffix_node->active_node->GetEdge(current_char) != NULL) {
          // Prefix already presents.
          break;
        }
      } else {
        // check if the current_char already presents in the edge
        const char c = text_[suffix_node->first_char_index];
        suffix_tree_impl::Edge* existing_edge = suffix_node->active_node->GetEdge(c);
        assert(existing_edge != NULL);
        size_t span = suffix_node->last_char_index - suffix_node->first_char_index;
        const char next_char = text_[existing_edge->start_index + span + 1];
        if (current_char != next_char) {
          // found the splitting point.
          parent_node = SplitEdge(existing_edge, span);
        } else {
          // current char already present in one of the edges of the implicit node.
          break;
        }
      }

      // Prefix not present, add it to parent_node
      assert(parent_node->GetEdge(current_char) == NULL);
      suffix_tree_impl::Edge* new_edge = new suffix_tree_impl::Edge(char_index, last_index);
      parent_node->edges[current_char] = new_edge;
      if (prev_parent_node != NULL && prev_parent_node != root_) {
        prev_parent_node->suffix_node = parent_node;
      }
      prev_parent_node = parent_node;

      if (suffix_node->active_node == root_) {
        // Finished processing the current suffix.
        suffix_node->first_char_index++;
      } else {
        // Need to process the next smaller suffix.
        assert(suffix_node->active_node != NULL);
        suffix_node->active_node = suffix_node->active_node->suffix_node;
      }
      suffix_node->Canonize();
    }
    if (prev_parent_node != NULL && prev_parent_node != root_) {
      prev_parent_node->suffix_node = parent_node;
    }
    suffix_node->last_char_index++;
    suffix_node->Canonize();

//    PrintTree();
//    std::cout << "SuffixNode{S@" << suffix_node->active_node
//        << ",first_index=" << suffix_node->first_char_index
//        << ",last_index=" << suffix_node->last_char_index << "}" << std::endl;
//    std::cout << std::endl;
  }

  suffix_tree_impl::Node* SplitEdge(suffix_tree_impl::Edge* edge, size_t span) {
    suffix_tree_impl::Edge* new_edge =
        new suffix_tree_impl::Edge(edge->start_index+span+1, edge->end_index);
    edge->end_index = edge->start_index + span;
    suffix_tree_impl::Node* new_node = new suffix_tree_impl::Node();
    const char next_char = text_[edge->start_index+span+1];
    new_node->edges[next_char] = new_edge;
    assert(edge->end_node == NULL);
    edge->end_node = new_node;
    return new_node;
  }

  void DestroyTree(suffix_tree_impl::Node* parent) {
    for (std::map<char, suffix_tree_impl::Edge*>::iterator iter = parent->edges.begin();
        iter != parent->edges.end(); ++iter) {
      if (iter->second->end_node != NULL) {
        DestroyTree(iter->second->end_node);
        iter->second->end_node = NULL;
      }
    }
    delete parent;
  }

  void Print(suffix_tree_impl::Node* parent, size_t padding, bool is_new_line) const {
    using namespace suffix_tree_impl;
    std::ostringstream node_oss;
    node_oss << "[N@" << parent;
    if (parent != NULL) {
      node_oss << ",S@" << parent->suffix_node;
    }
    node_oss << "]";
    const std::string& node_str = node_oss.str();
    std::cout << node_str;
    if (parent != NULL) {
      for (std::map<char, Edge*>::iterator iter = parent->edges.begin();
          iter != parent->edges.end(); ++iter) {
        std::ostringstream oss;
        oss << "->{" << iter->first << "}"
            << " [" << iter->second->start_index << "," << iter->second->end_index << "]->";
        if (iter != parent->edges.begin()) {
          std::cout << std::endl;
          std::cout << std::string(padding + node_str.length()-1, ' ');
          std::cout << "+";
        }
        std::cout << oss.str();
        Print(iter->second->end_node, oss.str().length() + node_str.length(), false);
      }
    }
  }

  std::string text_;
  suffix_tree_impl::Node* root_;
};

int main() {
  std::string str1 = "abcabxabcd";
  SuffixTree suffix_tree;
  suffix_tree.AddString(str1);
  std::cout << "The Final Tree that has been constructed is: " << std::endl;
  suffix_tree.PrintTree();
  std::cout << std::endl;

  bool all_success = true;
  for (size_t i = 0; i < str1.length() - 1; ++i) {
    if (!suffix_tree.Contains(str1.substr(i))) {
      std::cout << "Expected suffix [" << str1.substr(i) << "] to be in the suffix tree" << std::endl;
      all_success = false;
    }
  }

  if (all_success) {
    std::cout << "Tests passed" << std::endl;
  }

  return 0;
}

