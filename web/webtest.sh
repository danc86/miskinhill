#!/bin/bash

rm -rf webtest.out

./app.py --port=9996 --content-dir=/sjkwi/home/dan/.www/miskinhill.com.au/content &
app_pid=$!
function cleanup {
    kill $app_pid
}
trap cleanup SIGINT SIGTERM
sleep 2

wget -nv -r -p -l inf -P webtest.out http://localhost:9996/ 2>&1 | grep -B1 ERROR
grep_status=$?

cleanup

# grep exit status 1 means no lines (i.e. ERRORs) found, which is good
[[ $grep_status != 1 ]] && exit 1
exit 0
