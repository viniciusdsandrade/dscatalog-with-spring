package com.restful.dscatalog.validation;

import com.restful.dscatalog.validation.annotation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.text.Normalizer;

import static java.lang.Character.*;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, CharSequence> {

    private Policy policy;

    private static final class Policy {
        final int min;
        final int max;
        final boolean requireUpper;
        final boolean requireLower;
        final boolean requireDigit;
        final boolean requireSpecial;
        final boolean allowWhitespace;
        final boolean normalizeNFKC;

        Policy(StrongPassword ann) {
            this.min = ann.min();
            this.max = ann.max();
            this.requireUpper = ann.requireUpper();
            this.requireLower = ann.requireLower();
            this.requireDigit = ann.requireDigit();
            this.requireSpecial = ann.requireSpecial();
            this.allowWhitespace = ann.allowWhitespace();
            this.normalizeNFKC = ann.normalizeNFKC();
        }
    }

    private static final class Scan {
        boolean hasUpper;
        boolean hasLower;
        boolean hasDigit;
        boolean hasSpecial;
        boolean hasIllegalWhitespace;
    }

    @Override
    public void initialize(StrongPassword ann) {
        this.policy = new Policy(ann);
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true;

        String s = value.toString();
        if (policy.normalizeNFKC) {
            s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        }

        final int len = s.codePointCount(0, s.length());
        if (len < policy.min || len > policy.max) return false;

        final Scan scan = scanCodePoints(s, policy.allowWhitespace);

        if (scan.hasIllegalWhitespace) return false;
        if (policy.requireUpper && !scan.hasUpper) return false;
        if (policy.requireLower && !scan.hasLower) return false;
        if (policy.requireDigit && !scan.hasDigit) return false;
        return !policy.requireSpecial || scan.hasSpecial;
    }

    private static Scan scanCodePoints(String s, boolean allowWhitespace) {
        final Scan scan = new Scan();
        for (int i = 0, n = s.length(); i < n; ) {
            final int cp = s.codePointAt(i);

            switch (getType(cp)) {
                case UPPERCASE_LETTER -> scan.hasUpper = true;
                case LOWERCASE_LETTER -> scan.hasLower = true;
                case DECIMAL_DIGIT_NUMBER -> scan.hasDigit = true;
                case SPACE_SEPARATOR, LINE_SEPARATOR, PARAGRAPH_SEPARATOR -> {
                    if (cp != 0x00A0 && cp != 0x2007 && cp != 0x202F) {
                        if (!allowWhitespace) scan.hasIllegalWhitespace = true;
                    } else {
                        scan.hasSpecial = true;
                    }
                }
                default -> {
                    switch (cp) {
                        case '\t', '\n', '\u000B', '\f', '\r',
                             '\u001C', '\u001D', '\u001E', '\u001F' -> {
                            if (!allowWhitespace) scan.hasIllegalWhitespace = true;
                        }
                        default -> scan.hasSpecial = true;
                    }
                }
            }

            i += Character.charCount(cp);
        }
        return scan;
    }
}
