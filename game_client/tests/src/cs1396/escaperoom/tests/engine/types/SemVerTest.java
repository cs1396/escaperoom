package cs1396.escaperoom.tests.engine.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import cs1396.escaperoom.engine.types.SemVer;
import cs1396.escaperoom.tests.TestUtils;

public class SemVerTest {
  @ParameterizedTest
  @CsvSource({
      "'1.1.1', 1, 1, 1",
      "'2.0.1', 2, 0, 1",
      "'3.11.1', 3, 11, 1",
      "'1.2.3', 1, 2, 3",
      "'4.13.1', 4, 13, 1",
      "'4.14.1', 4, 14, 1",
      "'1.0.0', 1, 0, 0",
      "'0.1.0', 0, 1, 0",
      "'0.0.1', 0, 0, 1",
      "'10.20.30', 10, 20, 30",
      "'0.0.0', 0, 0, 0",
      "'999.999.999', 999, 999, 999",
      "'1000000000.2.3', 1000000000, 2, 3",
      "'1.2000000000.3', 1, 2000000000, 3", 
      "'1.2.3000000000', 1, 2, 3000000000"
  })
  void validBasic(String version, Long major, Long minor, Long patch) {
    TestUtils.assertOkAnd(
        SemVer.parse(version),
        (sv) -> sv.equals(new SemVer(major, minor, patch)),
        (sv) -> String.format("Expected: %s Parsed %s", version, sv.toString()));
  }

  @ParameterizedTest
  @CsvSource({
      "'01.1.1','leading zero in major'",
      "'2.099.1','leading zero in minor'",
      "'3.1a1.1','letter in minor'",
      "'1.a.3','minor is letter'",
      "'4..1','empty minor'",
      "'4.1','two few'",
      "'1.0.012','leading zero in patch'",
      "'','empty string'",
      "'   ','whitespace only'",
      "'1.0.0.0','too many segments'",
      "'-1.0.0','negative major'",
      "'1.-2.0','negative minor'",
      "'1.0.-3','negative patch'",
      "'a.b.c','all letters'",
      "'1.b.3','letters in minor'",
      "'1.2.c','letters in patch'",
      "'v1.2.3','leading v'",
      "'1a.0.0','letters in major'",
      "'1.0a.0','letters in minor'",
      "'1.0.0a','letters in patch'",
      "'1..0.0','double dots'",
      "'1.0..0','double dots'",
      "'1.0.0..','trailing dots'",
      "'.1.0.0','leading dot'",
      "'1','single segment'",
      "'1.0','two segments'",
      "'1,0,0,', 'wrong separator'",
      "'1.0.0-alpha€beta','unicode in version'"
  })
  void invalidBasic(String version, String reason) {
    TestUtils.assertErr(
      SemVer.parse(version),
      (sv) -> String.format("Parsed %s from %s (invalid due to \"%s\")", sv.toString(), version, reason)
    );
  }

  @ParameterizedTest
  @CsvSource({
    "'0.1.1-1a-2b.1b-2c', 0, 1, 1, '1a-2b.1b-2c'",
    "'1.0.0-alpha', 1, 0, 0, 'alpha'",
    "'1.0.0-alpha.1', 1, 0, 0, 'alpha.1'",
    "'1.0.0-alpha.beta', 1, 0, 0, 'alpha.beta'",
    "'1.0.0-beta.2', 1, 0, 0, 'beta.2'",
    "'1.0.0-beta.11', 1, 0, 0, 'beta.11'",
    "'1.0.0-rc.1', 1, 0, 0, 'rc.1'",
    "'1.0.0-alpha1', 1, 0, 0, 'alpha1'",
    "'1.0.0-alpha1', 1, 0, 0, 'alpha1'",
    "'1.0.0-1alpha', 1, 0, 0, '1alpha'",
    "'1.0.0---', 1, 0, 0, '--'",
    "'1.0.0-a', 1, 0, 0, 'a'",
    "'1.0.0-1', 1, 0, 0, '1'",
    "'1.0.0-very-long-identifier-name', 1, 0, 0, 'very-long-identifier-name'",
    "'1.0.0--alpha', 1, 0, 0, '-alpha'",
    "'1.0.0-alpha.beta.rc.1', 1, 0, 0, 'alpha.beta.rc.1'"
  })
  void validPreRelease(String version, Long major, Long minor, Long patch, String preRelease) {

    SemVer expected = new SemVer
      .Builder()
      .version(major, minor, patch)
      .preReleaseIDs(preRelease.split("[.]"))
      .build();

    TestUtils.assertOkAnd(
        SemVer.parse(version),
        (sv) -> sv.equals(expected),
        (sv) -> String.format("Expected: %s Parsed %s", version, sv.toString()));
  }

