/* Copyright: Weidong Liang (2014), All Right Reserved.
 * Date: 2014.04.03
 * Description: Procedure to reverse a given CString.
 */
#include <string.h>
#include <algorithm>
#include <iostream>

using std::cout;
using std::endl;
using std::swap;

/**
 * CStringReverse reverses a given CString.
 * Given implementation's time complexity is O(n).
 * Assuming the given string is single-byte string, and
 * that it is \0 terminated.
 */
void CStringReverse(char* str) {
  char *left = str, *right = str;
  for (;*right != '\0'; ++right);
  --right;
  while (left < right) {
    swap(*left, *right);
    ++left;
    --right;
  }
}

int main() {
  const char* test_cases[][2] = {
    {"abcdefg", "gfedcba"},
    {"123 456", "654 321"},
    {"", ""},
    {"#$%^&&*&", "&*&&^%$#"},
  };
  
  char tmp_str[1024];
  int all_correct = 0;
  for (int i = 0; i < sizeof(test_cases)/sizeof(test_cases[0]); ++i) {
    strcpy(tmp_str, test_cases[i][0]);
    CStringReverse(tmp_str);
    if (strcmp(tmp_str, test_cases[i][1]) != 0) {
      cout << "Failed in test case #" << i << ", str=\"" << test_cases[i][0]
        << "\", expected \"" << test_cases[i][1] << "\", but got \""
        << tmp_str << "\"" << endl;
      all_correct = 1;
    }
  }
  return all_correct;
}
