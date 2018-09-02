#!/bin/sh

# ----------------------------------------------------
# Start script for the Joker
# ----------------------------------------------------
#!/bin/sh
###
#ps -ef |grep joker-boot.jar |grep -v grep
#if [ $? -eq 0 ];then
#  	echo 'Joker is running!' 
#else
#	echo "///startup joker///"
#	cd ../static
#	nohup java -Dhazelcast.logging.type=slf4j -Dvertx.hazelcast.config=../conf/cluster.xml -jar ../joker-boot.jar -cp .:../static:../entry > ../logs/joker.log& 
#	echo 'start joker finished'
#fi
###

PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`

cd $PRGDIR
exec './joker.sh' start '$@'