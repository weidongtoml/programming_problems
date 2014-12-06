#include <iostream>
#include <string>
#include <vector>
#include <algorithm>
#include <iterator>
#include <cmath>

/**
 * class KMPPatternMatcher implements the Knuth-Morris-Pratt algorithm for
 * string matching.
 * Example:
 *   std::string pattern("abababc");
 *   KMPPatternMatcher matcher(pattern);
 *   ssize_t index = matcher.IndexInString("ababababc"); // -> 2
 *   ssize_t index_2 = matcher.IndexInString("ab"); // -> -1
 */
class KMPPatternMatcher {
 public:
  explicit KMPPatternMatcher(const std::string& pattern): pattern_(pattern) {
    table_.resize(pattern_.length());
    size_t cnd = 0;
    table_[0] = 0;
    for (size_t i = 1; i < pattern_.length(); ++i) {
      bool is_c_eq = pattern_[i] == pattern_[cnd];
      while (cnd > 0 && !is_c_eq) {
        cnd = table_[cnd];
        is_c_eq = pattern_[i] == pattern_[cnd];
      }
      if (is_c_eq) {
        ++cnd;
        table_[i] = cnd;
      } else {
        table_[i] = 0;
      }
    }
  }
  ssize_t IndexInString(const std::string& text) const {
    size_t i = 0;
    size_t j = 0;
    while (i < text.length()) {
      while (j > 0 && text[i] != pattern_[j]) {
        if (table_[j] > 0) {
          j = table_[j];
        } else {
          j = 0;
        }  
      }
      if (text[i] == pattern_[j]) {
        ++j;
      } 
      ++i;
      if (j == pattern_.length()) {
        return i-j;
      }
    }
    return -1;
  }
  const std::vector<size_t>& PatternTable() const {
    return table_;
  }
 private:
  const std::string& pattern_;
  std::vector<size_t> table_;
}; 

bool KMPPatternMatcher_TestPatternCreation() {
  std::cout << "Start Test [" << __FUNCTION__ << "]" << std::endl;
  struct TestCase {
    const char* pattern;
    size_t table[100];
  };

  TestCase test_cases[] = {
    {"abc", 	{0, 0, 0}},
    {"abab", 	{0, 0, 1, 2}},
    {"abcabad", {0, 0, 0, 1, 2, 1, 0}},
    {"ababac",  {0, 0, 1, 2, 3, 0}},
    {"abcdefg", {0, 0, 0, 0, 0, 0, 0}},
    {"ababa",   {0, 0, 1, 2, 3}},
  };

  bool is_all_passed = true;
  for (size_t i = 0; i < sizeof(test_cases)/sizeof(test_cases[0]); ++i) {
    const TestCase& t_case = test_cases[i];
    std::string pattern(t_case.pattern);
    std::vector<size_t> table(pattern.length());
    std::copy_n(t_case.table, table.size(), table.begin());
    KMPPatternMatcher matcher(pattern);
    const std::vector<size_t>& r_table = matcher.PatternTable();
    std::cout << "Test Case #" << i << ": ";
    if (table != r_table) {
      std::ostream_iterator<size_t> out_it(std::cout, ",");
      std::cout << "Failed. " << "Expected table of \"" << pattern << "\" to be [";
      std::copy(table.begin(), table.end(), out_it);
      std::cout << "] but got [";
      std::copy(r_table.begin(), r_table.end(), out_it);
      std::cout << "]" << std::endl;
      is_all_passed = false;
    } else {
      std::cout << "Passed. " << std::endl;
    }
  }
  return is_all_passed;
}

bool KMPPatternMatcher_TestIndexInString() {
  std::cout << "Start Test [" << __FUNCTION__ << "]" << std::endl;
  struct TestCase {
    const char* pattern;
    const char* text;
    ssize_t index;
  };

  TestCase test_cases[] = {
    {"abc", 	"abc", 			0},
    {"abab", 	"abcabab", 		3},
    {"abcdefg", "a", 			-1},
    {"ababa", 	"ababcababfababb", 	-1},
    {"abc", 	"ababababababaabc", 	13}, 
  };

  bool is_all_passed = true;
  for (size_t i = 0; i < sizeof(test_cases)/sizeof(test_cases[0]); ++i) {
    const TestCase& t_case = test_cases[i];
    std::string pattern(t_case.pattern);
    std::string text(t_case.text);
    KMPPatternMatcher matcher(pattern);
    ssize_t index = matcher.IndexInString(text);
    std::cout << "Test Case #" << i << ": ";
    if (index != t_case.index) {
      std::cout << "Failed. KMPPatternMatcher(" << pattern 
        << ").IndexString(" << text << ") is expected to be "
        << t_case.index << ", but got " << index << std::endl;
      is_all_passed = false;
    } else {
     std::cout << "Passed." << std::endl;
    }
  }
  return is_all_passed;
}

int main() {
  typedef bool (*TestFunc)();
  TestFunc tests[] = {
    &KMPPatternMatcher_TestPatternCreation,
    &KMPPatternMatcher_TestIndexInString,
  };
  int ret = 0;
  for (size_t i = 0; i < sizeof(tests)/sizeof(tests[0]); ++i) {
    if (!tests[i]()) {
      ret = -1;
    }
  }
  return ret;
}
