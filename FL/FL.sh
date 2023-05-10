#!/usr/bin/env bash
#
# ------------------------------------------------------------------------------
# This script performs fault-localization on a Java project using the GZoltar
# command line interface either using instrumentation 'at runtime' or 'offline'.
#
# Usage:
# ./run.sh
#     --instrumentation <online|offline>
#     [--help]
#
# Requirements:
# - `java` and `javac` needs to be set and must point to the Java installation.
#
# ------------------------------------------------------------------------------
# ./FL.sh <input> <test-dir>
# ./FL.sh /data/jchecker/{className}/{std_id}/yyyy_MM_dd_HH_mm_SS/autoGeneration /data/jchecker/data/test/{class_name}
SCRIPT_DIR=$(cd "$1" && pwd)

#
# Print error message and exit
#
die() {
  echo "$@" >&2
  exit 1
}

# ------------------------------------------------------------------ Envs & Args

GZOLTAR_VERSION="1.7.2"

# Check whether GZOLTAR_CLI_JAR is set
export GZOLTAR_CLI_JAR="/home/DPMiner/lib/gzoltarcli.jar"
[ "$GZOLTAR_CLI_JAR" != "" ] || die "GZOLTAR_CLI is not set!"
[ -s "$GZOLTAR_CLI_JAR" ] || die "$GZOLTAR_CLI_JAR does not exist or it is empty!"

export GZOLTAR_AGENT_RT_JAR="/home/DPMiner/lib/gzoltaragent.jar"
[ "$GZOLTAR_AGENT_RT_JAR" != "" ] || die "GZOLTAR_AGENT_RT_JAR is not set!"
[ -s "$GZOLTAR_AGENT_RT_JAR" ] || die "$GZOLTAR_AGENT_RT_JAR does not exist or it is empty!"

#
# Prepare runtime dependencies
#
export JUNIT_JAR="/home/DPMiner/lib/junit.jar"

export HAMCREST_JAR="/home/DPMiner/lib/hamcrest-core.jar"

SCRIPT_DIR="$SCRIPT_DIR/src"

BUILD_DIR="$SCRIPT_DIR/bin"

TEST_DIR="${2}" #/data/jchecker/data/test/{class_name}

# ------------------------------------------------------------------------- Main

#
# Compile
#

echo "Compile source and test cases ..."

cd "$TEST_DIR" || die "Failed to change directory to $TEST_DIR!"
javac -cp $JUNIT_JAR:$BUILD_DIR "$TEST_DIR/src/JunitTest.java" -d "$TEST_DIR/bin" || die "Failed to compile test cases!"

cd "$SCRIPT_DIR" || die "Failed to change directory to $SCRIPT_DIR!"

#
# Collect list of unit test cases to run
#

echo "Collect list of unit test cases to run ..."

UNIT_TESTS_FILE="$TEST_DIR/tests.txt"

java -cp $TEST_DIR:$GZOLTAR_CLI_JAR \
  com.gzoltar.cli.Main listTestMethods $TEST_DIR \
    --outputFile "$UNIT_TESTS_FILE" \
    --includes "*Test#*" || die "Collection of unit test cases has failed!"
[ -s "$UNIT_TESTS_FILE" ] || die "$UNIT_TESTS_FILE does not exist or it is empty!"

#
# Collect coverage
#

SER_FILE="$BUILD_DIR/gzoltar.ser"

echo "Perform offline instrumentation ..."

# Backup original classes
BUILD_BACKUP_DIR="$SCRIPT_DIR/.build"
rm -rf "$BUILD_BACKUP_DIR"
mv "$BUILD_DIR" "$BUILD_BACKUP_DIR" || die "Backup of original classes has failed!"
mkdir -p "$BUILD_DIR"

# Perform offline instrumentation
java -cp $BUILD_BACKUP_DIR:$GZOLTAR_AGENT_RT_JAR:$GZOLTAR_CLI_JAR \
  com.gzoltar.cli.Main instrument \
  --outputDirectory "$BUILD_DIR" \
  $BUILD_BACKUP_DIR || die "Offline instrumentation has failed!"

echo "Run each unit test case in isolation ..."

# Run each unit test case in isolation
java -cp $BUILD_DIR:$TEST_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_AGENT_RT_JAR:$GZOLTAR_CLI_JAR \
  -Dgzoltar-agent.destfile=$SER_FILE \
  -Dgzoltar-agent.output="file" \
  com.gzoltar.cli.Main runTestMethods \
    --testMethods "$UNIT_TESTS_FILE" \
    --offline \
    --collectCoverage || die "Coverage collection has failed!"

# Restore original classes
cp -R $BUILD_BACKUP_DIR/* "$BUILD_DIR" || die "Restore of original classes has failed!"
rm -rf "$BUILD_BACKUP_DIR"


[ -s "$SER_FILE" ] || die "$SER_FILE does not exist or it is empty!"

#
# Create fault localization report
#

echo "Create fault localization report ..."

OUTPUT_DIR="../"

SPECTRA_FILE="$OUTPUT_DIR/sfl/txt/spectra.csv"
MATRIX_FILE="$OUTPUT_DIR/sfl/txt/matrix.txt"
TESTS_FILE="$OUTPUT_DIR/sfl/txt/tests.csv"

java -cp $BUILD_DIR:$JUNIT_JAR:$HAMCREST_JAR:$GZOLTAR_CLI_JAR \
  com.gzoltar.cli.Main faultLocalizationReport \
    --buildLocation "$BUILD_DIR" \
    --granularity "line" \
    --inclPublicMethods \
    --inclStaticConstructors \
    --inclDeprecatedMethods \
    --dataFile "$SER_FILE" \
    --outputDirectory "$OUTPUT_DIR" \
    --family "sfl" \
    --formula "ochiai" \
    --metric "entropy" \
    --formatter "txt" || die "Generation of fault-localization report has failed!"

[ -s "$SPECTRA_FILE" ] || die "$SPECTRA_FILE does not exist or it is empty!"
[ -s "$MATRIX_FILE" ] || die "$MATRIX_FILE does not exist or it is empty!"
[ -s "$TESTS_FILE" ] || die "$TESTS_FILE does not exist or it is empty!"

echo "DONE!"
exit 0
