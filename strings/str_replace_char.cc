/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: str_replace_char.cc
 * Date: 2014.05.02
 * Description:
 *   Replaces all spaces in a string with "%20".
 */
#include <algorithm>
#include <iostream>
#include <string>

using std::string;
using std::cout;
using std::endl;
using std::count_if;

/**
 * StrReplaceChar replaces every occurence of the given character search,
 * with the string replace.
 * Current implementation has a time complexity of O(m*n), where
 * m = len(str), and n = len(replace).
 */
void StrReplaceChar(char search, const string& replace, string* str) {
  if (str == NULL) {
    return;
  }
  string& s = *str;
  const size_t num_replacements = count_if(s.begin(), s.end(),
    [&](char c){return c == search;});
  const size_t old_len = s.length();
  s.resize(old_len + num_replacements * (replace.length() - 1));
  string::iterator out_iter = s.begin() + s.length() - 1;
  for (string::const_iterator iter = s.begin() + old_len - 1; 
    iter >= s.begin(); --iter) {
    if (*iter == search) {
       // replace search with replace.
       for (string::const_iterator iter2 = replace.end() - 1;
         iter2 >= replace.begin(); --iter2) {
         *out_iter-- = *iter2;
       }
    } else {
      // copy the other characters.
      *out_iter-- = *iter;
    }
  }
}

#define ARRAY_SIZE(x) (sizeof(x)/sizeof((x)[0]))

int main() {
  const char* test_cases[][2] = {
    {"", ""},
    {"abc ", "abc%20"},
    {"a b c", "a%20b%20c"},
    {" a ", "%20a%20"},
    {"abc", "abc"},
  };

  int has_error = 0;
  for (size_t i = 0; i < ARRAY_SIZE(test_cases); ++i) {
    string input(test_cases[i][0]);
    string answer(test_cases[i][1]);
    StrReplaceChar(' ', "%20", &input);
    if (input != answer) {
      cout << "Failed for test case #" << i << ", str=\""
        << test_cases[i][0] << "\", expected result to be \""
        << answer << "\", but got: \"" << input << "\" instead."
        << endl;
      has_error = 1;
    }
  }
  return has_error;
}

