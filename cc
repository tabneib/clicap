#######################################################################################
#!/bin/sh
#######################################################################################
# @author Hoang-Duong Nguyen
#######################################################################################

#######################################################################################
JAVA_HOME=/usr/lib/jvm/java-7-oracle
OPTIONS='-Xms256m -Xmx256m -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true'
pidpath=/tmp/
classname='clicap'
pid=${classname}
####################################################################################### 

#######################################################################################
start () {
       # Run 10 Clicap - 1 central & 9 minor
       cd jar	
       echo -n 'Starting CliCap...'
       echo $JAVA_HOME/bin/java
       # Arguments: 		 IS_CENTRAL - ID - COR_PORT - ENF_PORT -  REMOTE_PORT - DOMAIN - PREDECESSOR - CAPACITY - [remoteUnitID - domain - port]* 
       # TODO write loop for this (@see argsPrinter.java)
      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 1 2 10001 10002 10003 localhost 63 6     \
                                                                 7 localhost 10006    \
                                                                 7 localhost 10006    \
                                                                 7 localhost 10006    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 37 localhost 10018    \
                                                                  & echo $!>${pidpath}${pid}-2.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 7 10004 10005 10006 localhost 2 6     \
                                                                 8 localhost 10009    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 48 localhost 10021    \
                                                                  & echo $!>${pidpath}${pid}-7.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 8 10007 10008 10009 localhost 7 6     \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                 48 localhost 10021    \
                                                                  & echo $!>${pidpath}${pid}-8.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 29 10010 10011 10012 localhost 8 6     \
                                                                 33 localhost 10015    \
                                                                 33 localhost 10015    \
                                                                 33 localhost 10015    \
                                                                 37 localhost 10018    \
                                                                 48 localhost 10021    \
                                                                 63 localhost 10030    \
                                                                  & echo $!>${pidpath}${pid}-29.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 33 10013 10014 10015 localhost 29 6     \
                                                                 37 localhost 10018    \
                                                                 37 localhost 10018    \
                                                                 37 localhost 10018    \
                                                                 48 localhost 10021    \
                                                                 51 localhost 10024    \
                                                                 2 localhost 10003    \
                                                                  & echo $!>${pidpath}${pid}-33.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 37 10016 10017 10018 localhost 33 6     \
                                                                 48 localhost 10021    \
                                                                 48 localhost 10021    \
                                                                 48 localhost 10021    \
                                                                 48 localhost 10021    \
                                                                 60 localhost 10027    \
                                                                 7 localhost 10006    \
                                                                  & echo $!>${pidpath}${pid}-37.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 48 10019 10020 10021 localhost 37 6     \
                                                                 51 localhost 10024    \
                                                                 51 localhost 10024    \
                                                                 60 localhost 10027    \
                                                                 60 localhost 10027    \
                                                                 2 localhost 10003    \
                                                                 29 localhost 10012    \
                                                                  & echo $!>${pidpath}${pid}-48.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 51 10022 10023 10024 localhost 48 6     \
                                                                 60 localhost 10027    \
                                                                 60 localhost 10027    \
                                                                 60 localhost 10027    \
                                                                 60 localhost 10027    \
                                                                 7 localhost 10006    \
                                                                 29 localhost 10012    \
                                                                  & echo $!>${pidpath}${pid}-51.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 60 10025 10026 10027 localhost 51 6     \
                                                                 63 localhost 10030    \
                                                                 63 localhost 10030    \
                                                                 2 localhost 10003    \
                                                                 7 localhost 10006    \
                                                                 29 localhost 10012    \
                                                                 29 localhost 10012    \
                                                                  & echo $!>${pidpath}${pid}-60.pid


      $JAVA_HOME/bin/java -server $OPTIONS -jar clicap.jar 0 63 10028 10029 10030 localhost 60 6     \
                                                                 2 localhost 10003    \
                                                                 2 localhost 10003    \
                                                                 7 localhost 10006    \
                                                                 7 localhost 10006    \
                                                                 29 localhost 10012    \
                                                                 33 localhost 10015    \
                                                                  & echo $!>${pidpath}${pid}-63.pid


}
stop () {
       kill -15 `cat ${pidpath}${pid}-2.pid`
       rm -r ${pidpath}${pid}-2.pid
       kill -15 `cat ${pidpath}${pid}-7.pid`
       rm -r ${pidpath}${pid}-7.pid
       kill -15 `cat ${pidpath}${pid}-8.pid`
       rm -r ${pidpath}${pid}-8.pid
       kill -15 `cat ${pidpath}${pid}-29.pid`
       rm -r ${pidpath}${pid}-29.pid
       kill -15 `cat ${pidpath}${pid}-33.pid`
       rm -r ${pidpath}${pid}-33.pid
       kill -15 `cat ${pidpath}${pid}-37.pid`
       rm -r ${pidpath}${pid}-37.pid
       kill -15 `cat ${pidpath}${pid}-48.pid`
       rm -r ${pidpath}${pid}-48.pid
       kill -15 `cat ${pidpath}${pid}-51.pid`
       rm -r ${pidpath}${pid}-51.pid
       kill -15 `cat ${pidpath}${pid}-60.pid`
       rm -r ${pidpath}${pid}-60.pid
       kill -15 `cat ${pidpath}${pid}-63.pid`
       rm -r ${pidpath}${pid}-63.pid
       echo -e "\nStopped CliCap\n"
       exit 1
}
info () {
       echo
       echo 'BUILD:            .\clicap build'
       echo 'RUN:              .\clicap start'
       echo 'STOP:             .\clicap stop'
       echo 'BUILD AND RUN:    .\clicap build start'
       echo
       return '0'
}
####################################################################################### 

#######################################################################################
if [ $# -gt 2 ] || [ $# -eq 0 ]; then
       echo -e "\nWrong number of arguments!"
       info
       exit 1
fi
####################################################################################### 

#######################################################################################
if [ $# -eq 2 ]; then
       if [ "$1" = "build" ] && [ "$2" = "start" ]; then
               # Build CliCap		
               ant		
               # Start CliCap
               	start
       else
               echo -e "\nIncorrect arguments!"
               info
               exit 1
       fi
fi
####################################################################################### 

#######################################################################################
if [ $# -eq 1 ]; then
       if [ "$1" = "start" ]; then	
               # Run CliCap
               start
       else
               if [ "$1" = "stop" ]; then
                       # Stop CliCap
                       stop
               else
                       if [  "$1" = "build" ]; then
                               # Build CliCap				
                               ant
                       else
                               echo -e "\nIncorrect argument!"
                               info
                               exit 1
                       fi
               fi
      fi
fi
####################################################################################### 

#######################################################################################
