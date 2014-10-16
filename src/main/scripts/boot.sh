#!/bin/sh

# 程序识别号, 规则为 [gw name]_[gw no]
APPID="GpsGW809_Superior"

# 工作目录
rootdir=$PWD

# 本地程序库
LOCAL_LIB=""

for jar in `find ./lib -name "*.jar"`
do
      LOCAL_LIB="$LOCAL_LIB:""$jar"
done

# classpath
JAVACLASSPATH=./conf/:.:$LOCAL_LIB

# echo "local libs:$LOCAL_LIB"

# main class
MAIN_CLASS=com.jsecode.GpsGWServer

# --- function ---
function displayHelp()
{	echo '    '
    echo ' '
	echo '  Parameters:'
	echo '      -start  :       Start The Server'
    echo '      -stop   :       Stop  The Server'
	echo '      -status :       Display Running Status of the Server'
	echo '    '
}

function  displayServerStatus()
{
   echo '  '   
   echo 'Current Server['$APPID'] Status: '
   echo ' '

   ps -ef | grep APPID=$APPID | grep -v grep

   echo ''
}

function killServer()
{
      kill -9 `ps -ef|grep APPID=$APPID|grep -v console|grep -v grep|awk '{print $2}'`

}

function startServer()
{ 
    JAVA_OPTS="-Xms256m -Xmx512m -XX:PermSize=64M -XX:MaxPermSize=128M"
    nohup java -server -DAPPID=$APPID -DROOTDIR=$rootdir $JAVA_OPTS -classpath $JAVACLASSPATH $MAIN_CLASS & > ./log/nohup.log &
    touch ${rootdir}/pid/pid
    echo $! > ${rootdir}/pid/pid
}


if [ $# -eq 0 ]; then
    displayHelp
    exit
fi

# Shell Control
if [ $1 = "-start" ]; then

    killServer 
    startServer
    sleep 2
    displayServerStatus
    exit
fi


if [ $1 = "-stop" ]; then
    killServer 
    echo '正在关闭,请稍等......'
    sleep 2
    displayServerStatus
    exit
fi

if [ $1 = "-status" ]; then
    displayServerStatus
    exit
fi

echo "end !!!"

