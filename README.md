# MQClientMultiThread

## Introduction
This Java program is to launch multiple threads of IBM MQ Java clients or JMS clients to test the message throughput of an IBM MQ queue manager.

This program has been tested on Linux and Windows.
<br>
<br>

## How to run the program
To run the program, you must have JRE (6.0 or above) installed or configured properly. 

Besides, you should have IBM MQ client or server installed on the same machine. To use the logging function, you should also have Log4j downloaded and configured.

The command to run the program is:
  java -jar MQClientMultiThread.jar 
<br>
<br>


## Descriptions of files used by this program:
**Input**

  - MQClientMultiThread.properties: to provide parameters for running the program.
  - DataFile folder: with different sizes of text files as message payload data.
  - Log4j_MQClientMultiThread.properties: to provide log4j configuration.

**Output**

  - MQClientMultiThread.log: record the logs during running.
  
**Utilities**

  - QM01.mqsc is a sample MQSC script for defining a sample queue manager name QM01 for testing.
  - mqstatus_linux.sh is a shell running a loop for 60 seconds, displaying the current depth of TESTQ queue at every 1 second. So the delta value indicate the number of messages being put in the interval.
<br>
<br>  

## How to set up the test environment
Please use a user id which belongs to mqm group, so that you are authorized to issue MQ commands and do the operations by MQ client.

**Phase I - Set up and tune MQ**

  - crtmqm QM01 (we choose QM01 as the name for queue manager)
  - strmqm QM01
  - runmqsc QM01 < QM01.mqsc
  - vi /var/mqm/qmgrs/QM01/qm.ini to add below stanza. These tune the persistent queue buffer and non-persistent queue buffer to 50MB. It will only take effect after the queue manger is restarted and new queues are defined.
    TuningParameters:
      DefaultQBufferSize=52428800
      DefaultPQBufferSize=52428800
  - Restart QM01 (ie. endmqm QM01, then strmqm QM01)
  - runmqsc QM01
    - DELETE QLOCAL(TESTQ) PURGE  -- if there is any existing queue
    - DEFINE QLOCAL(TESTQ) MAXDEPTH(999999999)
    - END

**Phase II - Run test and record the results**

  - runmqsc QM01
    - CLEAR QLOCAL(TESTQ)  -- clear the queue before each test
    - END
  - Modify MQClientMultiThread.properties for test parameters
  - ./mqstatus_linux.sh    -- display the current depth of TESTQ every second on Linux for one minutes (you can customize the length of time)
  - java -jar MQClientMultiThread.jar  -- start to run
  - Check the status.dump result, calculate the average message rate by choosing a stable period, and save the status.dump file if needed, because it will be overwritten in next test.
<br>
<br>

## Author
Shan Yu (yushan0624@gmail.com or yushan@cn.ibm.com)

## License
MQClientMultiThread uses [Apache License Version 2.0 software license](LICENSE).


