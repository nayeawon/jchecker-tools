#!/bin/bash
EMAIL="$2" # {studentEmail}
echo "

We are sorry to inform you that your recent request on jChecker for program repair has unfortunately failed.
Please consider reaching out to the Teaching Assistants (TAs) or the professor for further assistance.

We apologize for any inconvenience and remain available to support you in improving your code.

-jChecker2.1" | mail -s '[JAVA] Repair Patch Results (jChecker2.1)' "$EMAIL"