#!/bin/bash

rm -rf go-arm443
tar xvzf go1.4.2.linux-386.tar.gz 
mv go go-arm443
export GOROOT=$PWD/go-arm443
export PATH=$PATH:$GOROOT/bin
CC=/opt/FriendlyARM/toolschain/4.4.3/bin/arm-linux-gcc
cd go-arm443/src/
GOARM=5 CGO_ENABLED=0 GOARCH=arm GOOS=linux ./make.bash
