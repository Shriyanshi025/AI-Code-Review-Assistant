package com.codereview.backend.util;

import com.codereview.backend.exception.InvalidLanguageException;

public final class LanguageValidator {

    public static final String JAVA = "JAVA";
    public static final String CPP = "CPP";

    private LanguageValidator() {}

    /**
     * Validates and normalizes the language identifier.
     * Trims leading/trailing whitespace and normalizes case/aliases.
     *
     * @param language the input language identifier
     * @return normalized canonical language identifier ("JAVA" or "CPP")
     * @throws InvalidLanguageException if language is null, blank, or unsupported
     */
    public static String validateAndNormalize(String language) {
        if (language == null) {
            throw new InvalidLanguageException("Language identifier is required and cannot be blank. Supported languages: JAVA, CPP (or C++).");
        }
        String trimmed = language.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidLanguageException("Language identifier cannot be blank. Supported languages: JAVA, CPP (or C++).");
        }

        String upper = trimmed.toUpperCase();
        if ("JAVA".equals(upper)) {
            return JAVA;
        }

        if ("CPP".equals(upper) || "C++".equalsIgnoreCase(trimmed) || "CPLUSPLUS".equals(upper) || "CXX".equals(upper)) {
            return CPP;
        }

        throw new InvalidLanguageException("Unsupported language: '" + trimmed + "'. Supported languages: JAVA, CPP (or C++).");
    }

    /**
     * Checks if the language is C++ or any of its recognized aliases.
     */
    public static boolean isCpp(String language) {
        if (language == null) return false;
        String trimmed = language.trim();
        String upper = trimmed.toUpperCase();
        return "CPP".equals(upper) || "C++".equalsIgnoreCase(trimmed) || "CPLUSPLUS".equals(upper) || "CXX".equals(upper);
    }

    /**
     * Checks if the language is Java.
     */
    public static boolean isJava(String language) {
        if (language == null) return false;
        return "JAVA".equalsIgnoreCase(language.trim());
    }

    /**
     * Checks if the language is supported (Java or C++).
     */
    public static boolean isSupported(String language) {
        return isJava(language) || isCpp(language);
    }
}
