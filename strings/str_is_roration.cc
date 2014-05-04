/* Copyright: Weidoliang Liang, All Right Reserved.
 * File: str_is_rotation.cc
 * Date: 2014.05.02
 * Description:
 *   Check whether a given string is the rotation of another,
 *   e.g. "watermellon" is a rotation of "llonwaterme".
 */
#include <string>
#include <iostream>

using std::string;
using std::cout;
using std::endl;

/**
 * StrIsRotation checks if str1 is a rotatio of str2.
 * Current implementation's time complexity is O(n^2) and 
 * a space complexity of O(n).
 */
bool StrIsRotation(const string& str1, const string& str2) {
  if (str1.length() != str2.length()) {
    return false;
  }
  return (str1.empty() && str2.empty()) 
    || (str1 + str1).find(str2) != string::npos;
}

#define ARRAY_SIZE(x) (sizeof(x)/sizeof((x)[0]))

int main() {
  struct TestCase {
    const char* str1;
    const char* str2;
    bool is_rotation;
  } test_cases[] = {
    {"abc", "cab", true},
    {"", "", true},
    {"watermellon", "mellonwater", true},
    {"ellonwaterm", "watermellon", true},
    {"abcdef", "acefbd", false},
    {"watch", "witch", false},
    {"watches", "watch", false},
  };

  int has_error = 0;
  for (size_t i = 0; i < ARRAY_SIZE(test_cases); ++i) {
    const TestCase& t_case = test_cases[i];
    if (StrIsRotation(t_case.str1, t_case.str2) != t_case.is_rotation) {
      cout << "Failed for test case #" << i << ", str1=\"" << t_case.str1
        << "\", str2=\"" << t_case.str2 << "\", expected "
        << t_case.is_rotation << ", but got otherwise." << endl;
      has_error = 1;
    }
  }
  return has_error;
}