  @ParameterizedTest
  @CsvSource(
    value = {
      "'1.0.0-alpha..beta','empty identifier'",
      "'1.0.0-alpha.','trailing dot'",
      "'1.0.0.alpha-','trailing hyphen'",
      "'1.0.0-','empty pre-release'",
      "'1.0.0-.' ,'single dot'",
      "'1.0.0-1.','trailing dot after number'",
      "'1.0.0-.1','leading dot before number'",
      "'1.0.0-alpha@beta','invalid character @'",
      "'1.0.0-alpha#beta','invalid character #'",
      "'1.0.0-alpha beta','space'",
      "'1.0.0-alpha/beta','slash'",
      "'1.0.0-alpha\\beta','backslash'",
      "'1.0.0-alpha.01','leading zero on numeric id'",
      "'1.0.0-alpha=beta','equals in pre-release'",
      "'1.0.0-alpha€beta','unicode character'",
      "'1.0.0-alpha      ','trailing spaces'",
      "'1.0.0-      alpha','leading spaces'"
    }, 
    ignoreLeadingAndTrailingWhitespace = false
  )
  void invalidPreRelease(String version, String reason) {
    TestUtils.assertErr(
      SemVer.parse(version),
      (sv) -> String.format("Parsed %s from %s (invalid due to \"%s\")", sv.toString(), version, reason)
    );
  }

  @ParameterizedTest
  @CsvSource({
    "'0.1.1+1a-2b.1b-2c', 0, 1, 1, '1a-2b.1b-2c'",
    "'1.0.0+alpha', 1, 0, 0, 'alpha'",
    "'1.0.0+alpha.1', 1, 0, 0, 'alpha.1'",
    "'1.0.0+alpha.beta', 1, 0, 0, 'alpha.beta'",
    "'1.0.0+beta.2', 1, 0, 0, 'beta.2'",
    "'1.0.0+beta.11', 1, 0, 0, 'beta.11'",
    "'1.0.0+rc.1', 1, 0, 0, 'rc.1'",
    "'1.0.0+123', 1, 0, 0, '123'",
    "'1.0.0+123.456', 1, 0, 0, '123.456'",
    "'1.0.0+build1', 1, 0, 0, 'build1'",
    "'1.0.0+1build', 1, 0, 0, '1build'",
    "'1.0.0+very-long-build-metadata', 1, 0, 0, 'very-long-build-metadata'",
    "'1.0.0+build.123.456', 1, 0, 0, 'build.123.456'"
  })
  void validBuild(String version, Long major, Long minor, Long patch, String build) {
    SemVer expected = new SemVer
      .Builder()
      .version(major, minor, patch)
      .buildIDs(build.split("[.]"))
      .build();

    TestUtils.assertOkAnd(
        SemVer.parse(version),
        (sv) -> sv.equals(expected),
        (sv) -> String.format("Expected: %s Parsed %s", version, sv.toString()));
  }

  @ParameterizedTest
  @CsvSource(
    value = {
      "'1.0.0+alpha..beta','empty identifier'",
      "'1.0.0-asdf+alpha.','trailing dot'",
      "'1.0.0+','empty build'",
      "'1.0.0-asfd+.' ,'trailing single dot'",
      "'1.0.0+1.','trailing dot after number'",
      "'1.0.0+.1','leading dot before number'",
      "'1.0.0+alpha@beta','invalid character @'",
      "'1.0.0+alpha#beta','invalid character #'",
      "'1.0.0+alpha beta','space'",
      "'1.0.0+alpha/beta','slash'",
      "'1.0.0+alpha\\beta','backslash'",
      "'1.0.0+build+beta','multiple plus signs'",
      "'1.0.0+build=beta','equals in build identifier'",
      "'1.0.0+build€beta','unicode character'",
      "'1.0.0+build      ','trailing spaces'",
      "'1.0.0+      build','leading spaces'",
    }, 
    ignoreLeadingAndTrailingWhitespace = false
  )
  void invalidBuild(String version, String reason) {
    TestUtils.assertErr(
      SemVer.parse(version),
      (sv) -> String.format("Parsed %s from %s (invalid due to \"%s\")", sv.toString(), version, reason)
    );
  }

