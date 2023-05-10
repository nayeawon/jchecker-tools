#!/bin/bash
OUTPUT="$1" # /data/jchecker/{className}/{studentNum}/feedback/{date}/autoGeneration/pr/{FixedBugs:PartiallyFixedBugs}
EMAIL="$2" # {studentEmail}
OPENAI_API_KEY="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"
declare -a LST=$(ls "$OUTPUT")
CONTENT="
The following is the result of the automated program repair.
Please check this.

"
for file in $LST
do
    value=$(<"$OUTPUT/$file")
    response=$(curl https://api.openai.com/v1/chat/completions \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $OPENAI_API_KEY" \
        -d '{
            "model": "gpt-3.5-turbo",
            "messages" : [{"role": "user", "content": "You are a teaching assistant of a java programming class. Please elaborate the following patch. ${value}"}]
        }')
    if [[ $response == *"error"* ]]; then
        echo "error: openai api error"
        CONTENT="
${CONTENT}

==============================================
Patch   		:
$value
==============================================
"
    else
        echo "success: openai api success"
        explanation=$(grep -o '"content": "[^"]*' "$response" | grep -o '[^"]*$')
    CONTENT="
${CONTENT}

==============================================
Explanation		: 
$explanation

Patch   		:
$value
==============================================
"
    fi
done

echo "
${CONTENT}

-jChecker2.1" | mail -s '[JAVA] Repair Patch Results (jChecker2.1)' "$EMAIL"

