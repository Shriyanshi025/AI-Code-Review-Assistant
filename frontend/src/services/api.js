const BASE_URL = 'http://localhost:8080/api';

export async function fetchProblems(difficulty = '', category = '') {
  try {
    let url = `${BASE_URL}/problems`;
    const params = new URLSearchParams();
    if (difficulty) params.append('difficulty', difficulty);
    if (category) params.append('category', category);
    if (params.toString()) url += `?${params.toString()}`;

    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch problems');
    return await res.json();
  } catch (err) {
    console.warn('Backend API connection failed, using fallback client data', err);
    return getFallbackProblems();
  }
}

export async function fetchProblemById(id) {
  try {
    const res = await fetch(`${BASE_URL}/problems/${id}`);
    if (!res.ok) throw new Error('Problem not found');
    return await res.json();
  } catch (err) {
    console.warn('Backend problem fetch error, using fallback problem detail', err);
    const problems = getFallbackProblems();
    return problems.find(p => p.id === Number(id)) || problems[0];
  }
}

export async function analyzeFreeformCode(code, language = 'JAVA') {
  try {
    const res = await fetch(`${BASE_URL}/reviews/freeform`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ code, language })
    });
    if (!res.ok) throw new Error('Freeform analysis failed');
    return await res.json();
  } catch (err) {
    console.warn('API analyzeFreeform error, fallback to client parsing simulator', err);
    return simulateClientReview(code, null, language);
  }
}

export async function analyzeProblemCode(problemId, code, language = 'JAVA') {
  try {
    const res = await fetch(`${BASE_URL}/reviews/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ problemId, code, language })
    });
    if (!res.ok) throw new Error('Problem analysis failed');
    return await res.json();
  } catch (err) {
    console.warn('API analyzeProblem error, fallback to client parsing simulator', err);
    return simulateClientReview(code, problemId, language);
  }
}

export async function submitSolution(problemId, code, language = 'JAVA') {
  let res;
  try {
    res = await fetch(`${BASE_URL}/submissions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ problemId, code, language })
    });
  } catch (networkErr) {
    console.error('API submitSolution network error:', networkErr);
    throw new Error('Unable to connect to submission server. Please ensure the backend is running.');
  }

  if (!res.ok) {
    let errorMessage = `Submission failed (HTTP ${res.status})`;
    try {
      const errorData = await res.json();
      if (errorData?.message) {
        errorMessage = errorData.message;
      } else if (errorData?.error) {
        errorMessage = errorData.error;
      }
    } catch {
      if (res.statusText) {
        errorMessage = `Submission failed: ${res.statusText} (HTTP ${res.status})`;
      }
    }
    throw new Error(errorMessage);
  }

  return await res.json();
}

// Single-user architecture: defaults to primary seeded developer identity (demo_developer, id: 1)
export async function fetchUserProgress(userId = 1) {
  try {
    const res = await fetch(`${BASE_URL}/progress/user/${userId}`);
    if (!res.ok) throw new Error('Failed to fetch progress');
    return await res.json();
  } catch (err) {
    return {
      solvedCount: 0,
      attemptedCount: 0,
      totalSubmissions: 0,
      averageScore: null,
      topicMasteryJson: '{}'
    };
  }
}

export async function fetchBadges(userId = null) {
  try {
    const url = userId ? `${BASE_URL}/badges?userId=${userId}` : `${BASE_URL}/badges`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch badges');
    return await res.json();
  } catch (err) {
    console.warn('Backend API connection failed for badges', err);
    return getFallbackBadges();
  }
}

export async function fetchUnlockedBadges(userId = null) {
  try {
    const url = userId ? `${BASE_URL}/badges/unlocked?userId=${userId}` : `${BASE_URL}/badges/unlocked`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch unlocked badges');
    return await res.json();
  } catch (err) {
    return getFallbackBadges().filter(b => b.unlocked);
  }
}

export async function fetchBadgeProgress(userId = null) {
  try {
    const url = userId ? `${BASE_URL}/badges/progress?userId=${userId}` : `${BASE_URL}/badges/progress`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch badge progress');
    return await res.json();
  } catch (err) {
    return getFallbackBadges().filter(b => !b.unlocked);
  }
}

