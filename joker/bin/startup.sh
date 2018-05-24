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

exec './joker.sh' start '$@'