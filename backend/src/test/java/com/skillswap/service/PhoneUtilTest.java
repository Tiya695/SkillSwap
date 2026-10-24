package com.skillswap.service;

import com.skillswap.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneUtilTest {

    @Test
    void acceptsTenDigitsAndIndianPrefixes() {
        assertThat(PhoneUtil.normalize("9876543210")).isEqualTo("+919876543210");
        assertThat(PhoneUtil.normalize("+91 98765 43210")).isEqualTo("+919876543210");
        assertThat(PhoneUtil.normalize("+91-9876543210")).isEqualTo("+919876543210");
        assertThat(PhoneUtil.normalize("919876543210")).isEqualTo("+919876543210");
        assertThat(PhoneUtil.normalize("09876543210")).isEqualTo("+919876543210");
    }

    @Test
    void emptyMeansNoNumber() {
        assertThat(PhoneUtil.normalize(null)).isNull();
        assertThat(PhoneUtil.normalize("   ")).isNull();
    }

    @Test
    void rejectsWrongLengthsLettersAndNumbersNotStartingWith6To9() {
        for (String bad : new String[]{"12345", "98765432101", "987654321", "5876543210", "98765abc10", "+1 9876543210", "+92 9876543210"}) {
            assertThatThrownBy(() -> PhoneUtil.normalize(bad)).as(bad).isInstanceOf(BadRequestException.class);
        }
    }
}
