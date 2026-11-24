package com.codereview.backend.service;

import com.codereview.backend.entity.HintEntity;
import com.codereview.backend.entity.ProblemEntity;
import com.codereview.backend.entity.TestCaseEntity;
import com.codereview.backend.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DsaProblemSeeder {

    private final ProblemRepository problemRepository;

    @Transactional
    public void seedProblems() {
        seedArrays();
        seedStrings();
        seedMathematics();
        seedSearching();
        seedSorting();
        seedHashing();
        seedTwoPointers();
        seedSlidingWindow();
        seedPrefixSum();
        seedRecursion();
        seedBacktracking();
        seedLinkedLists();
        seedStack();
        seedQueueDeque();
        seedBinaryTrees();
        seedBinarySearchTrees();
        seedHeaps();
        seedGraphTraversal();
        seedShortestPath();
        seedGraphAlgorithms();
        seedGreedy();
        seedDynamicProgramming();
        seedBitManipulation();
        seedIntervals();
        seedMatrix();
        seedAdvancedDataStructures();
        seedJavaOop();
    }

    // 1. ARRAYS (9 problems)
    private void seedArrays() {
        upsertProblem("find-maximum-element", "Find Maximum Element", "EASY", "Arrays",
                "Given an array of integers `arr`, find and return the maximum element in the array.",
                "1 <= arr.length <= 10^5\n-10^9 <= arr[i] <= 10^9",
                "Enter the array as a bracketed list of comma-separated integers.", "Integer representing the maximum value.",
                "[{\"input\": \"arr = [3, 7, 2, 9, 5]\", \"output\": \"9\", \"explanation\": \"9 is the largest element.\"}]",
                "public class Solution {\n    public int findLargest(int[] arr) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Keep track of maximum value seen so far.", "Initialize with arr[0] to handle negative numbers."),
                List.of(new TestCaseSpec("[3, 7, 2, 9, 5]", "9", false, "Standard array"), new TestCaseSpec("[-5, -2, -8, -1]", "-1", true, "All negative values")));

        upsertProblem("second-largest-element", "Second Largest Element", "EASY", "Arrays",
                "Given an array of integers `arr`, find the second largest distinct element. Return -1 if no second largest exists.",
                "1 <= arr.length <= 10^5", "An integer array `arr`.", "Integer second largest element or -1.",
                "[{\"input\": \"arr = [12, 35, 1, 10, 34, 1]\", \"output\": \"34\", \"explanation\": \"34 is second largest.\"}]",
                "public class Solution {\n    public int secondLargest(int[] arr) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Use two tracking variables in a single pass.", "Update second largest when finding element smaller than largest."),
                List.of(new TestCaseSpec("[12, 35, 1, 10, 34, 1]", "34", false, "Standard case"), new TestCaseSpec("[10, 10, 10]", "-1", true, "No distinct second largest")));

        upsertProblem("reverse-an-array", "Reverse an Array", "EASY", "Arrays",
                "Given an array `arr`, reverse the array in-place and return it.",
                "1 <= arr.length <= 10^5", "Integer array `arr`.", "Reversed array.",
                "[{\"input\": \"arr = [1, 2, 3, 4, 5]\", \"output\": \"[5, 4, 3, 2, 1]\", \"explanation\": \"Reversed in-place.\"}]",
                "public class Solution {\n    public int[] reverseArray(int[] arr) {\n        // Write your solution here\n        return arr;\n    }\n}",
                List.of("Use two pointers starting at left and right ends.", "Swap elements while left < right."),
                List.of(new TestCaseSpec("[1, 2, 3, 4, 5]", "[5, 4, 3, 2, 1]", false, "Odd length"), new TestCaseSpec("[10, 20]", "[20, 10]", true, "Even length")),
                "O(N)", "O(1)", true);

        upsertProblem("move-zeroes", "Move Zeroes", "EASY", "Arrays",
                "Given an integer array `nums`, move all zeroes to the end while maintaining relative order of non-zero elements.",
                "1 <= nums.length <= 10^4", "Integer array `nums`.", "Modified array.",
                "[{\"input\": \"nums = [0, 1, 0, 3, 12]\", \"output\": \"[1, 3, 12, 0, 0]\", \"explanation\": \"Zeroes moved to end.\"}]",
                "public class Solution {\n    public int[] moveZeroes(int[] nums) {\n        // Write your solution here\n        return nums;\n    }\n}",
                List.of("Use write pointer for non-zero items.", "Fill remaining space with zeroes."),
                List.of(new TestCaseSpec("[0, 1, 0, 3, 12]", "[1, 3, 12, 0, 0]", false, "Mixed zeroes"), new TestCaseSpec("[0, 0, 0]", "[0, 0, 0]", true, "All zeroes")));

        upsertProblem("rotate-array-k", "Rotate Array", "MEDIUM", "Arrays",
                "Given an integer array `nums`, rotate the array to the right by `k` steps.",
                "1 <= nums.length <= 10^5\n0 <= k <= 10^5", "Array `nums` and integer `k`.", "Rotated array.",
                "[{\"input\": \"nums = [1,2,3,4,5,6,7], k = 3\", \"output\": \"[5, 6, 7, 1, 2, 3, 4]\", \"explanation\": \"Rotated 3 steps right.\"}]",
                "public class Solution {\n    public int[] rotate(int[] nums, int k) {\n        // Write your solution here\n        return nums;\n    }\n}",
                List.of("Compute k = k % length.", "Reverse entire array, then reverse first k, then reverse remaining."),
                List.of(new TestCaseSpec("[1,2,3,4,5,6,7], 3", "[5, 6, 7, 1, 2, 3, 4]", false, "Standard rotation"), new TestCaseSpec("[-1,-100,3,99], 2", "[3, 99, -1, -100]", true, "Negative values")));

        upsertProblem("maximum-subarray-sum", "Maximum Subarray Sum", "MEDIUM", "Arrays",
                "Given an integer array `nums`, find the contiguous subarray with the largest sum and return its sum.",
                "1 <= nums.length <= 10^5", "Integer array `nums`.", "Maximum subarray sum integer.",
                "[{\"input\": \"nums = [-2,1,-3,4,-1,2,1,-5,4]\", \"output\": \"6\", \"explanation\": \"Subarray [4,-1,2,1] has sum 6.\"}]",
                "public class Solution {\n    public int maxSubArray(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Kadane's algorithm.", "Reset current sum to 0 if it drops below zero."),
                List.of(new TestCaseSpec("[-2,1,-3,4,-1,2,1,-5,4]", "6", false, "Standard array"), new TestCaseSpec("[-5,-2,-8,-1]", "-1", true, "All negative values")));

        upsertProblem("product-except-self", "Product of Array Except Self", "MEDIUM", "Arrays",
                "Given an integer array `nums`, return an array `output` such that `output[i]` equals the product of all elements of `nums` except `nums[i]` without division.",
                "2 <= nums.length <= 10^5", "Integer array `nums`.", "Product array.",
                "[{\"input\": \"nums = [1,2,3,4]\", \"output\": \"[24, 12, 8, 6]\", \"explanation\": \"Calculated using prefix and suffix products.\"}]",
                "public class Solution {\n    public int[] productExceptSelf(int[] nums) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Build prefix products in left-to-right pass.", "Multiply with suffix products in right-to-left pass."),
                List.of(new TestCaseSpec("[1,2,3,4]", "[24, 12, 8, 6]", false, "Standard product"), new TestCaseSpec("[-1,1,0,-3,3]", "[0, 0, 9, 0, 0]", true, "Array with zero")));

        upsertProblem("trapping-rain-water", "Trapping Rain Water", "HARD", "Arrays",
                "Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.",
                "1 <= height.length <= 2 * 10^4", "Integer array `height`.", "Total trapped water integer.",
                "[{\"input\": \"height = [0,1,0,2,1,0,1,3,2,1,2,1]\", \"output\": \"6\", \"explanation\": \"Traps 6 units of rain water.\"}]",
                "public class Solution {\n    public int trap(int[] height) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use two pointers (left and right) with leftMax and rightMax.", "Water trapped at pointer is min(leftMax, rightMax) - height."),
                List.of(new TestCaseSpec("[0,1,0,2,1,0,1,3,2,1,2,1]", "6", false, "Complex elevation map"), new TestCaseSpec("[4,2,0,3,2,5]", "9", true, "Deep valley elevation")));

        upsertProblem("first-missing-positive", "First Missing Positive", "HARD", "Arrays",
                "Given an unsorted integer array `nums`, return the smallest missing positive integer in O(n) time and O(1) auxiliary space.",
                "1 <= nums.length <= 10^5", "Integer array `nums`.", "Smallest missing positive integer.",
                "[{\"input\": \"nums = [3,4,-1,1]\", \"output\": \"2\", \"explanation\": \"2 is the smallest missing positive.\"}]",
                "public class Solution {\n    public int firstMissingPositive(int[] nums) {\n        // Write your solution here\n        return 1;\n    }\n}",
                List.of("Place each number x in index x-1 if possible using cyclic swaps.", "First index i where nums[i] != i+1 gives answer i+1."),
                List.of(new TestCaseSpec("[3,4,-1,1]", "2", false, "Standard unsorted array"), new TestCaseSpec("[7,8,9,11,12]", "1", true, "Missing 1")));
    }

    // 2. STRINGS (8 problems)
    private void seedStrings() {
        upsertProblem("reverse-string-chars", "Reverse String Chars", "EASY", "Strings",
                "Write a function that reverses a string.",
                "1 <= s.length <= 10^5", "String `s`.", "Reversed String.",
                "[{\"input\": \"s = \\\"hello\\\"\", \"output\": \"olleh\", \"explanation\": \"Reversed string.\"}]",
                "public class Solution {\n    public String reverseString(String s) {\n        // Write your solution here\n        return \"\";\n    }\n}",
                List.of("Use StringBuilder or swap characters with two pointers."),
                List.of(new TestCaseSpec("hello", "olleh", false, "Standard string"), new TestCaseSpec("Java", "avaJ", true, "Mixed case")));

        upsertProblem("valid-palindrome-check", "Valid Palindrome Check", "EASY", "Strings",
                "Determine if a phrase is a palindrome, considering only alphanumeric characters and ignoring cases.",
                "1 <= s.length <= 2 * 10^5", "String `s`.", "Boolean true or false.",
                "[{\"input\": \"s = \\\"A man, a plan, a canal: Panama\\\"\", \"output\": \"true\", \"explanation\": \"Ignoring non-alphanumeric chars.\"}]",
                "public class Solution {\n    public boolean isPalindrome(String s) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Filter non-alphanumeric chars and compare two pointers."),
                List.of(new TestCaseSpec("A man, a plan, a canal: Panama", "true", false, "Valid palindrome"), new TestCaseSpec("race a car", "false", true, "Invalid palindrome")));

        upsertProblem("valid-anagram", "Valid Anagram", "EASY", "Strings",
                "Given two strings `s` and `t`, return `true` if `t` is an anagram of `s`, and `false` otherwise.",
                "1 <= s.length, t.length <= 5 * 10^4", "Strings `s` and `t`.", "Boolean true or false.",
                "[{\"input\": \"s = \\\"anagram\\\", t = \\\"nagaram\\\"\", \"output\": \"true\", \"explanation\": \"Same char frequencies.\"}]",
                "public class Solution {\n    public boolean isAnagram(String s, String t) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Use integer frequency array of size 26."),
                List.of(new TestCaseSpec("anagram, nagaram", "true", false, "Valid anagram"), new TestCaseSpec("rat, car", "false", true, "Invalid anagram")));

        upsertProblem("longest-common-prefix", "Longest Common Prefix", "EASY", "Strings",
                "Find the longest common prefix string amongst an array of strings.",
                "1 <= strs.length <= 200", "String array `strs`.", "Common prefix string.",
                "[{\"input\": \"strs = [\\\"flower\\\",\\\"flow\\\",\\\"flight\\\"]\", \"output\": \"fl\", \"explanation\": \"fl is shared.\"}]",
                "public class Solution {\n    public String longestCommonPrefix(String[] strs) {\n        // Write your solution here\n        return \"\";\n    }\n}",
                List.of("Start with first string as prefix and trim it against remaining strings."),
                List.of(new TestCaseSpec("flower, flow, flight", "fl", false, "Common prefix fl"), new TestCaseSpec("dog, racecar, car", "", true, "No common prefix")));

        upsertProblem("first-unique-character", "First Unique Character", "MEDIUM", "Strings",
                "Find the first non-repeating character in a string and return its index. Return -1 if none exists.",
                "1 <= s.length <= 10^5", "String `s`.", "Integer index.",
                "[{\"input\": \"s = \\\"code\\\"\", \"output\": \"0\", \"explanation\": \"c is first unique.\"}]",
                "public class Solution {\n    public int firstUniqChar(String s) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Count char frequencies first, then find first char with frequency 1."),
                List.of(new TestCaseSpec("code", "0", false, "First char unique"), new TestCaseSpec("aabb", "-1", true, "All chars repeat")));

        upsertProblem("string-to-integer-atoi", "String to Integer (atoi)", "MEDIUM", "Strings",
                "Implement the `myAtoi(String s)` function which converts a string to a 32-bit signed integer following parsing rules.",
                "0 <= s.length <= 200", "String `s`.", "Parsed 32-bit integer.",
                "[{\"input\": \"s = \\\"   -42\\\"\", \"output\": \"-42\", \"explanation\": \"Parsed negative integer with leading spaces.\"}]",
                "public class Solution {\n    public int myAtoi(String s) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Skip leading whitespace, check sign symbol, and clamp overflow to Integer.MIN_VALUE or Integer.MAX_VALUE."),
                List.of(new TestCaseSpec("   -42", "-42", false, "Negative number with whitespace"), new TestCaseSpec("4193 with words", "4193", true, "Trailing words")));

        upsertProblem("group-anagrams-list", "Group Anagrams List", "MEDIUM", "Strings",
                "Given an array of strings `strs`, group the anagrams together. (Return total count of unique anagram groups for metric check).",
                "1 <= strs.length <= 10^4", "String array `strs`.", "Integer number of anagram groups.",
                "[{\"input\": \"strs = [\\\"eat\\\",\\\"tea\\\",\\\"tan\\\",\\\"ate\\\",\\\"nat\\\",\\\"bat\\\"]\", \"output\": \"3\", \"explanation\": \"3 groups: [eat,tea,ate], [tan,nat], [bat].\"}]",
                "public class Solution {\n    public int groupAnagramCount(String[] strs) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Map sorted character string key to list of anagrams in a HashMap."),
                List.of(new TestCaseSpec("eat, tea, tan, ate, nat, bat", "3", false, "Standard 3 groups"), new TestCaseSpec("a", "1", true, "Single group")));

        upsertProblem("regular-expression-matching", "Regular Expression Matching", "HARD", "Strings",
                "Given an input string `s` and a pattern `p`, implement regular expression matching with support for `'.'` and `'*'`. Return `true` if matched.",
                "1 <= s.length, p.length <= 20", "Strings `s` and `p`.", "Boolean true or false.",
                "[{\"input\": \"s = \\\"aa\\\", p = \\\"a*\\\"\", \"output\": \"true\", \"explanation\": \"'*' means zero or more of preceding element.\"}]",
                "public class Solution {\n    public boolean isMatch(String s, String p) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Use 2D dynamic programming dp[i][j] representing if s[i:] matches p[j:]."),
                List.of(new TestCaseSpec("aa, a*", "true", false, "Star pattern match"), new TestCaseSpec("ab, .* ", "true", true, "Dot star match")));
    }

    // 3. MATHEMATICS (5 problems)
    private void seedMathematics() {
        upsertProblem("palindrome-number", "Palindrome Number", "EASY", "Mathematics",
                "Given an integer `x`, return `true` if `x` is a palindrome integer.",
                "-2^31 <= x <= 2^31 - 1", "Integer `x`.", "Boolean true or false.",
                "[{\"input\": \"x = 121\", \"output\": \"true\", \"explanation\": \"121 reads same backwards.\"}]",
                "public class Solution {\n    public boolean isPalindrome(int x) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Negative numbers are never palindromes.", "Reverse lower half of integer and compare with upper half."),
                List.of(new TestCaseSpec("121", "true", false, "Positive palindrome"), new TestCaseSpec("-121", "false", true, "Negative number")));

        upsertProblem("prime-number-check", "Check Prime Number", "EASY", "Mathematics",
                "Return true if given positive integer `n` is prime.",
                "1 <= n <= 10^9", "Integer `n`.", "Boolean true or false.",
                "[{\"input\": \"n = 29\", \"output\": \"true\", \"explanation\": \"29 is prime.\"}]",
                "public class Solution {\n    public boolean isPrime(int n) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Check divisors up to sqrt(n)."),
                List.of(new TestCaseSpec("29", "true", false, "Prime number"), new TestCaseSpec("4", "false", true, "Composite number")));

        upsertProblem("gcd-lcm-calculator", "Greatest Common Divisor", "EASY", "Mathematics",
                "Find the Greatest Common Divisor (GCD) of two non-negative integers `a` and `b`.",
                "0 <= a, b <= 10^9", "Two integers `a` and `b`.", "Integer GCD.",
                "[{\"input\": \"a = 12, b = 18\", \"output\": \"6\", \"explanation\": \"6 is largest common divisor.\"}]",
                "public class Solution {\n    public int gcd(int a, int b) {\n        // Write your solution here\n        return 1;\n    }\n}",
                List.of("Use Euclidean algorithm: gcd(a, b) = gcd(b, a % b)."),
                List.of(new TestCaseSpec("12, 18", "6", false, "GCD of 12 and 18"), new TestCaseSpec("101, 10", "1", true, "Coprime numbers")));

        upsertProblem("pow-x-n", "Pow(x, n)", "MEDIUM", "Mathematics",
                "Implement `pow(x, n)`, which calculates `x` raised to the power `n` ($x^n$).",
                "-100.0 < x < 100.0\n-2^31 <= n <= 2^31-1", "Double `x` and integer `n`.", "Double result.",
                "[{\"input\": \"x = 2.0, n = 10\", \"output\": \"1024.0\", \"explanation\": \"2^10 = 1024.\"}]",
                "public class Solution {\n    public double myPow(double x, int n) {\n        // Write your solution here\n        return 1.0;\n    }\n}",
                List.of("Use exponentiation by squaring to achieve O(log n) time.", "Handle negative powers by setting x = 1/x."),
                List.of(new TestCaseSpec("2.0, 10", "1024.0", false, "Positive exponent"), new TestCaseSpec("2.1, 3", "9.261", true, "Decimal base")));

        upsertProblem("count-primes-sieve", "Count Primes (Sieve)", "MEDIUM", "Mathematics",
                "Given an integer `n`, return the number of prime numbers strictly less than `n`.",
                "0 <= n <= 5 * 10^6", "Integer `n`.", "Count of primes.",
                "[{\"input\": \"n = 10\", \"output\": \"4\", \"explanation\": \"4 primes less than 10 (2, 3, 5, 7).\"}]",
                "public class Solution {\n    public int countPrimes(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Sieve of Eratosthenes with boolean array of size n."),
                List.of(new TestCaseSpec("10", "4", false, "Primes < 10"), new TestCaseSpec("0", "0", true, "Base case 0")));
    }

    // 4. SEARCHING (4 problems)
    private void seedSearching() {
        upsertProblem("linear-search-index", "Linear Search Index", "EASY", "Searching",
                "Return index of `target` in array `arr` or -1 if missing.",
                "1 <= arr.length <= 10^5", "Array `arr` and integer `target`.", "Integer index or -1.",
                "[{\"input\": \"arr = [10, 20, 30, 40], target = 30\", \"output\": \"2\", \"explanation\": \"30 found at index 2.\"}]",
                "public class Solution {\n    public int search(int[] arr, int target) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Iterate index 0 to length - 1."),
                List.of(new TestCaseSpec("[10, 20, 30, 40], 30", "2", false, "Target present"), new TestCaseSpec("[1, 2, 3], 5", "-1", true, "Target absent")));

        upsertProblem("binary-search-sorted", "Binary Search Sorted", "EASY", "Searching",
                "Given a sorted integer array `nums` and a `target`, return its index using binary search or -1.",
                "1 <= nums.length <= 10^5", "Sorted array `nums` and integer `target`.", "Integer index or -1.",
                "[{\"input\": \"nums = [-1,0,3,5,9,12], target = 9\", \"output\": \"4\", \"explanation\": \"9 found at index 4.\"}]",
                "public class Solution {\n    public int search(int[] nums, int target) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Compute mid = low + (high - low) / 2."),
                List.of(new TestCaseSpec("[-1,0,3,5,9,12], 9", "4", false, "Target present"), new TestCaseSpec("[-1,0,3,5,9,12], 2", "-1", true, "Target absent")));

        upsertProblem("search-rotated-sorted-array", "Search in Rotated Sorted Array", "MEDIUM", "Searching",
                "Given a rotated sorted array `nums` and a `target`, return index of `target` or -1 in O(log n) time.",
                "1 <= nums.length <= 5000", "Rotated array `nums` and integer `target`.", "Integer index.",
                "[{\"input\": \"nums = [4,5,6,7,0,1,2], target = 0\", \"output\": \"4\", \"explanation\": \"0 at index 4.\"}]",
                "public class Solution {\n    public int search(int[] nums, int target) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Identify which half is strictly sorted in each step."),
                List.of(new TestCaseSpec("[4,5,6,7,0,1,2], 0", "4", false, "Target in second half"), new TestCaseSpec("[4,5,6,7,0,1,2], 3", "-1", true, "Target missing")));

        upsertProblem("search-2d-matrix", "Search a 2D Matrix", "MEDIUM", "Searching",
                "Write an efficient algorithm that searches for a `target` value in an `m x n` integer matrix where rows and columns are sorted.",
                "1 <= m, n <= 100", "2D integer matrix `matrix` and integer `target`.", "Boolean true or false.",
                "[{\"input\": \"matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3\", \"output\": \"true\", \"explanation\": \"3 exists in matrix.\"}]",
                "public class Solution {\n    public boolean searchMatrix(int[][] matrix, int target) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Start search at top-right corner (row 0, col n-1).", "Move left if current > target, move down if current < target."),
                List.of(new TestCaseSpec("[[1,3,5,7],[10,11,16,20],[23,30,34,60]], 3", "true", false, "Target present"), new TestCaseSpec("[[1,3,5,7],[10,11,16,20],[23,30,34,60]], 13", "false", true, "Target absent")));
    }

    // 5. SORTING (4 problems)
    private void seedSorting() {
        upsertProblem("bubble-sort-array", "Bubble Sort Array", "EASY", "Sorting",
                "Sort an unsorted array in ascending order using Bubble Sort.",
                "1 <= arr.length <= 1000", "Unsorted array `arr`.", "Sorted array.",
                "[{\"input\": \"arr = [64, 34, 25, 12, 22]\", \"output\": \"[12, 22, 25, 34, 64]\", \"explanation\": \"Sorted array.\"}]",
                "public class Solution {\n    public int[] bubbleSort(int[] arr) {\n        // Write your solution here\n        return arr;\n    }\n}",
                List.of("Swap adjacent elements if out of order."),
                List.of(new TestCaseSpec("[64, 34, 25, 12, 22]", "[12, 22, 25, 34, 64]", false, "Standard array"), new TestCaseSpec("[5, 1, 4, 2, 8]", "[1, 2, 4, 5, 8]", true, "Small array")));

        upsertProblem("insertion-sort-array", "Insertion Sort Array", "EASY", "Sorting",
                "Sort an array in ascending order using Insertion Sort.",
                "1 <= arr.length <= 1000", "Unsorted array `arr`.", "Sorted array.",
                "[{\"input\": \"arr = [12, 11, 13, 5, 6]\", \"output\": \"[5, 6, 11, 12, 13]\", \"explanation\": \"Sorted array.\"}]",
                "public class Solution {\n    public int[] insertionSort(int[] arr) {\n        // Write your solution here\n        return arr;\n    }\n}",
                List.of("Insert current element into sorted subarray on left."),
                List.of(new TestCaseSpec("[12, 11, 13, 5, 6]", "[5, 6, 11, 12, 13]", false, "Standard array"), new TestCaseSpec("[3, 1, 2]", "[1, 2, 3]", true, "Small array")));

        upsertProblem("sort-colors-dutch-flag", "Sort Colors (0s, 1s, 2s)", "MEDIUM", "Sorting",
                "Given an array `nums` with 0s, 1s, and 2s, sort them in-place in linear time.",
                "1 <= nums.length <= 300", "Array of 0s, 1s, 2s `nums`.", "Sorted array.",
                "[{\"input\": \"nums = [2,0,2,1,1,0]\", \"output\": \"[0, 0, 1, 1, 2, 2]\", \"explanation\": \"Sorted in-place.\"}]",
                "public class Solution {\n    public int[] sortColors(int[] nums) {\n        // Write your solution here\n        return nums;\n    }\n}",
                List.of("Use Dutch National Flag algorithm with low, mid, high pointers."),
                List.of(new TestCaseSpec("[2,0,2,1,1,0]", "[0, 0, 1, 1, 2, 2]", false, "Mixed elements"), new TestCaseSpec("[2,0,1]", "[0, 1, 2]", true, "Small array")));

        upsertProblem("merge-sort-array", "Merge Sort Array", "MEDIUM", "Sorting",
                "Sort an array in non-decreasing order using Merge Sort.",
                "1 <= arr.length <= 5 * 10^4", "Unsorted array `arr`.", "Sorted array.",
                "[{\"input\": \"arr = [38, 27, 43, 3, 9, 82, 10]\", \"output\": \"[3, 9, 10, 27, 38, 43, 82]\", \"explanation\": \"Merge sort divide and conquer.\"}]",
                "public class Solution {\n    public int[] mergeSort(int[] arr) {\n        // Write your solution here\n        return arr;\n    }\n}",
                List.of("Divide array into left and right halves recursively.", "Merge sorted halves together in linear time."),
                List.of(new TestCaseSpec("[38, 27, 43, 3, 9, 82, 10]", "[3, 9, 10, 27, 38, 43, 82]", false, "Standard array"), new TestCaseSpec("[5, 2, 3, 1]", "[1, 2, 3, 5]", true, "Small array")));
    }

    // 6. HASHING (5 problems)
    private void seedHashing() {
        upsertProblem("two-sum-indices", "Two Sum", "EASY", "Hashing",
                "Find indices of two numbers in `nums` that add up to `target`.",
                "2 <= nums.length <= 10^4", "Array `nums` and integer `target`.", "Array of 2 indices.",
                "[{\"input\": \"nums = [2,7,11,15], target = 9\", \"output\": \"[0, 1]\", \"explanation\": \"2 + 7 = 9.\"}]",
                "public class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Use HashMap mapping number to its index."),
                List.of(new TestCaseSpec("[2,7,11,15], 9", "[0, 1]", false, "Standard two sum"), new TestCaseSpec("[3,3], 6", "[0, 1]", true, "Duplicate values")));

        upsertProblem("contains-duplicate-hash", "Contains Duplicate", "EASY", "Hashing",
                "Return true if any value appears at least twice in array `nums`.",
                "1 <= nums.length <= 10^5", "Integer array `nums`.", "Boolean true or false.",
                "[{\"input\": \"nums = [1,2,3,1]\", \"output\": \"true\", \"explanation\": \"1 is repeated.\"}]",
                "public class Solution {\n    public boolean containsDuplicate(int[] nums) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Use a HashSet to record visited elements."),
                List.of(new TestCaseSpec("[1,2,3,1]", "true", false, "Duplicate present"), new TestCaseSpec("[1,2,3,4]", "false", true, "All distinct")));

        upsertProblem("intersection-two-arrays", "Intersection of Two Arrays", "MEDIUM", "Hashing",
                "Given two integer arrays `nums1` and `nums2`, return an array of their intersection. Each element must be unique.",
                "1 <= nums1.length, nums2.length <= 1000", "Two integer arrays `nums1` and `nums2`.", "Array of common unique integers.",
                "[{\"input\": \"nums1 = [1,2,2,1], nums2 = [2,2]\", \"output\": \"[2]\", \"explanation\": \"2 is common in both arrays.\"}]",
                "public class Solution {\n    public int[] intersection(int[] nums1, int[] nums2) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Put elements of nums1 into a HashSet.", "Filter nums2 against HashSet."),
                List.of(new TestCaseSpec("[1,2,2,1], [2,2]", "[2]", false, "Single intersection"), new TestCaseSpec("[4,9,5], [9,4,9,8,4]", "[9, 4]", true, "Multiple intersection")));

        upsertProblem("subarrays-with-equal-zeroes-ones", "Subarrays with Equal 0s and 1s", "MEDIUM", "Hashing",
                "Given a binary array `nums` containing 0s and 1s, return maximum length of a contiguous subarray with equal number of 0s and 1s.",
                "1 <= nums.length <= 5 * 10^4", "Binary array `nums`.", "Maximum length integer.",
                "[{\"input\": \"nums = [0,1,0]\", \"output\": \"2\", \"explanation\": \"[0, 1] has equal 0s and 1s.\"}]",
                "public class Solution {\n    public int findMaxLength(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Treat 0 as -1 and calculate prefix sum.", "Store first occurrence of each prefix sum in a HashMap."),
                List.of(new TestCaseSpec("[0,1,0]", "2", false, "Subarray length 2"), new TestCaseSpec("[0,1,0,1]", "4", true, "Subarray length 4")));

        upsertProblem("longest-consecutive-sequence", "Longest Consecutive Sequence", "HARD", "Hashing",
                "Given an unsorted array of integers `nums`, return the length of the longest consecutive elements sequence in O(n) time.",
                "0 <= nums.length <= 10^5", "Unsorted array `nums`.", "Maximum consecutive length.",
                "[{\"input\": \"nums = [100,4,200,1,3,2]\", \"output\": \"4\", \"explanation\": \"Sequence [1, 2, 3, 4] has length 4.\"}]",
                "public class Solution {\n    public int longestConsecutive(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Add all numbers into a HashSet.", "Only start building sequence if num - 1 is NOT in the set."),
                List.of(new TestCaseSpec("[100,4,200,1,3,2]", "4", false, "Consecutive 1,2,3,4"), new TestCaseSpec("[0,3,7,2,5,8,4,6,0,1]", "9", true, "Long consecutive sequence")));
    }

    // 7. TWO POINTERS (5 problems)
    private void seedTwoPointers() {
        upsertProblem("pair-with-target-sum-sorted", "Pair With Target Sum", "EASY", "Two Pointers",
                "Find if a sorted array `arr` contains a pair that adds up to `target`.",
                "2 <= arr.length <= 10^5", "Sorted array `arr` and integer `target`.", "Boolean true or false.",
                "[{\"input\": \"arr = [1, 2, 3, 4, 6], target = 6\", \"output\": \"true\", \"explanation\": \"2 + 4 = 6.\"}]",
                "public class Solution {\n    public boolean hasPairWithSum(int[] arr, int target) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Use two pointers at start and end."),
                List.of(new TestCaseSpec("[1, 2, 3, 4, 6], 6", "true", false, "Pair present"), new TestCaseSpec("[1, 2, 3, 4, 6], 11", "false", true, "Pair absent")));

        upsertProblem("remove-duplicates-sorted-array", "Remove Duplicates Sorted", "EASY", "Two Pointers",
                "Remove duplicates from sorted array `nums` in-place and return number of unique elements.",
                "1 <= nums.length <= 3 * 10^4", "Sorted array `nums`.", "Integer unique count.",
                "[{\"input\": \"nums = [1,1,2]\", \"output\": \"2\", \"explanation\": \"Unique elements 1 and 2.\"}]",
                "public class Solution {\n    public int removeDuplicates(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Maintain write index pointer for unique values."),
                List.of(new TestCaseSpec("[1,1,2]", "2", false, "Small duplicates"), new TestCaseSpec("[0,0,1,1,1,2,2,3,3,4]", "5", true, "Multiple duplicates")));

        upsertProblem("container-most-water", "Container With Most Water", "MEDIUM", "Two Pointers",
                "Find two lines in `height` array forming a container that holds the most water.",
                "2 <= height.length <= 10^5", "Integer array `height`.", "Maximum area integer.",
                "[{\"input\": \"height = [1,8,6,2,5,4,8,3,7]\", \"output\": \"49\", \"explanation\": \"Lines at index 1 and 8.\"}]",
                "public class Solution {\n    public int maxArea(int[] height) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Move pointer at shorter height inward."),
                List.of(new TestCaseSpec("[1,8,6,2,5,4,8,3,7]", "49", false, "Standard container"), new TestCaseSpec("[1,1]", "1", true, "Min container")));

        upsertProblem("three-sum-zero", "3Sum Zero Triplets", "MEDIUM", "Two Pointers",
                "Given an integer array nums, return the number of unique triplets [nums[i], nums[j], nums[k]] such that nums[i] + nums[j] + nums[k] == 0.",
                "3 <= nums.length <= 3000", "Integer array `nums`.", "Integer count of unique zero triplets.",
                "[{\"input\": \"nums = [-1,0,1,2,-1,-4]\", \"output\": \"2\", \"explanation\": \"Triplets [-1,-1,2] and [-1,0,1].\"}]",
                "public class Solution {\n    public int threeSumCount(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Sort array first.", "For each element nums[i], use two pointers for remaining target -nums[i]."),
                List.of(new TestCaseSpec("[-1,0,1,2,-1,-4]", "2", false, "Two triplets"), new TestCaseSpec("[0,1,1]", "0", true, "No zero triplets")));

        upsertProblem("trapping-rain-water-two-pointer", "Rain Water Trap Two Pointer", "HARD", "Two Pointers",
                "Compute total rainwater trapped in height elevation map using O(1) space.",
                "1 <= height.length <= 2 * 10^4", "Integer array `height`.", "Trapped water integer.",
                "[{\"input\": \"height = [0,1,0,2,1,0,1,3,2,1,2,1]\", \"output\": \"6\", \"explanation\": \"6 units trapped.\"}]",
                "public class Solution {\n    public int trap(int[] height) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Maintain leftMax and rightMax while moving left/right pointers."),
                List.of(new TestCaseSpec("[0,1,0,2,1,0,1,3,2,1,2,1]", "6", false, "Standard elevation map"), new TestCaseSpec("[4,2,0,3,2,5]", "9", true, "Deep valley elevation")));
    }

    // 8. SLIDING WINDOW (5 problems)
    private void seedSlidingWindow() {
        upsertProblem("max-sum-subarray-k-size", "Max Sum Subarray Size K", "EASY", "Sliding Window",
                "Find max sum of contiguous subarray of size `k` in array `arr`.",
                "1 <= k <= arr.length <= 10^5", "Array `arr` and integer `k`.", "Maximum sum integer.",
                "[{\"input\": \"arr = [2, 1, 5, 1, 3, 2], k = 3\", \"output\": \"9\", \"explanation\": \"[5, 1, 3] gives sum 9.\"}]",
                "public class Solution {\n    public int maxSumSubarray(int[] arr, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Maintain sum of window of size k.", "Slide right adding next element and removing first element."),
                List.of(new TestCaseSpec("[2, 1, 5, 1, 3, 2], 3", "9", false, "Window size 3"), new TestCaseSpec("[2, 3, 4, 1, 5], 2", "7", true, "Window size 2")));

        upsertProblem("min-size-subarray-sum", "Minimum Size Subarray Sum", "EASY", "Sliding Window",
                "Find minimal length of contiguous subarray of which sum is >= `target`. Return 0 if none.",
                "1 <= target <= 10^9", "Integer array `nums` and integer `target`.", "Minimum length integer.",
                "[{\"input\": \"nums = [2,3,1,2,4,3], target = 7\", \"output\": \"2\", \"explanation\": \"Subarray [4,3] has min length 2.\"}]",
                "public class Solution {\n    public int minSubArrayLen(int target, int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Expand right pointer adding elements.", "Shrink left pointer while window sum >= target."),
                List.of(new TestCaseSpec("[2,3,1,2,4,3], 7", "2", false, "Subarray length 2"), new TestCaseSpec("[1,4,4], 4", "1", true, "Single element target")));

        upsertProblem("longest-substring-non-repeating", "Longest Substring Non-Repeating", "MEDIUM", "Sliding Window",
                "Find length of longest substring without repeating characters in string `s`.",
                "0 <= s.length <= 5 * 10^4", "String `s`.", "Length integer.",
                "[{\"input\": \"s = \\\"abcabcbb\\\"\", \"output\": \"3\", \"explanation\": \"abc has length 3.\"}]",
                "public class Solution {\n    public int lengthOfLongestSubstring(String s) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use HashSet for current window characters."),
                List.of(new TestCaseSpec("abcabcbb", "3", false, "Standard repeating string"), new TestCaseSpec("bbbbb", "1", true, "Single char string")));

        upsertProblem("permutation-in-string", "Permutation in String", "MEDIUM", "Sliding Window",
                "Given two strings `s1` and `s2`, return `true` if `s2` contains a permutation of `s1`.",
                "1 <= s1.length, s2.length <= 10^4", "Two strings `s1` and `s2`.", "Boolean true or false.",
                "[{\"input\": \"s1 = \\\"ab\\\", s2 = \\\"eidbaooo\\\"\", \"output\": \"true\", \"explanation\": \"s2 contains ba which is permutation of ab.\"}]",
                "public class Solution {\n    public boolean checkInclusion(String s1, String s2) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Maintain char frequency count for s1.", "Slide window of fixed size s1.length over s2."),
                List.of(new TestCaseSpec("ab, eidbaooo", "true", false, "Permutation present"), new TestCaseSpec("ab, eidboaoo", "false", true, "Permutation absent")));

        upsertProblem("subarrays-with-k-distinct", "Subarrays with K Different Integers", "HARD", "Sliding Window",
                "Given an integer array `nums` and an integer `k`, return number of good subarrays containing exactly `k` different integers.",
                "1 <= nums.length <= 2 * 10^4", "Integer array `nums` and integer `k`.", "Number of good subarrays.",
                "[{\"input\": \"nums = [1,2,1,2,3], k = 2\", \"output\": \"7\", \"explanation\": \"7 subarrays with 2 distinct integers.\"}]",
                "public class Solution {\n    public int subarraysWithKDistinct(int[] nums, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Number of subarrays with exactly k distinct = atMost(k) - atMost(k-1)."),
                List.of(new TestCaseSpec("[1,2,1,2,3], 2", "7", false, "7 subarrays"), new TestCaseSpec("[1,2,1,3,4], 3", "3", true, "3 subarrays")));
    }

    // 9. PREFIX SUM (4 problems)
    private void seedPrefixSum() {
        upsertProblem("range-sum-query", "Range Sum Query", "EASY", "Prefix Sum / Difference Array",
                "Calculate sum of elements of `nums` between indices `left` and `right` inclusive.",
                "1 <= nums.length <= 10^4", "Integer array `nums`, integer `left`, integer `right`.", "Sum integer.",
                "[{\"input\": \"nums = [-2, 0, 3, -5, 2, -1], left = 2, right = 5\", \"output\": \"-1\", \"explanation\": \"3 + (-5) + 2 + (-1) = -1.\"}]",
                "public class Solution {\n    public int sumRange(int[] nums, int left, int right) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Build prefix sum array prefix[i] = sum(0..i).", "Range sum = prefix[right] - prefix[left-1]."),
                List.of(new TestCaseSpec("[-2, 0, 3, -5, 2, -1], 2, 5", "-1", false, "Range 2 to 5"), new TestCaseSpec("[1, 2, 3, 4], 0, 2", "6", true, "Range 0 to 2")));

        upsertProblem("subarray-sum-equals-k", "Subarray Sum Equals K", "MEDIUM", "Prefix Sum / Difference Array",
                "Find total number of contiguous subarrays whose sum equals `k`.",
                "1 <= nums.length <= 2 * 10^4", "Integer array `nums` and integer `k`.", "Subarray count integer.",
                "[{\"input\": \"nums = [1,1,1], k = 2\", \"output\": \"2\", \"explanation\": \"Subarrays [1,1] at index 0..1 and 1..2.\"}]",
                "public class Solution {\n    public int subarraySum(int[] nums, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Maintain running prefix sum.", "Use HashMap to track frequency of prefix sums seen."),
                List.of(new TestCaseSpec("[1,1,1], 2", "2", false, "2 subarrays"), new TestCaseSpec("[1,2,3], 3", "2", true, "2 subarrays")));

        upsertProblem("contiguous-array-zero-one", "Contiguous Zero One Subarray", "MEDIUM", "Prefix Sum / Difference Array",
                "Given a binary array `nums`, find max length of contiguous subarray with equal number of 0s and 1s.",
                "1 <= nums.length <= 5 * 10^4", "Binary array `nums`.", "Maximum length integer.",
                "[{\"input\": \"nums = [0,1]\", \"output\": \"2\", \"explanation\": \"[0,1] has length 2.\"}]",
                "public class Solution {\n    public int findMaxLength(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Transform 0 to -1.", "Use HashMap to find max distance between identical prefix sums."),
                List.of(new TestCaseSpec("[0,1]", "2", false, "Length 2"), new TestCaseSpec("[0,1,0,1,1,0]", "6", true, "Length 6")));

        upsertProblem("maximum-subarray-sum-difference", "Max Subarray Sum Difference", "HARD", "Prefix Sum / Difference Array",
                "Given an array `nums`, find two non-overlapping contiguous subarrays such that difference between their sums is maximized.",
                "2 <= nums.length <= 2000", "Integer array `nums`.", "Maximum difference integer.",
                "[{\"input\": \"nums = [2,-1,-2,1,-4,28]\", \"output\": \"35\", \"explanation\": \"Subarrays [-4] and [28] give difference 35.\"}]",
                "public class Solution {\n    public int maxSubarrayDiff(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Compute max and min subarray sums from left to right and right to left.", "Maximize |leftMax - rightMin| and |leftMin - rightMax|."),
                List.of(new TestCaseSpec("[2,-1,-2,1,-4,28]", "35", false, "Max diff 35"), new TestCaseSpec("[-1,-2,-3]", "3", true, "Negative numbers")));
    }

    // 10. RECURSION (4 problems)
    private void seedRecursion() {
        upsertProblem("recursive-factorial", "Recursive Factorial", "EASY", "Recursion",
                "Compute factorial of non-negative integer `n` recursively.",
                "0 <= n <= 12", "Integer `n`.", "Factorial integer.",
                "[{\"input\": \"n = 5\", \"output\": \"120\", \"explanation\": \"5! = 120.\"}]",
                "public class Solution {\n    public int factorial(int n) {\n        // Write your solution here\n        return 1;\n    }\n}",
                List.of("Base case: n <= 1 return 1."),
                List.of(new TestCaseSpec("5", "120", false, "Factorial 5"), new TestCaseSpec("0", "1", true, "Base case 0")));

        upsertProblem("recursive-fibonacci", "Recursive Fibonacci", "EASY", "Recursion",
                "Compute `n`-th Fibonacci number recursively.",
                "0 <= n <= 30", "Integer `n`.", "Fibonacci integer.",
                "[{\"input\": \"n = 6\", \"output\": \"8\", \"explanation\": \"F(6) = 8.\"}]",
                "public class Solution {\n    public int fib(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Base cases F(0)=0, F(1)=1."),
                List.of(new TestCaseSpec("6", "8", false, "Fibonacci 6"), new TestCaseSpec("1", "1", true, "Base case 1")));

        upsertProblem("power-of-three-recursive", "Power of Three", "MEDIUM", "Recursion",
                "Determine if integer `n` is a power of three recursively.",
                "-2^31 <= n <= 2^31 - 1", "Integer `n`.", "Boolean true or false.",
                "[{\"input\": \"n = 27\", \"output\": \"true\", \"explanation\": \"3^3 = 27.\"}]",
                "public class Solution {\n    public boolean isPowerOfThree(int n) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("If n <= 0 return false.", "If n % 3 == 0 recurse on n / 3."),
                List.of(new TestCaseSpec("27", "true", false, "Power of 3"), new TestCaseSpec("45", "false", true, "Not power of 3")));

        upsertProblem("kth-symbol-in-grammar", "K-th Symbol in Grammar", "MEDIUM", "Recursion",
                "Row 1 is 0. Next row is generated by replacing 0 with 01 and 1 with 10. Return K-th symbol in N-th row.",
                "1 <= n <= 30\n1 <= k <= 2^(n-1)", "Integers `n` and `k`.", "Symbol 0 or 1.",
                "[{\"input\": \"n = 2, k = 2\", \"output\": \"1\", \"explanation\": \"Row 2 is 01, index 2 is 1.\"}]",
                "public class Solution {\n    public int kthGrammar(int n, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("If k is odd, symbol equals kthGrammar(n-1, (k+1)/2).", "If k is even, symbol is complement of kthGrammar(n-1, k/2)."),
                List.of(new TestCaseSpec("2, 2", "1", false, "Row 2 index 2"), new TestCaseSpec("1, 1", "0", true, "Base case")));
    }

    // 11. BACKTRACKING (5 problems)
    private void seedBacktracking() {
        upsertProblem("generate-all-subsets", "Generate All Subsets", "EASY", "Backtracking",
                "Given an array `nums` of unique elements, return the number of distinct subsets (power set).",
                "1 <= nums.length <= 10", "Integer array `nums`.", "Integer count of subsets.",
                "[{\"input\": \"nums = [1,2,3]\", \"output\": \"8\", \"explanation\": \"2^3 = 8 subsets.\"}]",
                "public class Solution {\n    public int subsetCount(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("For each element, decide whether to include or exclude."),
                List.of(new TestCaseSpec("[1,2,3]", "8", false, "3 elements"), new TestCaseSpec("[0]", "2", true, "1 element")));

        upsertProblem("array-permutations", "Array Permutations", "MEDIUM", "Backtracking",
                "Return total count of unique permutations for array `nums` with distinct integers.",
                "1 <= nums.length <= 6", "Integer array `nums`.", "Integer count of permutations.",
                "[{\"input\": \"nums = [1,2,3]\", \"output\": \"6\", \"explanation\": \"3! = 6 permutations.\"}]",
                "public class Solution {\n    public int permuteCount(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use backtracking to swap each element into current position."),
                List.of(new TestCaseSpec("[1,2,3]", "6", false, "3! = 6"), new TestCaseSpec("[1,2]", "2", true, "2! = 2")));

        upsertProblem("combination-sum-target", "Combination Sum Target", "MEDIUM", "Backtracking",
                "Return number of unique combinations of candidates that sum to `target`. Candidates may be used unlimited times.",
                "1 <= candidates.length <= 30", "Array `candidates` and integer `target`.", "Integer combinations count.",
                "[{\"input\": \"candidates = [2,3,6,7], target = 7\", \"output\": \"2\", \"explanation\": \"Combinations: [2,2,3] and [7].\"}]",
                "public class Solution {\n    public int combinationSumCount(int[] candidates, int target) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Sort candidates and backtrack.", "Subtract candidate from target and recurse."),
                List.of(new TestCaseSpec("[2,3,6,7], 7", "2", false, "2 combinations"), new TestCaseSpec("[2,3,5], 8", "3", true, "3 combinations")));

        upsertProblem("word-search-grid", "Word Search in Grid", "MEDIUM", "Backtracking",
                "Check if `word` exists in 2D character `board` moving horizontally or vertically.",
                "1 <= m, n <= 6", "2D char board `board` and string `word`.", "Boolean true or false.",
                "[{\"input\": \"board = [[\\\"A\\\",\\\"B\\\",\\\"C\\\",\\\"E\\\"],[\\\"S\\\",\\\"F\\\",\\\"C\\\",\\\"S\\\"],[\\\"A\\\",\\\"D\\\",\\\"E\\\",\\\"E\\\"]], word = \\\"ABCCED\\\"\", \"output\": \"true\", \"explanation\": \"Word found in grid.\"}]",
                "public class Solution {\n    public boolean exist(char[][] board, String word) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Use DFS with backtracking.", "Mark visited characters temporarily with '#'."),
                List.of(new TestCaseSpec("board = [[\"A\",\"B\",\"C\",\"E\"],[\"S\",\"F\",\"C\",\"S\"],[\"A\",\"D\",\"E\",\"E\"]], word = ABCCED", "true", false, "Word exists"), new TestCaseSpec("board = [[\"A\",\"B\"],[\"C\",\"D\"]], word = ABCD", "false", true, "Word absent")));

        upsertProblem("n-queens-puzzle", "N-Queens Solutions Count", "HARD", "Backtracking",
                "Return total number of distinct solutions to place `n` queens on an `n x n` chessboard without mutual attacks.",
                "1 <= n <= 9", "Integer `n`.", "Total distinct solutions count.",
                "[{\"input\": \"n = 4\", \"output\": \"2\", \"explanation\": \"2 distinct solutions for 4-Queens.\"}]",
                "public class Solution {\n    public int totalNQueens(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use column, positive diagonal, and negative diagonal boolean tracking sets."),
                List.of(new TestCaseSpec("4", "2", false, "4-Queens"), new TestCaseSpec("1", "1", true, "1-Queen")));
    }

    // 12. LINKED LISTS (5 problems)
    private void seedLinkedLists() {
        upsertProblem("reverse-singly-linked-list", "Reverse Singly Linked List", "EASY", "Linked Lists",
                "Reverse a singly linked list represented as an array of values.",
                "0 <= arr.length <= 5000", "Integer array `arr`.", "Reversed array values.",
                "[{\"input\": \"arr = [1,2,3,4,5]\", \"output\": \"[5, 4, 3, 2, 1]\", \"explanation\": \"List reversed.\"}]",
                "public class Solution {\n    public int[] reverseList(int[] arr) {\n        // Write your solution here\n        return arr;\n    }\n}",
                List.of("Track prev, curr, and next pointers."),
                List.of(new TestCaseSpec("[1,2,3,4,5]", "[5, 4, 3, 2, 1]", false, "Standard list"), new TestCaseSpec("[1,2]", "[2, 1]", true, "Two elements")));

        upsertProblem("middle-linked-list", "Middle of Linked List", "EASY", "Linked Lists",
                "Find middle element value of a linked list represented as array.",
                "1 <= arr.length <= 100", "Integer array `arr`.", "Middle element value.",
                "[{\"input\": \"arr = [1,2,3,4,5]\", \"output\": \"3\", \"explanation\": \"3 is middle node.\"}]",
                "public class Solution {\n    public int findMiddle(int[] arr) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use slow and fast pointers."),
                List.of(new TestCaseSpec("[1,2,3,4,5]", "3", false, "Odd length"), new TestCaseSpec("[1,2,3,4,5,6]", "4", true, "Even length")));

        upsertProblem("detect-linked-list-cycle", "Detect Linked List Cycle", "MEDIUM", "Linked Lists",
                "Determine if a linked list has a cycle given node values and index `pos` where tail connects.",
                "1 <= arr.length <= 10^4", "Integer array `arr` and integer `pos`.", "Boolean true or false.",
                "[{\"input\": \"arr = [3,2,0,-4], pos = 1\", \"output\": \"true\", \"explanation\": \"Tail connects to node index 1.\"}]",
                "public class Solution {\n    public boolean hasCycle(int[] arr, int pos) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Floyd's Cycle-Finding Algorithm (slow and fast pointers)."),
                List.of(new TestCaseSpec("[3,2,0,-4], 1", "true", false, "Cycle present"), new TestCaseSpec("[1], -1", "false", true, "No cycle")));

        upsertProblem("merge-two-sorted-lists", "Merge Two Sorted Lists", "EASY", "Linked Lists",
                "Merge two sorted integer arrays representing sorted linked lists.",
                "0 <= arr1.length, arr2.length <= 50", "Two sorted arrays `arr1` and `arr2`.", "Merged sorted array.",
                "[{\"input\": \"arr1 = [1,2,4], arr2 = [1,3,4]\", \"output\": \"[1, 1, 2, 3, 4, 4]\", \"explanation\": \"Merged sorted list.\"}]",
                "public class Solution {\n    public int[] mergeTwoLists(int[] arr1, int[] arr2) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Compare heads of both lists iteratively."),
                List.of(new TestCaseSpec("[1,2,4], [1,3,4]", "[1, 1, 2, 3, 4, 4]", false, "Standard merge"), new TestCaseSpec("[], [0]", "[0]", true, "Empty list merge")));

        upsertProblem("merge-k-sorted-lists", "Merge K Sorted Lists", "HARD", "Linked Lists",
                "Merge `k` sorted integer arrays into one sorted array.",
                "0 <= k <= 10^4", "2D integer array `lists`.", "Merged sorted array.",
                "[{\"input\": \"lists = [[1,4,5],[1,3,4],[2,6]]\", \"output\": \"[1, 1, 2, 3, 4, 4, 5, 6]\", \"explanation\": \"Merged k sorted lists.\"}]",
                "public class Solution {\n    public int[] mergeKLists(int[][] lists) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Use a Min-Heap (PriorityQueue) to store current head of each list."),
                List.of(new TestCaseSpec("[[1,4,5],[1,3,4],[2,6]]", "[1, 1, 2, 3, 4, 4, 5, 6]", false, "3 lists merge"), new TestCaseSpec("[]", "[]", true, "Empty input")));
    }

    // 13. STACK (5 problems)
    private void seedStack() {
        upsertProblem("valid-parentheses-stack", "Valid Parentheses", "EASY", "Stack",
                "Given a string `s` containing brackets, determine if the input string is valid.",
                "1 <= s.length <= 10^4", "String `s`.", "Boolean true or false.",
                "[{\"input\": \"s = \\\"()[]{}\\\"\", \"output\": \"true\", \"explanation\": \"All matched.\"}]",
                "public class Solution {\n    public boolean isValid(String s) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Push opening brackets to stack and match closing brackets."),
                List.of(new TestCaseSpec("()[]{}", "true", false, "Valid brackets"), new TestCaseSpec("(]", "false", true, "Mismatched bracket")));

        upsertProblem("min-stack-design", "Min Stack Design", "MEDIUM", "Stack",
                "Design a stack that supports push, pop, top, and retrieving minimum element in O(1) time.",
                "1 <= operations <= 10^4", "Array of integers `values`.", "Integer minimum after all pushes.",
                "[{\"input\": \"values = [-2, 0, -3]\", \"output\": \"-3\", \"explanation\": \"Min is -3 after pushes.\"}]",
                "public class Solution {\n    public int getMinAfterPushes(int[] values) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Maintain auxiliary stack for minimums."),
                List.of(new TestCaseSpec("[-2, 0, -3]", "-3", false, "Min is -3"), new TestCaseSpec("[5, 3, 7]", "3", true, "Min is 3")));

        upsertProblem("next-greater-element-i", "Next Greater Element", "MEDIUM", "Stack",
                "Find next greater element for each item in array `nums`. If none, use -1.",
                "1 <= nums.length <= 10^4", "Integer array `nums`.", "Array of next greater elements.",
                "[{\"input\": \"nums = [4, 5, 2, 25]\", \"output\": \"[5, 25, 25, -1]\", \"explanation\": \"Next greater elements.\"}]",
                "public class Solution {\n    public int[] nextGreaterElement(int[] nums) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Use monotonic decreasing stack."),
                List.of(new TestCaseSpec("[4, 5, 2, 25]", "[5, 25, 25, -1]", false, "Standard array"), new TestCaseSpec("[13, 7, 6, 12]", "[-1, 12, 12, -1]", true, "Decreasing array")));

        upsertProblem("evaluate-postfix-notation", "Evaluate Postfix Notation", "MEDIUM", "Stack",
                "Evaluate value of an arithmetic expression in Reverse Polish Notation (Postfix).",
                "1 <= tokens.length <= 10^4", "String array `tokens`.", "Evaluated integer result.",
                "[{\"input\": \"tokens = [\\\"2\\\",\\\"1\\\",\\\"+\\\",\\\"3\\\",\\\"*\\\"]\", \"output\": \"9\", \"explanation\": \"((2 + 1) * 3) = 9.\"}]",
                "public class Solution {\n    public int evalRPN(String[] tokens) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Push numbers onto stack.", "Pop top two operands when operator is encountered."),
                List.of(new TestCaseSpec("2, 1, +, 3, *", "9", false, "Simple RPN"), new TestCaseSpec("4, 13, 5, /, +", "6", true, "Division RPN")));

        upsertProblem("largest-rectangle-histogram", "Largest Rectangle in Histogram", "HARD", "Stack",
                "Given an array of integers `heights` representing histogram bars, find area of largest rectangle in histogram.",
                "1 <= heights.length <= 10^5", "Integer array `heights`.", "Maximum area integer.",
                "[{\"input\": \"heights = [2,1,5,6,2,3]\", \"output\": \"10\", \"explanation\": \"Subarray [5,6] height 5 gives area 10.\"}]",
                "public class Solution {\n    public int largestRectangleArea(int[] heights) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use monotonic increasing stack storing bar indices."),
                List.of(new TestCaseSpec("[2,1,5,6,2,3]", "10", false, "Standard histogram"), new TestCaseSpec("[2,4]", "4", true, "Increasing histogram")));
    }

    // 14. QUEUE / DEQUE (4 problems)
    private void seedQueueDeque() {
        upsertProblem("queue-using-stacks", "Queue Using Stacks", "EASY", "Queue / Deque",
                "Implement FIFO queue using two stacks.",
                "1 <= operations <= 100", "Integer array `operations`.", "First popped element integer.",
                "[{\"input\": \"operations = [1, 2, 3]\", \"output\": \"1\", \"explanation\": \"1 was pushed first.\"}]",
                "public class Solution {\n    public int firstOut(int[] operations) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use inStack for push and outStack for pop."),
                List.of(new TestCaseSpec("[1, 2, 3]", "1", false, "FIFO check"), new TestCaseSpec("[10, 20]", "10", true, "Two elements")));

        upsertProblem("circular-queue-buffer", "Design Circular Queue", "MEDIUM", "Queue / Deque",
                "Design a circular queue data structure with fixed capacity `k`.",
                "1 <= k <= 1000", "Integer `k` and array `elements`.", "Boolean true if full.",
                "[{\"input\": \"k = 3, elements = [1, 2, 3]\", \"output\": \"true\", \"explanation\": \"Queue is full with 3 elements.\"}]",
                "public class Solution {\n    public boolean isFull(int k, int[] elements) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Maintain head and tail pointers with modular arithmetic."),
                List.of(new TestCaseSpec("3, [1, 2, 3]", "true", false, "Full queue"), new TestCaseSpec("5, [1, 2]", "false", true, "Not full queue")));

        upsertProblem("generate-binary-numbers-queue", "Generate Binary Numbers", "MEDIUM", "Queue / Deque",
                "Generate binary numbers from 1 to `n` using a Queue.",
                "1 <= n <= 10^4", "Integer `n`.", "String array of binary numbers.",
                "[{\"input\": \"n = 5\", \"output\": \"[1, 10, 11, 100, 101]\", \"explanation\": \"Binary representation 1 to 5.\"}]",
                "public class Solution {\n    public String[] generateBinary(int n) {\n        // Write your solution here\n        return new String[]{};\n    }\n}",
                List.of("Enqueue '1'. In each step dequeue s, append s+'0' and s+'1'."),
                List.of(new TestCaseSpec("5", "[1, 10, 11, 100, 101]", false, "Generate 1 to 5"), new TestCaseSpec("2", "[1, 10]", true, "Generate 1 to 2")));

        upsertProblem("max-sliding-window", "Sliding Window Maximum", "HARD", "Queue / Deque",
                "Find maximum element in each sliding window of size `k` moving left to right.",
                "1 <= nums.length <= 10^5", "Integer array `nums` and integer `k`.", "Array of maximums.",
                "[{\"input\": \"nums = [1,3,-1,-3,5,3,6,7], k = 3\", \"output\": \"[3, 3, 5, 5, 6, 7]\", \"explanation\": \"Sliding window maximums.\"}]",
                "public class Solution {\n    public int[] maxSlidingWindow(int[] nums, int k) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Use monotonic decreasing Deque storing array indices."),
                List.of(new TestCaseSpec("[1,3,-1,-3,5,3,6,7], 3", "[3, 3, 5, 5, 6, 7]", false, "Standard window max"), new TestCaseSpec("[1]", "1", true, "Single element")));
    }

    // 15. BINARY TREES (7 problems)
    private void seedBinaryTrees() {
        upsertProblem("tree-inorder-traversal", "Binary Tree Inorder Traversal", "EASY", "Binary Trees",
                "Return inorder traversal values of binary tree given serialized level order array.",
                "1 <= nodes <= 100", "Integer array `tree`.", "Inorder values array.",
                "[{\"input\": \"tree = [1, 2, 3]\", \"output\": \"[2, 1, 3]\", \"explanation\": \"Inorder is Left -> Root -> Right.\"}]",
                "public class Solution {\n    public int[] inorderTraversal(int[] tree) {\n        // Write your solution here\n        return tree;\n    }\n}",
                List.of("Left subtree -> Root -> Right subtree."),
                List.of(new TestCaseSpec("[1, 2, 3]", "[2, 1, 3]", false, "Complete tree"), new TestCaseSpec("[1]", "[1]", true, "Single node")));

        upsertProblem("maximum-depth-tree", "Maximum Depth of Binary Tree", "EASY", "Binary Trees",
                "Find maximum depth (height) of a binary tree given root level array.",
                "0 <= nodes <= 10^4", "Integer array `tree`.", "Maximum depth integer.",
                "[{\"input\": \"tree = [3,9,20,0,0,15,7]\", \"output\": \"3\", \"explanation\": \"Tree height is 3.\"}]",
                "public class Solution {\n    public int maxDepth(int[] tree) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Depth = 1 + max(depth(left), depth(right))."),
                List.of(new TestCaseSpec("[3,9,20,0,0,15,7]", "3", false, "Depth 3 tree"), new TestCaseSpec("[1,0,2]", "2", true, "Skewed tree")));

        upsertProblem("invert-binary-tree", "Invert Binary Tree", "EASY", "Binary Trees",
                "Invert a binary tree (mirror) and return inverted level order array.",
                "0 <= nodes <= 100", "Integer array `tree`.", "Inverted array.",
                "[{\"input\": \"tree = [4,2,7,1,3,6,9]\", \"output\": \"[4, 7, 2, 9, 6, 3, 1]\", \"explanation\": \"Left and right children swapped.\"}]",
                "public class Solution {\n    public int[] invertTree(int[] tree) {\n        // Write your solution here\n        return tree;\n    }\n}",
                List.of("Swap left and right subtrees recursively."),
                List.of(new TestCaseSpec("[4,2,7,1,3,6,9]", "[4, 7, 2, 9, 6, 3, 1]", false, "Standard tree"), new TestCaseSpec("[2,1,3]", "[2, 3, 1]", true, "Small tree")));

        upsertProblem("level-order-traversal-tree", "Tree Level Order Traversal", "MEDIUM", "Binary Trees",
                "Return level order traversal count of levels in binary tree.",
                "0 <= nodes <= 2000", "Integer array `tree`.", "Level count integer.",
                "[{\"input\": \"tree = [3,9,20,15,7]\", \"output\": \"3\", \"explanation\": \"3 levels present.\"}]",
                "public class Solution {\n    public int levelCount(int[] tree) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use BFS Queue level by level."),
                List.of(new TestCaseSpec("[3,9,20,15,7]", "3", false, "3 levels"), new TestCaseSpec("[1]", "1", true, "1 level")));

        upsertProblem("lowest-common-ancestor-tree", "Lowest Common Ancestor", "MEDIUM", "Binary Trees",
                "Find lowest common ancestor node value of two nodes `p` and `q` in a binary tree.",
                "2 <= nodes <= 10^4", "Integer array `tree`, integer `p`, integer `q`.", "LCA node value.",
                "[{\"input\": \"tree = [3,5,1,6,2,0,8], p = 5, q = 1\", \"output\": \"3\", \"explanation\": \"LCA of 5 and 1 is 3.\"}]",
                "public class Solution {\n    public int lowestCommonAncestor(int[] tree, int p, int q) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("If root is null or p or q, return root."),
                List.of(new TestCaseSpec("[3,5,1,6,2,0,8], 5, 1", "3", false, "LCA is 3"), new TestCaseSpec("[3,5,1,6,2,0,8], 5, 4", "5", true, "LCA is 5")));

        upsertProblem("construct-tree-preorder-inorder", "Construct Tree Preorder Inorder", "MEDIUM", "Binary Trees",
                "Construct binary tree from preorder and inorder traversals and return root value.",
                "1 <= nodes <= 3000", "Preorder array `preorder` and inorder array `inorder`.", "Root node value.",
                "[{\"input\": \"preorder = [3,9,20,15,7], inorder = [9,3,15,20,7]\", \"output\": \"3\", \"explanation\": \"Root is 3.\"}]",
                "public class Solution {\n    public int buildTreeRoot(int[] preorder, int[] inorder) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("First element in preorder is root."),
                List.of(new TestCaseSpec("preorder = [3,9,20,15,7], inorder = [9,3,15,20,7]", "3", false, "Root 3"), new TestCaseSpec("preorder = [-1], inorder = [-1]", "-1", true, "Single node")));

        upsertProblem("binary-tree-maximum-path-sum", "Binary Tree Max Path Sum", "HARD", "Binary Trees",
                "Find maximum path sum in a binary tree where path can start and end at any node.",
                "1 <= nodes <= 3 * 10^4", "Integer array `tree`.", "Maximum path sum integer.",
                "[{\"input\": \"tree = [-10,9,20,0,0,15,7]\", \"output\": \"42\", \"explanation\": \"Path 15 -> 20 -> 7 has sum 42.\"}]",
                "public class Solution {\n    public int maxPathSum(int[] tree) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("For each node compute max gain from left and right children.", "Update global max with node.val + leftGain + rightGain."),
                List.of(new TestCaseSpec("[-10,9,20,0,0,15,7]", "42", false, "Max path sum 42"), new TestCaseSpec("[1,2,3]", "6", true, "Max path sum 6")));
    }

    // 16. BINARY SEARCH TREES (4 problems)
    private void seedBinarySearchTrees() {
        upsertProblem("search-in-bst", "Search in BST", "EASY", "Binary Search Trees",
                "Search for value `val` in a Binary Search Tree.",
                "1 <= nodes <= 5000", "BST array `tree` and integer `val`.", "Boolean true if found.",
                "[{\"input\": \"tree = [4,2,7,1,3], val = 2\", \"output\": \"true\", \"explanation\": \"2 exists in BST.\"}]",
                "public class Solution {\n    public boolean searchBST(int[] tree, int val) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("If val < node.val search left, if val > node.val search right."),
                List.of(new TestCaseSpec("[4,2,7,1,3], 2", "true", false, "Value found"), new TestCaseSpec("[4,2,7,1,3], 5", "false", true, "Value missing")));

        upsertProblem("lowest-common-ancestor-bst", "Lowest Common Ancestor in BST", "EASY", "Binary Search Trees",
                "Find LCA of two nodes `p` and `q` in a Binary Search Tree.",
                "2 <= nodes <= 10^5", "BST array `tree`, integer `p`, integer `q`.", "LCA node value.",
                "[{\"input\": \"tree = [6,2,8,0,4,7,9], p = 2, q = 8\", \"output\": \"6\", \"explanation\": \"LCA of 2 and 8 is 6.\"}]",
                "public class Solution {\n    public int lowestCommonAncestor(int[] tree, int p, int q) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("If both p and q < root, go left. If both > root, go right. Else root is LCA."),
                List.of(new TestCaseSpec("[6,2,8,0,4,7,9], 2, 8", "6", false, "LCA is 6"), new TestCaseSpec("[6,2,8,0,4,7,9], 2, 4", "2", true, "LCA is 2")));

        upsertProblem("validate-binary-search-tree", "Validate Binary Search Tree", "MEDIUM", "Binary Search Trees",
                "Determine if a binary tree is a valid Binary Search Tree.",
                "1 <= nodes <= 10^4", "Integer array `tree`.", "Boolean true or false.",
                "[{\"input\": \"tree = [2,1,3]\", \"output\": \"true\", \"explanation\": \"Valid BST.\"}]",
                "public class Solution {\n    public boolean isValidBST(int[] tree) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Pass min and max allowable bounds for each node recursively."),
                List.of(new TestCaseSpec("[2,1,3]", "true", false, "Valid BST"), new TestCaseSpec("[5,1,4,0,0,3,6]", "false", true, "Invalid BST")));

        upsertProblem("kth-smallest-element-bst", "Kth Smallest Element in BST", "MEDIUM", "Binary Search Trees",
                "Find `k`-th smallest element in a Binary Search Tree.",
                "1 <= k <= nodes <= 10^4", "BST array `tree` and integer `k`.", "K-th smallest integer.",
                "[{\"input\": \"tree = [3,1,4,0,2], k = 1\", \"output\": \"1\", \"explanation\": \"1 is smallest element.\"}]",
                "public class Solution {\n    public int kthSmallest(int[] tree, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Inorder traversal of BST visits nodes in strictly sorted order."),
                List.of(new TestCaseSpec("[3,1,4,0,2], 1", "1", false, "1st smallest"), new TestCaseSpec("[5,3,6,2,4,0,0,1], 3", "3", true, "3rd smallest")));
    }

    // 17. HEAPS / PRIORITY QUEUE (4 problems)
    private void seedHeaps() {
        upsertProblem("kth-largest-element-stream", "Kth Largest Element in Stream", "EASY", "Heaps / Priority Queue",
                "Maintain k-th largest element in a stream of numbers.",
                "1 <= k <= 10^4", "Integer `k` and array `nums`.", "K-th largest integer.",
                "[{\"input\": \"k = 3, nums = [4, 5, 8, 2]\", \"output\": \"4\", \"explanation\": \"Sorted: [8, 5, 4, 2], 3rd largest is 4.\"}]",
                "public class Solution {\n    public int kthLargest(int k, int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Min-Heap of fixed size k."),
                List.of(new TestCaseSpec("3, [4, 5, 8, 2]", "4", false, "3rd largest is 4"), new TestCaseSpec("1, [2, 10, 5]", "10", true, "1st largest is 10")));

        upsertProblem("kth-largest-element-array", "Kth Largest Element in Array", "MEDIUM", "Heaps / Priority Queue",
                "Find `k`-th largest element in an unsorted array `nums`.",
                "1 <= k <= nums.length <= 10^5", "Array `nums` and integer `k`.", "K-th largest integer.",
                "[{\"input\": \"nums = [3,2,1,5,6,4], k = 2\", \"output\": \"5\", \"explanation\": \"2nd largest is 5.\"}]",
                "public class Solution {\n    public int findKthLargest(int[] nums, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Min-Heap or QuickSelect algorithm."),
                List.of(new TestCaseSpec("[3,2,1,5,6,4], 2", "5", false, "2nd largest is 5"), new TestCaseSpec("[3,2,3,1,2,4,5,5,6], 4", "4", true, "4th largest is 4")));

        upsertProblem("top-k-frequent-elements", "Top K Frequent Elements", "MEDIUM", "Heaps / Priority Queue",
                "Given integer array `nums` and integer `k`, return count of elements in top `k` most frequent items.",
                "1 <= nums.length <= 10^5", "Array `nums` and integer `k`.", "Integer count of top k items.",
                "[{\"input\": \"nums = [1,1,1,2,2,3], k = 2\", \"output\": \"2\", \"explanation\": \"Top 2 frequent items are 1 and 2.\"}]",
                "public class Solution {\n    public int topKFrequentCount(int[] nums, int k) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Count frequencies in HashMap, then use Min-Heap of size k."),
                List.of(new TestCaseSpec("[1,1,1,2,2,3], 2", "2", false, "Top 2 items"), new TestCaseSpec("[1], 1", "1", true, "Single item")));

        upsertProblem("sliding-window-median", "Sliding Window Median", "HARD", "Heaps / Priority Queue",
                "Find median of each sliding window of size `k` in array `nums`.",
                "1 <= k <= nums.length <= 10^5", "Integer array `nums` and integer `k`.", "Double array of medians.",
                "[{\"input\": \"nums = [1,3,-1,-3,5,3,6,7], k = 3\", \"output\": \"[1.0, -1.0, -1.0, 3.0, 5.0, 6.0]\", \"explanation\": \"Medians of windows of size 3.\"}]",
                "public class Solution {\n    public double[] medianSlidingWindow(int[] nums, int k) {\n        // Write your solution here\n        return new double[]{};\n    }\n}",
                List.of("Use two balanced PriorityQueues: Max-Heap for lower half, Min-Heap for upper half."),
                List.of(new TestCaseSpec("[1,3,-1,-3,5,3,6,7], 3", "[1.0, -1.0, -1.0, 3.0, 5.0, 6.0]", false, "Window size 3"), new TestCaseSpec("[1,2,3,4,2,3,1,4,2], 3", "[2.0, 3.0, 3.0, 3.0, 2.0, 3.0, 2.0]", true, "Window size 3 medians")));
    }

    // 18. GRAPH TRAVERSAL (4 problems)
    private void seedGraphTraversal() {
        upsertProblem("graph-bfs-traversal", "Graph BFS Traversal", "EASY", "Graph Traversal",
                "Return count of visited nodes starting from node 0 in connected graph.",
                "1 <= nodes <= 100", "2D adjacency array `graph`.", "Visited nodes count.",
                "[{\"input\": \"graph = [[1,2],[0],[0]]\", \"output\": \"3\", \"explanation\": \"All 3 nodes visited.\"}]",
                "public class Solution {\n    public int bfsVisitedCount(int[][] graph) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Queue for BFS and visited boolean array."),
                List.of(new TestCaseSpec("[[1,2],[0],[0]]", "3", false, "3 nodes connected"), new TestCaseSpec("[[]]", "1", true, "1 node")));

        upsertProblem("number-of-islands", "Number of Islands", "MEDIUM", "Graph Traversal",
                "Given an `m x n` 2D binary grid `grid` representing a map of '1's (land) and '0's (water), return number of islands.",
                "1 <= m, n <= 300", "2D char grid `grid`.", "Number of islands integer.",
                "[{\"input\": \"grid = [[\\\"1\\\",\\\"1\\\",\\\"0\\\",\\\"0\\\"],[\\\"1\\\",\\\"1\\\",\\\"0\\\",\\\"0\\\"],[\\\"0\\\",\\\"0\\\",\\\"1\\\",\\\"0\\\"]]\", \"output\": \"2\", \"explanation\": \"2 distinct islands.\"}]",
                "public class Solution {\n    public int numIslands(char[][] grid) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Iterate cells and trigger BFS/DFS when seeing '1'."),
                List.of(new TestCaseSpec("grid = [[\"1\",\"1\",\"0\",\"0\"],[\"1\",\"1\",\"0\",\"0\"],[\"0\",\"0\",\"1\",\"0\"]]", "2", false, "2 islands"), new TestCaseSpec("grid = [[\"1\"]]", "1", true, "1 island")));

        upsertProblem("clone-graph", "Clone Graph", "MEDIUM", "Graph Traversal",
                "Return total node count of deep copied connected undirected graph.",
                "0 <= nodes <= 100", "2D adjacency array `adjList`.", "Total node count integer.",
                "[{\"input\": \"adjList = [[2,4],[1,3],[2,4],[1,3]]\", \"output\": \"4\", \"explanation\": \"Graph has 4 nodes.\"}]",
                "public class Solution {\n    public int cloneGraphCount(int[][] adjList) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use HashMap mapping original node to cloned node during BFS/DFS."),
                List.of(new TestCaseSpec("[[2,4],[1,3],[2,4],[1,3]]", "4", false, "4 nodes graph"), new TestCaseSpec("[[]]", "1", true, "Single node graph")));

        upsertProblem("alien-dictionary-order", "Alien Dictionary Order", "HARD", "Graph Traversal",
                "Given a sorted dictionary of alien language words, return length of valid character ordering.",
                "1 <= words.length <= 100", "String array `words`.", "Total unique character ordering length.",
                "[{\"input\": \"words = [\\\"wrt\\\",\\\"wrf\\\",\\\"er\\\",\\\"ett\\\",\\\"rftt\\\"]\", \"output\": \"5\", \"explanation\": \"Order wertf has length 5.\"}]",
                "public class Solution {\n    public int alienOrderLength(String[] words) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Build directed graph of character precedence.", "Perform Topological Sort via Kahn's BFS or DFS."),
                List.of(new TestCaseSpec("wrt, wrf, er, ett, rftt", "5", false, "5 unique chars"), new TestCaseSpec("z, x", "2", true, "2 chars")));
    }

    // 19. SHORTEST PATH (3 problems)
    private void seedShortestPath() {
        upsertProblem("shortest-path-unweighted", "Shortest Path Unweighted", "MEDIUM", "Shortest Path",
                "Find shortest distance from node 0 to target node in unweighted graph.",
                "1 <= nodes <= 1000", "2D adjacency array `graph` and integer `target`.", "Shortest distance integer.",
                "[{\"input\": \"graph = [[1,2],[0,3],[0,3],[1,2]], target = 3\", \"output\": \"2\", \"explanation\": \"Path 0 -> 1 -> 3 has length 2.\"}]",
                "public class Solution {\n    public int shortestDistance(int[][] graph, int target) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Use BFS for unweighted shortest path."),
                List.of(new TestCaseSpec("[[1,2],[0,3],[0,3],[1,2]], 3", "2", false, "Distance 2"), new TestCaseSpec("[[1],[0]], 1", "1", true, "Distance 1")));

        upsertProblem("cheapest-flights-within-k-stops", "Cheapest Flights Within K Stops", "MEDIUM", "Shortest Path",
                "Find cheapest price from `src` to `dst` with at most `k` stops.",
                "1 <= n <= 100", "2D array `flights`, integer `src`, integer `dst`, integer `k`.", "Cheapest price integer or -1.",
                "[{\"input\": \"flights = [[0,1,100],[1,2,100],[0,2,500]], src = 0, dst = 2, k = 1\", \"output\": \"200\", \"explanation\": \"0 -> 1 -> 2 costs 200 with 1 stop.\"}]",
                "public class Solution {\n    public int findCheapestPrice(int[][] flights, int src, int dst, int k) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Use Bellman-Ford algorithm with k+1 iterations."),
                List.of(new TestCaseSpec("flights = [[0,1,100],[1,2,100],[0,2,500]], 0, 2, 1", "200", false, "1 stop cost 200"), new TestCaseSpec("flights = [[0,1,100],[1,2,100],[0,2,500]], 0, 2, 0", "500", true, "0 stops cost 500")));

        upsertProblem("network-delay-dijkstra", "Network Delay Time (Dijkstra)", "HARD", "Shortest Path",
                "Find minimum time for all nodes to receive signal sent from node `k` in weighted directed network.",
                "1 <= n <= 100", "2D array `times`, integer `n`, integer `k`.", "Minimum time integer or -1.",
                "[{\"input\": \"times = [[2,1,1],[2,3,1],[3,4,1]], n = 4, k = 2\", \"output\": \"2\", \"explanation\": \"All nodes receive signal at t = 2.\"}]",
                "public class Solution {\n    public int networkDelayTime(int[][] times, int n, int k) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Use Dijkstra's algorithm with Min-Heap storing (distance, node)."),
                List.of(new TestCaseSpec("times = [[2,1,1],[2,3,1],[3,4,1]], 4, 2", "2", false, "Delay time 2"), new TestCaseSpec("times = [[1,2,1]], 2, 2", "-1", true, "Unreachable node")));
    }

    // 20. GRAPH ALGORITHMS (3 problems)
    private void seedGraphAlgorithms() {
        upsertProblem("course-schedule-cycle", "Course Schedule (Cycle Check)", "MEDIUM", "Graph Algorithms",
                "Determine if you can finish all `numCourses` given prerequisite pairs.",
                "1 <= numCourses <= 2000", "Integer `numCourses` and 2D prerequisites array `prerequisites`.", "Boolean true or false.",
                "[{\"input\": \"numCourses = 2, prerequisites = [[1,0]]\", \"output\": \"true\", \"explanation\": \"Course 0 then 1.\"}]",
                "public class Solution {\n    public boolean canFinish(int numCourses, int[][] prerequisites) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Check for cycle in directed graph using Kahn's algorithm or DFS colors."),
                List.of(new TestCaseSpec("2, [[1,0]]", "true", false, "No cycle"), new TestCaseSpec("2, [[1,0],[0,1]]", "false", true, "Cycle present")));

        upsertProblem("course-schedule-ii-order", "Course Schedule II (Topological Sort)", "MEDIUM", "Graph Algorithms",
                "Return length of valid topological ordering of courses.",
                "1 <= numCourses <= 2000", "Integer `numCourses` and 2D prerequisites array `prerequisites`.", "Ordering length integer.",
                "[{\"input\": \"numCourses = 4, prerequisites = [[1,0],[2,0],[3,1],[3,2]]\", \"output\": \"4\", \"explanation\": \"Topological sort has 4 courses.\"}]",
                "public class Solution {\n    public int findOrderLength(int numCourses, int[][] prerequisites) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Return number of courses in topological sort."),
                List.of(new TestCaseSpec("4, [[1,0],[2,0],[3,1],[3,2]]", "4", false, "4 courses"), new TestCaseSpec("2, [[1,0],[0,1]]", "0", true, "Cycle returns 0")));

        upsertProblem("min-cost-connect-points-kruskal", "Min Cost to Connect All Points (MST)", "HARD", "Graph Algorithms",
                "Find minimum cost to connect all 2D points using Kruskal's or Prim's MST algorithm.",
                "1 <= points.length <= 1000", "2D integer array `points`.", "Minimum cost integer.",
                "[{\"input\": \"points = [[0,0],[2,2],[3,10],[5,2],[7,0]]\", \"output\": \"20\", \"explanation\": \"MST cost is 20.\"}]",
                "public class Solution {\n    public int minCostConnectPoints(int[][] points) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Prim's algorithm with Min-Heap or Kruskal's algorithm with Union-Find."),
                List.of(new TestCaseSpec("[[0,0],[2,2],[3,10],[5,2],[7,0]]", "20", false, "MST cost 20"), new TestCaseSpec("[[3,12],[-2,5],[7,7]]", "18", true, "MST cost 18")));
    }

    // 21. GREEDY (4 problems)
    private void seedGreedy() {
        upsertProblem("assign-cookies-greedy", "Assign Cookies", "EASY", "Greedy",
                "Maximize number of content children given greed factor array `g` and cookie size array `s`.",
                "1 <= g.length, s.length <= 3 * 10^4", "Two integer arrays `g` and `s`.", "Content children count integer.",
                "[{\"input\": \"g = [1,2,3], s = [1,1]\", \"output\": \"1\", \"explanation\": \"Only 1 child satisfied.\"}]",
                "public class Solution {\n    public int findContentChildren(int[] g, int[] s) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Sort both arrays and match smallest suitable cookie to child."),
                List.of(new TestCaseSpec("g = [1,2,3], s = [1,1]", "1", false, "1 child"), new TestCaseSpec("g = [1,2], s = [1,2,3]", "2", true, "2 children")));

        upsertProblem("jump-game-reach", "Jump Game", "MEDIUM", "Greedy",
                "Determine if you can reach the last index starting at index 0 given max jump lengths in array `nums`.",
                "1 <= nums.length <= 10^4", "Integer array `nums`.", "Boolean true or false.",
                "[{\"input\": \"nums = [2,3,1,1,4]\", \"output\": \"true\", \"explanation\": \"Jump 1 step to 1, then 3 steps to end.\"}]",
                "public class Solution {\n    public boolean canJump(int[] nums) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Track maxReachable index while iterating."),
                List.of(new TestCaseSpec("[2,3,1,1,4]", "true", false, "Reachable"), new TestCaseSpec("[3,2,1,0,4]", "false", true, "Unreachable")));

        upsertProblem("gas-station-circuit", "Gas Station Circuit", "MEDIUM", "Greedy",
                "Find starting gas station index to travel complete circuit once, or -1 if impossible.",
                "1 <= gas.length <= 10^5", "Two integer arrays `gas` and `cost`.", "Starting index integer or -1.",
                "[{\"input\": \"gas = [1,2,3,4,5], cost = [3,4,5,1,2]\", \"output\": \"3\", \"explanation\": \"Start at index 3.\"}]",
                "public class Solution {\n    public int canCompleteCircuit(int[] gas, int[] cost) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("If total gas < total cost, return -1. Otherwise reset start index whenever current gas balance < 0."),
                List.of(new TestCaseSpec("gas = [1,2,3,4,5], cost = [3,4,5,1,2]", "3", false, "Start at 3"), new TestCaseSpec("gas = [2,3,4], cost = [3,4,3]", "-1", true, "Impossible")));

        upsertProblem("candy-distribution-greedy", "Candy Distribution", "HARD", "Greedy",
                "Distribute minimum total candies to children based on ratings such that higher rated child gets more candies than neighbors.",
                "1 <= ratings.length <= 2 * 10^4", "Integer array `ratings`.", "Minimum total candies integer.",
                "[{\"input\": \"ratings = [1,0,2]\", \"output\": \"5\", \"explanation\": \"Candies: [2, 1, 2], sum = 5.\"}]",
                "public class Solution {\n    public int candy(int[] ratings) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Do left-to-right pass then right-to-left pass taking max."),
                List.of(new TestCaseSpec("[1,0,2]", "5", false, "5 candies"), new TestCaseSpec("[1,2,2]", "4", true, "4 candies")));
    }

    // 22. DYNAMIC PROGRAMMING (11 problems)
    private void seedDynamicProgramming() {
        upsertProblem("climbing-stairs-dp", "Climbing Stairs", "EASY", "Dynamic Programming",
                "Find number of distinct ways to climb `n` steps taking 1 or 2 steps at a time.",
                "1 <= n <= 45", "Integer `n`.", "Distinct ways integer.",
                "[{\"input\": \"n = 3\", \"output\": \"3\", \"explanation\": \"3 ways.\"}]",
                "public class Solution {\n    public int climbStairs(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("dp[i] = dp[i-1] + dp[i-2]."),
                List.of(new TestCaseSpec("3", "3", false, "3 steps"), new TestCaseSpec("4", "5", true, "4 steps")));

        upsertProblem("min-cost-climbing-stairs", "Min Cost Climbing Stairs", "EASY", "Dynamic Programming",
                "Find minimum cost to reach top of staircase given cost array `cost`.",
                "2 <= cost.length <= 1000", "Integer array `cost`.", "Minimum cost integer.",
                "[{\"input\": \"cost = [10,15,20]\", \"output\": \"15\", \"explanation\": \"Start at index 1 and pay 15.\"}]",
                "public class Solution {\n    public int minCostClimbingStairs(int[] cost) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("dp[i] = cost[i] + min(dp[i-1], dp[i-2])."),
                List.of(new TestCaseSpec("[10,15,20]", "15", false, "Cost 15"), new TestCaseSpec("[1,100,1,1,1,100,1,1,100,1]", "6", true, "Cost 6")));

        upsertProblem("house-robber-dp", "House Robber", "MEDIUM", "Dynamic Programming",
                "Find maximum money robbed without robbing adjacent houses.",
                "1 <= nums.length <= 100", "Integer array `nums`.", "Maximum money integer.",
                "[{\"input\": \"nums = [1,2,3,1]\", \"output\": \"4\", \"explanation\": \"Rob house 1 and 3.\"}]",
                "public class Solution {\n    public int rob(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("dp[i] = max(dp[i-1], nums[i] + dp[i-2])."),
                List.of(new TestCaseSpec("[1,2,3,1]", "4", false, "Max 4"), new TestCaseSpec("[2,7,9,3,1]", "12", true, "Max 12")));

        upsertProblem("coin-change-fewest", "Coin Change Fewest Coins", "MEDIUM", "Dynamic Programming",
                "Find fewest number of coins needed to make up `amount` using coin denominations `coins`.",
                "1 <= coins.length <= 12", "Array `coins` and integer `amount`.", "Fewest coins integer or -1.",
                "[{\"input\": \"coins = [1,2,5], amount = 11\", \"output\": \"3\", \"explanation\": \"5 + 5 + 1 = 11 (3 coins).\"}]",
                "public class Solution {\n    public int coinChange(int[] coins, int amount) {\n        // Write your solution here\n        return -1;\n    }\n}",
                List.of("Use 1D dp array initialized to infinity."),
                List.of(new TestCaseSpec("coins = [1,2,5], amount = 11", "3", false, "3 coins"), new TestCaseSpec("coins = [2], amount = 3", "-1", true, "Impossible amount")));

        upsertProblem("longest-increasing-subsequence", "Longest Increasing Subsequence", "MEDIUM", "Dynamic Programming",
                "Find length of longest strictly increasing subsequence in integer array `nums`.",
                "1 <= nums.length <= 2500", "Integer array `nums`.", "Longest length integer.",
                "[{\"input\": \"nums = [10,9,2,5,3,7,101,18]\", \"output\": \"4\", \"explanation\": \"Subsequence [2,3,7,101] has length 4.\"}]",
                "public class Solution {\n    public int lengthOfLIS(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use dynamic programming dp[i] = max(dp[j] + 1) for j < i where nums[j] < nums[i], or binary search patience sorting."),
                List.of(new TestCaseSpec("[10,9,2,5,3,7,101,18]", "4", false, "LIS length 4"), new TestCaseSpec("[0,1,0,3,2,3]", "4", true, "LIS length 4")));

        upsertProblem("partition-equal-subset-sum", "Partition Equal Subset Sum", "MEDIUM", "Dynamic Programming",
                "Determine if array `nums` can be partitioned into two subsets with equal sum.",
                "1 <= nums.length <= 200", "Integer array `nums`.", "Boolean true or false.",
                "[{\"input\": \"nums = [1,5,11,5]\", \"output\": \"true\", \"explanation\": \"[1,5,5] and [11] both sum to 11.\"}]",
                "public class Solution {\n    public boolean canPartition(int[] nums) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Target sum = totalSum / 2. 0/1 Knapsack DP."),
                List.of(new TestCaseSpec("[1,5,11,5]", "true", false, "Equal partition possible"), new TestCaseSpec("[1,2,3,5]", "false", true, "Partition impossible")));

        upsertProblem("unique-paths-grid", "Unique Paths in Grid", "MEDIUM", "Dynamic Programming",
                "Find number of unique paths from top-left to bottom-right of `m x n` grid moving only down or right.",
                "1 <= m, n <= 100", "Integers `m` and `n`.", "Unique paths count integer.",
                "[{\"input\": \"m = 3, n = 7\", \"output\": \"28\", \"explanation\": \"28 unique paths.\"}]",
                "public class Solution {\n    public int uniquePaths(int m, int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("dp[i][j] = dp[i-1][j] + dp[i][j-1]."),
                List.of(new TestCaseSpec("3, 7", "28", false, "28 paths"), new TestCaseSpec("3, 2", "3", true, "3 paths")));

        upsertProblem("longest-common-subsequence-dp", "Longest Common Subsequence", "MEDIUM", "Dynamic Programming",
                "Given two strings `text1` and `text2`, return length of their longest common subsequence.",
                "1 <= text1.length, text2.length <= 1000", "Two strings `text1` and `text2`.", "LCS length integer.",
                "[{\"input\": \"text1 = \\\"abcde\\\", text2 = \\\"ace\\\"\", \"output\": \"3\", \"explanation\": \"LCS is ace.\"}]",
                "public class Solution {\n    public int longestCommonSubsequence(String text1, String text2) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("If char match: dp[i][j] = 1 + dp[i-1][j-1], else max(dp[i-1][j], dp[i][j-1])."),
                List.of(new TestCaseSpec("abcde, ace", "3", false, "LCS 3"), new TestCaseSpec("abc, def", "0", true, "LCS 0")));

        upsertProblem("edit-distance-alignment", "Edit Distance (Levenshtein)", "HARD", "Dynamic Programming",
                "Find minimum operations (insert, delete, replace) to convert `word1` to `word2`.",
                "0 <= word1.length, word2.length <= 500", "Two strings `word1` and `word2`.", "Minimum edit distance integer.",
                "[{\"input\": \"word1 = \\\"horse\\\", word2 = \\\"ros\\\"\", \"output\": \"3\", \"explanation\": \"horse -> rorse -> rose -> ros (3 ops).\"}]",
                "public class Solution {\n    public int minDistance(String word1, String word2) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use 2D DP matrix dp[i][j] storing minimum operations."),
                List.of(new TestCaseSpec("horse, ros", "3", false, "3 operations"), new TestCaseSpec("intention, execution", "5", true, "5 operations")));

        upsertProblem("wildcard-pattern-matching", "Wildcard Pattern Matching", "HARD", "Dynamic Programming",
                "Implement wildcard pattern matching with support for '?' and '*'.",
                "0 <= s.length, p.length <= 2000", "Strings `s` and `p`.", "Boolean true or false.",
                "[{\"input\": \"s = \\\"aa\\\", p = \\\"*\\\"\", \"output\": \"true\", \"explanation\": \"'*' matches any sequence.\"}]",
                "public class Solution {\n    public boolean isMatch(String s, String p) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("dp[i][j] represents matching prefix s[0..i] with p[0..j]."),
                List.of(new TestCaseSpec("aa, *", "true", false, "Star wildcard"), new TestCaseSpec("cb, ?a", "false", true, "Mismatched wildcard")));

        upsertProblem("burst-balloons-interval-dp", "Burst Balloons (Interval DP)", "HARD", "Dynamic Programming",
                "Burst balloons to maximize coins earned. Return maximum coins.",
                "1 <= nums.length <= 300", "Integer array `nums`.", "Maximum coins integer.",
                "[{\"input\": \"nums = [3,1,5,8]\", \"output\": \"167\", \"explanation\": \"Coins = 3*1*5 + 3*5*8 + 1*3*8 + 1*8*1 = 167.\"}]",
                "public class Solution {\n    public int maxCoins(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use interval DP dp[i][j] representing max coins bursting balloons in interval (i, j)."),
                List.of(new TestCaseSpec("[3,1,5,8]", "167", false, "Max coins 167"), new TestCaseSpec("[1,5]", "10", true, "Max coins 10")));
    }

    // 23. BIT MANIPULATION (4 problems)
    private void seedBitManipulation() {
        upsertProblem("check-even-odd-bit", "Check Even Odd Bit", "EASY", "Bit Manipulation",
                "Return true if given integer `n` is even using bitwise operators.",
                "-10^9 <= n <= 10^9", "Integer `n`.", "Boolean true if even.",
                "[{\"input\": \"n = 4\", \"output\": \"true\", \"explanation\": \"4 & 1 == 0.\"}]",
                "public class Solution {\n    public boolean isEven(int n) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Check lowest bit: (n & 1) == 0."),
                List.of(new TestCaseSpec("4", "true", false, "Even number"), new TestCaseSpec("7", "false", true, "Odd number")));

        upsertProblem("number-of-1-bits", "Number of 1 Bits", "EASY", "Bit Manipulation",
                "Return Hamming weight (number of set 1 bits) of positive integer `n`.",
                "1 <= n <= 2^31 - 1", "Integer `n`.", "Count of 1 bits integer.",
                "[{\"input\": \"n = 11\", \"output\": \"3\", \"explanation\": \"11 in binary is 1011 (3 ones).\"}]",
                "public class Solution {\n    public int hammingWeight(int n) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Brian Kernighan's algorithm: n = n & (n - 1)."),
                List.of(new TestCaseSpec("11", "3", false, "3 set bits"), new TestCaseSpec("128", "1", true, "1 set bit")));

        upsertProblem("single-number-bitwise", "Single Number Bitwise", "MEDIUM", "Bit Manipulation",
                "Find the non-duplicate element in `nums` where every other element appears twice.",
                "1 <= nums.length <= 3 * 10^4", "Integer array `nums`.", "Single element integer.",
                "[{\"input\": \"nums = [4,1,2,1,2]\", \"output\": \"4\", \"explanation\": \"XOR of all numbers isolates 4.\"}]",
                "public class Solution {\n    public int singleNumber(int[] nums) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("XOR all numbers together."),
                List.of(new TestCaseSpec("[4,1,2,1,2]", "4", false, "Single 4"), new TestCaseSpec("[2,2,1]", "1", true, "Single 1")));

        upsertProblem("counting-bits-range", "Counting Bits Range", "MEDIUM", "Bit Manipulation",
                "Given integer `n`, return array of size n+1 where ans[i] is number of 1's in binary representation of i.",
                "0 <= n <= 10^5", "Integer `n`.", "Array of bit counts.",
                "[{\"input\": \"n = 5\", \"output\": \"[0, 1, 1, 2, 1, 2]\", \"explanation\": \"Bit counts 0 through 5.\"}]",
                "public class Solution {\n    public int[] countBits(int n) {\n        // Write your solution here\n        return new int[]{};\n    }\n}",
                List.of("Use bit DP: ans[i] = ans[i >> 1] + (i & 1)."),
                List.of(new TestCaseSpec("5", "[0, 1, 1, 2, 1, 2]", false, "Bits up to 5"), new TestCaseSpec("2", "[0, 1, 1]", true, "Bits up to 2")));
    }

    // 24. INTERVALS (4 problems)
    private void seedIntervals() {
        upsertProblem("summary-ranges-intervals", "Summary Ranges", "EASY", "Intervals",
                "Given a sorted unique integer array `nums`, return smallest list of summary ranges.",
                "0 <= nums.length <= 20", "Sorted array `nums`.", "Array of range strings.",
                "[{\"input\": \"nums = [0,1,2,4,5,7]\", \"output\": \"[\\\"0->2\\\", \\\"4->5\\\", \\\"7\\\"]\", \"explanation\": \"Merged contiguous ranges.\"}]",
                "public class Solution {\n    public String[] summaryRanges(int[] nums) {\n        // Write your solution here\n        return new String[]{};\n    }\n}",
                List.of("Iterate array tracking start and end of contiguous sequence."),
                List.of(new TestCaseSpec("[0,1,2,4,5,7]", "[\"0->2\", \"4->5\", \"7\"]", false, "3 ranges"), new TestCaseSpec("[0,2,3,4,6,8,9]", "[\"0\", \"2->4\", \"6\", \"8->9\"]", true, "4 ranges")));

        upsertProblem("merge-overlapping-intervals", "Merge Overlapping Intervals", "MEDIUM", "Intervals",
                "Given an array of `intervals`, merge all overlapping intervals.",
                "1 <= intervals.length <= 10^4", "2D array `intervals`.", "Merged 2D intervals array.",
                "[{\"input\": \"intervals = [[1,3],[2,6],[8,10],[15,18]]\", \"output\": \"[[1, 6], [8, 10], [15, 18]]\", \"explanation\": \"[1,3] and [2,6] merged to [1,6].\"}]",
                "public class Solution {\n    public int[][] merge(int[][] intervals) {\n        // Write your solution here\n        return new int[][]{};\n    }\n}",
                List.of("Sort intervals by start time first."),
                List.of(new TestCaseSpec("[[1,3],[2,6],[8,10],[15,18]]", "[[1, 6], [8, 10], [15, 18]]", false, "Overlapping merged"), new TestCaseSpec("[[1,4],[4,5]]", "[[1, 5]]", true, "Touching intervals merged")));

        upsertProblem("insert-interval-sorted", "Insert Interval", "MEDIUM", "Intervals",
                "Insert `newInterval` into non-overlapping sorted `intervals` and merge if necessary.",
                "0 <= intervals.length <= 10^4", "2D array `intervals` and 1D array `newInterval`.", "Updated 2D intervals array.",
                "[{\"input\": \"intervals = [[1,3],[6,9]], newInterval = [2,5]\", \"output\": \"[[1, 5], [6, 9]]\", \"explanation\": \"Inserted and merged [2,5] into [1,3] -> [1,5].\"}]",
                "public class Solution {\n    public int[][] insert(int[][] intervals, int[] newInterval) {\n        // Write your solution here\n        return new int[][]{};\n    }\n}",
                List.of("Add left non-overlapping, merge overlapping, add right non-overlapping."),
                List.of(new TestCaseSpec("intervals = [[1,3],[6,9]], newInterval = [2,5]", "[[1, 5], [6, 9]]", false, "Merged insertion"), new TestCaseSpec("intervals = [[1,2],[3,5],[6,7],[8,10],[12,16]], newInterval = [4,8]", "[[1, 2], [3, 10], [12, 16]]", true, "Multi-merge insertion")));

        upsertProblem("non-overlapping-intervals-erase", "Non-overlapping Intervals", "HARD", "Intervals",
                "Find minimum number of intervals to remove to make remaining intervals non-overlapping.",
                "1 <= intervals.length <= 10^5", "2D array `intervals`.", "Minimum removals integer.",
                "[{\"input\": \"intervals = [[1,2],[2,3],[3,4],[1,3]]\", \"output\": \"1\", \"explanation\": \"Remove [1,3] to make non-overlapping.\"}]",
                "public class Solution {\n    public int eraseOverlapIntervals(int[][] intervals) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Greedy strategy: sort by end time and pick earliest ending interval."),
                List.of(new TestCaseSpec("[[1,2],[2,3],[3,4],[1,3]]", "1", false, "1 removal"), new TestCaseSpec("[[1,2],[1,2],[1,2]]", "2", true, "2 removals")));
    }

    // 25. MATRIX / 2D ARRAYS (5 problems)
    private void seedMatrix() {
        upsertProblem("matrix-row-sum-max", "Matrix Max Row Sum", "EASY", "Matrix / 2D Arrays",
                "Find maximum sum among all rows in 2D grid `mat`.",
                "1 <= mat.length, mat[i].length <= 100", "2D array `mat`.", "Maximum row sum integer.",
                "[{\"input\": \"mat = [[1,2,3],[4,5,6],[7,8,9]]\", \"output\": \"24\", \"explanation\": \"Row 2 (7+8+9) has max sum 24.\"}]",
                "public class Solution {\n    public int maxRowSum(int[][] mat) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Sum each row and track global maximum."),
                List.of(new TestCaseSpec("[[1,2,3],[4,5,6],[7,8,9]]", "24", false, "Max row sum 24"), new TestCaseSpec("[[5, 10],[1, 2]]", "15", true, "Max row sum 15")));

        upsertProblem("set-matrix-zeroes", "Set Matrix Zeroes", "MEDIUM", "Matrix / 2D Arrays",
                "If an element is 0, set its entire row and column to 0 in-place.",
                "1 <= m, n <= 200", "2D integer array `matrix`.", "Modified 2D array.",
                "[{\"input\": \"matrix = [[1,1,1],[1,0,1],[1,1,1]]\", \"output\": \"[[1, 0, 1], [0, 0, 0], [1, 0, 1]]\", \"explanation\": \"Row 1 and Col 1 set to zero.\"}]",
                "public class Solution {\n    public int[][] setZeroes(int[][] matrix) {\n        // Write your solution here\n        return matrix;\n    }\n}",
                List.of("Use first row and first column as status indicators for O(1) extra space."),
                List.of(new TestCaseSpec("[[1,1,1],[1,0,1],[1,1,1]]", "[[1, 0, 1], [0, 0, 0], [1, 0, 1]]", false, "Set zeroes middle"), new TestCaseSpec("[[0,1,2,0],[3,4,5,2],[1,3,1,5]]", "[[0, 0, 0, 0], [0, 4, 5, 0], [0, 3, 1, 0]]", true, "Multiple zeroes")));

        upsertProblem("spiral-matrix-traversal", "Spiral Matrix Traversal", "MEDIUM", "Matrix / 2D Arrays",
                "Return total element count visited in spiral order of `m x n` matrix.",
                "1 <= m, n <= 100", "2D integer matrix `matrix`.", "Total elements count integer.",
                "[{\"input\": \"matrix = [[1,2,3],[4,5,6],[7,8,9]]\", \"output\": \"9\", \"explanation\": \"Visits all 9 elements.\"}]",
                "public class Solution {\n    public int spiralElementCount(int[][] matrix) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Maintain top, bottom, left, right boundaries."),
                List.of(new TestCaseSpec("[[1,2,3],[4,5,6],[7,8,9]]", "9", false, "9 elements"), new TestCaseSpec("[[1,2],[3,4]]", "4", true, "4 elements")));

        upsertProblem("rotate-image-matrix", "Rotate Image 90 Degrees", "MEDIUM", "Matrix / 2D Arrays",
                "Rotate an `n x n` 2D matrix 90 degrees clockwise in-place.",
                "1 <= n <= 20", "2D integer matrix `matrix`.", "Rotated 2D matrix.",
                "[{\"input\": \"matrix = [[1,2],[3,4]]\", \"output\": \"[[3, 1], [4, 2]]\", \"explanation\": \"Rotated 90 degrees clockwise.\"}]",
                "public class Solution {\n    public int[][] rotate(int[][] matrix) {\n        // Write your solution here\n        return matrix;\n    }\n}",
                List.of("Transpose matrix first, then reverse each row."),
                List.of(new TestCaseSpec("[[1,2],[3,4]]", "[[3, 1], [4, 2]]", false, "2x2 rotation"), new TestCaseSpec("[[1,2,3],[4,5,6],[7,8,9]]", "[[7, 4, 1], [8, 5, 2], [9, 6, 3]]", true, "3x3 rotation")));

        upsertProblem("maximal-rectangle-binary-matrix", "Maximal Rectangle", "HARD", "Matrix / 2D Arrays",
                "Given a 2D binary matrix filled with 0's and 1's, find the largest rectangle containing only 1's and return its area.",
                "1 <= m, n <= 200", "2D char grid `matrix`.", "Maximum area integer.",
                "[{\"input\": \"matrix = [[\\\"1\\\",\\\"0\\\",\\\"1\\\",\\\"0\\\",\\\"0\\\"],[\\\"1\\\",\\\"0\\\",\\\"1\\\",\\\"1\\\",\\\"1\\\"],[\\\"1\\\",\\\"1\\\",\\\"1\\\",\\\"1\\\",\\\"1\\\"],[\\\"1\\\",\\\"0\\\",\\\"0\\\",\\\"1\\\",\\\"0\\\"]]\", \"output\": \"6\", \"explanation\": \"Maximal rectangle area is 6.\"}]",
                "public class Solution {\n    public int maximalRectangle(char[][] matrix) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Transform each row into a histogram and apply Largest Rectangle in Histogram algorithm."),
                List.of(new TestCaseSpec("matrix = [[\"1\",\"0\",\"1\",\"0\",\"0\"],[\"1\",\"0\",\"1\",\"1\",\"1\"],[\"1\",\"1\",\"1\",\"1\",\"1\"],[\"1\",\"0\",\"0\",\"1\",\"0\"]]", "6", false, "Area 6"), new TestCaseSpec("matrix = [[\"0\"]]", "0", true, "Area 0")));
    }

    // 26. ADVANCED DATA STRUCTURES (4 problems)
    private void seedAdvancedDataStructures() {
        upsertProblem("trie-prefix-search", "Trie Prefix Search", "MEDIUM", "Advanced Data Structures",
                "Check if string `prefix` exists as a prefix in set of inserted `words`.",
                "1 <= words.length <= 1000", "String array `words` and string `prefix`.", "Boolean true if prefix exists.",
                "[{\"input\": \"words = [\\\"apple\\\", \\\"app\\\"], prefix = \\\"app\\\"\", \"output\": \"true\", \"explanation\": \"app is prefix.\"}]",
                "public class Solution {\n    public boolean startsWith(String[] words, String prefix) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Build Trie (Prefix Tree) with child nodes array of size 26."),
                List.of(new TestCaseSpec("words = [\"apple\", \"app\"], prefix = app", "true", false, "Prefix exists"), new TestCaseSpec("words = [\"apple\"], prefix = cat", "false", true, "Prefix absent")));

        upsertProblem("lru-cache-capacity", "LRU Cache Capacity Check", "MEDIUM", "Advanced Data Structures",
                "Implement Least Recently Used (LRU) Cache behavior and return key evicted first when capacity is 2.",
                "1 <= operations <= 1000", "Array of put keys `keys`.", "First evicted key integer.",
                "[{\"input\": \"keys = [1, 2, 3]\", \"output\": \"1\", \"explanation\": \"Key 1 evicted when key 3 added to cap 2 LRU cache.\"}]",
                "public class Solution {\n    public int getFirstEvictedKey(int[] keys) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Doubly Linked List + HashMap for O(1) get and put."),
                List.of(new TestCaseSpec("[1, 2, 3]", "1", false, "Key 1 evicted"), new TestCaseSpec("[10, 20, 30]", "10", true, "Key 10 evicted")));

        upsertProblem("word-ladder-shortest-path", "Word Ladder Shortest Transformation", "HARD", "Advanced Data Structures",
                "Find minimum transformation sequence length from `beginWord` to `endWord` changing 1 letter at a time.",
                "1 <= wordList.length <= 5000", "String `beginWord`, string `endWord`, string array `wordList`.", "Minimum length integer or 0.",
                "[{\"input\": \"beginWord = \\\"hit\\\", endWord = \\\"cog\\\", wordList = [\\\"hot\\\",\\\"dot\\\",\\\"dog\\\",\\\"lot\\\",\\\"log\\\",\\\"cog\\\"]\", \"output\": \"5\", \"explanation\": \"hit -> hot -> dot -> dog -> cog (5 words).\"}]",
                "public class Solution {\n    public int ladderLength(String beginWord, String endWord, String[] wordList) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Bidirectional BFS for optimal shortest path search in word graph."),
                List.of(new TestCaseSpec("beginWord = hit, endWord = cog, wordList = [hot, dot, dog, lot, log, cog]", "5", false, "Length 5"), new TestCaseSpec("beginWord = hit, endWord = cog, wordList = [hot, dot, dog, lot, log]", "0", true, "No transformation path")));

        upsertProblem("lfu-cache-eviction", "LFU Cache Eviction", "HARD", "Advanced Data Structures",
                "Find key evicted from Least Frequently Used (LFU) cache of capacity 2 when tie broken by LRU.",
                "1 <= keys.length <= 1000", "Array of inserted keys `keys`.", "Evicted key integer.",
                "[{\"input\": \"keys = [1, 2, 1, 3]\", \"output\": \"2\", \"explanation\": \"Key 2 has freq 1, key 1 has freq 2. Key 2 evicted.\"}]",
                "public class Solution {\n    public int getLfuEvictedKey(int[] keys) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use Map of frequency to DoublyLinkedList of keys with minFrequency tracking."),
                List.of(new TestCaseSpec("[1, 2, 1, 3]", "2", false, "Key 2 evicted"), new TestCaseSpec("[5, 6, 7]", "5", true, "Key 5 evicted")));
    }

    // 27. JAVA / OOP (3 problems)
    private void seedJavaOop() {
        upsertProblem("custom-stack-implementation", "Custom Generic Stack", "EASY", "Java / OOP",
                "Implement a custom Stack data structure supporting push, pop, and peek operations.",
                "1 <= operations <= 1000", "Integer array `items`.", "Top item remaining integer.",
                "[{\"input\": \"items = [10, 20, 30]\", \"output\": \"30\", \"explanation\": \"30 is on top of stack.\"}]",
                "public class Solution {\n    public int peekTop(int[] items) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use array backing store with topIndex pointer."),
                List.of(new TestCaseSpec("[10, 20, 30]", "30", false, "Top is 30"), new TestCaseSpec("[5]", "5", true, "Single element")));

        upsertProblem("thread-safe-singleton-check", "Thread Safe Singleton", "EASY", "Java / OOP",
                "Demonstrate Singleton Pattern behavior by returning true if instance reference remains identical.",
                "1 <= calls <= 100", "Integer `calls`.", "Boolean true.",
                "[{\"input\": \"calls = 5\", \"output\": \"true\", \"explanation\": \"Same instance returned for 5 calls.\"}]",
                "public class Solution {\n    public boolean isSingletonInstance(int calls) {\n        // Write your solution here\n        return false;\n    }\n}",
                List.of("Use double-checked locking or static inner Bill Pugh holder class."),
                List.of(new TestCaseSpec("5", "true", false, "5 calls"), new TestCaseSpec("1", "true", true, "1 call")));

        upsertProblem("custom-hash-map-chaining", "Custom HashMap Chaining", "MEDIUM", "Java / OOP",
                "Implement a HashMap using Separate Chaining with LinkedList buckets.",
                "1 <= operations <= 1000", "2D integer array `pairs`.", "Value retrieved integer.",
                "[{\"input\": \"pairs = [[1,10],[2,20],[1,15]]\", \"output\": \"15\", \"explanation\": \"Key 1 updated to 15.\"}]",
                "public class Solution {\n    public int getLatestValue(int[][] pairs) {\n        // Write your solution here\n        return 0;\n    }\n}",
                List.of("Use array of Node<K, V>LinkedList buckets with hash code modulo bucket count."),
                List.of(new TestCaseSpec("[[1,10],[2,20],[1,15]]", "15", false, "Updated key 1"), new TestCaseSpec("[[5,50]]", "50", true, "Single pair")));
    }

    private void upsertProblem(String slug, String title, String difficulty, String category,
                               String description, String constraints, String inputFormat, String outputFormat,
                               String examplesJson, String initialCode, List<String> hints, List<TestCaseSpec> testCases) {
        upsertProblem(slug, title, difficulty, category, description, constraints, inputFormat, outputFormat,
                examplesJson, initialCode, hints, testCases, null, null, false);
    }

    private void upsertProblem(String slug, String title, String difficulty, String category,
                               String description, String constraints, String inputFormat, String outputFormat,
                               String examplesJson, String initialCode, List<String> hints, List<TestCaseSpec> testCases,
                               String expectedTimeComplexity, String expectedSpaceComplexity, Boolean inPlaceRequired) {

        Optional<ProblemEntity> existingOpt = problemRepository.findBySlug(slug);
        ProblemEntity problem = existingOpt.orElseGet(() -> ProblemEntity.builder().slug(slug).build());

        problem.setTitle(title);
        problem.setDifficulty(difficulty);
        problem.setCategory(category);
        problem.setDescription(description);
        problem.setConstraints(constraints);
        problem.setInputFormat(inputFormat);
        problem.setOutputFormat(outputFormat);
        problem.setExamplesJson(examplesJson);
        problem.setInitialCode(initialCode);
        problem.setExpectedTimeComplexity(expectedTimeComplexity);
        problem.setExpectedSpaceComplexity(expectedSpaceComplexity);
        problem.setInPlaceRequired(inPlaceRequired);

        if (problem.getHints() == null) {
            problem.setHints(new ArrayList<>());
        } else {
            problem.getHints().clear();
        }

        for (int i = 0; i < hints.size(); i++) {
            problem.getHints().add(HintEntity.builder()
                    .problem(problem)
                    .hintOrder(i + 1)
                    .title("Hint " + (i + 1))
                    .content(hints.get(i))
                    .build());
        }

        if (problem.getTestCases() == null) {
            problem.setTestCases(new ArrayList<>());
        } else {
            problem.getTestCases().clear();
        }

        for (int i = 0; i < testCases.size(); i++) {
            TestCaseSpec spec = testCases.get(i);
            problem.getTestCases().add(TestCaseEntity.builder()
                    .problem(problem)
                    .input(spec.input)
                    .expectedOutput(spec.expectedOutput)
                    .isHidden(spec.isHidden)
                    .explanation(spec.explanation)
                    .orderIndex(i + 1)
                    .build());
        }

        problemRepository.save(problem);
    }

    private record TestCaseSpec(String input, String expectedOutput, boolean isHidden, String explanation) {}
}
