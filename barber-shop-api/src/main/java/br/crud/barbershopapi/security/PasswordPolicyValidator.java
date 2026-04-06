package br.crud.barbershopapi.security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordPolicyValidator {

    private static final int MIN_LENGTH = 10;
    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    public void validateOrThrow(final String rawPassword) {
        final List<String> errors = validate(rawPassword);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    public List<String> validate(final String rawPassword) {
        final List<String> errors = new ArrayList<>();
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            errors.add("A senha deve ter no mínimo " + MIN_LENGTH + " caracteres.");
        }
        if (rawPassword != null) {
            if (!UPPER.matcher(rawPassword).matches()) {
                errors.add("A senha deve conter ao menos uma letra maiúscula.");
            }
            if (!LOWER.matcher(rawPassword).matches()) {
                errors.add("A senha deve conter ao menos uma letra minúscula.");
            }
            if (!DIGIT.matcher(rawPassword).matches()) {
                errors.add("A senha deve conter ao menos um dígito numérico.");
            }
            if (!SPECIAL.matcher(rawPassword).matches()) {
                errors.add("A senha deve conter ao menos um caractere especial.");
            }
        }
        return errors;
    }
}
