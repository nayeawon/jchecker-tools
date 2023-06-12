#!/bin/bash
OUTPUT="$1" # /data/jchecker/{className}/{studentNum}/feedback/{date}/autoGeneration/pr/{FixedBugs:PartiallyFixedBugs}
EMAIL="$2" # {studentEmail}
CONTENT="
The following is the result of the automated program repair.
Please check this.

==============================================
$OUTPUT
==============================================
"

echo "
${CONTENT}

-jChecker2.0" | mail -s '[JAVA] Repair Patch Results (jChecker2.0)' "$EMAIL",'jcnam@handong.edu'
