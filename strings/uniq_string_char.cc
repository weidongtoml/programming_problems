/**
 * Copyright: weidong liang (2014). All right reserved.
 * File: uniq_string_char.cc
 * Description: various ways of solving the problem of determining
 *  whether a given single-byte string contains unique characters.
 * Date: 2014.04.30
 */
#include <algorithm>
#include <bitset>
#include <iostream>
#include <map>
#include <string>

using std::map;
using std::string;
using std::cout;
using std::endl;
using std::sort;
using std::bitset;

/**
 * StringHasUniqChar determines whether a string has all unique characters.
 * Current implementation uses map to look up for character duplications,
 * if the map is implemented as RB-tree, then the time complexity of the
 * algorithm would be O(n*log(n)), this could be reduced to O(n) if hashmap
 * is used instead.
 */
bool StringHasUniqChar(const string& str) {
  map<char, bool> seen_char;
  for (string::const_iterator iter = str.begin(); iter != str.end(); ++iter) {
    if (seen_char.find(*iter) != seen_char.end()) {
      return false;
    }
    seen_char[*iter] = true;
  }
  return true;
}


/**
 * StringHasUniqChar2 is another implementation that is guarrenteed to 
 * have a time complexity of O(n). Note that this assume string to be consists
 * of only single-byte characters.
 */
bool StringHasUniqChar2(const string& str) {
  bool seen_char[512];
  for (string::const_iterator iter = str.begin(); iter != str.end(); ++iter) {
    const int index = static_cast<unsigned int>(*iter);
    if (seen_char[index] == true) {
      return false;
    }
    seen_char[index] = true;
  }
  return true;
}

/**
 * StringHasUniqChar3 is another implementation that uses no additional 
 * data structure, and is expected to have O(n*log(n)) time complexity,
 * with worse case of O(n^2) time complexity.
 * But this requires O(n) space in order not to change the origianl string.
 */
bool StringHasUniqChar3(const string& str) {
  string str_cpy = str;
  sort(str_cpy.begin(), str_cpy.end());
  for (string::const_iterator iter = str_cpy.begin(); 
    iter != str_cpy.end() && iter+1 != str_cpy.end();
    ++iter) {
    if (*iter == *(iter+1)) {
      return false;
    }
  }
  return true;
}

/**
 * StringHasUniqChar4 is another implementation of StringHasUniqChar that
 * uses a bitset to reduce space usage, time complexity would be O(n).
 */
bool StringHasUniqChar4(const string& str) {
  bitset<512> seen_char;
  for (string::const_iterator iter = str.begin(); iter != str.end(); ++iter) {
    if (seen_char.test(*iter)) {
      return false;
    }
    seen_char.set(*iter);
  }
  return true;
}

/**
 * StringHasUniqChar5 is another implementation that has a space complexity
 * of O(1), and a time complexity of O(n^2).
 */
bool StringHasUniqChar5(const string& str) {
  for (string::const_iterator iter = str.begin(); iter != str.end(); ++iter) {
    for (string::const_iterator iter2 = iter+1; iter2 < str.end(); ++iter2) {
      if (*iter == *iter2) {
        return false;
       }
    }
  }
  return true;
}


int main() {
  struct {
    const char* str;
    bool uniq_char;
  } test_cases[] = {
    {"abcdef", true},
    {"", true},
    {"abca", false},
    {"    a", false},
    {"_(#)_", false},
  };

  typedef bool (*StringHasUniqCharFunc)(const string&);
  StringHasUniqCharFunc implementations[] = {
    &StringHasUniqChar,
    &StringHasUniqChar2,
    &StringHasUniqChar3,
    &StringHasUniqChar4,
    &StringHasUniqChar5,
  };

  for (int f = 0; f < sizeof(implementations)/sizeof(implementations[0]); ++f) {
    StringHasUniqCharFunc impl = implementations[f];
    for (int i = 0; i < sizeof(test_cases)/sizeof(test_cases[0]); ++i) {
      bool result = StringHasUniqChar(test_cases[i].str);
      if (result != test_cases[i].uniq_char) {
        cout << "Failed on test case #" << i << " impl #" << f << ", str=\"" 
          << test_cases[i].str << "\"" << ", expected result to be: "
          << test_cases[i].uniq_char << ", but got: " << result << endl;
      }
    }
  }
  return 0;
}

