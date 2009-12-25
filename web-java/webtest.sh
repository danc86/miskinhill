#!/bin/bash

rm -rf webtest.out

mvn jetty:run &
app_pid=$!
echo app_pid is $app_pid
function cleanup {
    echo interrupting $app_pid
    kill -9 $app_pid
}
trap cleanup SIGINT SIGTERM

# wait for app to begin listening
while ! lsof -iTCP:8082 >/dev/null ; do
    echo waiting for app to start listening
    sleep 1
done

wget -nv -r -p -l inf -erobots=off -P webtest.out http://localhost:8082/ 2>&1 | grep -B1 ERROR
grep_status=$?

cleanup

# grep exit status 1 means no lines (i.e. ERRORs) found, which is good
[[ $grep_status != 1 ]] && exit 1
exit 0
