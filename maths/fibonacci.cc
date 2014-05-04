/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: fibonacci.cc
 * Date: 2014.05.04
 * Description:
 *   Implement a function to calculate the nth Fibonacci number
 * without the use of recursion and with only two temporay
 * variables in addition to the parameter argument.
 */
#include <stdlib.h>
#include <algorithm>
#include <iostream>

using std::cout;
using std::endl;
using std::swap;

/**
 * Fibonacci returns the ith Fibonacci number with i >= 0.
 * Currently implementation has a time complexity of O(n).
 */
size_t Fibonacci(uint32_t i) {
  size_t a = 1, b = 1;
  while (i-- > 1) {
    size_t c = a + b;
    b = a;
    a = c;
  }
  return a;
}

/**
 * Another implementation of Fibonacci without the use of a
 * temporay variable c.
 */
size_t Fibonacci2(uint32_t i) {
  size_t a = 1, b = 1;
  while (i-- > 1) {
    swap(a, b);
    a += b;
  }
  return a;
}

/**
 * Another implementation of Fibonacci without the use of
 * a temporary variable c and no additional function call.
 */
size_t Fibonacci3(uint32_t i) {
  size_t a = 1, b = 1;
  while (i-- > 1) {
    // Since a' =  a + b, b' =  a
    // therefore a' =  a + b, b' =  a' - b = (a+b) - b
    a = a + b;
    b = a - b;
  }
  return a;
}

typedef size_t (*FibonacciFunc)(uint32_t);

#define ARRAY_SIZE(x) (sizeof(x)/sizeof((x)[0]))

int main() {
  int num_errors = 0;

  FibonacciFunc impls[] = {
    &Fibonacci,
    &Fibonacci2,
    &Fibonacci3,
  };

  int test_cases[][2] = {
    {0, 1},
    {1, 1},
    {2, 2},
    {3, 3},
    {4, 5},
    {5, 8},
    {6, 13},
    {7, 21},
    {8, 34},
    {9, 55},
    {10, 89},
  };
  
  for (size_t i = 0; i < ARRAY_SIZE(impls); ++i) {
    FibonacciFunc impl = impls[i];
    for (size_t j = 0; j < ARRAY_SIZE(test_cases); ++j) {
      if (impl(test_cases[j][0]) != test_cases[j][1]) {
        cout << "Impl #" << i << " failed in test case #" << j << endl;
        ++num_errors;
      }
    }
  }

  return num_errors;
}

