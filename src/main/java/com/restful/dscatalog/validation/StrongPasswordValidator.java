package com.restful.dscatalog.validation;

import com.restful.dscatalog.validation.annotation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.text.Normalizer;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, CharSequence> {

    private int min;
    private int max;
    private boolean requireUpper;
    private boolean requireLower;
    private boolean requireDigit;
    private boolean requireSpecial;
    private boolean allowWhitespace;
    private boolean normalizeNFKC;

    @Override
    public void initialize(StrongPassword ann) {
        this.min = ann.min();
        this.max = ann.max();
        this.requireUpper = ann.requireUpper();
        this.requireLower = ann.requireLower();
        this.requireDigit = ann.requireDigit();
        this.requireSpecial = ann.requireSpecial();
        this.allowWhitespace = ann.allowWhitespace();
        this.normalizeNFKC = ann.normalizeNFKC();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true;

        String s = value.toString();
        if (normalizeNFKC) {
            s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        }

        final int len = s.codePointCount(0, s.length());
        if (len < min || len > max) return false;

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false, hasIllegalWS = false;

        for (int i = 0; i < s.length(); ) {
            final int cp = s.codePointAt(i);
            if (Character.isUpperCase(cp)) hasUpper = true;
            else if (Character.isLowerCase(cp)) hasLower = true;
            else if (Character.isDigit(cp)) hasDigit = true;
            else if (Character.isWhitespace(cp)) {
                if (!allowWhitespace) hasIllegalWS = true;
            } else {
                hasSpecial = true;
            }
            i += Character.charCount(cp);
        }

        if (hasIllegalWS) return false;
        if (requireUpper && !hasUpper) return false;
        if (requireLower && !hasLower) return false;
        if (requireDigit && !hasDigit) return false;
        if (requireSpecial && !hasSpecial) return false;

        return true;
    }
}