export async function fetchProblemSubmissions(problemId, userId = null) {
  try {
    const url = userId != null ? `${BASE_URL}/submissions/problem/${problemId}?userId=${userId}` : `${BASE_URL}/submissions/problem/${problemId}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch problem submissions');
    return await res.json();
  } catch (err) {
    console.warn('Backend submissions API error, fallback to empty list', err);
    return [];
  }
}

function getFallbackBadges() {
  return [
    { code: 'FIRST_STEP', name: 'First Step', description: 'Solve 1 challenge successfully.', icon: '🎯', requirementType: 'SOLVED_COUNT', requirementValue: 1, unlocked: false, unlockedAt: null, progress: 0, target: 1 },
    { code: 'PROBLEM_SOLVER', name: 'Problem Solver', description: 'Solve 5 challenges successfully.', icon: '🔥', requirementType: 'SOLVED_COUNT', requirementValue: 5, unlocked: false, unlockedAt: null, progress: 0, target: 5 },
    { code: 'RISING_CODER', name: 'Rising Coder', description: 'Solve 10 challenges successfully.', icon: '🚀', requirementType: 'SOLVED_COUNT', requirementValue: 10, unlocked: false, unlockedAt: null, progress: 0, target: 10 },
    { code: 'ARRAY_EXPLORER', name: 'Array Explorer', description: 'Successfully solve 5 Array problems.', icon: '🧩', requirementType: 'CATEGORY_SOLVED_COUNT', requirementValue: 5, category: 'Arrays', unlocked: false, unlockedAt: null, progress: 0, target: 5 },
    { code: 'TOPIC_EXPLORER', name: 'Topic Explorer', description: 'Successfully solve problems from at least 5 different categories.', icon: '🗺️', requirementType: 'TOPIC_COUNT', requirementValue: 5, unlocked: false, unlockedAt: null, progress: 0, target: 5 },
    { code: 'FIRST_ATTEMPT_ACE', name: 'First-Attempt Ace', description: 'Successfully solve 5 problems on the first submission attempt.', icon: '⚡', requirementType: 'FIRST_ATTEMPT_SOLVES', requirementValue: 5, unlocked: false, unlockedAt: null, progress: 0, target: 5 },
    { code: 'QUALITY_CODER', name: 'Quality Coder', description: 'Maintain an average review score of at least 90 across at least 5 accepted challenge submissions.', icon: '💎', requirementType: 'AVERAGE_REVIEW_SCORE', requirementValue: 5, unlocked: false, unlockedAt: null, progress: 0, target: 5 },
    { code: 'DEDICATED_SOLVER', name: 'Dedicated Solver', description: 'Solve 25 challenges successfully.', icon: '🌟', requirementType: 'SOLVED_COUNT', requirementValue: 25, unlocked: false, unlockedAt: null, progress: 0, target: 25 },
    { code: 'CHALLENGE_MASTER', name: 'Challenge Master', description: 'Solve 50 challenges successfully.', icon: '👑', requirementType: 'SOLVED_COUNT', requirementValue: 50, unlocked: false, unlockedAt: null, progress: 0, target: 50 }
  ];
}

// Fallback seed data in case backend API is starting up
function getFallbackProblems() {
  return [
    {
      id: 1,
      title: 'Maximum Subarray Sum',
      slug: 'maximum-subarray-sum',
      difficulty: 'EASY',
      category: 'Arrays',
      description: 'Given an integer array `nums`, find the subarray with the largest sum, and return its sum.',
      constraints: '1 <= nums.length <= 10^5\n-10^4 <= nums[i] <= 10^4',
      inputFormat: 'An array of integers `nums`.',
      outputFormat: 'An integer representing the maximum subarray sum.',
      examplesJson: JSON.stringify([{ input: 'nums = [-2,1,-3,4,-1,2,1,-5,4]', output: '6', explanation: 'Subarray [4,-1,2,1] has the largest sum 6.' }]),
      initialCode: `public class Solution {
    public int maxSubArray(int[] nums) {
        // Write your Java solution here
        int maxSoFar = 0; // Hint: What if all numbers are negative?
        int currentMax = 0;
        
        for (int i = 0; i < nums.length; i++) {
            currentMax += nums[i];
            if (currentMax > maxSoFar) {
                maxSoFar = currentMax;
            }
            if (currentMax < 0) {
                currentMax = 0;
            }
        }
        return maxSoFar;
    }
}`,
      hints: [
        { id: 1, hintOrder: 1, title: 'Handling Negative Values', content: 'Think about what happens when the input array contains only negative numbers (e.g. [-5, -2, -8]).' },
        { id: 2, hintOrder: 2, title: 'Initialization Strategy', content: 'Avoid initializing `maxSoFar = 0`. Instead, initialize it with `nums[0]` or `Integer.MIN_VALUE`.' }
      ]
    },
    {
      id: 2,
      title: 'Two Sum',
      slug: 'two-sum',
      difficulty: 'EASY',
      category: 'Arrays',
      description: 'Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.',
      constraints: '2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9',
      inputFormat: 'Array `nums` and integer `target`.',
      outputFormat: 'Array of 2 indices `[i, j]`.',
      examplesJson: JSON.stringify([{ input: 'nums = [2,7,11,15], target = 9', output: '[0, 1]' }]),
      initialCode: `public class Solution {
    public int[] twoSum(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[] { i, j };
                }
            }
        }
        return new int[] {};
    }
}`,
      hints: [
        { id: 1, hintOrder: 1, title: 'HashMap Optimization', content: 'Can you use a HashMap to store target remainders for O(N) linear time lookups?' }
      ]
    },
    {
      id: 3,
      title: 'Find Maximum Element',
      slug: 'find-maximum-element',
      difficulty: 'EASY',
      category: 'Arrays',
      description: 'Given an array of integers `arr`, find and return the maximum element in the array.',
      constraints: '1 <= arr.length <= 10^5\n-10^9 <= arr[i] <= 10^9',
      inputFormat: 'Enter the array as a bracketed list of comma-separated integers.',
      outputFormat: 'Integer representing the maximum value.',
      examplesJson: JSON.stringify([{ input: 'arr = [3, 7, 2, 9, 5]', output: '9', explanation: '9 is the largest element.' }]),
      testCases: [
        { id: 1, input: '[3, 7, 2, 9, 5]', expectedOutput: '9', isHidden: false, explanation: 'Standard array' },
        { id: 2, input: '[-5, -2, -8, -1]', expectedOutput: '-1', isHidden: true, explanation: 'All negative values' }
      ],
      initialCode: `public class Solution {\n    public int findLargest(int[] arr) {\n        // Write your solution here\n        return 0;\n    }\n}`,
      hints: [
        { id: 1, hintOrder: 1, title: 'Keep track of max', content: 'Keep track of maximum value seen so far.' },
        { id: 2, hintOrder: 2, title: 'Handle negatives', content: 'Initialize with arr[0] to handle negative numbers.' }
      ]
    }
  ];
}

