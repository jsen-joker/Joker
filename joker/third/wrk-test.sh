#!/bin/bash  

j=0
for i in {1..10}  
do  
j=$[j+100]
eval './wrk -t16 -c'$j' -d30s http://127.0.0.1:8080/echo/hello'
done  
# ./wrk -t16 -c1000 -d30s http://127.0.0.1:8080/echo/hello

# spring boot
# ./wrk -t16 -c1000 -d30s http://127.0.0.1:8081/echo/hello