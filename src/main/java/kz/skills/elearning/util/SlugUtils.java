package kz.skills.elearning.util;

import java.util.Locale;

public final class SlugUtils {

    private SlugUtils() {
    }

    /**
     * Converts arbitrary text to a URL-safe slug.
     *
     * <p>Rules: lowercase, strip non-alphanumeric characters (except spaces and hyphens),
     * collapse whitespace/hyphens into single hyphens, trim leading/trailing hyphens.
     *
     * <p>Examples:
     * <pre>
     *   "Introduction to Java!" → "introduction-to-java"
     *   "  Hello   World  "    → "hello-world"
     *   "C++ Programming"      → "c-programming"
     * </pre>
     */
    public static String toSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }
}
