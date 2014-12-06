/* Copyright: Weidong Liang (2014), All Right Reserved.
 * longest_common_substring.cc
 *
 *  Created on: May 30, 2014
 *      Author: Tom
 */
#include <iostream>
#include <string>
#include <vector>

using std::cout;
using std::endl;
using std::string;
using std::vector;

/**
 * LongestCommonSubstring: calculates the length of the longest common substring
 * between str1 and str2, and if lcs is not null, it also returns the common
 * substring. (Note: in case of a tie, it returns the first longest common
 * susbtring).
 * Current implementation has a time complexity of O(m*n), and space complexity
 * of O(m*n), where m and n are lengths of str1 and str2.
 */
std::size_t LongestCommonSubstring(const string& str1,
                                   const string& str2,
                                   string* lcs) {
  vector<size_t> matches(str1.length() * str2.length());
  size_t lcs_len = 0;
  size_t lcs_str1_index = 0;
  for (size_t i = 0; i < str1.size(); ++i) {
    for (size_t j = 0; j < str2.size(); ++j) {
      const size_t prev_cs_len =
          ((i > 0 && j > 0) ? matches[(i-1) * str2.size() + (j-1)] : 0);
      size_t& cur_cs_len = matches[i*str2.size()+j];
      if (str1[i] == str2[j]) {
        cur_cs_len = prev_cs_len + 1;
        if (cur_cs_len > lcs_len) {
          lcs_len = cur_cs_len;
          lcs_str1_index = i;
        }
      } else {
        cur_cs_len = 0;
      }
    }
  }
  if (lcs != NULL) {
    *lcs = (lcs_len == 0) ? "" :
        str1.substr(lcs_str1_index - lcs_len + 1, lcs_len);
  }
  return lcs_len;
}

/**
 *  LongestCommonSubstr2: another implementation of the longest common
 *  substring algorithm thtat has the same time complexity of O(m*n), but only
 *  a space complexity of O(min(m, n)).
 */
std::size_t LongestCommonSubstring2(const string& str1, const string& str2,
                                    string* lcs) {
  const string *long_str = &str1, *short_str = &str2;
  if (str1.length() < str2.length()) {
    swap(long_str, short_str);
  }

  vector<size_t> matches[2];
  matches[0].resize(short_str->length());
  matches[1].resize(short_str->length());

  size_t lcs_len = 0;
  size_t lcs_str1_index = 0;
  for (size_t i = 0; i < long_str->size(); ++i) {
    for (size_t j = 0; j < short_str->size(); ++j) {
      const size_t prev_cs_len = ((i > 0 && j > 0) ? matches[(i+1)%2][j - 1] : 0);
      size_t& cur_cs_len = matches[i%2][j];
      if ((*long_str)[i] == (*short_str)[j]) {
        cur_cs_len = prev_cs_len + 1;
        if (cur_cs_len > lcs_len) {
          lcs_len = cur_cs_len;
          lcs_str1_index = i;
        }
      } else {
        cur_cs_len = 0;
      }
    }
  }
  if (lcs != NULL) {
    *lcs = (lcs_len == 0) ? "" :
        long_str->substr(lcs_str1_index - lcs_len + 1, lcs_len);
  }
  return lcs_len;
}

#define ARRAY_SIZE(x) (sizeof(x)/sizeof(x[0]))


typedef std::size_t (*LCSFuncPtr)(const string&, const string&, string*);

LCSFuncPtr kLCSImpls[] = {
    LongestCommonSubstring,
    LongestCommonSubstring2
};

int main() {
  struct TestCase {
    const char* str1;
    const char* str2;
    const char* lcs;
  } test_cases[] = {
      {
          "a", "", "",
      }, {
          "", "a", "",
      }, {
          "abc", "abc", "abc",
      }, {
          "xyzabc", "eabc", "abc",
      }, {
          "eabc", "xyzabc", "abc",
      }, {
          "abcdefg", "abxdegg", "ab",
      }, {
          "abcdefg", "abcdefg", "abcdefg",
      },
  };

  int num_errors = 0;
  string lcs;
  for (size_t iter = 0; iter < ARRAY_SIZE(kLCSImpls); ++iter) {
    LCSFuncPtr impl = kLCSImpls[iter];
    cout << "Test impl #" << iter << endl;
    for (size_t i = 0; i < ARRAY_SIZE(test_cases); ++i) {
      const TestCase& t_case = test_cases[i];
      const size_t lcs_len = impl(t_case.str1, t_case.str2, &lcs);
      const size_t exp_lcs_len = strlen(t_case.lcs);
      if (lcs_len != exp_lcs_len) {
        cout << "Failed for test case #" << i << ", expected lcs length to be["
            << exp_lcs_len << "], but got [" << lcs_len << "] instead."
            << "TestCase{" << t_case.str1 << ", " << t_case.str2 << ", "
            << t_case.lcs << "}"<< endl;
        ++num_errors;
        continue;
      }
      if (lcs != t_case.lcs) {
        cout << "Expected LCS to be [" << t_case.lcs << "] but got ["
            << lcs << "] instead" << endl
            << "TestCase{" << t_case.str1 << ", " << t_case.str2 << ", "
            << t_case.lcs << "}"<< endl;
        ++num_errors;
        continue;
      }
    }
  }

  if (num_errors == 0) {
    cout << "Passed all tests for LongestCommonSubstring" << endl;
  }

  return num_errors;
}