  @ParameterizedTest
  @CsvSource({
    "'0.1.1-1a-2b.1b-2c+1a-2b.1b-2c', 0, 1, 1,  '1a-2b.1b-2c', '1a-2b.1b-2c'",
    "'1.0.0-alpha+alpha', 1, 0, 0,  'alpha', 'alpha'",
    "'1.0.0-alpha.1+alpha.1', 1, 0, 0,  'alpha.1', 'alpha.1'",
    "'1.0.0-alpha.beta+alpha.beta', 1, 0, 0,  'alpha.beta', 'alpha.beta'",
    "'1.0.0-beta.2+beta.2', 1, 0, 0,  'beta.2', 'beta.2'",
    "'1.0.0-beta.11+beta.11', 1, 0, 0,  'beta.11', 'beta.11'",
    "'1.0.0-rc.1+rc.1', 1, 0, 0,  'rc.1', 'rc.1'",
    "'1.2.3-alpha.1.beta.2+build.345.678', 1, 2, 3, 'alpha.1.beta.2', 'build.345.678'",
    "'1.0.0-alpha1+build1', 1, 0, 0, 'alpha1', 'build1'",
    "'1.0.0-1alpha+1build', 1, 0, 0, '1alpha', '1build'",
    "'1.0.0-a+123', 1, 0, 0, 'a', '123'",
    "'1.0.0-1+123.456', 1, 0, 0, '1', '123.456'"
  })
  void validPreReleaseAndBuild(
    String version, Long major, Long minor, Long patch,
    String preRelease, String build
  ) {
    SemVer expected = new SemVer
      .Builder()
      .version(major, minor, patch)
      .buildIDs(build.split("[.]"))
      .preReleaseIDs(preRelease.split("[.]"))
      .build();

    TestUtils.assertOkAnd(
        SemVer.parse(version),
        (sv) -> sv.equals(expected),
        (sv) -> String.format("Expected: %s Parsed %s", version, sv.toString()));
  }

  @ParameterizedTest
  @CsvSource({
    "'1.0.0', '1.0.1', -1",
    "'1.0.0', '1.1.0', -1",
    "'1.0.0', '2.0.0', -1",
    "'1.0.1', '1.0.0', 1",
    "'1.1.0', '1.0.0', 1",
    "'2.0.0', '1.0.0', 1",
    "'1.0.0', '1.0.0', 0",
    "'1.2.3', '2.0.0', -1",
    "'2.2.3', '1.9.9', 1",
    "'10.0.0', '9.9.9', 1",
    "'1.2.3', '1.3.0', -1",
    "'1.3.0', '1.2.3', 1",
    "'1.0.0', '1.0.1', -1",
    "'1.2.3', '1.2.4', -1",
    "'1.2.4', '1.2.3', 1",
    "'1.0.0-alpha', '1.0.0', -1",
    "'1.0.0', '1.0.0-alpha', 1",
    "'1.0.0-alpha.1', '1.0.0', -1",
    "'1.0.0-alpha', '1.0.0-alpha.1', -1",
    "'1.0.0-alpha.1', '1.0.0-alpha.beta', -1",
    "'1.0.0-alpha.1', '1.0.0-alpha.2', -1",
    "'1.0.0-alpha.2', '1.0.0-alpha.10', -1",
    "'1.0.0-alpha.10', '1.0.0-alpha.beta', -1",
    "'1.0.0-alpha.1', '1.0.0-alpha.1.1', -1",
    "'1.0.0-alpha.beta', '1.0.0-alpha.1', 1",
    "'1.0.0-alpha', '1.0.0-beta', -1",
    "'1.0.0-beta', '1.0.0-rc', -1",
    "'1.0.0-rc', '1.0.0', -1",
    "'1.0.0-alpha.1.beta.2', '1.0.0-alpha.1.beta.3', -1",
    "'1.0.0-alpha.1.beta.2', '1.0.0-alpha.2', -1",
    "'1.0.0-alpha.1', '1.0.0-alpha.1.beta', -1",
    "'1.0.0+build.1', '1.0.0+build.2', 0",
    "'1.0.0+build.1', '1.0.0', 0",
    "'1.0.0', '1.0.0+build.1', 0",
    "'0.0.1', '0.0.2', -1",
    "'0.1.0', '0.2.0', -1",
    "'1.0.0', '1.0.0-alpha.0', 1",
    "'1.0.0-alpha', '1.0.0-alpha.0', -1",
    "'1.0.0-alpha.1', '1.0.0-alpha.1', 0",
    "'1.0.0-0', '1.0.0-0.0', -1",
    "'1.0.0-alpha.0', '1.0.0-alpha.1', -1",
    "'1.0.0-alpha.0.beta', '1.0.0-alpha.0.alpha', 1",
    "'2.0.0-alpha', '1.9.9', 1",
    "'1.0.0-alpha+build', '1.0.0-alpha', 0",
    "'1.0.0-alpha+build.1', '1.0.0-alpha+build.2', 0"
  })
  void compare(String s1, String s2, int expected) {
    SemVer sv1 = SemVer.parse(s1).unwrap();
    SemVer sv2 = SemVer.parse(s2).unwrap();

    int res = sv1.compareTo(sv2);

    String errorString = String.format(
      "Expected %s %s %s, got %s %s %s",
      s1,
      switch (expected){
        case -1 -> "<";
        case 0 -> "==";
        case 1 -> ">";
        default -> "n/a";
      }, s2, s1,
      switch (res){
        case -1 -> "<";
        case 0 -> "==";
        case 1 -> ">";
        default -> "n/a";
      }, s2
    );
    assertEquals(Math.signum(res), Math.signum(expected), errorString);
  }
}
