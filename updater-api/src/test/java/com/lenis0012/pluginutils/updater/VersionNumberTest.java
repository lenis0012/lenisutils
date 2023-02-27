package com.lenis0012.pluginutils.updater;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
class VersionNumberTest {

    @Test
    void testVersionNumber() {
        VersionNumber versionNumber = VersionNumber.of("1.2.3-SNAPSHOT");
        assertThat(versionNumber.toString()).isEqualTo("1.2.3");
    }

    @Test
    void testGreaterThan() {
        assertThat(VersionNumber.of("1.2.3").greaterThan(VersionNumber.of("1.2.3"))).isFalse();
        assertThat(VersionNumber.of("1.2.3").greaterThan(VersionNumber.of("1.2.2"))).isTrue();
        assertThat(VersionNumber.of("1.3.0").greaterThan(VersionNumber.of("1.2.2"))).isTrue();
        assertThat(VersionNumber.of("2.0.0").greaterThan(VersionNumber.of("1.2.2"))).isTrue();
    }

    @Test
    void testGreaterThanEquals() {
        assertThat(VersionNumber.of("1.2.3").greaterThanOrEqual(VersionNumber.of("1.2.3"))).isTrue();
        assertThat(VersionNumber.of("1.2.3").greaterThanOrEqual(VersionNumber.of("1.2.2"))).isTrue();
        assertThat(VersionNumber.of("1.3.0").greaterThanOrEqual(VersionNumber.of("1.2.2"))).isTrue();
        assertThat(VersionNumber.of("2.0.0").greaterThanOrEqual(VersionNumber.of("1.2.2"))).isTrue();
        assertThat(VersionNumber.of("1.0.0").greaterThanOrEqual(VersionNumber.of("1.2.2"))).isFalse();
    }

    @Test
    void testLessThan() {
        assertThat(VersionNumber.of("1.2.3").lessThan(VersionNumber.of("1.2.3"))).isFalse();
        assertThat(VersionNumber.of("1.2.3").lessThan(VersionNumber.of("1.2.2"))).isFalse();
        assertThat(VersionNumber.of("1.3.0").lessThan(VersionNumber.of("1.2.2"))).isFalse();
        assertThat(VersionNumber.of("2.0.0").lessThan(VersionNumber.of("1.2.2"))).isFalse();
        assertThat(VersionNumber.of("1.2.3").lessThan(VersionNumber.of("1.2.4"))).isTrue();
    }

    @Test
    void testLessThanEquals() {
        assertThat(VersionNumber.of("1.2.3").lessThanOrEqual(VersionNumber.of("1.2.3"))).isTrue();
        assertThat(VersionNumber.of("1.2.3").lessThanOrEqual(VersionNumber.of("1.2.2"))).isFalse();
        assertThat(VersionNumber.of("1.3.0").lessThanOrEqual(VersionNumber.of("1.2.2"))).isFalse();
        assertThat(VersionNumber.of("2.0.0").lessThanOrEqual(VersionNumber.of("1.2.2"))).isFalse();
        assertThat(VersionNumber.of("1.2.3").lessThanOrEqual(VersionNumber.of("1.2.4"))).isTrue();
    }
}