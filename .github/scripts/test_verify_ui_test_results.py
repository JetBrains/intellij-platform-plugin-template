import tempfile
import unittest
from pathlib import Path

from verify_ui_test_results import verify_results


class VerifyUiTestResultsTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.results = Path(self.directory.name)

    def write_report(self, test_cases, attributes='tests="1" failures="0" errors="0" skipped="0"'):
        report = self.results / "TEST-MyToolWindowTest.xml"
        report.write_text(f"<testsuite {attributes}>{test_cases}</testsuite>", encoding="utf-8")

    def expected_test(self, result="", name="testShuffleDisplaysRandomNumber()"):
        return (
            '<testcase classname="org.jetbrains.plugins.template.MyToolWindowTest" '
            f'name="{name}">{result}</testcase>'
        )

    def test_accepts_passed_test_with_or_without_parentheses(self):
        for name in ("testShuffleDisplaysRandomNumber", "testShuffleDisplaysRandomNumber()"):
            with self.subTest(name=name):
                self.write_report(self.expected_test(name=name))
                self.assertEqual(verify_results(self.results), 1)

    def test_rejects_missing_reports(self):
        with self.assertRaisesRegex(ValueError, "No UI test XML reports"):
            verify_results(self.results)

    def test_rejects_empty_suite(self):
        self.write_report("", 'tests="0" failures="0" errors="0" skipped="0"')
        with self.assertRaisesRegex(ValueError, "No UI test cases"):
            verify_results(self.results)

    def test_rejects_missing_expected_test(self):
        self.write_report('<testcase classname="AnotherTest" name="testSomethingElse"/>')
        with self.assertRaisesRegex(ValueError, "Expected exactly one"):
            verify_results(self.results)

    def test_rejects_duplicate_expected_test(self):
        self.write_report(self.expected_test() * 2, 'tests="2" failures="0" errors="0" skipped="0"')
        with self.assertRaisesRegex(ValueError, "Expected exactly one"):
            verify_results(self.results)

    def test_rejects_skipped_failed_or_errored_expected_test(self):
        for outcome in ("skipped", "failure", "error"):
            with self.subTest(outcome=outcome):
                self.write_report(self.expected_test(f"<{outcome}/>"))
                with self.assertRaisesRegex(ValueError, "did not pass"):
                    verify_results(self.results)

    def test_rejects_other_test_failures(self):
        self.write_report(
            self.expected_test() + '<testcase classname="AnotherTest" name="testFailure"><failure/></testcase>',
            'tests="2" failures="1" errors="0" skipped="0"',
        )
        with self.assertRaisesRegex(ValueError, "failure|error"):
            verify_results(self.results)

    def test_rejects_suite_level_failures_without_testcase_failure(self):
        self.write_report(self.expected_test(), 'tests="1" failures="1" errors="0" skipped="0"')
        with self.assertRaisesRegex(ValueError, "failure|error"):
            verify_results(self.results)

    def test_rejects_malformed_report(self):
        (self.results / "TEST-broken.xml").write_text("<testsuite>", encoding="utf-8")
        with self.assertRaisesRegex(ValueError, "Cannot read"):
            verify_results(self.results)


if __name__ == "__main__":
    unittest.main()