#!/bin/bash
set +u
shopt -s xpg_echo
shopt -s expand_aliases
unset PATH JAVA_HOME LD_LIBRARY_PATH
function abort {
   echo "aborting: $@" 1>&2
   exit 1
}
function default {
   export INSTANCE_NAME="bootstrap"
export INSTANCE_HOME="/tmp/bootstrap"
export LOG_DIR="/tmp/bootstrap"
   return 0
}
function bootstrap {
      return 0
}
function findPid {
   unset FOUND_PID;
   [ $# -eq 1 ] || {
      abort "findPid requires a parameter of pattern to match"
      return 1
   }
   local PATTERN="$1"; shift
   local _FOUND=`ps auxwww|grep "$PATTERN"|grep -v " $0"|grep -v grep|awk '{print $2}'`
   [ -n "$_FOUND" ] && {
      export FOUND_PID=$_FOUND
      return 0
   } || {
      return 1
   }
}
function forget {
   unset FOUND_PID;
   [ $# -eq 3 ] || {
      abort "forget requires parameters INSTANCE_NAME SCRIPT LOG_DIR"
      return 1
   }
   local INSTANCE_NAME="$1"; shift
   local SCRIPT="$1"; shift
   local LOG_DIR="$1"; shift
   mkdir -p $LOG_DIR
   findPid $INSTANCE_NAME
   [ -n "$FOUND_PID" ] && {
      echo $INSTANCE_NAME already running pid [$FOUND_PID]
   } || {
      nohup $SCRIPT >$LOG_DIR/stdout.log 2>$LOG_DIR/stderr.log &
      sleep 1
      findPid $INSTANCE_NAME
      [ -n "$FOUND_PID" ] || abort "$INSTANCE_NAME did not start"
   }
   return 0
}
export PATH=/usr/ucb/bin:/bin:/sbin:/usr/bin:/usr/sbin
case $1 in
init)
   default || exit 1
   bootstrap || exit 1
   mkdir -p $INSTANCE_HOME
   
   # create runscript header
   cat > $INSTANCE_HOME/bootstrap.sh <<END_OF_SCRIPT
#!/bin/bash
set +u
shopt -s xpg_echo
shopt -s expand_aliases
PROMPT_COMMAND='echo -ne "\033]0;bootstrap\007"'
export PATH=/usr/ucb/bin:/bin:/sbin:/usr/bin:/usr/sbin
export INSTANCE_NAME='bootstrap'
export INSTANCE_NAME='$INSTANCE_NAME'
export INSTANCE_HOME='$INSTANCE_HOME'
export LOG_DIR='$LOG_DIR'
END_OF_SCRIPT
   
   # add desired commands from the user
   cat >> $INSTANCE_HOME/bootstrap.sh <<'END_OF_SCRIPT'
cd $INSTANCE_HOME
echo nameserver 208.67.222.222 >> /etc/resolv.conf
cp /etc/apt/sources.list /etc/apt/sources.list.old
sed 's~us.archive.ubuntu.com~mirror.anl.gov/pub~g' /etc/apt/sources.list.old >/etc/apt/sources.list
which curl || apt-get update -y -qq && apt-get install -f -y -qq --force-yes curl
(which java && java -fullversion 2>&1|egrep -q 1.6 ) || apt-get install -f -y -qq --force-yes openjdk-6-jre
rm -rf /var/cache/apt /usr/lib/vmware-tools

mkdir -p ~/.ssh
cat >> ~/.ssh/authorized_keys <<'END_OF_FILE'
ssh-rsa
END_OF_FILE
chmod 600 ~/.ssh/authorized_keys
mkdir -p ~/.ssh
rm ~/.ssh/id_rsa
cat >> ~/.ssh/id_rsa <<'END_OF_FILE'
-----BEGIN RSA PRIVATE KEY-----
END_OF_FILE
chmod 600 ~/.ssh/id_rsa

END_OF_SCRIPT
   
   # add runscript footer
   cat >> $INSTANCE_HOME/bootstrap.sh <<'END_OF_SCRIPT'
exit 0
END_OF_SCRIPT
   
   chmod u+x $INSTANCE_HOME/bootstrap.sh
   ;;
status)
   default || exit 1
   findPid $INSTANCE_NAME || exit 1
   echo [$FOUND_PID]
   ;;
stop)
   default || exit 1
   findPid $INSTANCE_NAME || exit 1
   [ -n "$FOUND_PID" ]  && {
      echo stopping $FOUND_PID
      kill -9 $FOUND_PID
   }
   ;;
start)
   default || exit 1
   forget $INSTANCE_NAME $INSTANCE_HOME/$INSTANCE_NAME.sh $LOG_DIR || exit 1
   ;;
tail)
   default || exit 1
   tail $LOG_DIR/stdout.log
   ;;
tailerr)
   default || exit 1
   tail $LOG_DIR/stderr.log
   ;;
run)
   default || exit 1
   $INSTANCE_HOME/$INSTANCE_NAME.sh
   ;;
esac
exit 0
