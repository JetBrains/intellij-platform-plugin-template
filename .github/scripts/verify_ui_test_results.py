import sys
import xml.etree.ElementTree as ET
from pathlib import Path


EXPECTED_CLASS = "org.jetbrains.plugins.template.MyToolWindowTest"
EXPECTED_METHOD = "testShuffleDisplaysRandomNumber"


def verify_results(results_dir):
    reports = sorted(results_dir.glob("TEST-*.xml"))
    if not reports:
        raise ValueError(f"No UI test XML reports found in {results_dir}")

    test_cases = []
    for report in reports:
        try:
            root = ET.parse(report).getroot()
            if root.tag not in ("testsuite", "testsuites"):
                raise ValueError(f"Unexpected report root: {root.tag}")
            for suite in root.iter("testsuite"):
                if any(int(suite.get(outcome, "0")) > 0 for outcome in ("failures", "errors")):
                    raise ValueError("UI test suite reports failures or errors")
            test_cases.extend(root.iter("testcase"))
        except (ET.ParseError, OSError, ValueError) as error:
            raise ValueError(f"Cannot read successful UI test results from {report}: {error}") from error

    if not test_cases:
        raise ValueError("No UI test cases were reported")

    expected = [
        case for case in test_cases
        if case.get("classname") == EXPECTED_CLASS
        and case.get("name") in (EXPECTED_METHOD, f"{EXPECTED_METHOD}()")
    ]
    if len(expected) != 1:
        raise ValueError(f"Expected exactly one {EXPECTED_CLASS}.{EXPECTED_METHOD}, found {len(expected)}")
    if any(expected[0].find(outcome) is not None for outcome in ("failure", "error", "skipped")):
        raise ValueError(f"{EXPECTED_CLASS}.{EXPECTED_METHOD} did not pass")
    if any(case.find(outcome) is not None for case in test_cases for outcome in ("failure", "error")):
        raise ValueError("UI test results contain a failure or error")

    return len(test_cases)


if __name__ == "__main__":
    results_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("build/test-results/testUi")
    try:
        count = verify_results(results_dir)
    except ValueError as error:
        print(f"UI test verification failed: {error}", file=sys.stderr)
        sys.exit(1)
    print(f"Verified {EXPECTED_CLASS}.{EXPECTED_METHOD} passed ({count} UI test cases reported)")