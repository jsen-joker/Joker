#!/bin/sh

# ----------------------------------------------------
# Start script for the Joker
# ----------------------------------------------------
#!/bin/sh

_NOHUP=nohup

PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`/

. "$PRGDIR"setclasspath.sh

if [ -z "$JOKER_OUT" ] ; then
  JOKER_OUT="$PRGDIR"../logs/joker.out
fi

CLASSPATH=.:"$PRGDIR../static":"$PRGDIR../entry":"$PRGDIR../lib":"$PRGDIR../joker-boot.jar"

JAVA_OPTS="$JAVA_OPTS $JSSE_OPTS"

JAVA_OPTS="$JAVA_OPTS -Dhazelcast.logging.type=slf4j -Dvertx.hazelcast.config=$PRGDIR../conf/cluster/hazelcast.xml -Dvertx.zookeeper.config=$PRGDIR../conf/cluster/zookeeper.json -Dorg.apache.zookeeper.level=INFO"

if [ "$1" = "run" ] ; then
	shift
  	eval exec "\"$_RUNJAVA\"" $JAVA_OPTS \
      -classpath "\"$CLASSPATH\"" \
      com.jsen.joker.boot.loader.Boot "$@" start
elif [ "$1" = "install" ] ; then
  cd .. 
  mvn dependency:copy-dependencies
elif [ "$1" = "start" ] ; then

  if [ ! -z "$JOKER_PID" ]; then
    if [ -f "$JOKER_PID" ]; then
      if [ -s "$JOKER_PID" ]; then
        echo "Existing PID file found during start."
        if [ -r "$JOKER_PID" ]; then
          PID=`cat "$JOKER_PID"`
          ps -p $PID >/dev/null 2>&1
          if [ $? -eq 0 ] ; then
            echo "Joker appears to still be running with PID $PID. Start aborted."
            echo "If the following process is not a Joker process, remove the PID file and try again:"
            ps -f -p $PID
            exit 1
          else
            echo "Removing/clearing stale PID file."
            rm -f "$JOKER_PID" >/dev/null 2>&1
            if [ $? != 0 ]; then
              if [ -w "$JOKER_PID" ]; then
                cat /dev/null > "$JOKER_PID"
              else
                echo "Unable to remove or clear stale PID file. Start aborted."
                exit 1
              fi
            fi
          fi
        else
          echo "Unable to read PID file. Start aborted."
          exit 1
        fi
      else
        rm -f "$JOKER_PID" >/dev/null 2>&1
        if [ $? != 0 ]; then
          if [ ! -w "$JOKER_PID" ]; then
            echo "Unable to remove or write to empty PID file. Start aborted."
            exit 1
          fi
        fi
      fi
    fi
  fi

  shift
  touch "$JOKER_OUT"
  eval $_NOHUP "\"$_RUNJAVA\"" $JAVA_OPTS \
	  -classpath "\"$CLASSPATH\"" \
	  com.jsen.joker.boot.loader.Boot "$@" start \
	  >> "$JOKER_OUT" 2>&1 "&"

	echo $JOKER_PID
  if [ ! -z "$JOKER_PID" ]; then
    echo $! > "$JOKER_PID"
  fi

  echo "Joker started."


elif [ "$1" = "restart" ] ; then
  shift
  touch "$JOKER_OUT"
  eval $_NOHUP "\"$_RUNJAVA\"" $JAVA_OPTS \
	  -classpath "\"$CLASSPATH\"" \
	  com.jsen.joker.boot.loader.Boot "$@" restart \
	  >> "$JOKER_OUT" 2>&1 "&"

	echo $JOKER_PID
  if [ ! -z "$JOKER_PID" ]; then
    echo $! > "$JOKER_PID"
  fi

  echo "Joker restarted."


elif [ "$1" = "rerun" ] ; then
  shift
  touch "$JOKER_OUT"
  eval "\"$_RUNJAVA\"" $JAVA_OPTS \
	  -classpath "\"$CLASSPATH\"" \
	  com.jsen.joker.boot.loader.Boot "$@" restart

	echo $JOKER_PID
  if [ ! -z "$JOKER_PID" ]; then
    echo $! > "$JOKER_PID"
  fi

  echo "Joker restarted."



elif [ "$1" = "stop" ] ; then

  shift

  SLEEP=5
  if [ ! -z "$1" ]; then
    echo $1 | grep "[^0-9]" >/dev/null 2>&1
    if [ $? -gt 0 ]; then
      SLEEP=$1
      shift
    fi
  fi

  FORCE=0
  if [ "$1" = "-force" ]; then
    shift
    FORCE=1
  fi

  if [ ! -z "$JOKER_PID" ]; then
    if [ -f "$JOKER_PID" ]; then
      if [ -s "$JOKER_PID" ]; then
        kill -0 `cat "$JOKER_PID"` >/dev/null 2>&1
        if [ $? -gt 0 ]; then
          echo "PID file found but either no matching process was found or the current user does not have permission to stop the process. Stop aborted."
          exit 1
        fi
      else
        echo "PID file is empty and has been ignored."
      fi
    else
      echo "\$JOKER_PID was set but the specified file does not exist. Is Joker running? Stop aborted."
      exit 1
    fi
  fi

  eval "\"$_RUNJAVA\"" $JAVA_OPTS \
    -classpath "\"$CLASSPATH\"" \
    com.jsen.joker.boot.loader.Boot "$@" stop

  # stop failed. Shutdown port disabled? Try a normal kill.
  if [ $? != 0 ]; then
    if [ ! -z "$JOKER_PID" ]; then
      echo "The stop command failed. Attempting to signal the process to stop through OS signal."
      kill -15 `cat "$JOKER_PID"` >/dev/null 2>&1
    fi
  fi

  if [ ! -z "$JOKER_PID" ]; then
    if [ -f "$JOKER_PID" ]; then
      while [ $SLEEP -ge 0 ]; do
        kill -0 `cat "$JOKER_PID"` >/dev/null 2>&1
        if [ $? -gt 0 ]; then
          rm -f "$JOKER_PID" >/dev/null 2>&1
          if [ $? != 0 ]; then
            if [ -w "$JOKER_PID" ]; then
              cat /dev/null > "$JOKER_PID"
              # If Joker has stopped don't try and force a stop with an empty PID file
              FORCE=0
            else
              echo "The PID file could not be removed or cleared."
            fi
          fi
          echo "Joker stopped."
          break
        fi
        if [ $SLEEP -gt 0 ]; then
          sleep 1
        fi
        if [ $SLEEP -eq 0 ]; then
          echo "Joker did not stop in time."
          if [ $FORCE -eq 0 ]; then
            echo "PID file was not removed."
          fi
          echo "To aid diagnostics a thread dump has been written to standard out."
          kill -3 `cat "$JOKER_PID"`
        fi
        SLEEP=`expr $SLEEP - 1 `
      done
    fi
  fi

  KILL_SLEEP_INTERVAL=5
  if [ $FORCE -eq 1 ]; then
    if [ -z "$JOKER_PID" ]; then
      echo "Kill failed: \$JOKER_PID not set"
    else
      if [ -f "$JOKER_PID" ]; then
        PID=`cat "$JOKER_PID"`
        echo "Killing Joker with the PID: $PID"
        kill -9 $PID
        while [ $KILL_SLEEP_INTERVAL -ge 0 ]; do
            kill -0 `cat "$JOKER_PID"` >/dev/null 2>&1
            if [ $? -gt 0 ]; then
                rm -f "$JOKER_PID" >/dev/null 2>&1
                if [ $? != 0 ]; then
                    if [ -w "$JOKER_PID" ]; then
                        cat /dev/null > "$JOKER_PID"
                    else
                        echo "The PID file could not be removed."
                    fi
                fi
                echo "The Joker process has been killed."
                break
            fi
            if [ $KILL_SLEEP_INTERVAL -gt 0 ]; then
                sleep 1
            fi
            KILL_SLEEP_INTERVAL=`expr $KILL_SLEEP_INTERVAL - 1 `
        done
        if [ $KILL_SLEEP_INTERVAL -lt 0 ]; then
            echo "Joker has not been killed completely yet. The process might be waiting on some system call or might be UNINTERRUPTIBLE."
        fi
      fi
    fi
  fi

else

  echo "Usage: joker.sh ( commands ... )"
  echo "commands:"
  echo "  run               Start Joker in the current window"
  echo "  start             Start Joker in a separate window"
  echo "  restart           Restart Joker in a separate window"
  echo "  stop              Stop Joker, waiting up to 5 seconds for the process to end"
  echo "  install           Install dependcy defined in pom.xml to lib directory, system need mvn installed"
  echo "Note: Waiting for the process to end and use of the -force option require that \$JOKER_PID is defined"
  exit 1

fi
