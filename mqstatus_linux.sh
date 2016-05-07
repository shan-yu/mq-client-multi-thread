rm -f status.dump

echo 'Collecting MQ queue status every second...'

date

for((i=0;i<60;i++));do

echo 'DISPLAY QSTATUS(TESTQ) all' | runmqsc QM01 | grep -E 'CURDEPTH' >> status.dump

sleep 1

done;




