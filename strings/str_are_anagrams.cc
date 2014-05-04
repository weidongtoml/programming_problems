/* Copyright: Weidong Liang (2014), All Right Reserved.
 * File: str_are_anagrams.cc
 * Date: 2014.05.02
 * Description: determine whether the given two strings are
 *   anagrams of each other.
 */
#include <stdlib.h>
#include <iostream>
#include <string>
#include <map>

using std::cout;
using std::endl;
using std::map;
using std::string;

/**
 * StrAreAnagrams determines if the given string str1 and str2 are
 * anagrams of each other. 
 * Current implementation's time complexity is O(n), and space complexity
 * is O(1).
 */
bool StrAreAnagrams(const string& str1, const string& str2) {
  if (str1.length() != str2.length()) {
    return false;
  }

  map<char, size_t> histogram;
  for (string::const_iterator iter = str1.begin(); iter != str1.end();
    ++iter) {
    map<char, size_t>::iterator h_iter = histogram.find(*iter);
    if (h_iter == histogram.end()) {
      histogram[*iter] = 1;
    } else {
      ++h_iter->second;
    }
  }
  for (string::const_iterator iter = str2.begin(); iter != str2.end();
    ++iter) {
    map<char, size_t>::iterator h_iter = histogram.find(*iter);
    if (h_iter == histogram.end()) {
      return false;
    } else {
      --h_iter->second;
      if (h_iter->second == 0) {
        histogram.erase(h_iter);
      }
    }
  }
  return histogram.empty();
}

/**
 * StrAreAnagrams2 is a faster implementation of StrAreAnagrams
 * which uses an array instead of a map to keep track of the histogram.
 */ 
bool StrAreAnagrams2(const string& str1, const string& str2) {
  if (str1.length() != str2.length()) {
    return false;
  }

  const size_t kCharRange = 2 << (sizeof(char)*8);
  size_t histogram[kCharRange] = {0};
  for (string::const_iterator iter = str1.begin(); iter != str1.end();
    ++iter) {
    ++histogram[static_cast<uint8_t>(*iter)];
  }
  for (string::const_iterator iter = str2.begin(); iter != str2.end();
    ++iter) {
    if (histogram[static_cast<uint8_t>(*iter)] == 0) {
      return false;
    } else {
      --histogram[static_cast<uint8_t>(*iter)];
    }
  }
  return true;
}

#define ARRAY_SIZE(x) (sizeof(x)/sizeof((x)[0]))

int main() {
  struct TestCase {
    const char* str1;
    const char* str2;
    bool is_anagram;
  } test_cases[] = {
    {"", "", true},
    {"ab", "ba", true},
    {"a", "ab", false},
    {"a", "b", false},
    {"abc", "abe", false},
    {"abcdefabc", "abcabcdef", true},
  };

  typedef bool (*StrAreAnagramsFunc)(const string&, const string&);
  StrAreAnagramsFunc implementations[] = {
    &StrAreAnagrams,
    &StrAreAnagrams2,
  };

  int has_error = 0;
  for (size_t f = 0; f < ARRAY_SIZE(implementations); ++f) {
    StrAreAnagramsFunc impl = implementations[f];
    for (size_t i = 0; i < ARRAY_SIZE(test_cases); ++i) {
      const TestCase& t_case = test_cases[i];
      if (impl(t_case.str1, t_case.str2) != t_case.is_anagram) {
        cout << "Failed in test case #" << i << ", impl=" << f 
          << ", str1=\"" << t_case.str1 << "\", str2=\"" << t_case.str2 
          << "\", expected result to be: " << t_case.is_anagram 
          << ", but got otherwise." << endl;
        has_error = 1;
      }
    }
  }
  return has_error;
}