function simulateClientReview(code, problemId, language = 'JAVA') {
  const isZeroInitBug = code.includes('maxSoFar = 0') || code.includes('max = 0');
  const isCpp = language === 'CPP' || language === 'C++';
  
  return {
    id: Date.now(),
    language: isCpp ? 'CPP' : 'JAVA',
    overallScore: isZeroInitBug ? 76 : 94,
    correctnessScore: isZeroInitBug ? 70 : 100,
    qualityScore: 88,
    complexityScore: 82,
    maintainabilityScore: 90,
    securityScore: 96,
    performanceScore: 80,
    testingScore: 75,
    timeComplexity: 'O(N)',
    spaceComplexity: 'O(1)',
    summary: isZeroInitBug ? 
      "Found 1 high-severity logical issue regarding zero-initialization for negative input values." :
      `Clean ${isCpp ? 'C++' : 'Java'} solution with solid O(N) linear time complexity.`,
    findings: isZeroInitBug ? [
      {
        id: 1,
        ruleId: isCpp ? 'CPP_ZERO_INIT_MAX_PITFALL' : 'JAVA_ZERO_INIT_MAX_PITFALL',
        severity: 'HIGH',
        category: 'CORRECTNESS',
        title: 'Potential Initialization Issue for Maximum Value',
        message: 'Your current initialization assumes that the input contains non-negative values.',
        explanation: 'For an input containing only negative values (e.g. [-5, -2, -8]), returning 0 produces an incorrect result.',
        recommendation: isCpp ? 'Initialize the tracked maximum using nums[0] or INT_MIN.' : 'Initialize the tracked maximum using the first valid element (nums[0]) or Integer.MIN_VALUE.',
        hint: 'Ask yourself what happens when every element is smaller than zero.',
        lineStart: 4,
        lineEnd: 4,
        codeSnippet: 'int maxSoFar = 0;',
        source: 'DETERMINISTIC'
      }
    ] : [],
    testSuggestions: [
      { category: 'NORMAL', description: 'Standard positive array case', sampleInput: '[2, 7, 11, 15]', expectedBehavior: 'Passes' },
      { category: 'NEGATIVE', description: 'Array with all negative numbers', sampleInput: '[-5, -2, -8]', expectedBehavior: 'Returns -2' }
    ]
  };
}
