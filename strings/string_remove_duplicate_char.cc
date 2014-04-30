/* Copyright: Weidong Liang (2014), All Right Reserved.
 * Date: 2014.04.03
 * Description: write a function to remove duplicate characters in a string
 *  without using any additional buffer, i.e. a space complexity of O(1).
 */
#include <string>
#include <iostream>
#include <bitset>

using std::string;
using std::cout;
using std::endl;
using std::bitset;

/**
 * StringRemoveDuplicateChar removes duplicate characters in a string. Current
 * implementation has a time complexity of O(n^2), and a space complexity of
 * O(1).
 */
void StringRemoveDuplicateChar(string& str) {
  size_t len = str.length();
  for (int i = 0; i < len; ++i) {
    char c = str[i];
    int char_to_remove = 0;
    for (int check_index = i+1, output_index = i+1; check_index < len; 
      ++check_index) {
      if (str[check_index] == c) {
        ++char_to_remove;
      } else {
        str[output_index] = str[check_index];
        ++output_index;
      }
    }
    len -= char_to_remove;
  }
  str.resize(len);
}

/**
 * Another implementation of StringRemoveDuplicateChar that has a space
 * complexity of O(n), and a time complexity of O(n).
 */
void StringRemoveDuplicateChar2(string& str) {
  bitset<(2 << (sizeof(char) * 8))> seen_char;
  string out_str;
  for (string::const_iterator iter = str.begin(); iter != str.end(); ++iter) {
    if (!seen_char.test(*iter)) {
      out_str += *iter;
      seen_char.set(static_cast<unsigned int>(*iter));
    }
  }
  str = out_str;
}

int main() {
  const char* test_cases[][2] = {
    {"abcdefg", "abcdefg"},
    {"abcabc", "abc"},
    {"", ""},
    {"abcaaaa", "abc"},
    {"aaaaabc", "abc"},
  };

  typedef void (*StringRemoveDuplicateFunc)(string&);
  StringRemoveDuplicateFunc implementations[] = {
    &StringRemoveDuplicateChar,
    &StringRemoveDuplicateChar2,
  };
  
  int has_error = 0;
  for (int f = 0; f < sizeof(implementations) / sizeof(implementations[0]); 
    ++f) {
    StringRemoveDuplicateFunc impl = implementations[f];
    for (int i = 0; i < sizeof(test_cases) / sizeof(test_cases[0]); ++i) {
      string str(test_cases[i][0]);
      impl(str);
      if (str != test_cases[i][1]) {
        has_error = 1;
        cout << "Failed in test case #" << i << "\"" << test_cases[i][0] << "\","
          << "expected: \"" << test_cases[i][1] << "\", but got \"" << str << "\""
          << endl;
      }
    }
  }
  return has_error;
}
