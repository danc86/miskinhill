#!/bin/bash

rm -rf webtest.out

ssh syn cat /etc/apache2/clients.d/miskinhill.com.au.conf \
    | egrep -v 'VirtualHost|CustomLog|ErrorLog' \
    | sed -e 's@/home/dan/\.www/miskinhill\.com\.au/code/web/app\.py@app.py@' \
    >miskinhill.com.au.conf \

/usr/sbin/apache2 -d `pwd` -f apache2.webtest.conf -X &
apache_pid=$!
function cleanup {
    kill -INT $apache_pid
    rm apache2.webtest.pid # why doesn't apache do this?
}
trap cleanup SIGINT SIGTERM
sleep 2

wget -nv -r -p -l inf -erobots=off -P webtest.out --user=demo --password=demo http://localhost:9996/ 2>&1 | grep -B1 ERROR
grep_status=$?

cleanup

# grep exit status 1 means no lines (i.e. ERRORs) found, which is good
[[ $grep_status != 1 ]] && exit 1
exit 0
