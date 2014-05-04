/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: graph_nodes_connected.cc
 * Date: 2014.05.03
 * Description:
 *   Determine if given two points are connected in a directed Graph.
 */
#include <iostream>
#include <map>
#include <queue>
#include <set>
#include <vector>

using std::map;
using std::queue;
using std::set;
using std::vector;
using std::cout;
using std::endl;

typedef map<int, vector<int>> Graph;

bool GraphNodesAreReachable(const Graph& g, int start, int end);

/**
 * GraphNodesAreConnected determines if the given two nodes: start and end
 * are connected in the given graph g; returns true if they are connected,
 * false otherwise. 
 * Current implementation has a time complexity of O(v * e).
 */
bool GraphNodesAreConnected(const Graph& g, int start, int end) {
  return GraphNodesAreReachable(g, start, end) &&
    GraphNodesAreReachable(g, end, start);
}

bool GraphNodesAreReachable(const Graph& g, int start, int end) {
  if (g.find(start) == g.end()) {
    return false;
  }
  set<int> discovered;
  queue<int> q;
  q.push(start);
  discovered.insert(start);
  while (!q.empty()) {
    int v = q.front();
    q.pop();
    if (v == end) {
      return true;
    }
    auto e_iter = g.find(v);
    if (e_iter == g.end()) {
      continue;
    }
    auto& edges = e_iter->second;
    for (auto e = edges.begin(); e != edges.end(); ++e) {
      if (discovered.find(*e) == discovered.end()) {
        discovered.insert(*e);
        q.push(*e);
      }
    }
  }
  return false;
}

#define ARRAY_SIZE(x) (sizeof(x)/sizeof((x)[0]))

int main() {
  struct TestCase {
    int connections[10][2];
    int num_connections;
    int start;
    int end;
    bool is_connected;
  } test_cases[] = {
    {
      {}, 0, 1, 2, false,
    }, {
      {
        {0, 1}, {0, 2},
      }, 2, 
      1, 3, false,
    }, {
      {
        {0, 1}, {1, 2},  
      }, 2,
      0, 2, true,
    }, {
      {
        {0, 1}, {1, 2}, {2, 3}, {1, 4}, {4, 0},
      }, 5,
      4, 3, true,
    }, {
      {
        {0, 1},
      }, 1,
      0, 0, true,
    },
  };

  int num_errors = 0;
  for (size_t i = 0; i < ARRAY_SIZE(test_cases); ++i) {
    const TestCase& t_case = test_cases[i];
    Graph g;
    for (int j = 0; j < t_case.num_connections; ++j) {
      int v = t_case.connections[j][0];
      int u = t_case.connections[j][1];
      g[v].push_back(u);
      g[u].push_back(v);
    }
    bool result = GraphNodesAreConnected(g, t_case.start, t_case.end);
    if (result != t_case.is_connected) {
      cout << "Failed for test case #" << i << ", expected " 
        << t_case.is_connected << ", but got " << result << endl;
      for (auto iter = g.begin(); iter != g.end(); ++iter) {
        cout << iter->first << ":";
        auto& edges = iter->second;
        for (auto e = edges.begin(); e != edges.end(); ++e) {
          cout << " " << *e;
        }
        cout << endl;
      }
      ++num_errors;
    }
  }
  return num_errors;
}

